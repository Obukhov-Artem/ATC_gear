package com.example.sapr.gear2;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;


/**
 * A simple {@link Fragment} subclass.
 */
public class ControlFragment extends Fragment implements SensorEventListener {
    private TextView pulseView;
    private TextView dampView;
    private TextView imitatorView;
    private TextView tempView;
    private EditText username;
    private EditText decription;
    private CheckBox flagDB;
    private CheckBox flagAUTO;

    private static final String LOG_TAG = "MyHeart";
    private Drawable imgStart;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;
    private String pulse_string;
    private String damp_string;
    private String imitator_string;

    private int temperature;
    private int pressure;
    private float spirogram=0;
    private float[] vsd_m;
    private int inner_temp;
    private int param_temp = 0;
    private int param_damp = 0;
    private int status_update = 0;
    private int status_start = 0;
    private int status_udp = 1;
    private int im_temp_max = 70;
    int mHeartRate=0;
    private int vsd = 0;
    private float tvolume;
    private int status_null = 0;
    private int d_current = 0;
    private int d_last = 0;
    private boolean auto_dumper= false;

    private Button btnStart;
    private Button btnPause;
    private Button heatStart;
    private Button heatPause;
    private SeekBar damper;
    private SeekBar damper_temp;
    private UDPHelper udp;
    private UDPHelper2 udp2;
    private byte[] control_imitator;
    private Thread udpConnect;
    private Thread udpConnect2;
    private Thread udpConnect3;
    private Thread udpConnect4;
    private View layout;
    private GraphListener g_listener;
    SQLiteDatabase db;
    DatabaseHelper dh;
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    new UpdateBaseTask().execute();
                    /*
                    if (flagDB.isChecked()) {
                        dh.insertData(db, String.valueOf(username.getText()), "testing",
                                temperature, pressure, inner_temp,
                                0, param_temp, param_damp, im_temp_max);
                    }*/
                }
            });
        }
    }


    public ControlFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setRetainInstance(true);
        layout = inflater.inflate(R.layout.fragment_control, container, false);
        pulse_string = getResources().getString(R.string.pulse);
        control_imitator = new byte[]{0, 0, 0};
        damp_string = getResources().getString(R.string.damper_text);
        pulseView = (TextView) layout.findViewById(R.id.pulse_value);
        dampView = (TextView) layout.findViewById(R.id.damper_text);
        tempView = (TextView) layout.findViewById(R.id.temp_text);
        username = (EditText) layout.findViewById(R.id.userName);
        decription = (EditText) layout.findViewById(R.id.decription);
        flagDB = (CheckBox) layout.findViewById(R.id.flagDB);
        flagAUTO = (CheckBox) layout.findViewById(R.id.flagAUTO);
        imitatorView = (TextView) layout.findViewById(R.id.imitator);
        Log.d(LOG_TAG, "start app");
        btnStart = (Button) layout.findViewById(R.id.btnStart);
        btnPause = (Button) layout.findViewById(R.id.btnPause);
        heatStart = (Button) layout.findViewById(R.id.heat_start);
        heatPause = (Button) layout.findViewById(R.id.heat_stop);

        damper = (SeekBar) layout.findViewById(R.id.damperBar);
        damper_temp = (SeekBar) layout.findViewById(R.id.tempBar);
        heatStart.setVisibility(Button.GONE);
        heatStart.setVisibility(Button.GONE);
        heatPause.setVisibility(Button.GONE);
        status_start = 0;
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);


        if (savedInstanceState != null) {
            temperature = savedInstanceState.getInt("temperature");
            pressure = savedInstanceState.getInt("pressure");
            spirogram = savedInstanceState.getFloat("spirogram");
            inner_temp = savedInstanceState.getInt("inner_temp");
            dampView.setText(damp_string + String.format(" - %d ", savedInstanceState.getInt("damper")) + "%");
            damper.setProgress(savedInstanceState.getInt("damper"));
            heatStart.setVisibility(savedInstanceState.getInt("heatStart"));
            heatPause.setVisibility(savedInstanceState.getInt("heatPause"));
            btnStart.setVisibility(savedInstanceState.getInt("btnStart"));
            btnPause.setVisibility(savedInstanceState.getInt("btnPause"));
            imitator_string = savedInstanceState.getString("imitator_string");
            imitatorView.setText(imitator_string);
            Log.d("bytes", String.valueOf(savedInstanceState.getByte("c1")));
            control_imitator[0] = savedInstanceState.getByte("c1");
            control_imitator[1] = savedInstanceState.getByte("c2");
            control_imitator[2] = savedInstanceState.getByte("c3");
            init_component();

        } else {

            init_component();
            startData();
            SystemClock.sleep(300);
            sinhro_initator();

        }

        return layout;
    }


    private void init_component() {
        if (flagDB.isChecked()) {
            mTimer = new Timer();
            mMyTimerTask = new MyTimerTask();
            mTimer.schedule(mMyTimerTask, 50, 50);
        }
        if (flagAUTO.isChecked()) {
            auto_dumper = true;
        } else {
            auto_dumper = false;
        }
        flagDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.flagDB) {
                    if (flagDB.isChecked()) {
                        mTimer = new Timer();
                        mMyTimerTask = new MyTimerTask();
                        mTimer.schedule(mMyTimerTask, 50, 50);
                    } else {
                        if (mTimer != null) {
                            mTimer.cancel();
                        }
                    }
                }
            }
        });
        flagAUTO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.flagAUTO) {
                    if (flagAUTO.isChecked()) {
                        auto_dumper = true;
                    } else {
                        auto_dumper = false;
                    }
                }
            }
        });
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
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
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
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
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

                Log.d("damper", String.valueOf(control_imitator[1]));
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

    private void sinhro_initator() {

        SystemClock.sleep(100);
        int n = 0;
        while (n < 5 && status_start == 0) {
            n += 1;
            control_imitator[0] = 2;
            startSend();
            if (status_update > 0) {
                status_start = 1;
                control_imitator[0] = 0;
                return;
            }
            SystemClock.sleep(200);
        }


        if (status_start == 0) {
            CharSequence text = getResources().getString(R.string.synchro);
            int duration = Snackbar.LENGTH_INDEFINITE;
            Log.d("ERROR", "Not connection");
            View id_l = layout.findViewById(R.id.control_layout_main);
            Log.d("View id_l", String.valueOf(id_l));
            Snackbar snackbar = Snackbar.make(layout, text, duration);
            snackbar.setAction(getResources().getString(R.string.synchro_repeat), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    control_imitator[0] = 2;
                    startSend();

                    control_imitator[0] = 0;

                    SystemClock.sleep(200);
                    if (status_update != 1) {

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


        savedInstanceState.putByte("c1", control_imitator[0]);
        savedInstanceState.putByte("c2", control_imitator[1]);
        savedInstanceState.putByte("c3", control_imitator[2]);
        savedInstanceState.putInt("temperature", temperature);
        savedInstanceState.putInt("pressure", pressure);
        savedInstanceState.putFloat("spirogram", spirogram);
        savedInstanceState.putInt("inner_temp", inner_temp);
        savedInstanceState.putInt("damper", damper.getProgress());
        savedInstanceState.putInt("heatStart", heatStart.getVisibility());
        savedInstanceState.putInt("heatPause", heatPause.getVisibility());
        savedInstanceState.putInt("btnStart", btnStart.getVisibility());
        savedInstanceState.putInt("btnPause", btnPause.getVisibility());
        savedInstanceState.putString("imitator_string", imitator_string);

    }

    public void setListener(GraphListener listener) {
        this.g_listener = listener;
    }

    private void startData() {
        udpConnect = new Thread(new Runnable() {

            boolean running;

            @Override
            public void run() {
                try {
                    udp = new UDPHelper(getActivity().getApplicationContext(), new UDPHelper.BroadcastListener() {


                        @Override
                        public void onReceive(final int status, float temp_value, float pressure_value, float inner_temp_value, int im_temp, int im_damper, int im_max_temp, int volume) {
                            if (status == 1) {
                                status_update = 1;
                                status_udp = 0;

                            }
                            if (status == 0) {
                                temperature = Math.round(temp_value);
                                pressure = Math.round(pressure_value);
                                inner_temp = Math.round(inner_temp_value);
                                tvolume = Math.round(volume);
                                imitator_string = String.format(getResources().getString(R.string.imitator), temperature, pressure, inner_temp);
                                //series.appendData(new DataPoint(data_num, pressure),true,100);


                            } else {
                                param_temp = im_temp;
                                param_damp = im_damper;
                                im_temp_max = im_max_temp;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (status_update == 0) {
                                        float k_spiro = 1;
                                        imitatorView.setText(imitator_string);
                                        if (g_listener != null) {
                                            if (status_null == 0) {
                                                status_null = 1;
                                                d_current = pressure;
                                                d_last = pressure;
                                            }
                                            if (status_null == 1) {
                                                d_last = d_current;
                                                d_current = pressure;
                                                int delta = d_current - d_last;
                                                Log.d("DELTA D", String.valueOf(d_current) + " " + String.valueOf(d_last));
                                                if (delta > 0 && d_last < 0 && d_current >= 0) {
                                                    spirogram = 0;


                                                }
                                                if (delta < 0 && d_current < 0) {
                                                    k_spiro = 0.5f;
                                                }
                                            }
                                            //float spirogram = 0.0181f*pressure*pressure+0.0287f*pressure-0.377f;
                                            float dp = (float) (pressure * 0.01);
                                            //spirogram = (int)(dp*dp*dp*0.1512f-3.3424f*dp*dp+41.657*dp);
                                            int pnevmo = (int) (dp * dp * dp * 0.1512f - 3.3424f * dp * dp + 41.657 * dp);
                                            if (tvolume > 100) tvolume = 100;
                                            float d_spiro = (float) (k_spiro * pnevmo * tvolume / (1000 * 60));
                                            if (spirogram > -0.02)
                                                spirogram += d_spiro;
                                            Log.d("VOLUME", String.valueOf(tvolume) + "   " + String.valueOf(spirogram));
                                            int[] new_values = g_listener.addSeries(pnevmo, spirogram, d_spiro, mHeartRate);
                                            int new_dump = new_values[0];
                                            vsd = new_values[1];
                                            if ((new_dump != param_damp) && (auto_dumper == true)) {
                                                param_damp = new_dump;
                                                control_imitator[0] = (byte) param_temp;
                                                control_imitator[1] = (byte) param_damp;
                                                control_imitator[2] = (byte) im_temp_max;
                                                dampView.setText(damp_string + String.format(" - %d ", param_damp) + "%");
                                                damper.setProgress(param_damp);
                                                damper_temp.setProgress((int) ((im_temp_max - 30) * 2.5));
                                                status_update = 0;
                                                status_udp = 0;
                                                startSend();
                                            }
                                        }
                                    } else {
                                        control_imitator[0] = (byte) param_temp;
                                        control_imitator[1] = (byte) param_damp;
                                        control_imitator[2] = (byte) im_temp_max;
                                        dampView.setText(damp_string + String.format(" - %d ", param_damp) + "%");
                                        damper.setProgress(param_damp);
                                        damper_temp.setProgress((int) ((im_temp_max - 30) * 2.5));


                                        if (param_temp == 1) {
                                            heatStart.setVisibility(Button.GONE);
                                            heatPause.setVisibility(Button.VISIBLE);
                                        } else {
                                            heatStart.setVisibility(Button.VISIBLE);
                                            heatPause.setVisibility(Button.GONE);
                                        }

                                        status_update = 0;
                                        status_udp = 0;
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

        udpConnect4 = new Thread(new Runnable() {

            boolean running;

            @Override
            public void run() {
                try {
                    udp2 = new UDPHelper2(getActivity().getApplicationContext(), new UDPHelper2.BroadcastListener() {
                        @Override
                        public void onReceive(final int pulse) {
                            if (g_listener != null) {
//                                int t = g_listener.addSeries2(pulse, 0, 0, 0);
                                mHeartRate =pulse;
                                pulseView.setText(pulse_string + ": " + String.format("%d", pulse));
                            }

                        }
                    });
                    udp2.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void end() {
                udp2.end();
            }
        });

        udpConnect4.start();



    }

    private void startSend() {
        udpConnect2 = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    Log.d("status_update", String.valueOf(status_update));
                    if (status_update == 1) {

                        SystemClock.sleep(800);
                        return;
                    } else {
                        udp.send(control_imitator);
                    }
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
    public void onResume() {
        Log.d("onResume", "-");

        super.onResume();
        if (udp == null)
            startData();
    }

    @Override
    public void onPause() {
        Log.d("onPause", "-");
        super.onPause();

        stopMeasure();
    }

    @Override
    public void onStop() {
        Log.d("onStop", "-");
        super.onStop();

        stopMeasure();
    }

    @Override
    public void onAttach(Context a) {
        super.onAttach(a);
        Log.d("attach", "23432424");
        g_listener = (GraphListener) a;
    }

    @Override
    public void onDestroy() {

        Log.d("onDestroy", "-");
        udpConnect.interrupt();
        if (udp != null) {
            udp.end();
            Log.d("onDestroy", String.valueOf(udp.status()));
        }
        udpConnect4.interrupt();
        if (udp2 != null) {
            udp2.end();
            Log.d("onDestroy", String.valueOf(udp2.status()));
        }
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


    private class UpdateBaseTask extends AsyncTask<Void, Void, Boolean> {


        protected void onPreExecute() {
            CheckBox favorite = (CheckBox) layout.findViewById(R.id.flagDB);

        }

        protected Boolean doInBackground(Void... voids) {

            dh = new DatabaseHelper(getActivity());
            try {
                if (flagDB.isChecked()) {
                    db = dh.getWritableDatabase();
                    dh.insertData(db, String.valueOf(username.getText()), String.valueOf(decription.getText()),
                            temperature, (float)pressure, (float)vsd,
                            mHeartRate,inner_temp, param_temp,param_damp, im_temp_max, spirogram);
                    db.close();
                    dh.close();
                }
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast toast = Toast.makeText(getActivity(),
                        "Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
