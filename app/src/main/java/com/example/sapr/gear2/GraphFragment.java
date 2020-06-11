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
public class GraphFragment extends Fragment {

    private GraphView graph;
    private GraphView graph2;
    private LineGraphSeries<DataPoint> series;
    private LineGraphSeries<DataPoint> series2;
    DataPoint[] values;
    DataPoint[] values2;
    double[] x;
    double[] y;
    double[] x2;
    double[] y2;

    private int data_num = 0;
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private int count = 50;
    private int graph_max1 = 300;
    private float max_y1 = 0;
    private float max_y2 = 0;
    private int graph_max2 = 10;
    public GraphFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View layout = inflater.inflate(R.layout.fragment_graph, container, false);
        setRetainInstance(true);
        data_num = 0;
        x = new double[count];
        y = new double[count];
        x2 = new double[count];
        y2 = new double[count];
        values = new DataPoint[count];
        values2 = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            DataPoint v = new DataPoint(0, 0);
            values[i] = v;
            values2[i] = v;
        }
        graph = (GraphView) layout.findViewById(R.id.graph);
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
        graph.getGridLabelRenderer().setTextSize(30);
        graph.getGridLabelRenderer().setPadding(30);
        graph.addSeries(series);


        graph2 = (GraphView) layout.findViewById(R.id.graph_2);
        series2 = new LineGraphSeries<DataPoint>();

        series2.setDrawDataPoints(true);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinY(-1);
        graph2.getViewport().setMaxY(3);

        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(count);
        //graph2.getViewport().setScrollable(true); // enables horizontal scrolling
        //graph2.getViewport().setScrollableY(true); // enables vertical scrolling
        //graph2.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        //graph2.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        graph2.addSeries(series2);
        graph2.getGridLabelRenderer().setTextSize(30);
        graph2.getGridLabelRenderer().setPadding(30);


        Log.d("graph", "start");

        if (savedInstanceState != null) {
            data_num = savedInstanceState.getInt("data_num");
            x = savedInstanceState.getDoubleArray("x");
            y = savedInstanceState.getDoubleArray("y");
            x2 = savedInstanceState.getDoubleArray("x2");
            y2 = savedInstanceState.getDoubleArray("y2");
            for (int i = 0; i < count; i++) {
                values[i] = new DataPoint(x[i],y[i]);
                values2[i] = new DataPoint(x2[i],y2[i]);

            }
            series.resetData(values);
            series2.resetData(values2);
        }

        return layout;
    }

    public void addSeries(float data,float data2) {
        DataPoint v = new DataPoint(data_num, data);
        DataPoint v2 = new DataPoint(data_num, data2);
        if(data_num ==0){
            Log.d("ds","gh");
        }
        try {
            for (int i = 0; i < count - 1; i++) {
                values[i] = values[i + 1];
                values2[i] = values2[i + 1];

            }
        }catch (NullPointerException e){
            Log.d("values",String.valueOf(values));
        }

        series.appendData(v, true, count);
        series2.appendData(v2, true, count);
        values[count - 1] = v;
        values2[count - 1] = v2;

        data_num++;
    }


    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {

                series.resetData(values);
                series2.resetData(values2);
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
            x2[i] = values2[i].getX();
            y2[i] = values2[i].getY();
        }
        savedInstanceState.putInt("data_num",data_num);
        savedInstanceState.putDoubleArray("x",x);
        savedInstanceState.putDoubleArray("y",y);
        savedInstanceState.putDoubleArray("x2",x2);
        savedInstanceState.putDoubleArray("y2",y2);
    }

}
