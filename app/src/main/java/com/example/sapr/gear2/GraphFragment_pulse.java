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

    private GraphView graph;
    private LineGraphSeries<DataPoint> series;
    DataPoint[] values;
    double[] x;
    double[] y;

    Calendar c;
    long start_time=0;
    long end_time=0;

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


        values = new DataPoint[count];

        for (int i = 0; i < count; i++) {
            DataPoint v = new DataPoint(0, 0);
            values[i] = v;
        }
        graph = (GraphView) layout.findViewById(R.id.graph2);
        series = new LineGraphSeries<DataPoint>();

        series.setDrawDataPoints(true);
        //graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(-graph_max1);
        graph.getViewport().setMaxY(graph_max1);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(count);
        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        //graph.getViewport().setScalable(true);
        //graph.getViewport().setScalableY(true);
        graph.getGridLabelRenderer().setTextSize(20);
        graph.getGridLabelRenderer().setPadding(40);
        graph.addSeries(series);




        Log.d("graph", "start");

        if (savedInstanceState != null) {
            data_num = savedInstanceState.getInt("data_num");
            x = savedInstanceState.getDoubleArray("x");
            y = savedInstanceState.getDoubleArray("y");

            for (int i = 0; i < count; i++) {
                values[i] = new DataPoint(x[i],y[i]);

            }
            series.resetData(values);
        }

        return layout;
    }

    public int addSeries2(float data,float data2, float data3, float data4) {

        end_time = c.getTimeInMillis();
        int cur_time = (int)((end_time-start_time)/1000);
        DataPoint v = new DataPoint(cur_time, data);

        try {
            for (int i = 0; i < count - 1; i++) {

                values[i] = values[i + 1];

            }

            }catch (NullPointerException e){
            Log.d("values",String.valueOf(values));
        }

        series.appendData(v, true, count);

        values[count - 1] = v;


        data_num++;
        return 0;
    }


    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {

                series.resetData(values);
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
            x[i] = values[i].getX();
            y[i] = values[i].getY();
        }
        savedInstanceState.putInt("data_num",data_num);
        savedInstanceState.putDoubleArray("x",x);
        savedInstanceState.putDoubleArray("y",y);
    }

}
