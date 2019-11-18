package com.example.sapr.gear2;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView pulseView;
    private TextView dampView;
    private TextView imitatorView;
    private TextView tempView;
    private int test;

    private static final String LOG_TAG = "MyHeart";
    private Drawable imgStart;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private String pulse_string;
    private String damp_string;
    private String imitator_string;

    private int temperature;
    private int pressure;
    private int inner_temp;
    private int param_temp = 0;
    private int param_damp = 0;
    private int status_update;
    private int status_start = 0;
    private int im_temp_max = 70;
    int mHeartRate;
    private int data_num=0;

    private Button btnStart;
    private Button btnPause;
    private Button heatStart;
    private Button heatPause;
    private SeekBar damper;
    private SeekBar damper_temp;
    private UDPHelper udp;
    private byte[] control_imitator;
    private Thread udpConnect;
    private Thread udpConnect2;
    private Thread udpConnect3;

    Snackbar snackbar;

    private GraphView graph;
    private LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pulse_string = getResources().getString(R.string.pulse);
        control_imitator = new byte[]{0, 0, 0};
        damp_string = getResources().getString(R.string.damper_text);
        pulseView = (TextView) findViewById(R.id.pulse_value);
        dampView = (TextView) findViewById(R.id.damper_text);
        tempView = (TextView) findViewById(R.id.temp_text);
        imitatorView = (TextView) findViewById(R.id.imitator);
        Log.d(LOG_TAG, "start app");
        btnStart = (Button) findViewById(R.id.btnStart);
        btnPause = (Button) findViewById(R.id.btnPause);
        heatStart = (Button) findViewById(R.id.heat_start);
        heatPause = (Button) findViewById(R.id.heat_stop);
        damper = (SeekBar) findViewById(R.id.damperBar);
        damper_temp = (SeekBar) findViewById(R.id.tempBar);
        heatStart.setVisibility(Button.GONE);
        heatPause.setVisibility(Button.GONE);
        status_start = 0;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        graph = (GraphView) findViewById(R.id.graph);


        if (savedInstanceState != null) {
            temperature = savedInstanceState.getInt("temperature");
            pressure = savedInstanceState.getInt("pressure");
            inner_temp = savedInstanceState.getInt("inner_temp");
            dampView.setText(damp_string + String.format(" - %d ", savedInstanceState.getInt("damper")) + "%");
            damper.setProgress(savedInstanceState.getInt("damper"));
            heatStart.setVisibility(savedInstanceState.getInt("heatStart"));
            heatPause.setVisibility(savedInstanceState.getInt("heatPause"));
            btnStart.setVisibility(savedInstanceState.getInt("btnStart"));
            btnPause.setVisibility(savedInstanceState.getInt("btnPause"));
            imitator_string = savedInstanceState.getString("imitator_string");
            imitatorView.setText(imitator_string);
            init_component();

        }
        else{

            init_component();
            startData();
            SystemClock.sleep(300);
            sinhro_initator();

        }





    }


    private void init_component(){

        series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 0),
                new DataPoint(1, 2),
                new DataPoint(2, 4),
                new DataPoint(3, 8),
                new DataPoint(4, 7),
                new DataPoint(5, 2),
                new DataPoint(6, -5),
        });
        graph.addSeries(series);

        series.setDrawDataPoints(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(100);





        if (btnStart.getVisibility() != View.GONE) {
            btnPause.setVisibility(Button.GONE);
            btnStart.setVisibility(Button.VISIBLE);

            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnPause.setVisibility(Button.VISIBLE);
                    btnStart.setVisibility(Button.GONE);
                    pulseView.setText(R.string.wait);
                    startMeasure();
                    startSend();

                    startSendPulse();
                }
            });

            btnPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnPause.setVisibility(Button.GONE);
                    btnStart.setVisibility(Button.VISIBLE);
                    pulseView.setText(pulse_string + ": --");
                    stopMeasure();
                    startSend();
                }
            });

        }
        heatStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatPause.setVisibility(Button.VISIBLE);
                heatStart.setVisibility(Button.GONE);
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.heat_start,
                        Toast.LENGTH_SHORT);
                toast.show();
                control_imitator[0] = 1;
                startSend();


            }
        });

        heatPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatPause.setVisibility(Button.GONE);
                heatStart.setVisibility(Button.VISIBLE);
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.heat_stop,
                        Toast.LENGTH_SHORT);
                toast.show();
                control_imitator[0] = 0;
                startSend();
            }
        });

        damper.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (pulseView.getVisibility() == View.GONE) {
                    dampView.setText(damp_string + String.format(" - %d ", i) + "%");
                }
                control_imitator[1] = (byte) (i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                startSend();
            }
        });
        damper_temp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int value = 30 + (int) ((float) i * 40 / 100);
                tempView.setText(getResources().getString(R.string.temp_text) + String.format("  %d ", value) + "C");
                control_imitator[2] = (byte) (value);

            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startSend();
            }
        });


    }

    private void sinhro_initator(){

        SystemClock.sleep(100);
        int n=0;
        while (n<5) {
            n +=1;
            SystemClock.sleep(50);
            control_imitator[0] = 2;
            startSend();
            if (status_update > 0) {
                status_start = 1;
                control_imitator[0] = 0;
                return;
            }
        }


        if(status_start==0){
            CharSequence text =  getResources().getString(R.string.synchro);
            int duration = Snackbar.LENGTH_INDEFINITE;
            snackbar = Snackbar.make(findViewById(R.id.layout_main), text, duration);
            snackbar.setAction(getResources().getString(R.string.synchro_repeat), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    control_imitator[0] = 2;
                    startSend();

                    control_imitator[0] = 0;

                    SystemClock.sleep(200);
                    if (status_update !=1) {

                        sinhro_initator();
                    }
                }
            });
            snackbar.show();

        }



    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);


        savedInstanceState.putInt("temperature", temperature);
        savedInstanceState.putInt("pressure", pressure);
        savedInstanceState.putInt("inner_temp", inner_temp);
        savedInstanceState.putInt("damper", damper.getProgress());
        savedInstanceState.putInt("heatStart", heatStart.getVisibility());
        savedInstanceState.putInt("heatPause", heatPause.getVisibility());
        savedInstanceState.putInt("btnStart", btnStart.getVisibility());
        savedInstanceState.putInt("btnPause", btnPause.getVisibility());
        savedInstanceState.putString("imitator_string", imitator_string);

    }

    private void startData() {
        udpConnect = new Thread(new Runnable() {

            boolean running;

            @Override
            public void run() {
                try {
                    udp = new UDPHelper(getApplicationContext(), new UDPHelper.BroadcastListener() {
                        @Override
                        public void onReceive(int status, float temp_value, float pressure_value, float inner_temp_value, int im_temp, int im_damper, int im_max_temp) {
                            if (status == 1) {
                                status_update = 1;

                            }
                            if (status == 0) {
                                temperature = Math.round(temp_value);
                                pressure = Math.round(pressure_value);
                                inner_temp = Math.round(inner_temp_value);
                                imitator_string = String.format(getResources().getString(R.string.imitator), temperature, pressure, inner_temp);
                                series.appendData(new DataPoint(data_num, pressure),true,100);
                                data_num +=1;

                            } else {
                                param_temp = im_temp;
                                param_damp = im_damper;
                                im_temp_max = im_max_temp;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (status_update == 0) {
                                        imitatorView.setText(imitator_string);
                                    } else {
                                        status_update = 0;
                                        dampView.setText(damp_string + String.format(" - %d ", param_damp) + "%");
                                        damper.setProgress(param_damp);
                                        damper_temp.setProgress((int) ((im_temp_max - 30) * 2.5));

                                        control_imitator[0] = (byte) param_temp;
                                        control_imitator[1] = (byte) param_damp;
                                        control_imitator[2] = (byte) im_temp_max;

                                        if (param_temp == 1) {
                                            heatStart.setVisibility(Button.GONE);
                                            heatPause.setVisibility(Button.VISIBLE);
                                        } else {
                                            heatStart.setVisibility(Button.VISIBLE);
                                            heatPause.setVisibility(Button.GONE);
                                        }
                                    }

                                }
                            });


                        }
                    });
                    udp.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void end() {
                udp.end();
            }
        });

        udpConnect.start();

    }

    private void startSend() {
        udpConnect2 = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    udp.send(control_imitator);
                    Log.d("UDP_out", String.valueOf(control_imitator[0]) + "    " + String.valueOf(control_imitator[1]) + "    " + String.valueOf(control_imitator[2]));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void end() {
                udp.end();
            }
        });

        udpConnect2.start();


    }

    private void startSendPulse() {
        udpConnect3 = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {

                        byte[] pulse_byte = new byte[]{(byte) mHeartRate};
                        udp.send_pulse(pulse_byte);
                        Log.v("udp_pulse_out", String.valueOf(pulse_byte[0]));
                        sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            public void end() {
                // running = false;
                udp.end();
            }
        });

        udpConnect3.start();


    }

    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        Log.d("onResume", "-");

        super.onResume();
        startData();
    }

    @Override
    protected void onPause() {
        Log.d("onPause", "-");
        super.onPause();

        stopMeasure();
    }

    @Override
    protected void onStop() {
        Log.d("onStop", "-");
        super.onStop();

        stopMeasure();
    }

    @Override
    protected void onDestroy() {

        Log.d("onDestroy", "-");
        udpConnect.interrupt();
        udp.end();
        Log.d("onDestroy", String.valueOf(udp.status()));
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float mHeartRateFloat = sensorEvent.values[0];

        mHeartRate = Math.round(mHeartRateFloat);

        pulseView.setText(pulse_string + ": " + String.format("%d", mHeartRate));


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }



}



