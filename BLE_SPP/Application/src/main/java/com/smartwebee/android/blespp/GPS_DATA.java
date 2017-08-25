package com.smartwebee.android.blespp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;

import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import java.io.File;


import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidgpx.data.GPXDocument;
import androidgpx.data.GPXSegment;
import androidgpx.data.GPXTrack;
import androidgpx.data.GPXTrackPoint;
import androidgpx.data.GPXWayPoint;
import androidgpx.print.GPXFilePrinter;


/**
 * Created by uidp5437 on 2017/8/10.
 */

public class GPS_DATA extends Activity {

    EditText show;
    LocationManager lm;
    public static List<Location> locations = new ArrayList<>();
    ArrayList<GPXWayPoint> wayPoints = new ArrayList<>();
    ArrayList<GPXTrack> tracks = new ArrayList<>();

    public static String path = null;

    BroadcastReceiver gpsServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra("location");
            locations.add(location);
            updateView(location);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gps_data);

        show = (EditText) findViewById(R.id.show);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) locations.add(location);
        updateView(location);

        IntentFilter filter = new IntentFilter("gps_location_changed");
        registerReceiver(gpsServiceReceiver, filter);


    }

    public void updateView(Location newLocation) {
        if (newLocation != null) {
            StringBuilder sb = new StringBuilder();

            sb.append("实时位置信息: \n");
            sb.append("经度:");
            sb.append(newLocation.getLongitude());

            sb.append("\n纬度:");
            sb.append(newLocation.getLatitude());

            sb.append("\n高度:");
            sb.append(newLocation.getAltitude());

            sb.append("\n速度:");
            sb.append(newLocation.getSpeed());

            sb.append("\n方向:");
            sb.append(newLocation.getBearing());
            sb.append("\n" + locations.size());

            show.setText(sb.toString());
        } else {
            show.setText("");
        }
    }

    public void save(View view) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File sdCardDir = Environment.getExternalStorageDirectory();

            File directory;
            File targetFile;
            try {
                directory = new File(sdCardDir.getCanonicalPath() + "/BLE_SPP");
                if (!directory.exists() || !directory.isDirectory()) directory.mkdir();

                targetFile = new File(directory.getCanonicalPath() + "/gps_data.gpx");
                if (!targetFile.exists() || !targetFile.isFile()) targetFile.createNewFile();
                path = targetFile.getCanonicalPath();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (locations != null) saveToFile(locations);

    }

    public void stopGpsService(View view) {
        Intent intent = new Intent(GPS_DATA.this, GPS_DATA_Service.class);
        stopService(intent);

        Toast.makeText(GPS_DATA.this, "自动定位服务已关闭!", Toast.LENGTH_LONG).show();
    }


    public void saveToFile(List<Location> locations) {

        wayPoints.clear();
        GPXSegment gpxSegment = new GPXSegment();

        for (Location location : locations) {
            float latitude = (float) location.getLatitude();
            float longitude = (float) location.getLongitude();
            float elevation = (float) location.getAltitude();
            Date date = new Date(location.getTime());
            GPXWayPoint gpxWayPoint = new GPXWayPoint(latitude, longitude, elevation, date);
            GPXTrackPoint gpxTrackPoint = new GPXTrackPoint(latitude, longitude);
            gpxTrackPoint.setTimeStamp(date);
            gpxTrackPoint.setElevation(elevation);

            gpxSegment.addPoint(gpxTrackPoint);
            wayPoints.add(gpxWayPoint);
        }

        GPXTrack gpxTrack = new GPXTrack();
        gpxTrack.addSegment(gpxSegment);
        gpxTrack.setName("road to Li Yadong");

        tracks.clear();
        tracks.add(gpxTrack);

        GPXDocument gpxDocument = new GPXDocument(wayPoints, tracks, null);
        GPXFilePrinter.GPXFilePrinterListener listener = new GPXFilePrinter.GPXFilePrinterListener() {
            @Override
            public void onGPXPrintStarted() {
                Toast.makeText(GPS_DATA.this, "开始写入!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGPXPrintCompleted() {
                Toast.makeText(GPS_DATA.this, "写入完成!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onGPXPrintError(String message) {
                Toast.makeText(GPS_DATA.this, message, Toast.LENGTH_SHORT).show();
            }
        };
        GPXFilePrinter gpxFilePrinter = new GPXFilePrinter(gpxDocument, path, listener);
        gpxFilePrinter.print();
    }

}
