package com.la.pdrparams;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView mTxtMonitor;

    SensorManager mManager;
    Sensor mAccelerometor;
    Sensor mAmbientTemperature;
    Sensor mGravity;
    Sensor mGyroscope;
    Sensor mLight;
    Sensor mLinearAccelerometor;
    Sensor mMageniticField;
    Sensor mOrientation;
    Sensor mPressure;
    Sensor mProximity;
    Sensor mRelativeHumidity;
    Sensor mRotationVector;
    Sensor mTemperature;

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
}
