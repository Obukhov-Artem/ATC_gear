package com.example.sapr.gear2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "imitator"; // the name of our database
    private static final int DB_VERSION = 3; // the version of the database

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    public void insertData(SQLiteDatabase db, String name, String description,
                                   float temp_value, float pressure_value, float inner_temp_value,
                                   float pulse, float param_temp,float param_damp, float im_temp_max, float spirogram){
        ContentValues Values = new ContentValues();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        Values.put("NAME", name);
        Values.put("DESCRIPTION", description);
        Values.put("TIME_VALUE", dateFormat.format(date));
        Values.put("TEMPERATURE", temp_value);
        Values.put("PRESSURE", pressure_value);
        Values.put("SPIROGRAM", spirogram);
        Values.put("TEMPERATURE_INNER", inner_temp_value);
        Values.put("PULSE", pulse);
        Values.put("PARAM_TEMP", param_temp);
        Values.put("PARAM_DAMPER", param_damp);
        Values.put("PARAM_TEMP_LIMIT", im_temp_max);
        db.insert("TRAINING", null, Values);
        Log.d("db_ok", String.valueOf(name)+String.valueOf(description)
                +String.valueOf(dateFormat.format(date))+String.valueOf(temp_value)
                +String.valueOf(pressure_value)+String.valueOf(spirogram)+String.valueOf(inner_temp_value)
                +String.valueOf(pulse)+String.valueOf(param_temp)
                +String.valueOf(param_damp)+String.valueOf(im_temp_max) );


    }

    private void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE TRAINING (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "NAME TEXT, "
                    + "DESCRIPTION TEXT, "
                    + "TIME_VALUE DATETIME,"
                    + "TEMPERATURE REAL,"
                    + "PRESSURE REAL,"
                    + "TEMPERATURE_INNER REAL,"
                    + "PULSE REAL,"
                    + "PARAM_TEMP REAL,"
                    + "PARAM_DAMPER REAL,"
                    + "PARAM_TEMP_LIMIT REAL);");
             }
        if (oldVersion >= 1) {
            db.execSQL("ALTER TABLE TRAINING ADD COLUMN SPIROGRAM REAL DEFAULT 0");

             }

    }
}
