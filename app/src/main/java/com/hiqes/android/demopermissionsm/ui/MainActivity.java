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
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.hiqes.andele.Andele;
import com.hiqes.andele.PermissionDetails;
import com.hiqes.andele.PermissionUse;
import com.hiqes.andele.ProtectedAction;
import com.hiqes.andele.SimpleUserPrompter;
import com.hiqes.android.demopermissionsm.R;
import com.hiqes.android.demopermissionsm.util.Logger;


public class MainActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener,
                                                               ProtectedAction.Listener {
    private static final String         TAG = MainActivity.class.getSimpleName();
    private static final int            REQ_PERM_READ_CALL_LOG = 708;

    private static final int            TAB_ECHO = 0;
    private static final int            TAB_LOAD_PROG_LOG = 1;
    private static final int            NUM_TABS = 2;

    private ViewPager           mPager;
    private FragTabAdapter      mAdapter;
    private ProtectedAction     mReadCallLogAction;
    private ProtectedAction     mGetLocationAction;
    private SimpleUserPrompter  mPrompter;
    private LocationManager     mLocMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mLocMgr = (LocationManager)getSystemService(LOCATION_SERVICE);

        mPager = (ViewPager)findViewById(R.id.pager);
        mAdapter = new FragTabAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(this);

        mPrompter = new SimpleUserPrompter(this,
                                           findViewById(android.R.id.content),
                                           -1,
                                           -1,
                                           -1,
                                           -1,
                                           -1);

        ProtectedAction.Builder builder = new ProtectedAction.Builder();
        builder.withPermission(Manifest.permission.READ_CALL_LOG)
               .withUsage(PermissionUse.ESSENTIAL)
               .listener(this)
               .actionCallback(mGetCallLogCb)
               .userPromptCallback(mPrompter);
        mReadCallLogAction = builder.build();

        builder = new ProtectedAction.Builder();
        builder.withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
               .withUsage(PermissionUse.CRITICAL)
               .listener(this)
               .actionCallback(mGetLocationCb)
               .userPromptCallback(mPrompter);

        mGetLocationAction = builder.build();

        ProtectedAction[] actions = new ProtectedAction[2];
        actions[0] = mReadCallLogAction;
        actions[1] = mGetLocationAction;
        Andele.checkAndRequestMandatoryPermissions(this, actions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater            inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater            inflater = new MenuInflater(this);
        MenuItem                lastCallItem;
        MenuItem                lastLocationItem;

        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        lastCallItem = menu.findItem(R.id.get_last_call);
        lastLocationItem = menu.findItem(R.id.get_last_location);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) ==
                    PackageManager.PERMISSION_DENIED) {
                menu.removeItem(lastCallItem.getItemId());
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {
                menu.removeItem(lastLocationItem.getItemId());
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean                 handled = false;
        int                     id = item.getItemId();

        if (id == R.id.get_last_call) {
            Andele.checkAndExecute(this, mReadCallLogAction);
            handled = true;
        }

        if (id == R.id.get_last_location) {
            Andele.checkAndExecute(this, mGetLocationAction);
            handled = true;
        }

        if (!handled) {
            handled = super.onOptionsItemSelected(item);
        }

        return handled;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!Andele.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onPermissionGranted(PermissionDetails details) {
        Log.d(TAG, "onPermissionGranted: " + details.getPermission());
    }

    @Override
    public void onPermissionDenied(PermissionDetails details) {
        Log.d(TAG, "onPermissionDenied: " + details.getPermission());
    }

    private ProtectedAction.ActionCallback mGetLocationCb = new ProtectedAction.ActionCallback() {
        @Override
        @SuppressWarnings("ResourceType")
        public void doAction(ProtectedAction action) {
            Location            lastLocation;
            String              lastLocString;

            lastLocation = mLocMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastLocation == null) {
                lastLocString = "<UNKNOWN>";
            } else {
                lastLocString = String.format("Lat: %f, Long: %f",
                                              lastLocation.getLatitude(),
                                              lastLocation.getLongitude());
            }

            Logger.i(TAG, "Retrieved last approx location: " + lastLocString);
        }
    };

    private ProtectedAction.ActionCallback mGetCallLogCb = new ProtectedAction.ActionCallback() {
        @Override
        public void doAction(ProtectedAction protectedAction) {
            String lastOutCallNum = CallLog.Calls.getLastOutgoingCall(MainActivity.this);
            if (TextUtils.isEmpty(lastOutCallNum)) {
                lastOutCallNum = getString(R.string.no_calL);
            }

            Logger.i(TAG,
                    "Retrieved last outgoing call number: " +
                            lastOutCallNum);
        }
    };

    private class FragTabAdapter extends FragmentPagerAdapter implements ActionBar.TabListener {
        public FragTabAdapter(FragmentManager fm) {
            super(fm);

            ActionBar       ab = getSupportActionBar();
            ActionBar.Tab   tab = ab.newTab()
                .setText(getString(R.string.echo_tab_label))
                .setTabListener(this);
            ab.addTab(tab, TAB_ECHO);

            tab = ab.newTab()
                .setText(getString(R.string.log_tab_label))
                .setTabListener(this);
            ab.addTab(tab, TAB_LOAD_PROG_LOG);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment            ret = null;

            switch (position) {
                case TAB_ECHO:
                    ret = EchoFragment.newInstance();
                    break;

                case TAB_LOAD_PROG_LOG:
                    ret = ProgLogFragment.newInstance();
                    break;
            }

            return ret;
        }

        @Override
        public int getCount() {
            return NUM_TABS;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //  Ignore
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //  Ignore
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //  Ignore
    }

    @Override
    public void onPageSelected(int position) {
        ActionBar ab = getSupportActionBar();
        ab.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        //  Ignore
    }
}
