package com.hiqes.android.demopermissionsm.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.hiqes.android.demopermissionsm.util.Logger;

public class TextViewLogWriter implements Logger.LoggerWriter,
                                          Handler.Callback {
    private static final int            MSG_WRITE_ENTRY = 934027;

    private TextView            mOutView;
    private Handler             mHandler;

    public TextViewLogWriter(TextView outView) {
        mOutView = outView;
        mHandler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public void writeEntry(int priority, String tag, String msg) {
        String                  out = Logger.formatEntry(priority, tag, msg);
        mHandler.obtainMessage(MSG_WRITE_ENTRY, out).sendToTarget();
    }

    @Override
    public boolean handleMessage(Message msg) {
        boolean                 ret = false;

        switch (msg.what) {
            case MSG_WRITE_ENTRY:
                String out = (String)msg.obj;
                mOutView.append(out);
                ret = true;
                break;
        }

        return ret;
    }
}
