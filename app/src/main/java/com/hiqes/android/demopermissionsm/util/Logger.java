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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Logger {
    private static final String         TAG = Logger.class.getSimpleName();
    public static final String          ENTRY_FMT = "%02d-%02d-%4d  %02d:%02d:%02d.%03d  %c/%s  %s\n";
    private static final char           PRIORITY_LOOKUP[] = {
            '.',
            '^',
            'V',
            'D',
            'I',
            'W',
            'E',
            'A',
    };

    private static ArrayList<LoggerWriter> sWriters;

    static {
        sWriters = new ArrayList<>();
        registerWriter(new AndroidLogWriter());
    }

    public interface LoggerWriter {
        void writeEntry(int priority, String tag, String msg);
    }

    public static void registerWriter(LoggerWriter writer) {
        synchronized (sWriters) {
            if (!sWriters.contains(writer)) {
                sWriters.add(writer);
            }
        }
    }

    public static boolean unregisterWriter(LoggerWriter writer) {
        boolean                 ret = false;

        synchronized (sWriters) {
            ret = sWriters.remove(writer);
        }

        return ret;
    }

    private static void writeEntry(int priority, String tag, String msg) {
        synchronized (sWriters) {
            for (int i = 0; i < sWriters.size(); i++) {
                LoggerWriter    curWriter = sWriters.get(i);
                curWriter.writeEntry(priority, tag, msg);
            }
        }
    }

    public static void d(String tag, String msg) {
        writeEntry(Log.DEBUG, tag, msg);
    }

    public static void w(String tag, String msg) {
        writeEntry(Log.WARN, tag, msg);
    }

    public static void i(String tag, String msg) {
        writeEntry(Log.INFO, tag, msg);
    }

    public static void e(String tag, String msg) {
        writeEntry(Log.ERROR, tag, msg);
    }

    public static String formatEntry(int priority, String tag, String msg) {
        GregorianCalendar cal = new GregorianCalendar();

        String out = String.format(Locale.getDefault(),
                                   Logger.ENTRY_FMT,
                                   cal.get(Calendar.MONTH),
                                   cal.get(Calendar.DATE),
                                   cal.get(Calendar.YEAR),
                                   cal.get(Calendar.HOUR_OF_DAY),
                                   cal.get(Calendar.MINUTE),
                                   cal.get(Calendar.SECOND),
                                   cal.get(Calendar.MILLISECOND),
                                   PRIORITY_LOOKUP[priority],
                                   tag,
                                   msg);
        return out;
    }
}
