package com.la.pdrparams;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private final String TAG = MainActivity.class.getSimpleName();
    public static final String PATH = "/sdcard/PDRParams";
    TextView mTxtMonitor;
    Button mBtnSensorInfo;
    Button mBtnStart;
    Button mBtnFinish;

    Handler mHandler;

    SensorManager mManager;
    Sensor mAccelerometor;
    Sensor mMageniticField;
    Sensor mOrientation;
    Sensor mGyroscope;
    Sensor mLight;
    Sensor mPressure;
    Sensor mTemperature;
    Sensor mProximity;
    Sensor mGravity;
    Sensor mLinearAccelerometor;
    Sensor mRotationVector;
    Sensor mRelativeHumidity;
    Sensor mAmbientTemperature;

    private float[] mValueAcc = new float[3];
    private float[] mValueMag = new float[3];
    private float[] mValueMOri = new float[3];
    private float[] mValueGyr = new float[3];
    private float[] mValueGOri = new float[3];

    private static Context context;

    private final int NUM_SENSOR = 13;

    private TaskObtainSensorData mTaskSensorData;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        mHandler = new Handler();

        mTxtMonitor = findViewById(R.id.txt_monitor);
        mBtnSensorInfo = findViewById(R.id.btn_sensor_info);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnFinish = findViewById(R.id.btn_finish);

        mBtnSensorInfo.setOnClickListener(new BtnSensorInfoListener());
        mBtnStart.setOnClickListener(new BtnStartListener());
        mBtnFinish.setOnClickListener(new BtnFinishListener());

        mManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometor = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMageniticField = mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mTaskSensorData = new TaskObtainSensorData(MainActivity.this);
        mTaskSensorData.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.registerListener(this, mAccelerometor, SensorManager.SENSOR_DELAY_GAME);
        mManager.registerListener(this, mMageniticField, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mManager.unregisterListener(this);
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
                mValueAcc[0] = event.values[0];
                mValueAcc[1] = event.values[1];
                mValueAcc[2] = event.values[2];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mValueMag[0] = event.values[0];
                mValueMag[1] = event.values[1];
                mValueMag[2] = event.values[2];
                break;
            default:
                break;
        }
    }


    public static Context getContext() {
        return context;
    }


    /**
     * button listeners.
     */
    private class BtnSensorInfoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            new TaskObtainSensorInfo(mManager).execute(new Void[1]);
        }
    }

    private class BtnStartListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

    private class BtnFinishListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mTaskSensorData.enableFinish();
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
}
