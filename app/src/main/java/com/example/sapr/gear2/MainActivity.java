package com.example.sapr.gear2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import static java.lang.Thread.sleep;


interface GraphListener {
    void addSeries(float data,float data2,float data3,float data4);
};


public class MainActivity extends AppCompatActivity implements GraphListener {
    private final ControlFragment CF = new ControlFragment();
    private final GraphFragment GF = new GraphFragment();
    private final BaseFragment BF = new BaseFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Attach the SectionsPagerAdapter to the ViewPager
        SectionsPagerAdapter pagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);

        //Attach the ViewPager to the TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public void addSeries(float data,float data2,float data3,float data4) {
        if (GF != null) {
            GF.addSeries(data,data2,data3,data4);
        }
    }


    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return CF;
                case 1:
                    return GF;
                case 2:
                    return BF;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getText(R.string.page_control);
                case 1:
                    return getResources().getText(R.string.page_graph);
                case 2:
                    return getResources().getText(R.string.page_base);
            }
            return null;
        }
    }
}



