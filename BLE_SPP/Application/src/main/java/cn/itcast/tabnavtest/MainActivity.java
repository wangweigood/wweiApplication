package cn.itcast.tabnavtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.smartwebee.android.blespp.BluetoothLeService;
import com.smartwebee.android.blespp.R;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    TabLayout tabLayout;
    static ArrayList<Fragment> fragments;
    public static FragmentPagerAdapter pagerAdapter;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (pagerAdapter != null) {
                pagerAdapter.notifyDataSetChanged();
            }

        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if (savedInstanceState != null) onSaveInstanceState(savedInstanceState);

        viewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        fragments = new ArrayList<>();
        fragments.add(new PlaceholderFragment());
        fragments.add(new PlaceholderFragment1());
        fragments.add(new PlaceholderFragment2());
        fragments.add(new PlaceholderFragment3());
        fragments.add(new PlaceholderFragment4());
        fragments.add(new PlaceholderFragment5());
        for (int i = 6; i < 10; i++) fragments.add(new PlaceholderFragment());

        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {

                return fragments.get(position);
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }

            @Override
            public int getCount() {
                return 10;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "POCw";
                    case 2:
                        return "AF List";
                    case 4:
                        return "MemoStore";
                    case 6:
                        return "Announce";
                    case 8:
                        return "Presets";
                }
                return null;
            }
        };

        for (int i = 0; i < pagerAdapter.getCount(); i += 2) {
            tabLayout.addTab(tabLayout.newTab().setText(pagerAdapter.getPageTitle(i)));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition() * 2);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tabLayout.setScrollPosition(position / 2, 0, false);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(pagerAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_data:
                Intent intent = new Intent(BluetoothLeService.ACTION_TO_SAVE);
                sendBroadcast(intent);
                return true;

            case R.id.manager:
                Intent explorerIntent = new Intent(MainActivity.this, FileExplorer.class);
                startActivity(explorerIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        IntentFilter intentFilter = new IntentFilter("parsed_data_update");
        registerReceiver(receiver, intentFilter);

        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    }

