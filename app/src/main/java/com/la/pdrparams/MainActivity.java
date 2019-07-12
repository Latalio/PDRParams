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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = MainActivity.class.getSimpleName();
    public static final String PATH = "/sdcard/PDRParams";
    public static final String PATH_SENSOR_DATA = PATH + "/sensorData";
    public static final String PATH_WIFI_RSS = PATH + "/wifiRss";
    private static final SimpleDateFormat statusDateFormat = new SimpleDateFormat("[hh:mm:ss.SSS] ");

    /**
     * Widgets
     */
    TextView mTxtMonitor;
    TextView mTxtStatus;
    Button mBtnSensorInfo;
    Button mBtnStart;
    Button mBtnFinish;
    Button mBtnWifiScan;

    // Setting region
    EditText mInputSampleFreq;

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
    private boolean mOriginalWifiState;


    private TaskObtainSensorData mTaskSensorData;
    private TaskObtainWiFiRSSData mTaskWifiRssData;

    static final int MSGTYPE_TOAST = 101;
    static final int MSGTYPE_STATUS = 102;
    static final int MSGTYPE_STATUS_INFO = 103;
    static final int MSGTYPE_STATUS_ERROR = 104;


    /**
     * tmp
     */

    /**
     * Life cycle methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new MessageHandler(this);

        // todo 在app启动后删除文件目录会出错
        checkDirs();

        mTxtMonitor = findViewById(R.id.txt_monitor);
        mTxtStatus = findViewById(R.id.txt_status);
        mTxtStatus.setMovementMethod(ScrollingMovementMethod.getInstance());
        mBtnSensorInfo = findViewById(R.id.btn_sensor_info);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnFinish = findViewById(R.id.btn_finish);

        mBtnSensorInfo.setOnClickListener(new BtnSensorInfoListener());
        mBtnStart.setOnClickListener(new BtnStartListener());
        mBtnFinish.setOnClickListener(new BtnFinishListener());

        mInputSampleFreq = findViewById(R.id.input_sample_freq);

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGameRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mOriginalWifiState = mWifiManager.isWifiEnabled();

        mTaskSensorData = new TaskObtainSensorData(MainActivity.this);
        mTaskSensorData.start();
        mTaskWifiRssData = new TaskObtainWiFiRSSData(mWifiManager);
        mTaskWifiRssData.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register sensors
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGameRotationVector, SensorManager.SENSOR_DELAY_GAME);

        // enable WiFi
        if(!mOriginalWifiState) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensors
        mSensorManager.unregisterListener(this);

        // recover wifi state
        mWifiManager.setWifiEnabled(mOriginalWifiState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Sensor-related methods
     */

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
     * Button listeners.
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
            if (mTaskSensorData.isIDLE()&&mTaskWifiRssData.isIDLE()) {
                int freq = Integer.parseInt(mInputSampleFreq.getText().toString());
                mTaskSensorData.setFrequency(freq);
                mTaskWifiRssData.setFrequency(freq);
                mTaskSensorData.enableStart();
                mTaskWifiRssData.enableStart();

            } else {
                Toast.makeText(MainActivity.this, "轨迹记录中，勿重复启动！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class BtnFinishListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mTaskSensorData.enableFinish();
            mTaskWifiRssData.enableFinish();
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
        return file.exists() || (file.getParentFile().mkdir() && file.createNewFile());
    }

    /**
     * Message showing methods.
     */

    static void sendToast(String msg) {
        Message message = Message.obtain();
        message.what = MainActivity.MSGTYPE_TOAST;
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    static synchronized void info(String msg) {
        msg = String.format("<font color=\"#00FF00\">%s<br>",statusDateFormat.format(new Date()) + msg);
        Message message = Message.obtain();
        message.what = MainActivity.MSGTYPE_STATUS;
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    static synchronized void error(String msg) {
        msg = String.format("<font color=\"#FF0000\">%s<br>",statusDateFormat.format(new Date()) + msg);
        Message message = Message.obtain();
        message.what = MainActivity.MSGTYPE_STATUS;
        message.obj = msg;
        mHandler.sendMessage(message);
    }

    private void checkDirs() {
        if (!checkDir(new File(PATH))) {
            MainActivity.error("Root directory creation failed!");
        }
        if (!checkDir(new File(PATH_SENSOR_DATA))) {
            MainActivity.error("Sensor data directory creation failed!");
        }
        if (!checkDir(new File(PATH_WIFI_RSS))) {
            MainActivity.error("Wifi rss directory creation failed!");
        }
    }

    private boolean checkDir(File file) {
        return file.exists() || file.mkdir();
    }
}
