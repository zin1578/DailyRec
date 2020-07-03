package com.gp.dailyrecord;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;

import android.annotation.SuppressLint;

import android.widget.Button;
import android.widget.DatePicker;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import static android.content.Context.MODE_NO_LOCALIZED_COLLATORS;


public class ThirdFragment extends Fragment implements  DatePickerDialog.OnDateSetListener {

    DatePicker datePicker;  //  datePicker - 날짜를 선택하는 달력
    TextView viewDatePick;  //  viewDatePick - 선택한 날짜를 보여주는 textView
    EditText searchText;
    EditText editDiary;   //  editDiary - 선택한 날짜의 일기를 쓰거나 기존에 저장된 일기가 있다면 보여주고 수정하는 영역
    Button btnSave;   //  btnSave - 선택한 날짜의 일기 저장 및 수정(덮어쓰기) 버튼
    Button btnDatePick;
    String fileName;   //  fileName - 돌고 도는 선택된 날짜의 파일 이름
    DatePickerDialog picker;
    HSSFSheet sheet;
    ListView listview;
    ListViewAdapter adapter;

    //중복방지
    boolean twice=false;

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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_third, container, false);

        viewDatePick = (TextView) view.findViewById(R.id.viewDatePick);
    //    editDiary = (EditText) view.findViewById(R.id.editDiary);
        btnSave = (Button) view.findViewById(R.id.btnSave);
        btnDatePick = (Button)view.findViewById(R.id.datePick); //달력 버튼
        searchText = (EditText) view.findViewById(R.id.searchText); //검색 텍스트

        // Adapter 생성
        adapter = new ListViewAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) view.findViewById(R.id.listview1);
        listview.setAdapter(adapter);

        // 오늘 날짜를 받게해주는 Calender 친구들
        Calendar c = Calendar.getInstance();
        int cYear = c.get(Calendar.YEAR);
        int cMonth = c.get(Calendar.MONTH);
        int cDay = c.get(Calendar.DAY_OF_MONTH);

        // 첫 시작 시에는 오늘 날짜 일기 읽어주기
            checkedDay(cYear, cMonth, cDay);

        // 저장/수정 버튼 누르면 실행되는 리스너
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fileName을 넣고 저장 시키는 메소드를 호출
                saveDiary(fileName);
            }
        });

        btnDatePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                viewDatePick.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                checkedDay(year, monthOfYear, dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        //검색창 리스너
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable edit) {
                String filterText = edit.toString() ;
                if (filterText.length() > 0) {
                    ((ListViewAdapter)listview.getAdapter()).getFilter().filter(filterText) ;
                } else {
                    ((ListViewAdapter)listview.getAdapter()).getFilter().filter("") ;
                    // listview.clearTextFilter() ;
                    Log.i("clearTextFilter", "executed");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        }) ;

        return view;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDate = DateFormat.getDateInstance().format(c.getTime());

        String dates = Integer.toString(dayOfMonth);
        String months = Integer.toString(month);
        String years = Integer.toString(year);
    }


    // 일기 파일 읽기
    private void checkedDay(int year, int monthOfYear, int dayOfMonth) {
        adapter.clear();
      /*  //중복방지
        if(twice == false){
            twice = true;
        }else {
            twice = false;
            return;
        }
*/
        StringBuilder sb = new StringBuilder(); //string buffer
        String time ="";
        String str ="";
        String tag ="";
        String emo ="";

        // 받은 날짜로 날짜 보여주는
        viewDatePick.setText(year + " - " + (monthOfYear+1)+ " - " + dayOfMonth);
        // 파일 이름을 만들어준다. 파일 이름은 "20170318.txt" 이런식으로 나옴

        if(dayOfMonth<10 && monthOfYear<10)
            fileName = year + "0" + (monthOfYear+1) + "0" + dayOfMonth + ".xls";
        else if(dayOfMonth>=10&&monthOfYear<10)
            fileName = year + "0" + (monthOfYear+1) + "" + dayOfMonth + ".xls";
        else if(dayOfMonth<10&&monthOfYear>=10)
            fileName = year + "" + (monthOfYear+1) + "0" + dayOfMonth + ".xls";
        else if(dayOfMonth>=10&&monthOfYear>=10)
            fileName = year + "" + (monthOfYear+1) + "" + dayOfMonth + ".xls";

            String filePath = getActivity().getFilesDir().getPath().toString() + "/"+fileName;
            java.io.File excelFile = new java.io.File(filePath);

            if(excelFile.exists()) {
                try {
                    FileInputStream myInput = new FileInputStream(excelFile);
                    POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
                    HSSFWorkbook writer = new HSSFWorkbook(myFileSystem);
                    if (writer.getNumberOfSheets() != 0) {
                        sheet = writer.getSheetAt(0);
                        /** We now need something to iterate through the cells. **/
                        Iterator rowIter = sheet.rowIterator();
                        HSSFRow myRow = (HSSFRow) rowIter.next(); //헤더 한 줄 건너뛰기
                        int counter = 0; //엑셀 셀 카운터
                        while (rowIter.hasNext()) {
                            myRow = (HSSFRow) rowIter.next(); // 한줄 데이터
                            Iterator cellIter = myRow.cellIterator();
                            Log.isLoggable("row", myRow.getRowNum());
                            while (cellIter.hasNext()) {
                                counter++;
                                HSSFCell myCell = (HSSFCell) cellIter.next();
                                if (counter == 1) {//time
                                   // sb.append(myCell.toString()+"  |  ");
                                   time =myCell.toString();
                                    Log.e("Cell", myCell.toString());
                                } else if (counter == 2) {//str
                                   // sb.append(myCell.toString());
                                    str = myCell.toString();
                                    Log.e("Cell", myCell.toString());
                                } else if (counter == 3) {//emotion
                                    if (myCell.toString().equals("좋음")) {
                                        emo="좋음";
                                              }
                                    if (myCell.toString().equals("보통")) {
                                        emo="보통";
                                         }
                                    if (myCell.toString().equals("나쁨")) {
                                        emo="나쁨";
                                      }
                                } else if (counter == 4) {//lat

                                } else if (counter == 5) {//lon

                                }else if (counter == 6) {//태그
                                  // sb.append("\n             "+myCell.toString()+"\n\n");
                                    tag = myCell.toString();
                                }

                            }
                            if(emo.equals("좋음")) {
                                adapter.addItem(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_sentiment_very_satisfied_48px),
                                        str, tag, time);
                            }else if (emo.equals("보통")){
                                adapter.addItem(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_sentiment_satisfied_48px),
                                        str, tag, time);
                            }else if(emo.equals("나쁨")){
                                adapter.addItem(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_sentiment_very_dissatisfied_48px),
                                        str, tag, time);
                            }
                            counter = 0;
                        }

                    }

                    adapter.notifyDataSetChanged();
              //     editDiary.setText(sb.toString());
                    writer.close();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    // 일기 저장하는 메소드
    @SuppressLint("WrongConstant")
    private void saveDiary(String readDay) {


    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Refresh your fragment here

            // 오늘 날짜를 받게해주는 Calender 친구들
            Calendar c = Calendar.getInstance();
            int cYear = c.get(Calendar.YEAR);
            int cMonth = c.get(Calendar.MONTH);
            int cDay = c.get(Calendar.DAY_OF_MONTH);
            // 첫 시작 시에는 오늘 날짜 일기 읽어주기
            checkedDay(cYear, cMonth, cDay);
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
            Log.i("IsRefresh", "Yes");
        } else {

        }
    }


}