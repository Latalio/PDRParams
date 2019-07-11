package com.la.pdrparams;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = MainActivity.class.getSimpleName();
    public static final String PATH = "/sdcard/PDRParams";
    TextView mTxtMonitor;
    Button mBtnSensorInfo;
    Button mBtnStart;
    Button mBtnFinish;
    Button mBtnWifiScan;

    static Handler mHandler;

    SensorManager mSensorManager;
    Sensor mAccelerometer;
    Sensor mMagneticField;
    Sensor mOrientation;
    Sensor mGyroscope;
    Sensor mLight;
    Sensor mPressure;
    Sensor mTemperature;
    Sensor mProximity;
    Sensor mGravity;
    Sensor mLinearAccelerometor;
    Sensor mRotationVector; // computed by magnetic and accelerometer
    Sensor mGameRotationVector; // computed by gyroscope and accelerometer
    Sensor mRelativeHumidity;
    Sensor mAmbientTemperature;

    private float[] mValueAcc = new float[3];
    private float[] mValueMag = new float[3];
    private float[] mValueMOri = new float[3];
    private float[] mValueGyr = new float[3];
    private float[] mValueGOri = new float[3];
    private float[] mMRotMat = new float[9];
    private float[] mGRotMat = new float[9];

    WifiManager mWifiManager;
    BroadcastReceiver mWifiScanReceiver;

    private final int NUM_SENSOR = 13;

    private TaskObtainSensorData mTaskSensorData;

    static final int MSGTYPE_TOAST = 101;


    long wifiScanSt = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File file = new File(PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        mHandler = new MessageHandler(this);

        mTxtMonitor = findViewById(R.id.txt_monitor);
        mBtnSensorInfo = findViewById(R.id.btn_sensor_info);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnFinish = findViewById(R.id.btn_finish);
        mBtnWifiScan = findViewById(R.id.btn_wifi_scan);

        mBtnSensorInfo.setOnClickListener(new BtnSensorInfoListener());
        mBtnStart.setOnClickListener(new BtnStartListener());
        mBtnFinish.setOnClickListener(new BtnFinishListener());
        mBtnWifiScan.setOnClickListener(new BtnWifiScanListener());

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGameRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                Log.e(TAG, "Broadcast Received.");

                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiScanReceiver, intentFilter);
        Log.e(TAG, "Broadcast Registered.");

        mTaskSensorData = new TaskObtainSensorData(MainActivity.this);
        mTaskSensorData.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this, mGameRotationVector, SensorManager.SENSOR_DELAY_GAME);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiScanReceiver);
    }

    private void scanSuccess() {
        List<ScanResult> results = mWifiManager.getScanResults();
        Log.e(TAG, "scan success and the duration is: " + (System.currentTimeMillis()-wifiScanSt));
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = mWifiManager.getScanResults();
        Log.e(TAG, "scan failure and the duration is: " + (System.currentTimeMillis()-wifiScanSt));

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // toDo something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        // Do something with this sensor value.
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values,0,mValueAcc,0,event.values.length);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values,0,mValueMag,0,event.values.length);
                break;
            case Sensor.TYPE_GYROSCOPE:
                System.arraycopy(event.values,0,mValueGyr,0,event.values.length);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(mMRotMat, event.values);
                SensorManager.getOrientation(mMRotMat, mValueMOri);
                break;
            case Sensor.TYPE_GAME_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(mGRotMat, event.values);
                SensorManager.getOrientation(mGRotMat, mValueGOri);
                break;
            default:
                break;
        }
//        StringBuilder sb = new StringBuilder();
//        sb.append("mag:\n")
//                .append(mValueMOri[0]).append("\n")
//                .append(mValueMOri[1]).append("\n")
//                .append(mValueMOri[2]).append("\n");
//        sb.append("gyr:\n")
//                .append(mValueGOri[0]).append("\n")
//                .append(mValueGOri[1]).append("\n")
//                .append(mValueGOri[2]).append("\n");
//        mTxtMonitor.setText(sb.toString());
    }


    /**
     * button listeners.
     */
    private class BtnSensorInfoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new TaskObtainSensorInfo(mSensorManager).execute(new Void[1]);
        }
    }

    private class BtnStartListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mTaskSensorData.isIDLE()) {
                mTaskSensorData.enableStart();
            } else {
                Toast.makeText(MainActivity.this, "轨迹记录中，勿重复启动！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class BtnFinishListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mTaskSensorData.enableFinish();
        }

    }

    private class BtnWifiScanListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            wifiScanSt = System.currentTimeMillis();
            boolean success = mWifiManager.startScan();
            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }

    }

    /**
     * sensor value getter
     */
    public float[] getValueAcc() {
        return mValueAcc;
    }

    public float[] getValueMag() {
        return mValueMag;
    }

    public float[] getValueMOri() {
        return mValueMOri;
    }

    public float[] getValueGyr() {
        return mValueGyr;
    }

    public float[] getValueGOri() {
        return mValueGOri;
    }

    public static boolean fileExist(File file) throws IOException {
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs()) {
                return false;
            }
            if (!file.createNewFile()) {
                return false;
            }
        }
        return true;
    }

    static void sendToast(String msg) {
        Message message = Message.obtain();
        message.what = MainActivity.MSGTYPE_TOAST;
        message.obj = msg;
        mHandler.sendMessage(message);
    }
}
