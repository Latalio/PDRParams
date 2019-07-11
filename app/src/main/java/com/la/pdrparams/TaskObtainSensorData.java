package com.la.pdrparams;

import android.os.Build;
import android.os.Handler;
import android.os.Message;
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

    private final MainActivity mActivity;
    private int mCount = 0;

    private int interval = 1000; // 50ms vs. 20Hz

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
        Log.e(TAG, "[BEGIN] run()");
        for(;;) {
            switch (mState) {
                case IDLE: idle(); break;
                case PRE_OPERATING: pre(); break;
                case OPERATING: oper(); break;
                case POST_OPERATING: post(); break;
            }
        }
    }

    private void idle() {
        if (start) {
            start = false;
            mState = ProcessState.PRE_OPERATING;
            MainActivity.sendToast("轨迹记录开始");
        }
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

    private void oper() {
        int count = 0;
        for(;;) {
            Log.e(TAG, Integer.toString(count++));
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

    private void post() {
        mState = ProcessState.IDLE;

        String dirpath = MainActivity.PATH;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        String filename = "/si " + sdf.format(new Date()) + ".txt";

        try {
            File file = new File(dirpath + filename);
            if (MainActivity.fileExist(file)) {
                MainActivity.sendToast("文件创建失败！");
                return;
            }

            FileWriter writer = new FileWriter(file);
            Log.e(TAG, dirpath + filename);
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            MainActivity.sendToast("文件存储失败！");
        }

        MainActivity.sendToast("轨迹记录完成");
    }

    void enableStart() {
        start = true;
    }

    void enableFinish() {
        finish = true;
    }

    boolean isIDLE() {
        return mState == ProcessState.IDLE;
    }



}
