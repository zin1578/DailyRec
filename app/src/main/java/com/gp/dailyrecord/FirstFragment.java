package com.gp.dailyrecord;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        MapView mapView = new MapView(this.getActivity());
        ViewGroup mapViewContainer = (ViewGroup) view.findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        // 중심점 변경 - 예제 좌표는 서울 남산
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.54892296550104, 126.99089033876304), true);
        // 줌 레벨 변경
        mapView.setZoomLevel(4, true);

        //마커 찍기
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.54892296550104, 126.99089033876304);
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Default Marker");
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);

        ExcelFileRead();

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
                        if(counter == 1) {//time
                            Log.e("Cell", myCell.toString());
                        }else if(counter == 2) {//str
                            marker.setItemName(myCell.toString());
                            Log.e("Cell", myCell.toString());
                        }else if(counter == 3) {//emotion
                            if(myCell.toString()=="좋음")
                                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                            if(myCell.toString()=="보통")
                                marker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
                            if(myCell.toString()=="나쁨")
                                marker.setMarkerType(MapPOIItem.MarkerType.RedPin);
                            Log.e("Cell", myCell.toString());
                        }else if(counter == 4) {//lat
                            lat = myCell.getNumericCellValue();
                           // MARKER_POINT = MapPoint.mapPointWithGeoCoord(myCell, );
                            Log.e("Cell", myCell.toString());
                        }else if(counter == 5) {//lon
                            lon = myCell.getNumericCellValue();
                            MARKER_POINT = MapPoint.mapPointWithGeoCoord(lat, lon);
                            Log.e("Cell", myCell.toString());
                        }
                    }
                    marker.setMapPoint(MARKER_POINT);
                    counter = 0;
                }

            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }





    }
}
