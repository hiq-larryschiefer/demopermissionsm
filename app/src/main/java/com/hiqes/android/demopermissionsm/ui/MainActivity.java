package com.hiqes.android.demopermissionsm.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

import com.hiqes.android.demopermissionsm.R;

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
