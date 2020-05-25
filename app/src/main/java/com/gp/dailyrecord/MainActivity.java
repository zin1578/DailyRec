package com.gp.dailyrecord;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.poi.*;

public class MainActivity extends AppCompatActivity {

    private AppCompatActivity mActivity;
    //adams 감정분석 관련 변수
    String key = "4258457626421016575"; //4258457626421016575  //342365250195746161
    String str1; //음성인식 텍스트
    String data;
    String emotionJson;
    double scoreJson;
    int badCount, normalCount, goodCount;

    //음성인식 중복 방지..
    boolean twice=false;


    // 위치 변수
    LocationManager mLocationManager;
    Intent intentThatCalled;
    public double latitude;
    public double longitude;
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;


    Intent intent;
    SpeechRecognizer mRecognizer;
    ImageButton sttBtn;
    TextView textView;
    //git config --global --add url."https://github.com/".insteadOf "git@github.com:"
    final int PERMISSION = 1;

    //엑셀 관련 변수
    HSSFSheet sheet;
    Workbook workbook;
    Row row;
    Cell c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(1, true);//처음화면을 프레그먼트 2로 (홈화면)

        if (Build.VERSION.SDK_INT >= 23) {
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sttBtn = (ImageButton) findViewById(R.id.sttStart);
        textView = (TextView) findViewById(R.id.sttResult);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        sttBtn.setOnClickListener(v -> {
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });
    }

    private void registerLocationUpdates() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,

                1000, 1, mLocationListener);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,

                1000, 1, mLocationListener);

//1000은 1초마다, 1은 1미터마다 해당 값을 갱신한다는 뜻으로, 딜레이마다 호출하기도 하지만

//위치값을 판별하여 일정 미터단위 움직임이 발생 했을 때에도 리스너를 호출 할 수 있다.

    }





    private final LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
//여기서 위치값이 갱신되면 이벤트가 발생한다.
//값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
//Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
                longitude = location.getLongitude();    //경도
                latitude = location.getLatitude();         //위도
                float accuracy = location.getAccuracy();        //신뢰도

            }

            else {
//Network 위치제공자에 의한 위치변화
//Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            }

        }

        public void onProviderDisabled(String provider) {

        }



        public void onProviderEnabled(String provider) {

        }



        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

      //  출처: https://biig.tistory.com/74 [덩치의 안드로이드 스터디]
    };


    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.

                }
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    private void getLastLocationNewMethod(){
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            String provider = location.getProvider();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
          //  double altitude = location.getAltitude();
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {

        }


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onResults(Bundle results) { //음성인식 후 감정분석
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                textView.setText(matches.get(0));

                //중복방지
            if(twice == false){
                twice = true;
            }else {
                twice = false;
                return;
            }
                //위치

            if (Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            } else {
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                registerLocationUpdates();
                getLastLocationNewMethod();
                latitude = getLastKnownLocation().getLatitude();
                longitude = getLastKnownLocation().getLongitude();
                mLocationManager.removeUpdates(mLocationListener);

            }

            str1 = (String) textView.getText();

            //감정분석
             {
             //   Emot t0 = new Emot(); 감정분석
             //   t0.start();
                 Senti t0 = new Senti();
                 t0.start();

                try {
                    t0.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*
                if (scoreJson < 0.8) {
                    Senti t1 = new Senti();
                    t1.start();
                }
                 */
                 //##감정 나쁨 보통 좋음 3가지로 수정
                if(emotionJson.equals("부정")||emotionJson.equals("공포")||emotionJson.equals("슬픔")
                    ||emotionJson.equals("혐오")||emotionJson.equals("분노")){
                    badCount++;
                    emotionJson = "나쁨";
                }else if(emotionJson.equals("중립")||emotionJson.equals("신뢰")||emotionJson.equals("기대")
                ||emotionJson.equals("놀라움")){
                    normalCount++;
                    emotionJson = "보통";
                }else if(emotionJson.equals("긍정")||emotionJson.equals("기쁨")){
                    goodCount++;
                    emotionJson = "좋음";
                }else if(true){ //아무것도 잡히지 않았을 때 보통으로
                    normalCount++;
                    emotionJson = "보통";
                }
                if (true) {
                    if (emotionJson.equals("나쁨")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 나쁨 /", str1);

                    } else if (emotionJson.equals("보통")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 보통 /", str1);

                    } else if (emotionJson.equals("좋음")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 좋음 /", str1);

                    }
                } //감정분석 끝
                 try {
                     WriteExcelFile();
                     return;
                 } catch (IOException e) {
                     e.printStackTrace();
                 }

             }
        }


        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };


    String getXmlData( String str){

        //EditText에 작성된 Text얻어오기
        String receiveMsg="";

        String queryUrl="http://api.adams.ai/datamixiApi/omAnalysis?" +
                "&key=" + key + "&query=" + str + "&type=" +1;

        try {
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("x-waple-authorization", key);

            if (conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
                Log.i("receiveMsg : ", receiveMsg);

                reader.close();
            } else {
                Log.i("통신 결과", conn.getResponseCode() + "에러");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receiveMsg;
    }


    String getXmlData0( String str){

        //EditText에 작성된 Text얻어오기
        String receiveMsg="";

        String queryUrl="http://api.adams.ai/datamixiApi/omAnalysis?" +
                "&key=" + key + "&query=" + str + "&type=" +0;


        try {
            URL url= new URL(queryUrl);//문자열로 된 요청 url을 URL 객체로 생성.

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            conn.setRequestProperty("x-waple-authorization", key);

            if (conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
                Log.i("receiveMsg : ", receiveMsg);

                reader.close();
            } else {
                Log.i("통신 결과", conn.getResponseCode() + "에러");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return receiveMsg;
    }


    public class Emot extends Thread{
        public void run() {
            data = getXmlData(str1);
            Log.v("데이터: ", "감정");
            //        Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONObject return_object = jsonObject.getJSONObject("return_object");
                JSONArray result = return_object.getJSONArray("Result");
                JSONArray context = result.getJSONArray(0);
                {
                    // jsonObject = result.getJSONObject(0);
                    scoreJson = context.getDouble(0);
                    emotionJson = context.getString(1);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class Senti extends Thread{
        public void run() {
            data = getXmlData(str1);
            Log.v("데이터: ", "감정");
            //        Toast.makeText(getApplicationContext(),data,Toast.LENGTH_SHORT).show();
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONObject return_object = jsonObject.getJSONObject("return_object");
                JSONArray result = return_object.getJSONArray("Result");
                JSONArray context = result.getJSONArray(0);
                {
                    // jsonObject = result.getJSONObject(0);
                    scoreJson = context.getDouble(0);
                    emotionJson = context.getString(1);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    void WriteFile(String curEmo, String text){
        try {
            Date currentTime = Calendar.getInstance().getTime();
            String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
            //FileOutputStream 객체생성, 파일명 "data.txt", 새로운 텍스트 추가하기 모드
            String fileName =  date_text+""+".txt";
            FileOutputStream fos=openFileOutput(fileName, Context.MODE_APPEND);
            long now = System.currentTimeMillis(); // 현재시간 받아오기
            Date date = new Date(now); // Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String nowTime = sdf.format(date);

            PrintWriter writer= new PrintWriter(fos);
            writer.append(nowTime + " ");
            writer.append(curEmo+ " ");
            writer.append(text);
            writer.append("\n");
            writer.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void WriteExcelFile() throws IOException {
        //excel file
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        //FileOutputStream 객체생성, 파일명 "data.txt", 새로운 텍스트 추가하기 모드
        String fileName =  date_text+""+".xls";

        long now = System.currentTimeMillis(); // 현재시간 받아오기
        Date date = new Date(now); // Date 객체 생성
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String nowTime = sdf.format(date); //시간 스트링


      //  HSSFWorkbook writer = new HSSFWorkbook();
        String filePath = getApplicationContext().getFilesDir().getPath().toString() + "/"+fileName;
        String fileChk = filePath;

        java.io.File excelFile = new java.io.File(filePath);
      /*  if(excelFile.delete()){
            Log.i("File Delete Success", excelFile.getName());
        }else{
            Log.i("File Delete Failed", excelFile.getName());
        }
*/
        File file = new File(fileChk);
        if(file.exists()) { //파일 존재하면 불러오기
            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                workbook = new HSSFWorkbook(inputStream);
                //내용 입력
                sheet = (HSSFSheet) workbook.getSheetAt(0);
                int rowsNum = sheet.getLastRowNum();

                row = sheet.createRow(++rowsNum); //다음번 로우
                //    Log.isLoggable("row", mLastRowNum);
                c = row.createCell(0); //시간
                c.setCellValue(nowTime);

                c = row.createCell(1); //내용
                c.setCellValue(str1);

                c = row.createCell(2); //감정
                c.setCellValue(emotionJson);

                c = row.createCell(3); //위도
                c.setCellValue(latitude);

                c = row.createCell(4); //경도
                c.setCellValue(longitude);

                //파일계속 null이라 막무가내로 파일 경로 만들기...

                //  File excelFile = new File(fileName);
                try {
                    FileOutputStream os = new FileOutputStream(excelFile);
                    workbook.write(os);
                    workbook.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{ //파일 존재하지 않을 경우
          //  FileInputStream inputStream = new FileInputStream(file);
            workbook = new HSSFWorkbook();
            sheet = (HSSFSheet) workbook.createSheet();
            row = sheet.createRow(1);

            c = row.createCell(0);
            c.setCellValue("시간");

            c = row.createCell(1);
            c.setCellValue("감정");

            c = row.createCell(2);
            c.setCellValue("내용");

            c = row.createCell(3);
            c.setCellValue("위도");

            c = row.createCell(4);
            c.setCellValue("경도");

            //내용 입력
          //  sheet = (HSSFSheet) workbook.getSheetAt(0);
            int rowsNum = sheet.getLastRowNum();
            row = sheet.createRow(++rowsNum); //다음번 로우
            //    Log.isLoggable("row", mLastRowNum);
            c = row.createCell(0); //시간
            c.setCellValue(nowTime);

            c = row.createCell(1); //내용
            c.setCellValue(str1);

            c = row.createCell(2); //감정
            c.setCellValue(emotionJson);

            c = row.createCell(3); //위도
            c.setCellValue(latitude);

            c = row.createCell(4); //경도
            c.setCellValue(longitude);

            //파일계속 null이라 막무가내로 파일 경로 만들기...

            //  File excelFile = new File(fileName);
            try {
                FileOutputStream os = new FileOutputStream(excelFile);
                workbook.write(os);
                workbook.close();

            }catch (IOException e){
                e.printStackTrace();
            }

        }
         /*   //내용 입력
            sheet = (HSSFSheet) workbook.getSheetAt(0);
            int rowsNum = sheet.getLastRowNum();

            row = sheet.createRow(++rowsNum); //다음번 로우
            //    Log.isLoggable("row", mLastRowNum);
            c = row.createCell(0); //시간
            c.setCellValue(nowTime);

            c = row.createCell(1); //내용
            c.setCellValue(str1);

            c = row.createCell(2); //감정
            c.setCellValue(emotionJson);

            c = row.createCell(3); //위도
            c.setCellValue(latitude);

            c = row.createCell(4); //경도
            c.setCellValue(longitude);

            //파일계속 null이라 막무가내로 파일 경로 만들기...

            //  File excelFile = new File(fileName);
            try {
                FileOutputStream os = new FileOutputStream(excelFile);
                workbook.write(os);
                workbook.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

          */

 /*
       //////read Excel TEST
       // String filePath = getApplicationContext().getFilesDir().getPath().toString() + "/"+fileName;
      //  java.io.File excelFile = new java.io.File(filePath);
        try {
            FileInputStream myInput = new FileInputStream(excelFile);
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            HSSFWorkbook writer = new HSSFWorkbook(myFileSystem);
            if (writer.getNumberOfSheets() != 0) {
                sheet = writer.getSheetAt(0);
                //We now need something to iterate through the cells.
                Iterator rowIter = sheet.rowIterator();

                while (rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next(); // 한줄 데이터
                    Iterator cellIter = myRow.cellIterator();
                    Log.isLoggable("row", myRow.getRowNum());
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        Log.e("Cell", myCell.toString());
                    }
                }
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }



    //혜빈수정 //2020-05-05 주현 FragmentPagerAdapter를 FragmentStatePagerAdapter로 수정(화면갱신때문에)
   MyPagerAdapter adapterViewPager;
    public static class MyPagerAdapter extends FragmentStatePagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }


        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return com.gp.dailyrecord.FirstFragment.newInstance(0, "Page # 1");//맵뷰
                case 1:
                    return com.gp.dailyrecord.SecondFragment.newInstance(1, "Page # 2"); //홈
                case 2:
                    return com.gp.dailyrecord.ThirdFragment.newInstance(2, "Page # 3");//다이어리
                default:
                    return null;
            }
        }



        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }



    }

    protected void refresh(){
        adapterViewPager.notifyDataSetChanged();
    }


}




