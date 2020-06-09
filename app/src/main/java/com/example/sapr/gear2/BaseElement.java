package com.example.sapr.gear2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BaseElement extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "id";
    SQLiteDatabase db;
    DatabaseHelper dh;
    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_element);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        String select_filter;
        String[] select_value;
        String[] notes =new String[]{"Пользователь: ",
                "Комментарий: ",
                "Время замера: ",
                "Температура: ",
                "Давление Па: ",
                "Скорость л в мин.: ",
                "Внутренняя температура: ",
                "Пульс: "};
        int id = intent.getIntExtra(EXTRA_MESSAGE, 0);
        try {
            DatabaseHelper dh = new DatabaseHelper(this);
            db = dh.getReadableDatabase();
            select_filter = "_id = ?";
            select_value = new String[]{String.valueOf(id)};
            cursor = db.query("TRAINING",
                    null,select_filter, select_value, null, null, null);

        } catch (SQLiteException e) {
            Log.e("error","DB error");
        }
        LinearLayout base_element_layout = (LinearLayout) findViewById(R.id.base_element_layout);

        if (cursor.moveToFirst())
            for(int i=0;i<notes.length;i++){
            String nameText = cursor.getString(i+1);
            TextView dynamicTextView = new TextView(this);
            dynamicTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            dynamicTextView.setText(notes[i]+" "+nameText);
            base_element_layout.addView(dynamicTextView);}


    }

}
