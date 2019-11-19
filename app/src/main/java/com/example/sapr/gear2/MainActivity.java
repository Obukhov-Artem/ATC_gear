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
import android.widget.Adapter;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Calendar;

import static java.lang.Thread.sleep;


interface GraphListener {
    void addSeries(float data);
};


public class MainActivity extends AppCompatActivity implements GraphListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void addSeries(float data) {

        Log.d("DATA ", String.valueOf(data));
    }
}



