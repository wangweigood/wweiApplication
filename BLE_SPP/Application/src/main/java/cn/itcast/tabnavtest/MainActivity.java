package cn.itcast.tabnavtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.smartwebee.android.blespp.R;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity {

    ViewPager viewPager;
    TabLayout tabLayout;

    public static FragmentPagerAdapter pagerAdapter;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final ArrayList<Fragment> fragments = new ArrayList<>();
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

            viewPager = (ViewPager) findViewById(R.id.container);

            viewPager.setAdapter(pagerAdapter);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if (savedInstanceState != null) onSaveInstanceState(savedInstanceState);

        viewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        final ArrayList<Fragment> fragments = new ArrayList<>();
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
