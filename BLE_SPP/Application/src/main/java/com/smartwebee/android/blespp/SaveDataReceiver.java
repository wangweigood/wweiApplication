package com.smartwebee.android.blespp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;

/**
 * Created by uidp5437 on 2017/8/7.
 */

public class SaveDataReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String FILE_NAME = "/received_data.repl";
        String mData;
        mData = intent.getStringExtra("data");

        File sdCardDir = Environment.getExternalStorageDirectory();
        ObjectOutputStream fos = null;
        try {

            File directory = new File(sdCardDir.getCanonicalPath() + "/BLE_SPP");
            if (!directory.exists() || !directory.isDirectory()) directory.mkdir();
            File targetFile = new File(directory.getCanonicalPath() + FILE_NAME);
            if (!targetFile.exists() || !targetFile.isFile()) targetFile.createNewFile();

            fos = new ObjectOutputStream(new FileOutputStream(targetFile, true));

            Date date = new Date();
            ReceivedDataFromBluetooth rdfb = new ReceivedDataFromBluetooth(date, mData);
            fos.writeObject(rdfb);

            Intent updateIntent = new Intent(BluetoothLeService.ACTION_UPDATE);
            context.sendBroadcast(updateIntent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
