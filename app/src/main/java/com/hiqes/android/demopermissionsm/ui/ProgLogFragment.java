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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hiqes.android.demopermissionsm.R;
import com.hiqes.android.demopermissionsm.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ProgLogFragment extends Fragment implements LogLoadDialog.Callbacks {
    private static final String         TAG = ProgLogFragment.class.getSimpleName();
    private static final int            REQ_CODE_READ_EXT_STORAGE = 808;

    private Button              mLoad;
    private TextView            mLoadedLog;


    public static ProgLogFragment newInstance() {
        ProgLogFragment fragment = new ProgLogFragment();
        return fragment;
    }

    public ProgLogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View                    root;

        root = inflater.inflate(R.layout.fragment_prog_log, container, false);
        mLoadedLog = (TextView)root.findViewById(R.id.progress_log);
        mLoadedLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        mLoad      = (Button)root.findViewById(R.id.prog_log_load);
        mLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Check to see if we have the permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AppCompatActivity act = (AppCompatActivity) getContext();
                    if (act.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        if (act.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            //  Just use toast for now
                            Toast.makeText(act,
                                    getString(R.string.read_ext_explain),
                                    Toast.LENGTH_LONG).show();

                            //  Return, we can't do anything
                            return;
                        }

                        //  Request the permission, we'll handle the response later
                        String[] reqPerms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        act.requestPermissions(reqPerms, REQ_CODE_READ_EXT_STORAGE);
                        return;
                    }
                }

                startLoadDialog();
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_CODE_READ_EXT_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLoadDialog();
            } else {
                Toast.makeText(getContext(),
                               R.string.read_perm_denied,
                               Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLoadDialog() {
        //  Load the dialog to choose which file to load
        DialogFragment df = new LogLoadDialog();
        df.show(getChildFragmentManager(),
                getString(R.string.prog_load_title));
    }

    public void doLoadFile(String filePath) {
        String                  errMsg;
        Context                 ctx = getActivity();

        try {
            File inFile = new File(filePath);
            FileReader reader = new FileReader(inFile);
            char[] text = new char[(int)inFile.length()];
            int readCount = reader.read(text);
            if (readCount != inFile.length()) {
                errMsg = ctx.getString(R.string.warn_file_truncated);
                Toast.makeText(getActivity(),
                               R.string.warn_file_truncated,
                               Toast.LENGTH_LONG).show();
                Logger.w(TAG, errMsg);
            }

            Logger.d(TAG,
                     "Read contents from log: " + inFile.getName());
            reader.close();
            mLoadedLog.setText(text, 0, readCount);
        } catch (FileNotFoundException e) {
            Logger.e(TAG, "Unable to open file: " + filePath);
            errMsg = ctx.getString(R.string.err_file_not_found);
            Toast.makeText(getActivity(),
                           errMsg,
                           Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Logger.e(TAG, "Failed to read saved file: " + e.getMessage());
            errMsg = ctx.getString(R.string.err_file_read);
            Toast.makeText(getActivity(),
                           errMsg,
                           Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onFileSelected(String path) {
        doLoadFile(path);
    }

}
