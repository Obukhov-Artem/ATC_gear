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
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity   implements SensorEventListener {

    private  TextView pulseView;
    private  TextView dampView;
    private  TextView imitatorView;
    private  TextView tempView;

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
    private int param_temp=0;
    private int param_damp=0;
    private int status_update=0;
    private int status_start=0;
    private int im_temp_max=70;
    int mHeartRate;

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

    private Timer mTimer;
    private MyTimerTask mTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pulse_string = getResources().getString(R.string.pulse);
        control_imitator = new byte[] {0,0,0};
        damp_string = getResources().getString(R.string.damper_text);
        pulseView = (TextView)findViewById(R.id.pulse_value);
        dampView = (TextView)findViewById(R.id.damper_text);
        tempView = (TextView)findViewById(R.id.temp_text);
        imitatorView = (TextView)findViewById(R.id.imitator);
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
        if (status_start ==0){
            status_start = 1;
            control_imitator[0]=2;
            startSend();
        }

        if (savedInstanceState != null) {
            temperature = savedInstanceState.getInt("temperature");
            pressure =  savedInstanceState.getInt("pressure");
            inner_temp =  savedInstanceState.getInt("inner_temp");
            dampView.setText(damp_string + String.format(" - %d ", savedInstanceState.getInt("damper")) + "%");
            damper.setProgress(savedInstanceState.getInt("damper"));
            heatStart.setVisibility(savedInstanceState.getInt("heatStart"));
            heatPause.setVisibility(savedInstanceState.getInt("heatPause"));
            btnStart.setVisibility(savedInstanceState.getInt("btnStart"));
            btnPause.setVisibility(savedInstanceState.getInt("btnPause"));
            imitator_string = savedInstanceState.getString("imitator_string");
            imitatorView.setText(imitator_string);



        }



        if (btnStart.getVisibility() !=View.GONE){
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
                mTimer.cancel();
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
                reCheckTimer();
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
                control_imitator[0]=1;
                startSend();
                reCheckTimer();


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
                mTimer.cancel();
            }
        });

        damper.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (pulseView.getVisibility()== View.GONE) {
                    dampView.setText(damp_string + String.format(" - %d ", i)+"%");
                }
                control_imitator[1]=(byte)(i);
                startSend();
                reCheckTimer();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        damper_temp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int value = (int)((float)(i*im_temp_max/100));
                tempView.setText(getResources().getString(R.string.temp_text) + String.format("  %d ", value)+"C");
                control_imitator[2]=(byte)(value);
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

    startData();

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
    private  void  startData(){
        udpConnect = new Thread(new Runnable() {

            boolean running;
            @Override
            public void run() {
                try {
                    udp = new UDPHelper(getApplicationContext(), new UDPHelper.BroadcastListener() {
                        @Override
                        public void onReceive(int status, float temp_value, float pressure_value, float inner_temp_value, int im_damper, int im_temp) {
                            status_update = status;
                            if (status_update == 0){
                                temperature = Math.round(temp_value);
                                pressure =  Math.round(pressure_value);
                                inner_temp =  Math.round(inner_temp_value);
                                imitator_string = String.format(getResources().getString(R.string.imitator), temperature, pressure, inner_temp);

                            }
                            else{
                                param_temp = im_temp;
                                param_damp = im_damper;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(status_update == 0) {
                                        imitatorView.setText(imitator_string);
                                    }
                                    else{
                                        dampView.setText(damp_string + String.format(" - %d ", param_damp) + "%");
                                        damper.setProgress(param_damp);

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

    private void startSend()
    {
        udpConnect2 = new Thread(new Runnable() {

            @Override
            public void run()
            {

                try {
                    udp.send(control_imitator);
                    Log.v("UDP_out", String.valueOf(control_imitator[0])+"    "+String.valueOf(control_imitator[1]));

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            public void end()
            {
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
                        Log.v("udp_pulse_out", String.valueOf(pulse_byte[0]));
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
        Log.d("onResume","-");

        super.onResume();
        startData();
    }

    @Override
    protected void onPause() {
        Log.d("onPause","-");
        super.onPause();

        stopMeasure();
    }

    @Override
    protected void onStop() {
        Log.d("onStop","-");
        super.onStop();

        stopMeasure();
    }
    @Override
    protected void onDestroy(){

        Log.d("onDestroy","-");
        udpConnect.interrupt();
        udp.end();
        Log.d("onDestroy",String.valueOf(udp.status()));
        super.onDestroy();
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

    private void reCheckTimer(){
        return;
        /*
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimerTask = new MyTimerTask();
        mTimer.schedule(mTimerTask, 10000, 10000);*/
    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Log.d("timer","done");
                    dampView.setText(damp_string + String.format(" - %d ", param_damp) + "%");
                    damper.setProgress(param_damp);

                    if (param_temp == 1) {
                        heatStart.setVisibility(Button.GONE);
                        heatPause.setVisibility(Button.VISIBLE);
                    } else {
                        heatStart.setVisibility(Button.VISIBLE);
                        heatPause.setVisibility(Button.GONE);
                    }
                }
            });
        }
    }

}



