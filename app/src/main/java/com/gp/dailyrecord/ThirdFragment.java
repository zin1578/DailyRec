package com.gp.dailyrecord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class ThirdFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    TextView textView;

    // newInstance constructor for creating fragment with arguments
    public static ThirdFragment newInstance(int page, String title) {
        ThirdFragment fragment = new ThirdFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third, container, false);
        TextView textView = (TextView) view.findViewById(R.id.diaryText);
        ReadFile();
        return view;

    }

    void ReadFile() {
        StringBuffer buffer = new StringBuffer();
        try {
            //FileInputStream 객체생성, 파일명 "data.txt"
            FileInputStream fis = getActivity().openFileInput("data.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String str = reader.readLine();//한 줄씩 읽어오기
            while (str != null) {
                buffer.append(str + "\n");
                str = reader.readLine();
            }
            textView.setText(buffer.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}