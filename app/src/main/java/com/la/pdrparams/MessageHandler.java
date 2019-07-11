package com.la.pdrparams;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class MessageHandler extends Handler {
    private Context context;

    public MessageHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MainActivity.MSGTYPE_TOAST:
                Toast.makeText(context, (String)msg.obj, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

}
