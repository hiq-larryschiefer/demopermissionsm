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
package com.hiqes.android.demopermissionsm.util;

import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class DiskLogWriter implements Logger.LoggerWriter {
    private static final String TAG = DiskLogWriter.class.getSimpleName();
    private FileWriter          mWriter;

    public DiskLogWriter(File path) throws IOException {
        //  Create a name based on the current date/time
        Calendar cal = Calendar.getInstance();
        String filename = String.format(Locale.getDefault(),
                                        "%04d%02d%02d-%02d%02d%02d-echolog.txt",
                                        cal.get(Calendar.YEAR),
                                        cal.get(Calendar.MONTH),
                                        cal.get(Calendar.DATE),
                                        cal.get(Calendar.HOUR_OF_DAY),
                                        cal.get(Calendar.MINUTE),
                                        cal.get(Calendar.SECOND));

        //  Create a file writer for this object
        File outFile = new File(path, filename);
        mWriter = new FileWriter(outFile);
    }

    public void cleanup() {
        if (mWriter != null) {
            try {
                mWriter.close();
            } catch (IOException e) {
                //  Ignore
            }

            mWriter = null;
        }
    }

    @Override
    public void writeEntry(int priority, String tag, String msg) {
        String entry = Logger.formatEntry(priority, tag, msg);
        try {
            mWriter.append(entry);
        } catch (IOException e) {
            //  Really should cause us to cleanup or callback the app via the Logger
           Log.d(TAG, "writeEntry: failed with exception - " + e.getMessage());
        }
    }
}
