package com.example.sapr.gear2;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
    private LineGraphSeries<DataPoint> series;
    private int data_num;

    public GraphFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout= inflater.inflate(R.layout.fragment_graph, container, false);
        data_num = 0;

        graph = (GraphView) layout.findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 0),
                new DataPoint(1, 2),
                new DataPoint(2, 4),
                new DataPoint(3, 8),
                new DataPoint(4, 7),
                new DataPoint(5, 2),
                new DataPoint(6, -5)
        });
        graph.addSeries(series);

        series.setDrawDataPoints(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(100);

        Log.d("graph","start");
        return layout;
    }

    public void addSeries(float data) {

        Log.d("graph","append");
        series.appendData(new DataPoint(data_num, data),true,100);

        graph.addSeries(series);
        data_num ++;
        Log.d("listener",String.valueOf(data));
    }
}
