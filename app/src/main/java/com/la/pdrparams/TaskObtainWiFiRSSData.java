package com.la.pdrparams;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class TaskObtainWiFiRSSData extends TaskThread {
    private final String TAG = TaskObtainWiFiRSSData.class.getSimpleName();

    private final WifiManager mManager;

    TaskObtainWiFiRSSData(WifiManager manager) {
        mManager = manager;
    }

    @Override
    void idle() {
        if (start) {
            start = false;
            mState = ProcessState.PRE_OPERATING;
            MainActivity.info("Wifi rss obtaining...");
        }
    }

    @Override
    void pre() {
        mCount = 0;

        sb.delete(0, sb.length());
        sb.append(Build.MODEL).append("\n");
        sb.append("index").append(",").append("stepTimestamp").append("\n");
        sb.append("BSSID").append(",")
                .append("SSID").append(",")
                .append("capabilities").append(",")
                .append("centerFreq0").append(",")
                .append("centerFreq1").append(",")
                .append("channelWidth").append(",")
                .append("frequency").append(",")
                .append("level").append(",")
                .append("operatorFriendlyName").append(",")
                .append("timestamp").append(",")
                .append("venueName").append(",")
                .append("is80211mcResponder").append(",")
                .append("isPasspointNetwork").append("\n");

        mState = ProcessState.OPERATING;
    }

    @Override
    void oper() {
        for(;;) {
            if (!mManager.startScan()) {
                MainActivity.error("AP scanning speed is too fast!");
            }
            sb.append("#").append(mCount++).append(",")
                    .append(System.currentTimeMillis()).append("\n");

            List<ScanResult> results = mManager.getScanResults();
            if (results.isEmpty()) {
                MainActivity.error("No scan result!");
            } else {
                for(ScanResult result : results) {
                    sb.append(result.BSSID).append(",")
                            .append(result.SSID).append(",")
                            .append(result.capabilities).append(",")
                            .append(result.centerFreq0).append(",")
                            .append(result.centerFreq1).append(",")
                            .append(result.channelWidth).append(",")
                            .append(result.frequency).append(",")
                            .append(result.level).append(",")
                            .append(result.operatorFriendlyName).append(",")
                            .append(result.timestamp).append(",")
                            .append(result.venueName).append(",")
                            .append(result.is80211mcResponder()).append(",")
                            .append(result.isPasspointNetwork()).append("\n");
                }
            }

            try {
                Thread.sleep(interval);
            } catch(InterruptedException e) {
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

        String dirpath = MainActivity.PATH_WIFI_RSS;
        String filename = "/rd " + filenameDF.format(new Date()) + ".txt";

        try {
            File file = new File(dirpath + filename);
            if (!file.createNewFile()) {
                MainActivity.error("Wifi Rss file creation failed!");
                return;
            }

            FileWriter writer = new FileWriter(file);
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            MainActivity.error("Wifi Rss file storage failed!");
        }

        MainActivity.info("Wifi Rss obtained.");

    }
}
