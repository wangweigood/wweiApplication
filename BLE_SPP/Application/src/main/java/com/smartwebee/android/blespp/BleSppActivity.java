package com.smartwebee.android.blespp;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class BleSppActivity extends Activity implements View.OnClickListener {
    private final static String TAG = BleSppActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static int i = 1;
    public static int i1 = 1;
    public static int i2 = 1;
    public static int i3 = 1;
    public static int i4 = 1;
    public static int i5 = 1;
    public static int i6 = 1;
    public static int i7 = 1;
    public static int i8 = 1;
    public static int i9 = 1;
    public static int i10 = 1;
    public static int i11 = 1;
    public static int i12 = 1;
    public static int i13 = 1;
    public static int i14 = 1;
    public static Bundle bundle = new Bundle();
    static long recv_cnt = 0;

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();


    private boolean mConnected = false;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


    private TextView mDataRecvText;
    private TextView mRecvBytes;
    private TextView mDataRecvFormat;
    private EditText mEditBox;
    private TextView mSendBytes;
    private TextView mDataSendFormat;
    private TextView mNotify_speed_text;

    private long recvBytes = 0;
    private long lastSecondBytes = 0;
    private long sendBytes;
    private StringBuilder mData;

    int sendIndex = 0;
    int sendDataLen = 0;
    byte[] sendBuf;

    //测速
    private Timer timer;
    private TimerTask task;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                mBluetoothLeService.connect(mDeviceAddress);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //特征值找到才代表连接成功
                mConnected = true;
                invalidateOptionsMenu();
                updateConnectionState(R.string.connected);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_NO_DISCOVERED.equals(action)) {
                mBluetoothLeService.connect(mDeviceAddress);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                final byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
//                final StringBuilder stringBuilder = new StringBuilder();
//                 for(byte byteChar : data)
//                      stringBuilder.append(String.format("%02X ", byteChar));
//                Log.v("log",stringBuilder.toString());
                displayData(intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_WRITE_SUCCESSFUL.equals(action)) {
                mSendBytes.setText(sendBytes + " ");
                if (sendDataLen > 0) {
                    Log.v("log", "Write OK,Send again");
                    onSendBtnClicked();
                } else {
                    Log.v("log", "Write Finish");
                }
            } else if (BluetoothLeService.ACTION_UPDATE.equals(action)) {

                String path = null;
                try {
                    path = Environment.getExternalStorageDirectory().getCanonicalPath()
                            + "/BLE_SPP/received_data.repl";
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ObjectInputStream fis = null;
                try {
                    File targetFile = new File(path);
                    fis = new ObjectInputStream(new FileInputStream(targetFile));
                    Object rdfb = fis.readObject();

                    Toast.makeText(BleSppActivity.this, rdfb.toString() + targetFile.length(),
                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }

        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.gatt_services_characteristics);
        setContentView(R.layout.ble_spp);

        //获取蓝牙的名字和地址
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        mDataRecvText = (TextView) findViewById(R.id.data_read_text);
        mRecvBytes = (TextView) findViewById(R.id.byte_received_text);
        mDataRecvFormat = (TextView) findViewById(R.id.data_received_format);
        mEditBox = (EditText) findViewById(R.id.data_edit_box);
        mSendBytes = (TextView) findViewById(R.id.byte_send_text);
        mDataSendFormat = (TextView) findViewById(R.id.data_sended_format);
        mNotify_speed_text = (TextView) findViewById(R.id.notify_speed_text);

        Button mSendBtn = (Button) findViewById(R.id.send_data_btn);
        Button mCleanBtn = (Button) findViewById(R.id.clean_data_btn);
        Button mCleanTextBtn = (Button) findViewById(R.id.clean_text_btn);
        Button mSaveBtn = (Button) findViewById(R.id.save_data_btn);

        mDataRecvFormat.setOnClickListener(this);
        mDataSendFormat.setOnClickListener(this);
        mRecvBytes.setOnClickListener(this);
        mSendBytes.setOnClickListener(this);

        mCleanBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mCleanTextBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);
        mDataRecvText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mData = new StringBuilder();

        final int SPEED = 1;
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SPEED:
                        lastSecondBytes = recvBytes - lastSecondBytes;
                        mNotify_speed_text.setText(String.valueOf(lastSecondBytes) + " B/s");
                        lastSecondBytes = recvBytes;
                        break;
                }
            }
        };

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = SPEED;
                message.obj = System.currentTimeMillis();
                handler.sendMessage(message);
            }
        };

        timer = new Timer();
        // 参数：
        // 1000，延时1秒后执行。
        // 1000，每隔2秒执行1次task。
        timer.schedule(task, 1000, 1000);

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_display:
                Intent intent = new Intent(BleSppActivity.this, cn.itcast.tabnavtest.MainActivity.class);
                //intent.putExtra("data", maps);
                startActivity(intent);
                return true;
            case R.id.menu_gps:
                Intent GpsIntent = new Intent(BleSppActivity.this, GPS_DATA.class);
                startActivity(GpsIntent);

                Intent GpsService = new Intent(BleSppActivity.this, GPS_DATA_Service.class);
                startService(GpsService);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // mConnectionState.setText(resourceId);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_WRITE_SUCCESSFUL);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_NO_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_UPDATE);
        return intentFilter;
    }

    //动态效果
    public void convertText(final TextView textView, final int convertTextId) {
        final Animation scaleIn = AnimationUtils.loadAnimation(this,
                R.anim.text_scale_in);
        Animation scaleOut = AnimationUtils.loadAnimation(this,
                R.anim.text_scale_out);
        scaleOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setText(convertTextId);
                textView.startAnimation(scaleIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        textView.startAnimation(scaleOut);
    }

    //获取输入框十六进制格式
    private String getHexString() {
        String s = mEditBox.getText().toString();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (('0' <= c && c <= '9') || ('a' <= c && c <= 'f') ||
                    ('A' <= c && c <= 'F')) {
                sb.append(c);
            }
        }
        if ((sb.length() % 2) != 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }


    private byte[] stringToBytes(String s) {
        byte[] buf = new byte[s.length() / 2];
        for (int i = 0; i < buf.length; i++) {
            try {
                buf[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return buf;
    }

    public String asciiToString(byte[] bytes) {
        char[] buf = new char[bytes.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char) bytes[i];
            sb.append(buf[i]);
        }
        return sb.toString();
    }

    public String bytesToString(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];

            sb.append(hexChars[i * 2]);
            sb.append(hexChars[i * 2 + 1]);
            sb.append(' ');
        }
        return sb.toString();
    }


    private void getSendBuf() {
        sendIndex = 0;
        if (mDataSendFormat.getText().equals(getResources().getString(R.string.data_format_default))) {
            sendBuf = mEditBox.getText().toString().trim().getBytes();
        } else {
            sendBuf = stringToBytes(getHexString());
        }
        sendDataLen = sendBuf.length;
    }

    private void onSendBtnClicked() {
        if (sendDataLen > 20) {
            sendBytes += 20;
            final byte[] buf = new byte[20];
            // System.arraycopy(buffer, 0, tmpBuf, 0, writeLength);
            for (int i = 0; i < 20; i++) {
                buf[i] = sendBuf[sendIndex + i];
            }
            sendIndex += 20;
            mBluetoothLeService.writeData(buf);
            sendDataLen -= 20;
        } else {
            sendBytes += sendDataLen;
            final byte[] buf = new byte[sendDataLen];
            for (int i = 0; i < sendDataLen; i++) {
                buf[i] = sendBuf[sendIndex + i];
            }
            mBluetoothLeService.writeData(buf);
            sendDataLen = 0;
            sendIndex = 0;
        }
    }

    private void displayData(byte[] buf) {
        recvBytes += buf.length;
        recv_cnt += buf.length;

        if (recv_cnt >= 1024) {
            recv_cnt = 0;
            mData.delete(0, mData.length() / 2); //UI界面只保留512个字节，免得APP卡顿
        }

        if (mDataRecvFormat.getText().equals("Ascii")) {
            String s = asciiToString(buf);
            mData.append(s);
            Log.e("==============", "s.length=" + s.length());
        } else {
            String s = bytesToString(buf);
            mData.append(s);
        }
        mDataRecvText.setText(mData.toString());
        mRecvBytes.setText(recvBytes + " ");

        String strmData = mData.toString();
        Log.e("================", "" + mData.length());
        char[] chars = strmData.toCharArray();
        for (int i = 0; i <= chars.length - 4; i++) {
            String str1 = new String(chars, i, 4);

            if (str1.equals("aa5a")) {
                mData.delete(0, i);
                String str2 = new String(chars, i + 4, chars.length - (i + 4));
                char[] chars1 = str2.toCharArray();
                parse(chars1);

                break;
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.data_received_format:
                if (mDataRecvFormat.getText().equals(getResources().getString(R.string.data_format_default))) {
                    convertText(mDataRecvFormat, R.string.data_format_hex);
                } else {
                    convertText(mDataRecvFormat, R.string.data_format_default);
                }
                break;

            case R.id.data_sended_format:
                if (mDataSendFormat.getText().equals(getResources().getString(R.string.data_format_default))) {
                    convertText(mDataSendFormat, R.string.data_format_hex);
                } else {
                    convertText(mDataSendFormat, R.string.data_format_default);
                }
                break;

            case R.id.byte_received_text:
                recvBytes = 0;
                lastSecondBytes = 0;
                convertText(mRecvBytes, R.string.zero);
                break;

            case R.id.byte_send_text:
                sendBytes = 0;
                convertText(mSendBytes, R.string.zero);
                break;

            case R.id.send_data_btn:
                getSendBuf();
                onSendBtnClicked();
                break;

            case R.id.clean_data_btn:
                mData.delete(0, mData.length());
                mDataRecvText.setText(mData.toString());
                break;

            case R.id.clean_text_btn:
                mEditBox.setText("");
                break;

            case R.id.save_data_btn:
                Intent intent = new Intent(BluetoothLeService.ACTION_TO_SAVE);
                intent.putExtra("data", bundle.toString());
                sendBroadcast(intent);
                break;

            default:
                break;
        }
    }


    public void parse(char[] data) {
        String str1, str2;
        if (data.length > 12) {
            str1 = new String(data, 0, 4);
            str2 = new String(data, 4, 8);
        } else {
            return;
        }


        if (str1.equals("3035")) {
            switch (str2) {
                case "34373031":
                    if (data.length < 16) break;
                    String tunnel = new String(data, 12, 4);
                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30373034":
                    if (data.length < 28) {
                        Log.e("==============", "data.length =" + data.length);
                        break;
                    }


                    String frequency = new String(data, 12, 8);
                    Log.e("==============", "frequency=" + frequency);

                    bundle.putString("frequency" + i, frequency);
                    i++;
                    Log.e("==============", "i=" + i);

                    String PI = new String(data, 20, 8);
                    Log.e("==============", "PI=" + PI);
                    bundle.putString("PI" + i1, PI);
                    i1++;
                    mData.delete(0, 32);

                    if (data.length > 32) {
                        String s = new String(data, 28, 4);
                        String s1 = new String(data, 32, data.length - 32);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }

                    break;

                case "30383130":

                    if (data.length < 52) {
                        Log.e("==============", "data.length =" + data.length);
                        break;
                    }
                    String frequency1 = new String(data, 12, 8);
                    String psName = new String(data, 20, 32);
                    bundle.putString("psName" + i2, psName);
                    i2++;
                    mData.delete(0, 56);

                    if (data.length > 56) {
                        String s = new String(data, 52, 4);
                        String s1 = new String(data, 56, data.length - 56);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30393033":
                    if (data.length < 24) {
                        Log.e("==============", "data.length =" + data.length);
                        break;
                    }
                    String frequency2 = new String(data, 12, 8);
                    String pty = new String(data, 20, 4);
                    bundle.putString("pty" + i3, pty);
                    i3++;
                    mData.delete(0, 28);

                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30413033":
                    if (data.length < 24) {
                        Log.e("==============", "data.length =" + data.length);
                        break;
                    }
                    String frequency3 = new String(data, 12, 8);
                    String fourHigh = new String(data, 20, 4); //[1:1:1:1…...]ta:tp:taReceived:tpReceived(最高4位)
                    byte[] x = stringToBytes(fourHigh);
                    Log.e("==============", "x[0] =" + x[0]);
                    Log.e("==============", "x[1] =" + x[1]);

                    String tp = Integer.toHexString((x[0] - 49 & 0x04));
                    bundle.putString("tp" + i4, tp);
                    i4++;

                    Log.e("==============", "i4=" + i4);

                    String ta = Integer.toHexString((x[0] - 49 & 0x0f));
                    Log.e("==============", "x[0] - 49 =" + Integer.toHexString((x[0] - 49)));
                    Log.e("==============", "ta =" + ta);
                    bundle.putString("ta" + i5, ta);
                    i5++;
                    mData.delete(0, 28);

                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "35343035":
                    if (data.length < 32) {
                        Log.e("==============", "data.length =" + data.length);
                        break;
                    }
                    String frequency4 = new String(data, 12, 8);
                    String OK_fs = new String(data, 20, 4); //[1:7]tunedOK:fieldStrength
                    byte[] x1 = stringToBytes(OK_fs);
                    String fieldStrength = (x1[0] & 0x07) + "" + (x1[1] & 0x0f);
                    bundle.putString("fieldStrength" + i6, fieldStrength);
                    i6++;

                    String noise_multipath = new String(data, 24, 4); //[4:4]noise:multipath
                    byte[] x2 = stringToBytes(noise_multipath);
                    String noise = (x2[0] & 0x0f) + "";
                    bundle.putString("noise" + i10, noise);
                    i10++;

                    String multipath = (x2[1] & 0x0f) + "";
                    bundle.putString("multipath" + i7, multipath);
                    i7++;

                    String globalQualit = new String(data, 28, 4);
                    bundle.putString("globalQualit" + i8, globalQualit);
                    i8++;
                    mData.delete(0, 36);

                    if (data.length > 36) {
                        String s = new String(data, 32, 4);
                        String s1 = new String(data, 36, data.length - 36);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "32383033":
                    if (data.length < 24) {
                        Log.e("==============", "data.length =" + data.length);
                        break;
                    }
                    String frequency5 = new String(data, 12, 8);
                    String confidence = new String(data, 20, 4);
                    bundle.putString("confidence" + i9, confidence);
                    i9++;
                    mData.delete(0, 28);

                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "33313032":
                    if (data.length < 20) break;
                    String frequency6 = new String(data, 12, 8);
                    if (data.length > 24) {
                        String s = new String(data, 20, 4);
                        String s1 = new String(data, 24, data.length - 24);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                default:
                    break;
            }

        } else if (str1.equals("3046")) {
            switch (str2) {

                case "30363033":
                    if (data.length < 24) break;
                    String affrequency = new String(data, 12, 8);
                    bundle.putString("affrequency" + i11, affrequency);
                    i11++;

                    String IsEvent = new String(data, 20, 4);

                    mData.delete(0, 28);
                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30383039":
                    if (data.length < 48) break;
                    String PSN = new String(data, 12, 32);
                    String isEvent = new String(data, 44, 4);
                    if (data.length > 52) {
                        String s = new String(data, 48, 4);
                        String s1 = new String(data, 52, data.length - 52);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "31313634":
                    if (data.length < 268) break;
                    String RadioText = new String(data, 12, 256);
                    if (data.length > 272) {
                        String s = new String(data, 256, 4);
                        String s1 = new String(data, 272, data.length - 272);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "31323031":
                    if (data.length < 16) break;
                    String musicSpeech = new String(data, 12, 4);
                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30393031":
                    if (data.length < 16) break;
                    String pty = new String(data, 12, 4);
                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30413031":
                    if (data.length < 16) break;
                    String tpReceived = new String(data, 12, 4);
                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "31353132":
                    if (data.length < 60) break;
                    String EonPi = new String(data, 12, 8);
                    String EonPSN = new String(data, 20, 32);
                    String taReceived = new String(data, 52, 4);
                    String EonPty = new String(data, 56, 4);
                    if (data.length > 64) {
                        String s = new String(data, 60, 4);
                        String s1 = new String(data, 64, data.length - 64);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "31383032":
                    if (data.length < 20) break;
                    String hours = new String(data, 12, 4);
                    String minutes = new String(data, 16, 4);
                    if (data.length > 24) {
                        String s = new String(data, 20, 4);
                        String s1 = new String(data, 24, data.length - 24);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "31433135":
                    if (data.length < 72) break;

                    String frequency1 = new String(data, 12, 8);
                    String Identification = new String(data, 20, 4);
                    String AfFmeGlobalQuality = new String(data, 24, 4);
                    String afWeightedFs = new String(data, 28, 4);
                    String afPiconfidence = new String(data, 32, 4);
                    bundle.putString("afPiconfidence" + i12, afPiconfidence);

                    String point8 = new String(data, 36, 4);//[1:1:1:1:1:1:1:1]lb:tn:ptn:tnr:eon:tmc:mps:listened
                    byte[] x1 = stringToBytes(point8);
                    String lb = (x1[0] & 0x08) + "";
                    bundle.putString("lb" + i12, lb);

                    String tn = (x1[0] & 0x04) + "";
                    bundle.putString("tn" + i12, tn);

                    String ptn = (x1[0] & 0x02) + "";
                    bundle.putString("ptn" + i12, ptn);

                    String tnr = (x1[0] & 0x01) + "";
                    bundle.putString("tnr" + i12, tnr);

                    String eon = (x1[1] & 0x08) + "";
                    bundle.putString("eon" + i12, eon);

                    String tmc = (x1[1] & 0x04) + "";
                    bundle.putString("tmc" + i12, tmc);

                    String mps = (x1[1] & 0x02) + "";
                    bundle.putString("mps" + i12, mps);

                    String listened = (x1[1] & 0x01) + "";
                    bundle.putString("listened" + i12, listened);
                    i12++;

                    String afNeighbourConf = new String(data, 40, 4);
                    String afMethod = new String(data, 44, 4);
                    String afWeightedMp = new String(data, 48, 4);
                    String afWeightedNoise = new String(data, 52, 4);
                    String afWrongPiEvent = new String(data, 56, 4);
                    String afCorrolateFactor = new String(data, 60, 4);
                    String afCorrolateAvailable = new String(data, 64, 4);
                    String afNextCorrelationTimeout = new String(data, 68, 4);

                    mData.delete(0, 76);
                    if (data.length > 76) {
                        String s = new String(data, 72, 4);
                        String s1 = new String(data, 76, data.length - 76);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }

                    break;

                case "30373032":
                    if (data.length < 20) break;
                    String PI = new String(data, 12, 8);
                    if (data.length > 24) {
                        String s = new String(data, 20, 4);
                        String s1 = new String(data, 24, data.length - 24);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "31443030":
                    String aCommand = "delete AF List";
                    if (data.length > 16) {
                        String s = new String(data, 12, 4);
                        String s1 = new String(data, 16, data.length - 16);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "32343030":
                    String aCommand1 = "clear the concerning page";
                    if (data.length > 16) {
                        String s = new String(data, 12, 4);
                        String s1 = new String(data, 16, data.length - 16);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "31453038":
                    if (data.length < 44) break;
                    String spNames = new String(data, 12, 32);
                    if (data.length > 48) {
                        String s = new String(data, 44, 4);
                        String s1 = new String(data, 48, data.length - 48);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "33303034":
                    if (data.length < 28) break;
                    String frequency2 = new String(data, 12, 8);
                    String MPSReceptionQuality = new String(data, 20, 4);
                    String BetterMpsEvent = new String(data, 24, 4);
                    if (data.length > 32) {
                        String s = new String(data, 28, 4);
                        String s1 = new String(data, 32, data.length - 32);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "35393038":
                    if (data.length < 44) break;

                    String TunedReceptionQuality = new String(data, 12, 4);
                    String affieldStrength = new String(data, 16, 4);
                    bundle.putString("affieldStrength" + i13, affieldStrength);

                    String afnoise = new String(data, 20, 4);
                    bundle.putString("afnoise" + i13, afnoise);

                    String multiPath = new String(data, 24, 4);
                    bundle.putString("multiPath" + i13, multiPath);

                    String gbBitField = new String(data, 28, 4);
                    String lostBlock = new String(data, 32, 4);
                    String globalQuality = new String(data, 36, 4);
                    bundle.putString("globalQuality" + i13, globalQuality);
                    i13++;

                    String NdPacs = new String(data, 40, 4);
                    if (data.length > 48) {
                        String s = new String(data, 44, 4);
                        String s1 = new String(data, 48, data.length - 48);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                default:
                    break;
            }
        } else if (str1.equals("3230")) {
            switch (str2) {
                case "30303031":
                    if (data.length < 16) break;
                    String MAX_MEMO_LIST_SIZE = new String(data, 12, 4);
                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "37343232":
                    if (data.length < 100) break;
                    String msfrequency = new String(data, 12, 8);
                    bundle.putString("msfrequency" + i14, msfrequency);

                    String mspi = new String(data, 20, 8);
                    bundle.putString("mspi" + i14, mspi);

                    String mspty = new String(data, 28, 4);
                    bundle.putString("mspty" + i14, mspty);

                    String mspsName = new String(data, 32, 32);
                    bundle.putString("mspsName" + i14, mspsName);

                    String mshigh4 = new String(data, 64, 4);//[1:1:1:1…...]ta:tp:taReceived:tpReceived(最高4位)
                    byte[] x = stringToBytes(mshigh4);
                    String msta = (x[0] & 0x08) + "";
                    bundle.putString("msta" + i14, msta);

                    String mstp = (x[0] & 0x04) + "";
                    bundle.putString("mstp" + i14, mstp);

                    String Sid = new String(data, 68, 4);
                    String msfieldStrength = new String(data, 72, 4);
                    bundle.putString("msfieldStrength" + i14, msfieldStrength);

                    String msnoise = new String(data, 76, 4);
                    bundle.putString("msnoise" + i14, msnoise);

                    String msmultiPath = new String(data, 80, 4);
                    bundle.putString("msmultiPath" + i14, msmultiPath);

                    String mstuneOk = new String(data, 84, 4);
                    bundle.putString("mstuneOk" + i14, mstuneOk);

                    String msglobalQuality = new String(data, 88, 4);
                    bundle.putString("msglobalQuality" + i14, msglobalQuality);

                    String msIndexStation = new String(data, 92, 4);
                    bundle.putString("msIndexStation" + i14, msIndexStation);

                    String msFromAList = new String(data, 96, 4);
                    bundle.putString("msFromAList" + i14, msFromAList);
                    i14++;

                    if (data.length > 104) {
                        String s = new String(data, 100, 4);
                        String s1 = new String(data, 104, data.length - 104);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "35443031":
                    if (data.length < 16) break;
                    String positionInList = new String(data, 12, 4);
                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "32323030":
                    String aCommand = "Init the previous list";
                    if (data.length > 16) {
                        String s = new String(data, 12, 4);
                        String s1 = new String(data, 16, data.length - 16);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "37353031":
                    if (data.length < 16) break;
                    String previousPosition = new String(data, 12, 4);
                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                default:
                    break;

            }
        } else if (str1.equals("3337")) {
            switch (str2) {
                case "33383033":
                    if (data.length < 24) break;
                    String alarm_status = new String(data, 12, 8);
                    String alarm_PI = new String(data, 20, 4);
                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "33393033":
                    if (data.length < 24) break;
                    String traffic_status = new String(data, 12, 8);
                    String traffic_PI = new String(data, 20, 4);
                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "33413033":
                    if (data.length < 24) break;
                    String news_status = new String(data, 12, 8);
                    String news_PI = new String(data, 20, 4);
                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "33423033":
                    if (data.length < 24) break;
                    String weather_status = new String(data, 12, 8);
                    String weather_PI = new String(data, 20, 4);
                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "33433033":
                    if (data.length < 24) break;
                    String user_pty_status = new String(data, 12, 8);
                    String user_pty_PI = new String(data, 20, 4);
                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "34413038":
                    if (data.length < 44) break;
                    String psName = new String(data, 12, 32);
                    if (data.length > 48) {
                        String s = new String(data, 44, 4);
                        String s1 = new String(data, 48, data.length - 48);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "34423038":
                    if (data.length < 44) break;
                    String psName1 = new String(data, 12, 32);
                    if (data.length > 48) {
                        String s = new String(data, 44, 4);
                        String s1 = new String(data, 48, data.length - 48);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30393031":
                    if (data.length < 16) break;
                    String userPty = new String(data, 12, 4);
                    if (userPty.equals("4646")) {
                        String aCommand = "Clear the previous UserPty list";
                    }
                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "34383031":
                    if (data.length < 16) break;
                    String reason = new String(data, 12, 4);
                    Log.v("DUMP_ANNOUNCE_EON", "DUMP_ANNOUNCE_EON_FAILED_FS = 0");
                    Log.v("DUMP_ANNOUNCE_EON", "DUMP_ANNOUNCE_EON_FAILED_PI=1");
                    Log.v("DUMP_ANNOUNCE_EON", "DUMP_ANNOUNCE_EON_FAILED_TP=2");
                    Log.v("DUMP_ANNOUNCE_EON", "DUMP_ANNOUNCE_EON_FAILED_MULTIPATH=3");

                    if (data.length > 20) {
                        String s = new String(data, 16, 4);
                        String s1 = new String(data, 20, data.length - 20);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                default:
                    break;
            }
        } else if (str1.equals("3433")) {
            switch (str2) {
                case "34343138":
                    if (data.length < 84) break;
                    String user_indexBand = new String(data, 12, 4);
                    String user_indexPreset = new String(data, 16, 4);
                    String user_psName = new String(data, 20, 32);
                    String user_currentlyTunedFreq = new String(data, 52, 8);
                    String user_piCode = new String(data, 60, 8);
                    String user_emptyPreset = new String(data, 68, 4);
                    String user_tpReceived = new String(data, 72, 4);
                    String user_pty = new String(data, 76, 4);
                    String user_afMode = new String(data, 80, 4);
                    if (data.length > 88) {
                        String s = new String(data, 84, 4);
                        String s1 = new String(data, 88, data.length - 88);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "34353138":
                    if (data.length < 84) break;
                    String announce_indexBand = new String(data, 12, 4);
                    String announce_indexPreset = new String(data, 16, 4);
                    String announce_psName = new String(data, 20, 32);
                    String announce_currentlyTunedFreq = new String(data, 52, 8);
                    String announce_piCode = new String(data, 60, 8);
                    String announce_emptyPreset = new String(data, 68, 4);
                    String announce_tpReceived = new String(data, 72, 4);
                    String announce_pty = new String(data, 76, 4);
                    String announce_afMode = new String(data, 80, 4);
                    if (data.length > 88) {
                        String s = new String(data, 84, 4);
                        String s1 = new String(data, 88, data.length - 88);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "34363138":
                    if (data.length < 84) break;
                    String system_indexBand = new String(data, 12, 4);
                    String system_indexPreset = new String(data, 16, 4);
                    String system_psName = new String(data, 20, 32);
                    String system_currentlyTunedFreq = new String(data, 52, 8);
                    String system_piCode = new String(data, 60, 8);
                    String system_emptyPreset = new String(data, 68, 4);
                    String system_tpReceived = new String(data, 72, 4);
                    String system_pty = new String(data, 76, 4);
                    String system_afMode = new String(data, 80, 4);
                    if (data.length > 88) {
                        String s = new String(data, 84, 4);
                        String s1 = new String(data, 88, data.length - 88);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                default:
                    break;
            }
        } else if (str1.equals("3443")) {
            switch (str2) {
                case "34453033":
                    if (data.length < 24) break;
                    String NbOfConnectedTuner = new String(data, 12, 4);
                    String mRdsEnable = new String(data, 16, 4);
                    String mCurrCoverArea = new String(data, 20, 4);
                    if (data.length > 28) {
                        String s = new String(data, 24, 4);
                        String s1 = new String(data, 28, data.length - 28);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "34463034":
                    if (data.length < 28) break;
                    String mAFMode = new String(data, 12, 4);
                    String mRegionalMode = new String(data, 16, 4);
                    String fmeState = new String(data, 20, 4);
                    String fmeStateTmc = new String(data, 24, 4);
                    if (data.length > 32) {
                        String s = new String(data, 28, 4);
                        String s1 = new String(data, 32, data.length - 32);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "33373139":
                    if (data.length < 88) break;
                    String alarmAnnMode = new String(data, 12, 4);
                    String trafficAnnMode = new String(data, 16, 4);
                    String newsAnnMode = new String(data, 20, 4);
                    String weatherAnnMode = new String(data, 24, 4);
                    String userPtyAnnMode = new String(data, 28, 4);
                    String NoTrafficTimeOut = new String(data, 32, 8);
                    String RefusedTimeOut = new String(data, 40, 8);
                    String LostTimeOut = new String(data, 48, 8);
                    String PoolingAnnounceTimeOut = new String(data, 56, 8);
                    String PoolingAnnounceForEmergencyLBTimeOut = new String(data, 64, 8);
                    String RetrieveFieldStrengthLimit = new String(data, 72, 4);
                    String RetrieveNoiseLimit = new String(data, 76, 4);
                    String RetrieveMultiPathLimit = new String(data, 80, 4);
                    String RetrieveConfidenceLimit = new String(data, 84, 4);
                    if (data.length > 92) {
                        String s = new String(data, 88, 4);
                        String s1 = new String(data, 92, data.length - 92);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30353039":
                    if (data.length < 48) break;
                    String fieldStrengthLimit = new String(data, 12, 4);
                    String noiseLimit = new String(data, 16, 4);
                    String multiPathLimit = new String(data, 20, 4);
                    String confidenceLimit = new String(data, 24, 4);
                    String mLBEmergencyModeTimeout = new String(data, 28, 4);
                    String mLBLowSpeedTimeout = new String(data, 32, 4);
                    String mLBHighSpeedTimeout = new String(data, 36, 4);
                    String mLBTunnelSpeedTimeout = new String(data, 40, 4);
                    String mLBEmergencySpeedTimeout = new String(data, 44, 4);
                    if (data.length > 52) {
                        String s = new String(data, 48, 4);
                        String s1 = new String(data, 52, data.length - 52);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "30323137":
                    if (data.length < 80) break;
                    String _mFMeBehaviour = new String(data, 12, 68);
                    if (data.length > 84) {
                        String s = new String(data, 80, 4);
                        String s1 = new String(data, 84, data.length - 84);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "36393234":
                    if (data.length < 108) break;
                    String RecLevelInstRAM = new String(data, 12, 96);
                    if (data.length > 112) {
                        String s = new String(data, 108, 4);
                        String s1 = new String(data, 112, data.length - 112);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                case "32303132":
                    if (data.length < 60) break;
                    String timeoutToGoodReception = new String(data, 12, 8);
                    String timeoutToMediumReception = new String(data, 20, 8);
                    String timeoutToBadReception = new String(data, 28, 8);
                    String levelHighLimit = new String(data, 36, 4);
                    String levelLowLimit = new String(data, 40, 4);
                    String multipathHighLimit = new String(data, 44, 4);
                    String multipathLowLimit = new String(data, 48, 4);
                    String adjChannelLowLimit = new String(data, 52, 4);
                    String levelHighestLimit = new String(data, 56, 4);
                    if (data.length > 64) {
                        String s = new String(data, 60, 4);
                        String s1 = new String(data, 64, data.length - 64);
                        char[] data1 = s1.toCharArray();
                        if (s.equals("aa5a")) parse(data1);
                    }
                    break;

                default:
                    break;
            }
        } else {
            mData.delete(0, 8);
        }

        Intent intent = new Intent("parsed_data_update");
        sendBroadcast(intent);
    }
}
