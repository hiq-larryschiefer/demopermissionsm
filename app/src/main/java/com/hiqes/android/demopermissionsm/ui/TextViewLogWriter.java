/*
 * Copyright (c) 2015 HIQES LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
