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
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity   implements SensorEventListener {

    private  TextView pulse;
    private  TextView damp_text;
    private  TextView temp;

    private static final String LOG_TAG = "MyHeart";
    private Drawable imgStart;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private String pulse_string;
    private String damp_string;

    private Button btnStart;
    private Button btnPause;
    private Button heatStart;
    private Button heatPause;
    private SeekBar damper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pulse_string = getResources().getString(R.string.pulse);
        damp_string = getResources().getString(R.string.damper_text);
        pulse = (TextView)findViewById(R.id.pulse_value);
        temp = (TextView)findViewById(R.id.temperature);
        damp_text = (TextView)findViewById(R.id.damper_text);
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
        DisplayMetrics dm = getResources().getDisplayMetrics();
        //Log.d("Display",Integer.toString(dm.widthPixels));
       // Log.d("Display",Integer.toString(dm.heightPixels));
       // Log.d("Display",Integer.toString(dm.densityDpi));

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPause.setVisibility(Button.VISIBLE);
                btnStart.setVisibility(Button.GONE);
                pulse.setText(R.string.wait);
                startMeasure();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPause.setVisibility(Button.GONE);
                btnStart.setVisibility(Button.VISIBLE);
                pulse.setText(pulse_string+": --");
                stopMeasure();
            }
        });

        heatStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatPause.setVisibility(Button.VISIBLE);
                heatStart.setVisibility(Button.GONE);
                temp.setText(R.string.heat_start);
            }
        });

        heatPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatPause.setVisibility(Button.GONE);
                heatStart.setVisibility(Button.VISIBLE);
                temp.setText(R.string.heat_stop);
            }
        });

        damper.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                damp_text.setText(damp_string+ String.format("( %d /100) ",i));
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

        int mHeartRate = Math.round(mHeartRateFloat);

        pulse.setText(pulse_string+": " + String.format("%d",mHeartRate));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}



