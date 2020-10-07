package com.gp.dailyrecord;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public class MyEditDialog extends DialogFragment{
    Button btnConfirm;
    Button btnCancel;
    EditText editText_title;
    EditText editText_tag;
    RadioGroup radioGroup;
    Bundle args;
    String icon, title, tag;
    private Fragment fragment;
    OnMyDialogResult mDialogResult;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        args = getArguments();

        View dialog = inflater.inflate(R.layout.edit_dialog, null);

        //edit text view
        editText_title = dialog.findViewById(R.id.edit_title);
        editText_tag = dialog.findViewById(R.id.edit_tag);

        //button
        btnConfirm = dialog.findViewById(R.id.btn_confirm);
        btnCancel = dialog.findViewById(R.id.btn_cancel);

        //라디오 그룹 설정
        radioGroup = (RadioGroup) dialog.findViewById(R.id.radioGroup);

        //초깃값 설정
        editText_title.setText(args.getString("title"));
        editText_tag.setText(args.getString("tag"));

        if(args.getString("icon")=="좋음"){
            radioGroup.check(R.id.radioButton_good);
            icon = "좋음";
        }else if(args.getString("icon")=="보통"){
            radioGroup.check(R.id.radioButton_normal);
            icon = "보통";
        }else{
            radioGroup.check(R.id.radioButton_bad);
            icon = "나쁨";
        }

        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MyEditDialog.this.getDialog().cancel();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                title = editText_title.getText().toString();
                tag = editText_tag.getText().toString();
                mDialogResult.finish(icon, title, tag);
                MyEditDialog.this.getDialog().cancel();
            }
        });

        //라디오 그룹 클릭 리스너
        RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if(i == R.id.radioButton_good){
                    icon = "좋음";
                }else if(i == R.id.radioButton_normal){
                    icon = "보통";
                }else if(i == R.id.radioButton_bad){
                    icon = "나쁨";
                }
            }
        };

        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        builder.setView(dialog);

        return builder.create();
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(String icon, String title, String tag);
    }
 //   @Override
 //  public void onClick(View v) {
 //      switch (v.getId()){
 //          case R.id.btn_confirm:
 //              title = editText_title.getText().toString();
 //              tag = editText_tag.getText().toString();
 //              mDialogResult.finish(icon, title, tag);
 //              MyEditDialog.this.getDialog().cancel();
 //              break;
 //          case R.id.btn_cancel:
 //              MyEditDialog.this.getDialog().cancel();
 //              break;
 //      }
 //  }

}
