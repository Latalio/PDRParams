package com.la.pdrparams;

import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;

public class TaskObtainWiFiRSSData extends Thread {
    private final String TAG = TaskObtainWiFiRSSData.class.getSimpleName();
    private final WifiManager mManager;

    private StringBuilder sb = new StringBuilder();

    private long interval = 1000;

    public TaskObtainWiFiRSSData(WifiManager manager) {
        mManager = manager;
    }

    private enum ProcessState {
        IDLE,
        PRE_OPERATING,
        OPERATING,
        POST_OPERATING,
        STOPPED
    }
    private ProcessState mState = ProcessState.IDLE;

    private boolean start = false;
    private boolean finish = false;


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

    void idle() {
        if (start) {
            start = false;
            mState = ProcessState.PRE_OPERATING;
            MainActivity.sendToast("RSS记录开始");
        }
    }

    void pre() {

    }

    void oper() {
        for(;;) {
            if (!mManager.startScan()) {
                MainActivity.sendToast("AP扫描速度过快！");
            }
            //


            try {
                Thread.sleep(interval);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    void post() {

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
