package com.example.sapr.gear1;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity   implements SensorEventListener {

    private  TextView pulseView;
    private  TextView dampView;
    private  TextView tempView;
    private  TextView imitatorView;

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
    int mHeartRate;

    private Button btnStart;
    private Button btnPause;
    private Button heatStart;
    private Button heatPause;
    private SeekBar damper;
    private UDPHelper udp;
    private byte[] control_imitator;
    private Thread udpConnect3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pulse_string = getResources().getString(R.string.pulse);
        control_imitator = new byte[] {0,0};
        damp_string = getResources().getString(R.string.damper_text);
        pulseView = (TextView)findViewById(R.id.pulse_value);
        tempView = (TextView)findViewById(R.id.temperature);
        dampView = (TextView)findViewById(R.id.damper_text);
        imitatorView = (TextView)findViewById(R.id.imitator);
        Log.d(LOG_TAG, "start app");
        btnStart = (Button) findViewById(R.id.btnStart);
        btnPause = (Button) findViewById(R.id.btnPause);
        heatStart = (Button) findViewById(R.id.heat_start);
        heatPause = (Button) findViewById(R.id.heat_stop);
        damper = (SeekBar) findViewById(R.id.damperBar);
        btnPause.setVisibility(Button.GONE);
        btnStart.setVisibility(Button.VISIBLE);
        heatStart.setVisibility(Button.VISIBLE);
        heatPause.setVisibility(Button.GONE);
        //Log.d("Display",Integer.toString(dm.widthPixels));
        // Log.d("Display",Integer.toString(dm.heightPixels));
        // Log.d("Display",Integer.toString(dm.densityDpi));

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
                pulseView.setText(pulse_string+": --");
                stopMeasure();
                startSend();
            }
        });

        heatStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatPause.setVisibility(Button.VISIBLE);
                heatStart.setVisibility(Button.GONE);
                Toast toast = Toast.makeText(getApplicationContext(),
                        R.string.heat_start,
                        Toast.LENGTH_SHORT);
                toast.show();
                control_imitator[0]=1;
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
                control_imitator[0]=0;
                startSend();
            }
        });

        damper.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                dampView.setText(damp_string+ String.format("( %d /100) ",i));

                control_imitator[1]=(byte)i;
                startSend();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Thread udpConnect = new Thread(new Runnable() {

            boolean running;
            @Override
            public void run() {
                try {
                    udp = new UDPHelper(getApplicationContext(), new UDPHelper.BroadcastListener() {
                        @Override
                        public void onReceive(float temp_value, float pressure_value, float inner_temp_value) {
                            temperature = Math.round(temp_value);
                            pressure =  Math.round(pressure_value);
                            inner_temp =  Math.round(inner_temp_value);
                            imitator_string = String.format(getResources().getString(R.string.imitator), temperature, pressure, inner_temp);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    imitatorView.setText(imitator_string);
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

    private void startSend()
    {
        Thread udpConnect2 = new Thread(new Runnable() {

            @Override
            public void run()
            {

                try {
                    udp.send(control_imitator);
                    Log.v("UDP_out", String.valueOf(control_imitator[0])+"    "+String.valueOf(control_imitator[1]));
                    sleep(100);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            public void end()
            {
                // running = false;
                udp.end();
            }
        });

        udpConnect2.start();




    }
    private void startSendPulse()
    {
          udpConnect3 = new Thread(new Runnable() {

            @Override
            public void run()
            {
                while (true) {
                    try {

                        byte[] pulse_byte = new byte[]{(byte) mHeartRate};
                        udp.send_pulse(pulse_byte);
                        Log.v("pulse_out", String.valueOf(pulse_byte[0]));
                        sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            public void end()
            {
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
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMeasure();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopMeasure();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float mHeartRateFloat = sensorEvent.values[0];

        mHeartRate = Math.round(mHeartRateFloat);

        pulseView.setText(pulse_string+": " + String.format("%d",mHeartRate));


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}



