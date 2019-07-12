package com.la.pdrparams;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class TaskObtainSensorData extends TaskThread {
    // todo How to ensure that only one TaskSensorData Thread is running?
    private final String TAG = TaskObtainSensorData.class.getSimpleName();
    private final MainActivity mActivity;

    TaskObtainSensorData(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    void idle() {
        if (start) {
            start = false;
            mState = ProcessState.PRE_OPERATING;
            MainActivity.info("Sensor data obtaining...");
        }
    }

    @Override
    void pre() {
        mCount = 0;

        sb.delete(0, sb.length());
        sb.append(Build.MODEL).append("\n");
        sb.append("index").append(",")
                .append("timestamp").append(",")
                .append("accx").append(",")
                .append("accy").append(",")
                .append("accz").append(",")
                .append("magx").append(",")
                .append("magy").append(",")
                .append("magz").append(",")
                .append("gyrx").append(",")
                .append("gyry").append(",")
                .append("gyrz").append(",")
                .append("mazimuth").append(",")
                .append("mpitch").append(",")
                .append("mroll").append(",")
                .append("gazimuth").append(",")
                .append("gpitch").append(",")
                .append("groll").append("\n");

        mState = ProcessState.OPERATING;
    }

    @Override
    void oper() {
        for(;;) {
            sb.append(mCount++).append(",")
                    .append(System.currentTimeMillis()).append(",")
                    .append(mActivity.getValueAcc()[0]).append(",")
                    .append(mActivity.getValueAcc()[1]).append(",")
                    .append(mActivity.getValueAcc()[2]).append(",")
                    .append(mActivity.getValueMag()[0]).append(",")
                    .append(mActivity.getValueMag()[1]).append(",")
                    .append(mActivity.getValueMag()[2]).append(",")
                    .append(mActivity.getValueGyr()[0]).append(",")
                    .append(mActivity.getValueGyr()[1]).append(",")
                    .append(mActivity.getValueGyr()[2]).append(",")
                    .append(mActivity.getValueMOri()[0]).append(",")
                    .append(mActivity.getValueMOri()[1]).append(",")
                    .append(mActivity.getValueMOri()[2]).append(",")
                    .append(mActivity.getValueGOri()[0]).append(",")
                    .append(mActivity.getValueGOri()[1]).append(",")
                    .append(mActivity.getValueGOri()[2]).append("\n");

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (finish) {
                finish = false;
                mState = ProcessState.POST_OPERATING;
                break;
            }
        }
    }

    @Override
    void post() {
        mState = ProcessState.IDLE;

        String dirpath = MainActivity.PATH_SENSOR_DATA;
        String filename = "/sd " + filenameDF.format(new Date()) + ".txt";

        try {
            File file = new File(dirpath + filename);
            if (!file.createNewFile()) {
                MainActivity.error("Sensor file creation failed!");
                return;
            }

            FileWriter writer = new FileWriter(file);
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            MainActivity.error("Sensor file storage failed!");
        }

        MainActivity.info("Sensor data obtained.");
    }
}
