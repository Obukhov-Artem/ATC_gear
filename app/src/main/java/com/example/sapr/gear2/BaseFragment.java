package com.example.sapr.gear2;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {


    private EditText username;
    private CheckBox flagDB;
    private View layout;

    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout = inflater.inflate(R.layout.fragment_control, container, false);

        username = (EditText) layout.findViewById(R.id.userName);
        flagDB = (CheckBox) layout.findViewById(R.id.flagDB);
        return layout;
    }

}
