package com.example.sapr.gear2;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class GraphFragment_pulse extends Fragment {

    private GraphView graph3;
    private LineGraphSeries<DataPoint> series3;
    private DataPoint[] values2;
    private double[] x;
    private double[] y;

    private Calendar c;
    private long start_time=0;
    private long end_time=0;

    private int data_num = 0;
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private int count = 400;
    private int graph_max1 = 300;
    public GraphFragment_pulse() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View layout = inflater.inflate(R.layout.fragment_graph2, container, false);
        setRetainInstance(true);
        c = Calendar.getInstance();

        start_time = c.getTimeInMillis();
        end_time = c.getTimeInMillis();

        data_num = 0;
        x = new double[count];
        x = new double[count];


        values2 = new DataPoint[count];

        for (int i = 0; i < count; i++) {
            DataPoint v = new DataPoint(0, 0);
            values2[i] = v;
        }
        graph3 = (GraphView) layout.findViewById(R.id.graph2);
        series3 = new LineGraphSeries<DataPoint>();

        series3.setDrawDataPoints(true);
        //graph.getViewport().setYAxisBoundsManual(true);
        graph3.getViewport().setXAxisBoundsManual(true);
        graph3.getViewport().setMinY(-5);
        graph3.getViewport().setMaxY(200);
        graph3.getViewport().setMinX(-5);
        graph3.getViewport().setMaxX(60);
        graph3.getViewport().setScrollable(true); // enables horizontal scrolling
        graph3.getViewport().setScrollableY(true); // enables vertical scrolling
        graph3.getViewport().setScalable(false); // enables horizontal zooming and scrolling
        graph3.getViewport().setScalableY(false); // enables vertical zooming and scrolling
        //graph3.getViewport().setScalable(true);
        //graph3.getViewport().setScalableY(true);
        graph3.getGridLabelRenderer().setTextSize(20);
        graph3.getGridLabelRenderer().setPadding(40);
        graph3.addSeries(series3);




        Log.d("graph3", "start2");

        if (savedInstanceState != null) {
            data_num = savedInstanceState.getInt("data_num");
            x = savedInstanceState.getDoubleArray("x");
            y = savedInstanceState.getDoubleArray("y");

            for (int i = 0; i < count; i++) {
                values2[i] = new DataPoint(x[i],y[i]);

            }
            series3.resetData(values2);
        }

        return layout;
    }

    public int addSeries2(float data,float data2, float data3, float data4) {

        c = Calendar.getInstance();
        end_time = c.getTimeInMillis();
        float cur_time = (float)((end_time-start_time));
        DataPoint v = new DataPoint(cur_time/1000, data);



        try {
            for (int i = 0; i < count - 1; i++) {

                values2[i] = values2[i + 1];

            }


        if (series3 != null) {
            series3.appendData(v, true, count);
            values2[count - 1] = v;
        }
        else{

            series3 = new LineGraphSeries<DataPoint>();
            values2 = new DataPoint[count];
            v = new DataPoint(cur_time/1000, 0);
            series3.appendData(v, true, count);
            values2[count - 1] = v;
        }

        }catch (NullPointerException e){
            Log.d("values2",String.valueOf(values2));
        }
        data_num++;
        return 0;
    }


    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {

                series3.resetData(values2);
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mTimer1, 1000);

    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        for (int i = 0; i < count ; i++) {
            x[i] = values2[i].getX();
            y[i] = values2[i].getY();
        }
        savedInstanceState.putInt("data_num",data_num);
        savedInstanceState.putDoubleArray("x",x);
        savedInstanceState.putDoubleArray("y",y);
    }

}
