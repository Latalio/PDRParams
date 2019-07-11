package com.la.pdrparams;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskObtainSensorInfo extends AsyncTask<Void,Void,Boolean> {
    private final String TAG = TaskObtainSensorInfo.class.getSimpleName();

    private List<Integer> mSensorTypes = new ArrayList<Integer>() {{
        add(Sensor.TYPE_ACCELEROMETER);
        add(Sensor.TYPE_MAGNETIC_FIELD);
        add(Sensor.TYPE_GYROSCOPE);
        add(Sensor.TYPE_ROTATION_VECTOR);
        add(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }};

    private final SensorManager mManager;

    public TaskObtainSensorInfo(SensorManager manager) {
        mManager = manager;
    }

    @Override
    public Boolean doInBackground(Void... voids) {
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

        // todo why this problem when using storage API?
//            String dirpath = getExternalFilesDir("sensorInfo").getAbsolutePath();
        //todo consider the Locale issues
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        String filename = "/sensorInfo/si " + sdf.format(new Date()) + ".txt";

        try {
            File file = new File(MainActivity.PATH + filename);
            if (MainActivity.fileExist(file)) return Boolean.FALSE;

            FileWriter writer = new FileWriter(file);
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            MainActivity.sendToast("传感器信息已获取");
        } else {
            MainActivity.sendToast("文件创建或存储失败");
        }
    }


}
