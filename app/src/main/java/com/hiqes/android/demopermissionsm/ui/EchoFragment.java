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
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiqes.android.demopermissionsm.R;
import com.hiqes.android.demopermissionsm.loader.EchoMessageEndpoint;
import com.hiqes.android.demopermissionsm.model.Message;
import com.hiqes.android.demopermissionsm.util.DiskLogWriter;
import com.hiqes.android.demopermissionsm.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EchoFragment extends Fragment implements Callback<Message> {
    private static final String         TAG = EchoFragment.class.getSimpleName();
    private static final int            REQ_CODE_EXT_STORAGE = 828;

    private EditText            mEchoText;
    private TextView            mProgLogText;
    private CheckBox            mSaveProgLog;
    private Button              mSubmitBtn;
    private EchoMessageEndpoint mEndpoint;
    private InputMethodManager  mInMgr;
    private TextViewLogWriter   mTvLogWriter;
    private DiskLogWriter       mDiskLogWriter;

    public static EchoFragment newInstance() {
        EchoFragment fragment = new EchoFragment();
        return fragment;
    }

    public EchoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInMgr =
            (InputMethodManager)getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        RestAdapter radapter = new RestAdapter.Builder()
            .setEndpoint(getString(R.string.endpoint_url))
            .build();
        mEndpoint = radapter.create(EchoMessageEndpoint.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup      container,
                             Bundle         savedInstanceState) {
        View                    root;

        // Inflate the layout for this fragment
        root         = inflater.inflate(R.layout.fragment_echo,
                                        container, false);
        mEchoText    = (EditText)root.findViewById(R.id.echo_text);
        mEchoText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSubmitListener.onClick(mSubmitBtn);
                }

                return false;
            }
        });

        mProgLogText = (TextView)root.findViewById(R.id.progress_log);
        mProgLogText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mSaveProgLog = (CheckBox)root.findViewById(R.id.save_log);
        mSaveProgLog.setOnCheckedChangeListener(mCheckChangeListener);
        mTvLogWriter = new TextViewLogWriter(mProgLogText);

        mSubmitBtn   = (Button)root.findViewById(R.id.echo_submit);
        mSubmitBtn.setOnClickListener(mSubmitListener);
        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        Logger.registerWriter(mTvLogWriter);
    }


    @Override
    public void onPause() {
        Logger.unregisterWriter(mTvLogWriter);
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_CODE_EXT_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDiskLogger();
            } else {
                AppCompatActivity act = (AppCompatActivity)getContext();

                if (act.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //  Just use toast for now
                    Toast.makeText(act,
                            getString(R.string.write_ext_explain),
                            Toast.LENGTH_LONG).show();

                    //  Return, we can't do anything
                    mSaveProgLog.setChecked(false);
                }
            }
        }
    }

    private View.OnClickListener    mSubmitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String              outMsg = mEchoText.getText().toString();

            if (TextUtils.isEmpty(outMsg)) {
                return;
            }

            mInMgr.hideSoftInputFromWindow(mEchoText.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            Message newMsg = new Message(outMsg);
            mEchoText.setText("");

            Logger.d(TAG, "Submitting: " + outMsg);
            setControlsEnable(false);
            mEndpoint.echo(newMsg, EchoFragment.this);
        }
    };


    private void startDiskLogger() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ||
                Environment.MEDIA_MOUNTED.equals(state)) {
            Logger.d(TAG, "External storage available: " + state);

            File extDir = new File(Environment.getExternalStorageDirectory(),
                    LogLoadDialog.PROG_SAVE_DIR_NAME);
            if (!extDir.exists()) {
                if (!extDir.mkdir()) {
                    String errMsg = getString(R.string.err_unable_to_mkdir);
                    Logger.e(TAG, errMsg);
                    Toast.makeText(getContext(),
                            errMsg,
                            Toast.LENGTH_LONG).show();
                    mSaveProgLog.setChecked(false);
                    return;
                }
            }

            try {
                mDiskLogWriter = new DiskLogWriter(extDir);
                Logger.registerWriter(mDiskLogWriter);
                Logger.d(TAG, "Added disk logger");
            } catch (IOException e) {
                String errMsg = getString(R.string.err_unable_to_create_log);
                Logger.e(TAG, errMsg);
                Toast.makeText(getContext(), errMsg, Toast.LENGTH_LONG).show();
                mSaveProgLog.setChecked(false);
            }
        }
    }


    private CompoundButton.OnCheckedChangeListener mCheckChangeListener =
        new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                //  Create and start our disk logger, if allowed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AppCompatActivity act = (AppCompatActivity)getContext();

                    if (act.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        //  Request the permission, we'll handle the response later
                        String[] reqPerms = { Manifest.permission.WRITE_EXTERNAL_STORAGE };
                        act.requestPermissions(reqPerms, REQ_CODE_EXT_STORAGE);
                        return;
                    }
                }

                startDiskLogger();
            } else {
                //  Unregister any previous logger we had injected
                if (mDiskLogWriter != null) {
                    Logger.d(TAG, "Removing disk logger");
                    Logger.unregisterWriter(mDiskLogWriter);
                    mDiskLogWriter.cleanup();
                    mDiskLogWriter = null;
                }
            }
        }
    };


    private void setControlsEnable(boolean enabled) {
        mSaveProgLog.setEnabled(enabled);
        mSubmitBtn.setEnabled(enabled);
        mEchoText.setEnabled(enabled);
    }


    @Override
    public void success(Message message, Response response) {
        Logger.d(TAG,
                 "Success! Endpoint sent back: " +
                     message.getMessage());
        setControlsEnable(true);
    }

    @Override
    public void failure(RetrofitError error) {
        Logger.d(TAG, "Failure!  Got error: " + error.getMessage());
        Toast.makeText(getContext(),
                       String.format(Locale.getDefault(),
                                     getString(R.string.err_endpoint_msg_fmt),
                                     error.getMessage()),
                Toast.LENGTH_LONG)
            .show();
        setControlsEnable(true);
    }
}
