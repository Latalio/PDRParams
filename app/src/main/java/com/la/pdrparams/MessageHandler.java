package com.la.pdrparams;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

public class MessageHandler extends Handler {
    private MainActivity activity;

    public MessageHandler(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MainActivity.MSGTYPE_TOAST:
                Toast.makeText(activity, (String)msg.obj, Toast.LENGTH_SHORT).show();
                break;
            case MainActivity.MSGTYPE_STATUS:
                activity.mTxtStatus.append(Html.fromHtml((String)msg.obj));
                toBottomLine(activity.mTxtStatus);
                break;
            default:
                break;
        }
    }

    private void toBottomLine(TextView view) {
        int offset=view.getLineCount()*view.getLineHeight();
        if(offset > view.getHeight()){
            view.scrollTo(0,offset-view.getHeight());
        }
    }


}
