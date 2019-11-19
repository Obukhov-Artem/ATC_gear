package com.example.sapr.gear2;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


/**
 * A simple {@link Fragment} subclass.
 */
public class GraphFragment extends Fragment  {

    private GraphView graph;
    private LineGraphSeries<DataPoint> series;
    DataPoint[] values;
    private int data_num=0;
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private int count = 50;
    public GraphFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout= inflater.inflate(R.layout.fragment_graph, container, false);
        data_num = 0;
        values = new DataPoint[count];
        for (int i=0; i<count; i++) {
            DataPoint v = new DataPoint(0, 0);
            values[i] = v;
        }
        graph = (GraphView) layout.findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();

        series.setDrawDataPoints(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(-500);
        graph.getViewport().setMaxY(500);

        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(count);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);
        graph.addSeries(series);


        Log.d("graph","start");
        return layout;
    }

    public void addSeries(float data) {
        DataPoint v = new DataPoint(data_num, data);
        for (int i=0; i<count-1; i++) {
            values[i] = values[i+1];
        }
        series.appendData(v,true,count);
        values[count-1] = v;

        data_num ++;
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



}
