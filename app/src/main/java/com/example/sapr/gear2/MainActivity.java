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
    int addSeries(float data,float data2,float data3,float data4);
};

interface GraphPulseListener {
    int addSeries2(float data,float data2,float data3,float data4);
};


public class MainActivity extends AppCompatActivity implements GraphListener,GraphPulseListener {
    private final ControlFragment CF = new ControlFragment();
    private final GraphFragment GF = new GraphFragment();
    private final GraphFragment_pulse GFP = new GraphFragment_pulse();
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
        pager.setOffscreenPageLimit(4);

        //Attach the ViewPager to the TabLayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    public int addSeries(float data,float data2,float data3,float data4) {
        if (GF != null) {
            return GF.addSeries(data,data2,data3,data4);
        }
        return 0;
    }
    @Override
    public int addSeries2(float data,float data2,float data3,float data4) {
        if (GF != null) {
            return GFP.addSeries2(data,data2,data3,data4);
        }
        return 0;
    }


    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return CF;
                case 1:
                    return GF;
                case 2:
                    return GFP;
                case 3:
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
                    return getResources().getText(R.string.page_graph2);
                case 3:
                    return getResources().getText(R.string.page_base);
            }
            return null;
        }
    }
}



