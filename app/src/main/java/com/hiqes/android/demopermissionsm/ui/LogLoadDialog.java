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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.ArrayAdapter;

import com.hiqes.android.demopermissionsm.R;
import com.hiqes.android.demopermissionsm.util.Logger;

import java.io.File;
import java.util.Arrays;

public class LogLoadDialog extends AppCompatDialogFragment implements DialogInterface.OnClickListener {
    private static final String         TAG = LogLoadDialog.class.getSimpleName();
    public static final String          PROG_SAVE_DIR_NAME = "demop";

    private ArrayAdapter<String>    mLogFileAdapter;
    private Callbacks               mCb;
    private boolean                 mNoFiles = true;
    private File                    mLogDir;

    public interface Callbacks {
        public void onFileSelected(String path);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder     bldr = new AlertDialog.Builder(getActivity());

        //  Make sure external storage is actually available, otherwise warn
        //  the user an get out.
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ||
            Environment.MEDIA_MOUNTED.equals(state)) {
            Logger.d(TAG, "External storage available: " + state);

            //  Get the list of all files saved in our directory on external
            //  storage.  Use the generic external storage location to show
            //  the use of permissions.
            mLogDir = new File(Environment.getExternalStorageDirectory(),
                               PROG_SAVE_DIR_NAME);
            String[] files = mLogDir.list();
            if ((files != null) && (files.length > 0)) {
                mNoFiles = false;
                Logger.d(TAG, "Found files: " + Arrays.toString(files));
            } else {
                files = new String[1];
                files[0] = getString(R.string.no_saved_files);
                Logger.d(TAG, files[0]);
            }

            mLogFileAdapter = new ArrayAdapter<>(getActivity(),
                                                 R.layout.list_files,
                                                 R.id.filename,
                                                 files);
            bldr.setAdapter(mLogFileAdapter, this);
        } else {
            Logger.w(TAG, "No external storage mounted");
            bldr.setMessage(R.string.prog_load_unavail);
            bldr.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LogLoadDialog.this.dismiss();
                }
            });
        }

        bldr.setTitle(R.string.prog_load_title);
        return bldr.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String                  fileName;

        if (!mNoFiles) {
            fileName = mLogFileAdapter.getItem(which);

            mCb.onFileSelected(mLogDir.getPath() +
                               File.separator +
                               fileName);
            LogLoadDialog.this.dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCb = (Callbacks)getParentFragment();
        } catch (ClassCastException e) {
            //  Parent Fragment doesn't implement our callback i/f
            throw new IllegalArgumentException("Parent doesn't implement callback i/f");
        }
    }
}
