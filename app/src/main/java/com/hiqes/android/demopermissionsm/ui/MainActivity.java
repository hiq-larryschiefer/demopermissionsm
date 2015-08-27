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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.hiqes.android.demopermissionsm.R;
import com.hiqes.android.demopermissionsm.util.Logger;

public class MainActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {
    private static final String         TAG = MainActivity.class.getSimpleName();

    private static final int            TAB_ECHO = 0;
    private static final int            TAB_LOAD_PROG_LOG = 1;
    private static final int            NUM_TABS = 2;

    private ViewPager           mPager;
    private FragTabAdapter      mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ActionBar ab = getSupportActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mPager = (ViewPager)findViewById(R.id.pager);
        mAdapter = new FragTabAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean                 handled = false;

        if (item.getItemId() == R.id.get_last_call) {
            //  Try to get the last outgoing call number
            String lastOutCallNum = CallLog.Calls.getLastOutgoingCall(this);
            if (TextUtils.isEmpty(lastOutCallNum)) {
                lastOutCallNum = getString(R.string.no_calL);
            }

            Logger.i(TAG,
                    "Retrieved last outgoing call number: " +
                            lastOutCallNum);
            handled = true;
        }

        if (!handled) {
            handled = super.onOptionsItemSelected(item);
        }

        return handled;
    }

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
