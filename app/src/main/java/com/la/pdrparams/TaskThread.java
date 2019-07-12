package com.la.pdrparams;

import java.text.SimpleDateFormat;

public abstract class TaskThread extends Thread {
    StringBuilder sb = new StringBuilder();
    final SimpleDateFormat filenameDF = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");

    boolean start = false;
    boolean finish = false;
    int interval = 1000; // 50ms vs. 20Hz

    enum ProcessState {
        IDLE,
        PRE_OPERATING,
        OPERATING,
        POST_OPERATING,
        STOPPED
    }
    ProcessState mState = ProcessState.IDLE;

    int mCount = 0;

    @Override
    public void run() {
        for(;;) {
            switch (mState) {
                case IDLE: idle(); break;
                case PRE_OPERATING: pre(); break;
                case OPERATING: oper(); break;
                case POST_OPERATING: post(); break;
            }
        }
    }

    abstract void idle();

    abstract void pre();

    abstract void oper();

    abstract void post();

    void setFrequency(int sampleFreq) {
        interval = 1000/sampleFreq; // 1000ms/Hz
    }

    int getFrequency() {
        return 1000/interval;
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
