package com.hiqes.android.demopermissionsm.util;

import android.util.Log;

public class AndroidLogWriter implements Logger.LoggerWriter {
    @Override
    public void writeEntry(int priority, String tag, String msg) {
        Log.println(priority, tag, msg);
    }
}
