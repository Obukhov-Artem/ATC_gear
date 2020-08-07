package com.example.sapr.gear2;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class GraphFragment extends Fragment {

    private GraphView graph;
    private GraphView graph2;
    private GraphView graph3;
    private LineGraphSeries<DataPoint> series;
    private LineGraphSeries<DataPoint> series2;
    private LineGraphSeries<DataPoint> series3;
    DataPoint[] values;
    DataPoint[] values2;
    DataPoint[] values3;
    double[] tt;
    double[] x;
    double[] y;
    double[] x2;
    double[] y2;
    double vsd=0;
    int vsd_current=0;

    private TextView vsd_text;
    private TextView vsd_text_pr;
    private Button vsd_reset;
    Calendar c;
    long start_time=0;
    long end_time=0;
    long period_start=0;
    long period_time=0;
    long time1=0;
    long time2=0;

    private int data_num = 0;
    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private int count = 500;
    private int graph_max1 = 300;
    double sum_TPA = 0;
    public GraphFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View layout = inflater.inflate(R.layout.fragment_graph, container, false);
        setRetainInstance(true);
        c = Calendar.getInstance();

        start_time = c.getTimeInMillis();
        period_start = c.getTimeInMillis();
        end_time = c.getTimeInMillis();
        time1 = c.getTimeInMillis();
        time2 = c.getTimeInMillis();
        vsd_text = (TextView) layout.findViewById(R.id.graph_VZD2);
        vsd_text_pr = (TextView) layout.findViewById(R.id.graph_VZD);
        vsd_reset = (Button) layout.findViewById(R.id.VSD_reset);
        vsd_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vsd = 0;

            }
        });
        data_num = 0;
        x = new double[count];
        x = new double[count];
        tt = new double[count];
        x2 = new double[count];
        y2 = new double[count];
        values = new DataPoint[count];
        values2 = new DataPoint[count];
        values3 = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            DataPoint v = new DataPoint(0, 0);
            values[i] = v;
            values2[i] = v;
            values3[i] = v;
        }
        graph = (GraphView) layout.findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        sum_TPA = 0;

        series.setDrawDataPoints(true);
        //graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinY(-graph_max1);
        graph.getViewport().setMaxY(graph_max1);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(30);
        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        //graph.getViewport().setScalable(true);
        //graph.getViewport().setScalableY(true);
        graph.getGridLabelRenderer().setTextSize(20);
        graph.getGridLabelRenderer().setPadding(40);
        graph.addSeries(series);


        graph2 = (GraphView) layout.findViewById(R.id.graph_2);
        series2 = new LineGraphSeries<DataPoint>();

        series2.setDrawDataPoints(true);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinY(-0.5);
        graph2.getViewport().setMaxY(3);

        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(30);
        //graph2.getViewport().setScrollable(true); // enables horizontal scrolling
        //graph2.getViewport().setScrollableY(true); // enables vertical scrolling
        //graph2.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        //graph2.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        graph2.addSeries(series2);
        graph2.getGridLabelRenderer().setTextSize(30);
        graph2.getGridLabelRenderer().setPadding(40);


        graph3 = (GraphView) layout.findViewById(R.id.graph_3);
        series3 = new LineGraphSeries<DataPoint>();

        series3.setDrawDataPoints(true);
        graph3.getViewport().setYAxisBoundsManual(true);
        graph3.getViewport().setXAxisBoundsManual(true);
        graph3.getViewport().setMinY(-30);
        graph3.getViewport().setMaxY(200);

        graph3.getViewport().setMinX(0);
        graph3.getViewport().setMaxX(30);
        graph3.getViewport().setScrollable(true); // enables horizontal scrolling
        graph3.getViewport().setScrollableY(true); // enables vertical scrolling
        //graph3.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph3.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        graph3.addSeries(series3);
        graph3.getGridLabelRenderer().setTextSize(30);
        graph3.getGridLabelRenderer().setPadding(40);


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

    public int[] addSeries(float data,float data2, float data3, float data4) {

        c = Calendar.getInstance();
        end_time = c.getTimeInMillis();
        time1 = c.getTimeInMillis();
        float cur_time = (float)((end_time-start_time));
        float delta_time = (float)(time1-time2)/1000;
        period_time = ((end_time-period_start))/1000;
        DataPoint v = new DataPoint(cur_time/1000, data);
        DataPoint v2 = new DataPoint(cur_time/1000, data2);
        DataPoint v3 = new DataPoint(cur_time/1000, data4);
        if (data3>0)
            vsd += data3;
        double TPA;
        TPA = (900-vsd)/9;
        if(TPA<0) TPA = 0;
        vsd_text.setText("Остаток: "+String.valueOf((int)TPA)+"%");
        if(data_num ==0){
            Log.d("ds","gh");
        }
        //float sum_t = 0;
        try {
            for (int i = 0; i < count - 1; i++) {
                //sum_TPA+=Math.abs(values2[i].getY())*delta_time;
                values[i] = values[i + 1];
                values2[i] = values2[i + 1];
                values3[i] = values3[i + 1];

            }
            //sum_TPA+=Math.abs(values2[count - 1].getY());

            Log.d("time",String.valueOf(delta_time));
            Log.d("sumTPA",String.valueOf(sum_TPA));
            if (period_time>60){
                sum_TPA = 9*TPA/(sum_TPA);
                vsd_text_pr.setText("Прогноз ВЗД: "+String.valueOf((int)sum_TPA)+" мин.");
                vsd_current = (int) sum_TPA;
                period_time = 0;
                period_start = c.getTimeInMillis();
                sum_TPA = 0;
            }
            }catch (NullPointerException e){
            Log.d("values",String.valueOf(values));
        }

        series.appendData(v, true, count);
        series2.appendData(v2, true, count);
        series3.appendData(v3, true, count);
        values[count - 1] = v;
        values2[count - 1] = v2;

        sum_TPA+=Math.abs(values2[count - 1].getY())*delta_time;
        values3[count - 1] = v3;
        time2 = time1;
        //tt[count - 1] = data4;

        data_num++;
        int res1 = 100-(int)TPA;
        return new int[] {res1, vsd_current};
        //return 100-(int)TPA;
    }


    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {

                series.resetData(values);
                series2.resetData(values2);
                series3.resetData(values3);
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mTimer1, 3000);

    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        try {
            x = new double[count];
            x = new double[count];
            for (int i = 0; i < count; i++) {
                x[i] = values[i].getX();
                y[i] = values[i].getY();
                x2[i] = values2[i].getX();
                y2[i] = values2[i].getY();
            }
            savedInstanceState.putInt("data_num", data_num);
            savedInstanceState.putDoubleArray("x", x);
            savedInstanceState.putDoubleArray("y", y);
            savedInstanceState.putDoubleArray("x2", x2);
            savedInstanceState.putDoubleArray("y2", y2);
        }
        catch (Exception e){
            Log.e("error","values");
        }
    }

}
