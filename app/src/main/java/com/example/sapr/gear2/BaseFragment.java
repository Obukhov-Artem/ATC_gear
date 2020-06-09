package com.example.sapr.gear2;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.AndroidException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {
    public static final String EXTRA_MESSAGE = "id";

    private EditText username;
    private EditText description;
    private View layout;
    private Button search;
    private ListView listData;
    SQLiteDatabase db;
    DatabaseHelper dh;
    private Cursor cursor;
    SimpleCursorAdapter listAdapter;

    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_base, container, false);
        search = (Button) layout.findViewById(R.id.Search_DB);
        username = (EditText) layout.findViewById(R.id.user_filter);
        description = (EditText) layout.findViewById(R.id.decription_filter);
        listData = (ListView) layout.findViewById(R.id.list_data);

        new UpdateBaseTask().execute();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateBaseTask().execute();
            }
        });

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView,
                                    View itemView,
                                    int position,
                                    long id) {
                TextView c = (TextView) itemView.findViewById(R.id.item_ID);
                int id_text = Integer.parseInt(c.getText().toString());
                Intent intent = new Intent(getActivity(), BaseElement.class);
                intent.putExtra(BaseElement.EXTRA_MESSAGE, id_text);
                startActivity(intent);

            }

        };
        listData.setOnItemClickListener(itemClickListener);
        return layout;
    }


    private class UpdateBaseTask extends AsyncTask<Void, Void, Boolean> {

        private String select_filter;
        private String[] select_value;

        protected void onPreExecute() {
            String user_filter = String.valueOf(username.getText());
            String desc_filter = String.valueOf(description.getText());
            select_filter = "";
            if (user_filter.length() > 0 && desc_filter.length() > 0) {
                select_filter = "NAME = ? and DESCRIPTION = ?";
                select_value = new String[]{user_filter, desc_filter};
            }
            if (user_filter.length() > 0) {
                select_filter = "NAME = ?";
                select_value = new String[]{user_filter};
            }
            if (desc_filter.length() > 0) {
                select_filter = "DESCRIPTION = ?";
                select_value = new String[]{desc_filter};
            }
            if (user_filter.length() == 0 && desc_filter.length() == 0) {
                select_filter = null;
                select_value = null;
            }
        }

        protected Boolean doInBackground(Void... voids) {

            try {
                dh = new DatabaseHelper(getActivity());
                db = dh.getReadableDatabase();
                cursor = db.query("TRAINING",
                        null,
                        select_filter, select_value, null, null, "_id DESC");
                listAdapter = new SimpleCursorAdapter(getActivity(),
                        R.layout.data_item,
                        cursor,
                        new String[]{"_id", "NAME", "DESCRIPTION", "TIME_VALUE", "TEMPERATURE", "PRESSURE", "SPIROGRAM", "TEMPERATURE_INNER", "PULSE", "PARAM_TEMP", "PARAM_DAMPER", "PARAM_TEMP_LIMIT"},
                        new int[]{R.id.item_ID, R.id.item_NAME, R.id.item_DESCRIPTION, R.id.item_TIME_VALUE, R.id.item_TEMPERATURE, R.id.item_TEMPERATURE_INNER, R.id.item_PULSE, R.id.item_PARAM_TEMP, R.id.item_PARAM_DAMPER, R.id.item_PARAM_TEMP_LIMIT},
                        0);


                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        protected void onPostExecute(Boolean success) {
            listData.setAdapter(listAdapter);

            if (!success) {
                Toast toast = Toast.makeText(getActivity(),
                        "Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        db.close();
    }
}
