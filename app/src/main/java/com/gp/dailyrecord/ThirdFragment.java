package com.gp.dailyrecord;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import android.annotation.SuppressLint;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;


import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import static android.content.Context.MODE_NO_LOCALIZED_COLLATORS;


public class ThirdFragment extends Fragment implements  DatePickerDialog.OnDateSetListener {

    DatePicker datePicker;  //  datePicker - 날짜를 선택하는 달력
    TextView viewDatePick;  //  viewDatePick - 선택한 날짜를 보여주는 textView
 //   EditText searchText;
    EditText editDiary;   //  editDiary - 선택한 날짜의 일기를 쓰거나 기존에 저장된 일기가 있다면 보여주고 수정하는 영역
    Button btnSave;   //  btnSave - 선택한 날짜의 일기 저장 및 수정(덮어쓰기) 버튼
    Button btnDatePick;
    String fileName;   //  fileName - 돌고 도는 선택된 날짜의 파일 이름
    DatePickerDialog picker;
    HSSFSheet sheet;
    ListView listview;
    ListViewAdapter adapter;
    MenuItem searchItem;
    MenuItem mSearch;
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
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      //  super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.menu, menu);
        searchItem = menu.findItem(R.id.menu_search);
        mSearch = menu.findItem(R.id.menu_search);


        mSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return false;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                ((ListViewAdapter)listview.getAdapter()).getFilter().filter("") ;
                Log.i("collapse", "executed");
                return false;
            }
        });
        SearchView sv = (SearchView)mSearch.getActionView();

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String filterText = query;
                if (filterText.length() > 0) {
                    ((ListViewAdapter)listview.getAdapter()).getFilter().filter(filterText);

                } else {
                    ((ListViewAdapter)listview.getAdapter()).getFilter().filter("") ;
                    //listview.clearTextFilter() ;
                    Log.i("clearTextFilter", "executed");
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //화면에 설정한 메뉴를 사용자가 선택하면 onOptionsItemSelected 메소드가 호출됨
        int curId = item.getItemId();
        //어떤 메뉴를 선택했는지를 id로 구분하여 처리
        switch(curId) {
            case R.id.menu_date_pick:
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                checkedDay(year, monthOfYear, dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
                break;
            case R.id.menu_fav:
                break;
            case R.id.menu_search:

        /*        searchView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable edit) {
                        String filterText = edit.toString() ;
                        if (filterText.length() > 0) {
                            ((ListViewAdapter)listview.getAdapter()).getFilter().filter(filterText);
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
          */
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_third, container, false);

        viewDatePick = (TextView) view.findViewById(R.id.viewDatePick);

    //    editDiary = (EditText) view.findViewById(R.id.editDiary);
     //   btnSave = (Button) view.findViewById(R.id.btnSave);
     //   btnDatePick = (Button)view.findViewById(R.id.datePick); //달력 버튼
     //   searchText = (EditText) view.findViewById(R.id.searchText); //검색 텍스트

        // Adapter 생성
        adapter = new ListViewAdapter() ;

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) view.findViewById(R.id.listview1);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(itemClickListener);
        listview.setOnItemLongClickListener(itemLongClickListener);
        // 오늘 날짜를 받게해주는 Calender 친구들
        Calendar c = Calendar.getInstance();
        int cYear = c.get(Calendar.YEAR);
        int cMonth = c.get(Calendar.MONTH);
        int cDay = c.get(Calendar.DAY_OF_MONTH);

        // 첫 시작 시에는 엑셀파일 다 읽어오기
        ReadExcel();
        // 첫 시작 시에는 오늘 날짜 일기 읽어주기
        checkedDay(cYear, cMonth, cDay);

        listview.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return false;
            }
        });



/*
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
                    ((ListViewAdapter)listview.getAdapter()).getFilter().filter(filterText);
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
*/
        return view;
    }


    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override

        //리스트형 다이얼로그를 띄움 -->수정 삭제 즐겨찾기
        public boolean onItemLongClick(AdapterView<?> parent, View arg1, int position, long uid) {
            //확인하면 수정, 취소하면 수정하지 않음
            // get item
            ListViewItem item = (ListViewItem) parent.getItemAtPosition(position);
            String icon_str = item.getEmotion(); //아이콘 //감정
            String titleStr = item.getTitle() ; //문장
            String dateStr = item.getDate() ; //날짜
            String descStr = item.getDesc(); //태그

            Bundle args = new Bundle();
            args.putString("icon", icon_str);
            args.putString("title", titleStr);
            args.putString("tag", descStr);
            args.putString("date", dateStr);

            //수정
            final String items[] = {"수정"};
            //final String items[] = {"수정", "삭제"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  switch (which) {
                      case 0: //수정
                          MyEditDialog dialogFragment = new MyEditDialog();
                          dialogFragment.setArguments(args);
                          dialogFragment.show(getFragmentManager(), "Sample Dialog Fragment");
                          dialogFragment.setDialogResult(new MyEditDialog.OnMyDialogResult() {
                              @Override
                              public void finish(String icon, String title, String tag) {
                                  //수정 edit
                                  ExcelUpdateAll(icon, title, tag, dateStr);
                              }
                          });
                          break;

           //           case 1: //삭제
            //              ExcelRemove(dateStr);
           //               break;
                  }
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
            return true;
        }
    };


    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long uid) {

        }
    };

    void show(String text, String date) {
        final EditText edittext = new EditText(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("수정");
        edittext.setText(text);
      //  builder.setMessage("AlertDialog Content");
        builder.setView(edittext);
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String editStr = edittext.getText().toString();
                        Toast.makeText(getActivity(),editStr ,Toast.LENGTH_LONG).show();
                        ExcelUpdate(editStr, date);
                        dialog.dismiss();
                    }

                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    void ExcelRemove(String date){
        fileName = "save_file_1.xls";
        String filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;
        java.io.File excelFile = new java.io.File(filePath);
        int rowNum = 0;
        if(excelFile.exists()) {
            try {
                FileInputStream myInput = new FileInputStream(excelFile);
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
                HSSFWorkbook writer = new HSSFWorkbook(myFileSystem);
                if (writer.getNumberOfSheets() != 0) {
                    sheet = writer.getSheetAt(0);
                    Iterator rowIter = sheet.rowIterator();
                    HSSFRow myRow = (HSSFRow) rowIter.next(); //헤더 한 줄 건너뛰기
                    while (rowIter.hasNext()) {
                        myRow = (HSSFRow) rowIter.next(); // 한줄 데이터
                        Iterator cellIter = myRow.cellIterator();
                        Log.isLoggable("row", myRow.getRowNum());
                        if (myRow.getCell(0).toString().contains(date)) {
                            rowNum = myRow.getRowNum();
                            removeRow(sheet, rowNum);
                        }
                    }
                }
                    try {
                        boolean isRowEmpty=false;
                        for(int i = 0; i < sheet.getLastRowNum(); i++){
                            if(sheet.getRow(i)==null){
                                sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
                                i--;
                                continue;
                            }
                            for(int j =0; j<1;j++){
                                if(sheet.getRow(i).getCell(j).toString().trim().equals("")){
                                    isRowEmpty=true;
                                }else {
                                    isRowEmpty=false;
                                    break;
                                }
                            }
                            if(isRowEmpty==true){
                                sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
                                i--;
                            }
                        }
                        FileOutputStream output_file = new FileOutputStream(new File(filePath));
                        //write changes
                        writer.write(output_file);
                        output_file.close();
                        writer.close();
                        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
                        //   ((MainActivity)getActivity()).refresh();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        adapter.notifyDataSetChanged();
    }
    void ExcelUpdateAll(String icon, String title, String tag, String date) {
        //엑셀 열고, date로 해당 열 찾고, 해당 셀 새로운 text로 변경
        //    int rownum = 0;
        boolean flag = false;
        fileName = "save_file_1.xls";
        String filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;
        java.io.File excelFile = new java.io.File(filePath);
         // /storage/self/primary/AndroidWorkSpace/save_file.xls
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
                    while (rowIter.hasNext()&&flag==false) {
                        myRow = (HSSFRow) rowIter.next(); // 한줄 데이터
                        Iterator cellIter = myRow.cellIterator();
                        Log.isLoggable("row", myRow.getRowNum());
                        if(myRow.getCell(0).toString().contains(date)){
                            //         rownum = myRow.getRowNum();
                            myRow.getCell(1).setCellValue(title); //내용
                            myRow.getCell(2).setCellValue(icon); //감정
                            myRow.getCell(5).setCellValue(tag); //태그
                         //   Log.e("updatedValue", myRow.getCell(1).toString());
                            flag=true;
                        }
                    }
                }
                try {
                    // /sdcard/AndroidWorkSpace
                    FileOutputStream output_file = new FileOutputStream(new File(filePath));
                    //write changes
                    writer.write(output_file);
                    output_file.close();
                    writer.close();
                    getFragmentManager().beginTransaction().detach(this).attach(this).commit();
                    //   ((MainActivity)getActivity()).refresh();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void RemoveEmptyCellInExcel(File f){
            HSSFWorkbook workbook;
            HSSFSheet sheet;
            int firstColumn;
            int endColumn;
            boolean isRowEmpty = true;
            try {
                FileInputStream is = new FileInputStream(f);

                workbook = new HSSFWorkbook(is);
                sheet = workbook.getSheetAt(0);
                //sheet.setDisplayGridlines(false);

                //block to set column bounds
                Iterator<Row> iter = sheet.rowIterator();
                firstColumn = (iter.hasNext() ? Integer.MAX_VALUE : 0);
                endColumn = 0;
                while (iter.hasNext()) {
                    Row row = iter.next();
                    short firstCell = row.getFirstCellNum();
                    if (firstCell >= 0) {
                        firstColumn = Math.min(firstColumn, firstCell);
                        endColumn = Math.max(endColumn, row.getLastCellNum());
                    }
                }

                // main logic block
                for (int i = 0; i < sheet.getLastRowNum(); i++) {
                    if (sheet.getRow(i) != null) {
                        isRowEmpty = true;
                        Row row = sheet.getRow(i);
                        for (int j = firstColumn; j < endColumn; j++) {
                            if (j >= row.getFirstCellNum() && j < row.getLastCellNum()) {
                                Cell cell = row.getCell(j);
                                if (cell != null) {
                                    if (!cell.getStringCellValue().equals("")) {
                                        isRowEmpty = false;
                                        break;
                                    }
                                }
                            }
                        }
                        //if empty
                        if (isRowEmpty) {
                            System.out.println("Found empty row on: " + row.getRowNum());
                            sheet.shiftRows(row.getRowNum() + 1, sheet.getLastRowNum(), -1);
                            i--;
                        }
                    }
                    // if row is null
                    else {
                        sheet.shiftRows(i + 1, sheet.getLastRowNum(), -1);
                        i--;
                    }
                }
                //Writing output to the same file.
                FileOutputStream fileOut = new FileOutputStream(f);
                workbook.write(fileOut);
                fileOut.close();
                System.out.println("Successfully wrote the content in the file");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    void ExcelUpdate(String text, String date) {
        //엑셀 열고, date로 해당 열 찾고, 해당 셀 새로운 text로 변경
    //    int rownum = 0;
        boolean flag = false;
        fileName = "save_file_1.xls";
        String  filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;
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
                    while (rowIter.hasNext()&&flag==false) {
                        myRow = (HSSFRow) rowIter.next(); // 한줄 데이터
                        Iterator cellIter = myRow.cellIterator();
                        Log.isLoggable("row", myRow.getRowNum());
                        if(myRow.getCell(0).toString().contains(date)){
                  //         rownum = myRow.getRowNum();
                           myRow.getCell(1).setCellValue(text);
                           Log.e("updatedValue", myRow.getCell(1).toString());
                           flag=true;
                        }
                    }
                }
                try {

                    FileOutputStream output_file = new FileOutputStream(new File(filePath));
                    //write changes
                    writer.write(output_file);
                    output_file.close();
                    writer.close();
                    getFragmentManager().beginTransaction().detach(this).attach(this).commit();
                 //   ((MainActivity)getActivity()).refresh();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    //
    // 일기 파일 읽기
    private void ReadExcel() {
        adapter.clear();
        StringBuilder sb = new StringBuilder(); //string buffer
        String time ="";
        String str ="";
        String tag ="";
        String emo ="";
        String dateBF = "";

        fileName = "save_file_1.xls";
        String filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;
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
                                time = myCell.toString();
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
                                    str, tag, time, emo);
                        }else if (emo.equals("보통")){
                            adapter.addItem(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_sentiment_satisfied_48px),
                                    str, tag, time, emo);
                        }else if(emo.equals("나쁨")){
                            adapter.addItem(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_sentiment_very_dissatisfied_48px),
                                    str, tag, time, emo);
                        }
                        counter = 0;
                    }
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove a row by its index
     * @param sheet a Excel sheet
     * @param rowIndex a 0 based index of removing row
     */
    public static void removeRow(HSSFSheet sheet, int rowIndex) {

        int lastRowNum=sheet.getLastRowNum();
        if(rowIndex>=0&&rowIndex<lastRowNum){
            sheet.shiftRows(rowIndex+1,lastRowNum, -1);
        }
        if(rowIndex==lastRowNum){
            HSSFRow removingRow=sheet.getRow(rowIndex);
            if(removingRow!=null){
                sheet.removeRow(removingRow);
            }
        }
    }
    // 일기 파일 읽기
    private void checkedDay(int year, int monthOfYear, int dayOfMonth) {
    //    adapter.clear();

        StringBuilder sb = new StringBuilder(); //string buffer
        String time ="";
        String str ="";
        String tag ="";
        String emo ="";

        String dateBF = "";
        // 받은 날짜로 날짜 보여주는
        viewDatePick.setText(year + " - " + (monthOfYear+1)+ " - " + dayOfMonth);
        // 파일 이름을 만들어준다. 파일 이름은 "20170318.txt" 이런식으로 나옴

        if(dayOfMonth<10 && monthOfYear<10)
            dateBF= year +"-"+ "0" + (monthOfYear+1) +"-"+ "0" + dayOfMonth;
        else if(dayOfMonth>=10&&monthOfYear<10)
            dateBF = year +"-"+ "0" + (monthOfYear+1) + "-" + dayOfMonth;
        else if(dayOfMonth<10&&monthOfYear>=10)
            dateBF = year + "-"+ (monthOfYear+1) +"-"+ "0" + dayOfMonth;
        else if(dayOfMonth>=10&&monthOfYear>=10)
            dateBF = year + "-"+ (monthOfYear+1) + "-" + dayOfMonth;

        ((ListViewAdapter)listview.getAdapter()).getFilter().filter(dateBF);

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
            fileName = "save_file_1.xls";
            String filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;
            java.io.File excelFile = new java.io.File(filePath);
      //      RemoveEmptyCellInExcel(excelFile);
            // 첫 시작 시에는 엑셀파일 다 읽어오기
            ReadExcel();
            //첫 시작 시에 오늘 날짜 일기만 추리기.
            checkedDay(cYear, cMonth, cDay);
        //    getFragmentManager().beginTransaction().detach(this).attach(this).commit();
            Log.i("IsRefresh", "Yes");
        } else {

        }
    }


}