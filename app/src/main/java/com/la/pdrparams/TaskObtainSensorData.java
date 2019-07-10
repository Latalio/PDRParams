package com.la.pdrparams;

import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskObtainSensorData extends Thread {
    // todo How to ensure that only one TaskSensorData Thread is running?
    private final String TAG = MainActivity.class.getSimpleName();

    private StringBuilder sb = new StringBuilder();

    private MainActivity mActivity;
    private int mCount = 0;

    private int interval = 50; // 50ms vs. 20Hz

    private boolean start = false;
    private boolean finish = false;



    private enum ProcessState {
        IDLE,
        PRE_OPERATING,
        OPERATING,
        POST_OPERATING,
        STOPPED
    }

    private ProcessState mState = ProcessState.IDLE;

    public TaskObtainSensorData(MainActivity activity) {
        mActivity = activity;
    }

    @Override
    public void run() {
        for(;;) {
            switch (mState) {
                case IDLE: idle(); break;
                case OPERATING: oper(); break;
            }
        }

    }

    private void idle() {
        // do nothing
    }

    private void pre() {
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
                .append("magz").append("\n");


        mState = ProcessState.OPERATING;
    }

    private void oper() {
        for(;;) {
            sb.append(mCount++).append(",")
                    .append(System.currentTimeMillis()).append(",")
                    .append(mActivity.getValueAcc()[0]).append(",")
                    .append(mActivity.getValueAcc()[1]).append(",")
                    .append(mActivity.getValueAcc()[2]).append(",")
                    .append(mActivity.getValueMag()[0]).append(",")
                    .append(mActivity.getValueMag()[1]).append(",")
                    .append(mActivity.getValueMag()[2]).append("\n");

            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (finish) mState = ProcessState.POST_OPERATING;
        }
    }

    private void post() {
        String dirpath = MainActivity.PATH;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        String filename = "/si " + sdf.format(new Date()) + ".txt";

        try {
            File file = new File(dirpath + filename);
            if (MainActivity.fileExist(file)) return;

            FileWriter writer = new FileWriter(file);
            Log.e(TAG, dirpath + filename);
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        return;
    }

    public void enableStart() {
        start = true;
    }

    public void enableFinish() {
        finish = true;
    }

}
