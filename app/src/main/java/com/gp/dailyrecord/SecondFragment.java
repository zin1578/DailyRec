package com.gp.dailyrecord;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;




public class SecondFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    HSSFSheet sheet;
    String filePath;
    String fileName;
    View view;

    // newInstance constructor for creating fragment with arguments
    public static SecondFragment newInstance(int page, String title) {
        SecondFragment fragment = new SecondFragment();
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //화면에 설정한 메뉴를 사용자가 선택하면 onOptionsItemSelected 메소드가 호출됨
        int curId = item.getItemId();
        //어떤 메뉴를 선택했는지를 id로 구분하여 처리
        switch(curId) {
            case R.id.menu_date_pick:
                MyYearMonthPickerDialog pd = new MyYearMonthPickerDialog();
                DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
                        Log.d("YearMonthPickerTest", "year = " + year + ", month = " + monthOfYear + ", day = " + dayOfMonth);
                        DatePickExcelFileRead(year, monthOfYear);
                        pd.dismiss();
                    }
                };

                pd.setListener(d);
                pd.show(getFragmentManager(), "YearMonthPickerTest");
                break;
            case R.id.menu_fav:
                break;
            case R.id.menu_search:

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
        view = inflater.inflate(R.layout.fragment_second, container, false);
     //   fileName = "save_file_1.xls";
    //    String filePath = getActivity().getFilesDir().getPath().toString() + "/" + fileName;
    //    java.io.File excelFile = new java.io.File(filePath);
     //   // 첫 시작 시에는 엑셀파일 다 읽어오기
    //    RemoveEmptyCellInExcel(excelFile);
    //    ExcelIntegrity();
        ExcelFileRead();

        return view;
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


    private void removeEmptyRows(HSSFSheet sheet) {
        Boolean isRowEmpty = Boolean.FALSE;
        for(int i = 0; i <= sheet.getLastRowNum(); i++){
            if(sheet.getRow(i)==null){
                isRowEmpty=true;
                sheet.shiftRows(i + 1, sheet.getLastRowNum()+1, -1);
                i--;
                continue;
            }
            for(int j =0; j<sheet.getRow(i).getLastCellNum();j++){
                if(sheet.getRow(i).getCell(j) == null ||
                        sheet.getRow(i).getCell(j).toString().trim().equals("")){
                    isRowEmpty=true;
                }else {
                    isRowEmpty=false;
                    break;
                }
            }
            if(isRowEmpty==true){
                sheet.shiftRows(i + 1, sheet.getLastRowNum()+1, -1);
                i--;
            }
        }
    }

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
    void DatePickExcelFileRead(int year, int month) {
        String mon = String.valueOf(month);
        if(month<10){
        mon = "0"+mon;
        }
        String date_text = year+"-"+mon+"-";
        //FileOutputStream 객체생성, 파일명 "data.txt", 새로운 텍스트 추가하기 모드
        int date_count = 1;
        int table_count = 0;
        int rowNum = 0;
        int i = 0;
     //   int resID = 0;
        boolean lrowCheck = false;
        boolean none = false;
        String dcStr = "";
        String buttonID ="";
        fileName = "save_file_1.xls";
        filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;

        //테이블초기화
        for(i=1;i<26 ;i++) {
            buttonID = "table_" + i;
            int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
            Button button = ((Button) view.findViewById(resID));
            button.setBackgroundResource(0);
        }

        //파일 경로를 지정
        File files = new File(filePath);
        //파일 유무를 확인
        if (files.exists() == true) {

            //파일이 있을시
            String filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;
            File excelFile = new File(filePath);
            try {
                FileInputStream myInput = new FileInputStream(excelFile);
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
                HSSFWorkbook writer = new HSSFWorkbook(myFileSystem);
                //label
                loop : for(i=0;i<2;i++){

                if (writer.getNumberOfSheets() != 0) {
                    sheet = writer.getSheetAt(0);

                    /** We now need something to iterate through the cells. **/
                    Iterator rowIter = sheet.rowIterator();
                    HSSFRow myRow = (HSSFRow) rowIter.next(); //헤더 한 줄 건너뛰기
                    myRow = (HSSFRow) rowIter.next();

                    //     Iterator rowIter = sheet.rowIterator();
                    //      HSSFRow myRow = sheet.getRow(0); //헤더 한 줄 건너뛰기
                    //blank delete


                    //해당 달 일기 찾기
                    while (!(myRow.getCell(0).toString().contains(date_text))) {
                        if(rowIter.hasNext()) {
                            myRow = (HSSFRow) rowIter.next(); //해당 달이 아니면 스킵
                            //      Log.e("row", myRow.getCell(0).toString());
                        }
                        if (!rowIter.hasNext()) { //마지막 줄이면
                            lrowCheck = true;
                            if(myRow.getRowNum()==sheet.getLastRowNum()){
                                none = true; //해당달 1도 없음
                                Toast.makeText(getActivity(), "해당 달의 일기가 없습니다.", Toast.LENGTH_SHORT).show();
                                break loop;
                            }
                        }
                    }
                    if (myRow.getCell(0) != null) {
                        while (lrowCheck == false && date_count < 33) {
                            //해당달을 포함하지 않으면 그만, 마지막 줄이면 그만, 데이트 카운트 32이면 그만
                            if (date_count < 10) {
                                dcStr = "0" + date_count;
                            } else {
                                dcStr = "" + date_count;
                            }
                            while (myRow.getCell(0).toString().contains(date_text + dcStr) && lrowCheck == false) {
                                while (myRow.getCell(0).toString().contains(date_text + dcStr) && rowIter.hasNext()) {
                                    //위의 while문 의심
                                    //  rowNum = myRow.getRowNum();
                                    Log.e("row", myRow.getCell(0).toString());
                                    myRow = (HSSFRow) rowIter.next(); // 한줄 데이터 건너뛰기
                                    if (myRow.getCell(0) == null) {
                                        int num = myRow.getRowNum();
                                        myRow.setRowNum(--num);
                                        break;
                                    }
                                    //   c = myRow.getCell(0);
              /*                  if (c == null || c.getCellType() == CellType.BLANK) {
                                    rowNum = myRow.getRowNum();
                                    while (rowNum < sheet.getLastRowNum()) {
                                        // This cell is empty
                                        int num = sheet.getLastRowNum();
                                        myRow.setRowNum(num);
                                        sheet.removeRow(myRow);
                                        //removeRow(sheet, myRow.getRowNum());
                                    }
                                    myRow.setRowNum(rowNum);
                                    sheet.removeRow(myRow);
                                    myRow.setRowNum(--rowNum);
                                } else {
                                    Log.e("row", myRow.getCell(0).toString());
                                    //            Log.e("row", myRow.toString());
                                }
*/
                                }
                                if (!rowIter.hasNext()) { //마지막 줄이면
                                    if (myRow.getCell(0).toString().contains(date_text + dcStr)) {
                                        //마지막 줄이고 날짜는 이전과 같으면
                                        rowNum = myRow.getRowNum();
                                        lrowCheck = true;
                                    } else {
                                        //마지막 줄이고 날짜가 이전과 다르면
                                        rowNum = myRow.getRowNum();
                                        rowNum--;
                                    }
                                } else {//마지막 줄이 아니면
                                    rowNum = myRow.getRowNum();
                                    rowNum--;
                                }
                                // 해당 일자의 마지막 감정 행 불러오기
                                myRow = sheet.getRow(rowNum);
                                Log.e("row", myRow.getCell(0).toString());
                                //          Log.e("row", myRow.toString());
                                // HSSFRow myRow = sheet.getRow(lrow);
                                HSSFCell myCell = myRow.getCell(2);
                                if (myCell.toString().equals("보통")) {
                                    buttonID = "table_" + (table_count + 1);
                                    int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
                                    Button button = ((Button) view.findViewById(resID));
                                    button.setBackgroundResource(R.drawable.ic_sentiment_satisfied_48px);
                                    table_count++;
                                    date_count++;
                                    if (date_count < 10) {
                                        dcStr = "0" + date_count;
                                    } else {
                                        dcStr = "" + date_count;
                                    }
                                    if (rowIter.hasNext()) {
                                        rowNum = myRow.getRowNum();
                                        myRow = sheet.getRow(++rowNum);
                                    } else {
                                        if (lrowCheck == false)
                                            myRow = sheet.getRow(++rowNum);
                                    }
                                } else if (myCell.toString().equals("좋음")) {
                                    buttonID = "table_" + (table_count + 1);
                                    int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
                                    Button button = ((Button) view.findViewById(resID));
                                    button.setBackgroundResource(R.drawable.ic_sentiment_very_satisfied_48px);
                                    table_count++;
                                    date_count++;
                                    if (date_count < 10) {
                                        dcStr = "0" + date_count;
                                    } else {
                                        dcStr = "" + date_count;
                                    }
                                    if (rowIter.hasNext()) {
                                        rowNum = myRow.getRowNum();
                                        myRow = sheet.getRow(++rowNum);
                                    } else {
                                        if (lrowCheck == false)
                                            myRow = sheet.getRow(++rowNum);
                                    }
                                } else if (myCell.toString().equals("나쁨")) {
                                    buttonID = "table_" + (table_count + 1);
                                    int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
                                    Button button = ((Button) view.findViewById(resID));
                                    button.setBackgroundResource(R.drawable.ic_sentiment_very_dissatisfied_48px);
                                    table_count++;
                                    date_count++;
                                    if (date_count < 10) {
                                        dcStr = "0" + date_count;
                                    } else {
                                        dcStr = "" + date_count;
                                    }
                                    if (rowIter.hasNext()) {
                                        rowNum = myRow.getRowNum();
                                        myRow = sheet.getRow(++rowNum);
                                        //     myRow = (HSSFRow) rowIter.next();
                                    } else {
                                        if (lrowCheck == false)
                                            myRow = sheet.getRow(++rowNum);
                                    }
                                }
                                if(myRow==null){
                                    lrowCheck = true;
                                    break;
                                }
                            }

                            date_count++;
                        }
                    }else{
                        writer.close();
                    }


                }

                }
                writer.close();
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

void ExcelIntegrity() {
    int rowNum = 0;
    boolean lrowCheck = false;
    Cell c;
    String dcStr = "";
    fileName = "save_file_1.xls";
    filePath = ":/storage/self/primary/AndroidWorkSpace" + "/"+fileName;

    //파일 경로를 지정
    File files = new File(filePath);
    //파일 유무를 확인
    if (files.exists() == true) {
        //파일이 있을시
        String filePath = getActivity().getFilesDir().getPath().toString() + "/" + fileName;
        File excelFile = new File(filePath);
        try {
            FileInputStream myInput = new FileInputStream(excelFile);
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            HSSFWorkbook writer = new HSSFWorkbook(myFileSystem);
            if (writer.getNumberOfSheets() != 0) {
                sheet = writer.getSheetAt(0);
                Iterator rowIter = sheet.rowIterator();
                HSSFRow myRow = sheet.getRow(0); //헤더 한 줄 건너뛰기
                //blank delete


                while (rowIter.hasNext()) {
                    if (myRow.getCell(0) == null) {
                        rowIter.remove();
                    }
                    myRow = (HSSFRow) rowIter.next();
                }
                if (myRow.getCell(0) == null) {
                    rowIter.remove();
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
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

    void ExcelFileRead() {
        //excel file   // 같은 달 맨 마지막 감정 불러
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy-MM-", Locale.getDefault()).format(currentTime);
        //FileOutputStream 객체생성, 파일명 "data.txt", 새로운 텍스트 추가하기 모드
        int date_count = 1;
        int table_count = 0;
        int rowNum = 0;
        boolean lrowCheck = false;
        Cell c;
        String dcStr = "";
        fileName = "save_file_1.xls";
        filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;

        //파일 경로를 지정
        File files = new File(filePath);
        //파일 유무를 확인
        if (files.exists() == true) {
            //파일이 있을시
            String filePath = "/storage/self/primary/AndroidWorkSpace" + "/"+fileName;
            File excelFile = new File(filePath);
            try {
                FileInputStream myInput = new FileInputStream(excelFile);
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
                HSSFWorkbook writer = new HSSFWorkbook(myFileSystem);
                if (writer.getNumberOfSheets() != 0) {
                    sheet = writer.getSheetAt(0);

                    /** We now need something to iterate through the cells. **/
                    Iterator rowIter = sheet.rowIterator();
                    HSSFRow myRow = (HSSFRow) rowIter.next(); //헤더 한 줄 건너뛰기
                    myRow = (HSSFRow) rowIter.next();

               //     Iterator rowIter = sheet.rowIterator();
              //      HSSFRow myRow = sheet.getRow(0); //헤더 한 줄 건너뛰기
                    //blank delete


                    //해당 달 일기 찾기
                    while (!(myRow.getCell(0).toString().contains(date_text))) {
                        if(rowIter.hasNext()) {
                            myRow = (HSSFRow) rowIter.next(); //해당 달이 아니면 스킵
                      //      Log.e("row", myRow.getCell(0).toString());
                        }
                        if (!rowIter.hasNext()) { //마지막 줄이면
                            lrowCheck = true;
                        }
                    }
                    if (myRow.getCell(0) != null) {
                    while (lrowCheck == false && date_count < 33) {
                        //해당달을 포함하지 않으면 그만, 마지막 줄이면 그만, 데이트 카운트 32이면 그만
                        if (date_count < 10) {
                            dcStr = "0" + date_count;
                        } else {
                            dcStr = "" + date_count;
                        }
                            while (myRow.getCell(0).toString().contains(date_text + dcStr) && lrowCheck == false) {
                                while (myRow.getCell(0).toString().contains(date_text + dcStr) && rowIter.hasNext()) {
                                    //위의 while문 의심
                                    //  rowNum = myRow.getRowNum();
                                    Log.e("row", myRow.getCell(0).toString());
                                    myRow = (HSSFRow) rowIter.next(); // 한줄 데이터 건너뛰기
                                    if (myRow.getCell(0) == null) {
                                        int num = myRow.getRowNum();
                                        myRow.setRowNum(--num);
                                        break;
                                    }
                                    //   c = myRow.getCell(0);
              /*                  if (c == null || c.getCellType() == CellType.BLANK) {
                                    rowNum = myRow.getRowNum();
                                    while (rowNum < sheet.getLastRowNum()) {
                                        // This cell is empty
                                        int num = sheet.getLastRowNum();
                                        myRow.setRowNum(num);
                                        sheet.removeRow(myRow);
                                        //removeRow(sheet, myRow.getRowNum());
                                    }
                                    myRow.setRowNum(rowNum);
                                    sheet.removeRow(myRow);
                                    myRow.setRowNum(--rowNum);
                                } else {
                                    Log.e("row", myRow.getCell(0).toString());
                                    //            Log.e("row", myRow.toString());
                                }
*/
                                }
                                if (!rowIter.hasNext()) { //마지막 줄이면
                                    if (myRow.getCell(0).toString().contains(date_text + dcStr)) {
                                        //마지막 줄이고 날짜는 이전과 같으면
                                        rowNum = myRow.getRowNum();
                                        lrowCheck = true;
                                    } else {
                                        //마지막 줄이고 날짜가 이전과 다르면
                                        rowNum = myRow.getRowNum();
                                        rowNum--;
                                    }
                                } else {//마지막 줄이 아니면
                                    rowNum = myRow.getRowNum();
                                    rowNum--;
                                }
                                // 해당 일자의 마지막 감정 행 불러오기
                                myRow = sheet.getRow(rowNum);
                                Log.e("row", myRow.getCell(0).toString());
                                //          Log.e("row", myRow.toString());
                                // HSSFRow myRow = sheet.getRow(lrow);
                                HSSFCell myCell = myRow.getCell(2);
                                if (myCell.toString().equals("보통")) {
                                    String buttonID = "table_" + (table_count + 1);
                                    int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
                                    Button button = ((Button) view.findViewById(resID));
                                    button.setBackgroundResource(R.drawable.ic_sentiment_satisfied_48px);
                                    table_count++;
                                    date_count++;
                                    if (date_count < 10) {
                                        dcStr = "0" + date_count;
                                    } else {
                                        dcStr = "" + date_count;
                                    }
                                    if (rowIter.hasNext()) {
                                        rowNum = myRow.getRowNum();
                                        myRow = sheet.getRow(++rowNum);
                                    } else {
                                        if (lrowCheck == false)
                                            myRow = sheet.getRow(++rowNum);
                                    }
                                } else if (myCell.toString().equals("좋음")) {
                                    String buttonID = "table_" + (table_count + 1);
                                    int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
                                    Button button = ((Button) view.findViewById(resID));
                                    button.setBackgroundResource(R.drawable.ic_sentiment_very_satisfied_48px);
                                    table_count++;
                                    date_count++;
                                    if (date_count < 10) {
                                        dcStr = "0" + date_count;
                                    } else {
                                        dcStr = "" + date_count;
                                    }
                                    if (rowIter.hasNext()) {
                                        rowNum = myRow.getRowNum();
                                        myRow = sheet.getRow(++rowNum);
                                    } else {
                                        if (lrowCheck == false)
                                            myRow = sheet.getRow(++rowNum);
                                    }
                                } else if (myCell.toString().equals("나쁨")) {
                                    String buttonID = "table_" + (table_count + 1);
                                    int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
                                    Button button = ((Button) view.findViewById(resID));
                                    button.setBackgroundResource(R.drawable.ic_sentiment_very_dissatisfied_48px);
                                    table_count++;
                                    date_count++;
                                    if (date_count < 10) {
                                        dcStr = "0" + date_count;
                                    } else {
                                        dcStr = "" + date_count;
                                    }
                                    if (rowIter.hasNext()) {
                                        rowNum = myRow.getRowNum();
                                        myRow = sheet.getRow(++rowNum);
                                        //     myRow = (HSSFRow) rowIter.next();
                                    } else {
                                        if (lrowCheck == false)
                                            myRow = sheet.getRow(++rowNum);
                                    }
                                }
                                if(myRow==null){
                                    lrowCheck = true;
                                    break;
                                }
                            }

                            date_count++;
                        }
                    }


                }
                writer.close();
                } catch(FileNotFoundException e){
                    e.printStackTrace();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
    }




                @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Refresh your fragment here
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
            Log.i("IsRefresh", "Yes");
        } else {

        }
    }

}
