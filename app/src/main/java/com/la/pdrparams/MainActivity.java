package com.la.pdrparams;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView mTxtMonitor;

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

    private final int NUM_SENSOR = 13;

    List<Integer> mSensorTypes = new ArrayList<Integer>() {{
        add(Sensor.TYPE_ACCELEROMETER);
        add(Sensor.TYPE_MAGNETIC_FIELD);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTxtMonitor = findViewById(R.id.txt_monitor);

        mManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private class CollectSensorInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String appRootDirPath = getExternalFilesDir(null).getAbsolutePath();
            File file = new File(appRootDirPath+"sensorInfo");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(Build.MODEL).append("\n");
            sb.append("name").append(",")
                    .append("vendor").append(",")
                    .append("version").append(",")
                    .append("type").append(",")
                    .append("maxRange").append(",")
                    .append("resolution").append(",")
                    .append("power").append(",")
                    .append("minDelay").append(",")
                    .append("fifoReservedEventCount").append(",")
                    .append("fifoMaxEventCount").append(",")
                    .append("stringType").append(",")
                    .append("maxDelay").append(",")
                    .append("id").append("\n");

            for (int sensorType : mSensorTypes) {
                Sensor sensor = mManager.getDefaultSensor(sensorType);

                sb.append(sensor.getName()).append(",")
                        .append(sensor.getVendor()).append(",")
                        .append(sensor.getVersion()).append(",")
                        .append(sensor.getType()).append(",")
                        .append(sensor.getMaximumRange()).append(",")
                        .append(sensor.getResolution()).append(",")
                        .append(sensor.getPower()).append(",")
                        .append(sensor.getMinDelay()).append(",")
                        .append(sensor.getFifoReservedEventCount()).append(",")
                        .append(sensor.getFifoMaxEventCount()).append(",")
                        .append(sensor.getStringType()).append(",")
                        .append(sensor.getMaxDelay()).append(",")
                        .append(sensor.getId()).append("\n");
            }
        }

    }
}
