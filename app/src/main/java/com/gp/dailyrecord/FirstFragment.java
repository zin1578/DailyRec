package com.gp.dailyrecord;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;



public class FirstFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    HSSFSheet sheet;
    double lat, lon;
    MapView mapView;
    Button btnDatePick;
    DatePickerDialog picker;
    // newInstance constructor for creating fragment with arguments
    public static FirstFragment newInstance(int page, String title) {
        FirstFragment fragment = new FirstFragment();
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
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        //EditText tvLabel = (EditText) view.findViewById(R.id.editText);
        // tvLabel.setText(page + " -- " + title);
        btnDatePick = (Button)view.findViewById(R.id.datePick); //달력 버튼
        //    editDiary = (EditText) view.findViewById(R.id.editDiary);
        mapView = new MapView(this.getActivity());
        ViewGroup mapViewContainer = (ViewGroup) view.findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        // 중심점 변경 - 예제 좌표는 서울 남산
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.54892296550104, 126.99089033876304), true);
        // 줌 레벨 변경
        mapView.setZoomLevel(4, true);

        // 오늘 날짜를 받게해주는 Calender 친구들
        Calendar c = Calendar.getInstance();
        int cYear = c.get(Calendar.YEAR);
        int cMonth = c.get(Calendar.MONTH);
        int cDay = c.get(Calendar.DAY_OF_MONTH);

      //  ReadExcel();
        // 첫 시작 시에는 오늘 날짜 일기 읽어주기
        checkedDay(cYear, cMonth, cDay);
       // ExcelFileRead();

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

                                checkedDay(year, monthOfYear, dayOfMonth);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        return view;
    }

    void ExcelFileRead(){
        //excel file
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        //FileOutputStream 객체생성, 파일명 "data.txt", 새로운 텍스트 추가하기 모드
        String fileName =  date_text+""+".xls";
        //파일계속 null이라 막무가내로 파일 경로 만들기...
        String filePath = getActivity().getFilesDir().getPath().toString() + "/"+fileName;
        java.io.File excelFile = new java.io.File(filePath);
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.54892296550104, 126.99089033876304);
        MapPOIItem marker = new MapPOIItem();
        //////read Excel TEST
        // String filePath = getApplicationContext().getFilesDir().getPath().toString() + "/"+fileName;
        //  java.io.File excelFile = new java.io.File(filePath);
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
                                Log.e("Cell", myCell.toString());
                            } else if (counter == 2) {//str
                                marker.setItemName(myCell.toString());
                                Log.e("Cell", myCell.toString());
                            } else if (counter == 3) {//emotion
                                if (myCell.toString().equals("좋음")) {
                                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                    marker.setCustomImageResourceId(R.drawable.ic_sentiment_very_satisfied_48x); // 마커 이미지.
                                    marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                    marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                }
                                if (myCell.toString().equals("보통")) {
                                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                    marker.setCustomImageResourceId(R.drawable.ic_sentiment_satisfied_48x); // 마커 이미지.
                                    marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                    marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                }
                                if (myCell.toString().equals("나쁨")) {
                                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                    marker.setCustomImageResourceId(R.drawable.ic_sentiment_very_dissatisfied_48x); // 마커 이미지.
                                    marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                    marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                }
                            } else if (counter == 4) {//lat
                                lat = myCell.getNumericCellValue();
                                // MARKER_POINT = MapPoint.mapPointWithGeoCoord(myCell, );
                                Log.e("Cell", myCell.toString());
                            } else if (counter == 5) {//lon
                                lon = myCell.getNumericCellValue();
                                MARKER_POINT = MapPoint.mapPointWithGeoCoord(lat, lon);
                                Log.e("Cell", myCell.toString());
                            }
                            marker.setMapPoint(MARKER_POINT);
                            mapView.addPOIItem(marker);
                        }

                        counter = 0;
                    }

                }
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                mapView.setZoomLevel(4, true);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


//엑셀 저장 읽기 방식 수정 07/03
    void ReadExcel(){
        //excel file
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime);
        //FileOutputStream 객체생성, 파일명 "data.txt", 새로운 텍스트 추가하기 모드
        String fileName =  "save_file"+".xls";
        String filePath = getActivity().getFilesDir().getPath().toString() + "/"+fileName;
        java.io.File excelFile = new java.io.File(filePath);
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.54892296550104, 126.99089033876304);
        MapPOIItem marker = new MapPOIItem();
        //////read Excel TEST
        // String filePath = getApplicationContext().getFilesDir().getPath().toString() + "/"+fileName;
        //  java.io.File excelFile = new java.io.File(filePath);
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
                                Log.e("Cell", myCell.toString());
                            } else if (counter == 2) {//str
                                marker.setItemName(myCell.toString());
                                Log.e("Cell", myCell.toString());
                            } else if (counter == 3) {//emotion
                                if (myCell.toString().equals("좋음")) {
                                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                    marker.setCustomImageResourceId(R.drawable.ic_sentiment_very_satisfied_48px); // 마커 이미지.
                                    marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                    marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                }
                                if (myCell.toString().equals("보통")) {
                                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                    marker.setCustomImageResourceId(R.drawable.ic_sentiment_satisfied_48px); // 마커 이미지.
                                    marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                    marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                }
                                if (myCell.toString().equals("나쁨")) {
                                    marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                    marker.setCustomImageResourceId(R.drawable.ic_sentiment_very_dissatisfied_48px); // 마커 이미지.
                                    marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                    marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                }
                            } else if (counter == 4) {//lat
                                lat = myCell.getNumericCellValue();
                                // MARKER_POINT = MapPoint.mapPointWithGeoCoord(myCell, );
                                Log.e("Cell", myCell.toString());
                            } else if (counter == 5) {//lon
                                lon = myCell.getNumericCellValue();
                                MARKER_POINT = MapPoint.mapPointWithGeoCoord(lat, lon);
                                Log.e("Cell", myCell.toString());
                                marker.setMapPoint(MARKER_POINT);
                                mapView.addPOIItem(marker);
                            }

                        }

                        counter = 0;
                    }

                }
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                mapView.setZoomLevel(4, true);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    // 일기 파일 읽기
    private void checkedDay(int year, int monthOfYear, int dayOfMonth) {
        //excel file

        String fileName =  "save_file.xls";
        String filePath = getActivity().getFilesDir().getPath().toString() + "/"+fileName;
        java.io.File excelFile = new java.io.File(filePath);
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.54892296550104, 126.99089033876304);
        MapPOIItem marker = new MapPOIItem();

        String dateBF = "";

        if(dayOfMonth<10 && monthOfYear<10)
            dateBF= year +"-"+ "0" + (monthOfYear+1) +"-"+ "0" + dayOfMonth;
        else if(dayOfMonth>=10&&monthOfYear<10)
            dateBF = year +"-"+ "0" + (monthOfYear+1) + "-" + dayOfMonth;
        else if(dayOfMonth<10&&monthOfYear>=10)
            dateBF = year + "-"+ (monthOfYear+1) +"-"+ "0" + dayOfMonth;
        else if(dayOfMonth>=10&&monthOfYear>=10)
            dateBF = year + "-"+ (monthOfYear+1) + "-" + dayOfMonth;

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
                            if(myRow.getCell(0).toString().contains(dateBF)) {
                                while (cellIter.hasNext()) {
                                    counter++;
                                    HSSFCell myCell = (HSSFCell) cellIter.next();
                                    if (counter == 1) {//time

                                        Log.e("Cell", myCell.toString());
                                    } else if (counter == 2) {//str
                                        marker.setItemName(myCell.toString());
                                        Log.e("Cell", myCell.toString());
                                    } else if (counter == 3) {//emotion
                                        if (myCell.toString().equals("좋음")) {
                                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                            marker.setCustomImageResourceId(R.drawable.ic_sentiment_very_satisfied_48px); // 마커 이미지.
                                            marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                            marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                        }
                                        if (myCell.toString().equals("보통")) {
                                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                            marker.setCustomImageResourceId(R.drawable.ic_sentiment_satisfied_48px); // 마커 이미지.
                                            marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                            marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                        }
                                        if (myCell.toString().equals("나쁨")) {
                                            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage); // 마커타입을 커스텀 마커로 지정.
                                            marker.setCustomImageResourceId(R.drawable.ic_sentiment_very_dissatisfied_48px); // 마커 이미지.
                                            marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
                                            marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
                                        }
                                    } else if (counter == 4) {//lat
                                        lat = myCell.getNumericCellValue();
                                        // MARKER_POINT = MapPoint.mapPointWithGeoCoord(myCell, );
                                        Log.e("Cell", myCell.toString());
                                    } else if (counter == 5) {//lon
                                        lon = myCell.getNumericCellValue();
                                        MARKER_POINT = MapPoint.mapPointWithGeoCoord(lat, lon);
                                        Log.e("Cell", myCell.toString());
                                        marker.setMapPoint(MARKER_POINT);
                                        mapView.addPOIItem(marker);
                                    }

                                }
                            }

                            counter = 0;
                        }

                    }
                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(lat, lon), true);
                    mapView.setZoomLevel(4, true);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

}
