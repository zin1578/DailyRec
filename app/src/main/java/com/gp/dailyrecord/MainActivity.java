package com.gp.dailyrecord;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity  {



    private AppCompatActivity mActivity;


    //adams 감정분석 관련 변수
    String key ="342365250195746161";
    String str1; //음성인식 텍스트
    double userLat, userLon; //사용자의 현재위치 위도 경도
    String data;
    String emotionJson;
    double scoreJson;

    Intent intent;
    SpeechRecognizer mRecognizer;
    ImageButton sttBtn;
    TextView textView;

    final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);


/*
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
*/

        if (Build.VERSION.SDK_INT >= 23) {
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }

        sttBtn = (ImageButton) findViewById(R.id.sttStart);
        textView = (TextView)findViewById(R.id.sttResult);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        sttBtn.setOnClickListener(v -> {
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });
    }


    /* ##캘린더
    public void onButtonDiaryClicked(View v){
        Intent intent = new Intent(getApplicationContext(), CalenderActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MENU);
    }
*/

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
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) { //음성인식 후 감정분석
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for (int i = 0; i < matches.size(); i++) {
                textView.setText(matches.get(i));
            }
            str1 = (String) textView.getText();



            //감정분석
             {
                //키워드 작동 안하면
                Emot t0 = new Emot();
                t0.start();
                try {
                    t0.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (scoreJson < 0.8) {
                    Senti t1 = new Senti();
                    t1.start();
                }
                if (scoreJson >= 0.8) {
                    if (emotionJson.equals("부정")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/감정: 나쁨 /", str1);

                    } else if (emotionJson.equals("중립")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 보통 /", str1);

                    } else if (emotionJson.equals("긍정")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 좋음 /", str1);

                    } else if (emotionJson.equals("기쁨")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 행복 /", str1);

                    } else if (emotionJson.equals("신뢰")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 신뢰 /", str1);

                    } else if (emotionJson.equals("공포")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 공포 /", str1);

                    } else if (emotionJson.equals("기대")) {
                        if (str1.contains("싶어") || str1.contains("싶다") || str1.contains("싶") || str1.contains("할래") || str1.contains("갈래") || str1.contains("거야")) {

                            WriteFile("/ 일상: 의지/", str1);
                        } else {
                            //감성 종류 : [부정, 중립, 긍정]
                            // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                            WriteFile("/ 감정: 기대 /", str1);
                        }
                    } else if (emotionJson.equals("놀라움")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 놀라움 /", str1);

                    } else if (emotionJson.equals("슬픔")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 슬픔 /", str1);

                    } else if (emotionJson.equals("혐오")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 혐오 /", str1);

                    } else if (emotionJson.equals("분노")) {                  //감성 종류 : [부정, 중립, 긍정]
                        // 감정 종류 : [기쁨, 신뢰, 공포, 기대, 놀라움, 슬픔, 혐오, 분노]

                        WriteFile("/ 감정: 화남 /", str1);

                    } else { //모두 아닌경우

                    }
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
            FileOutputStream fos=openFileOutput(date_text+""+".txt", Context.MODE_APPEND);
            long now = System.currentTimeMillis(); // 현재시간 받아오기
            Date date = new Date(now); // Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String nowTime = sdf.format(date);
            PrintWriter writer= new PrintWriter(fos);
            writer.append(nowTime + " ");
            // writer.println(nowTime+ " ");
            writer.append(curEmo+ " ");
            writer.append(text);
            writer.append("\n");
            writer.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
        /////////////////////// 파일 쓰기 ///////////////////////
        String emotion = curEmo;
        String str = text;
        // 파일 생성
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/camdata"); // 저장 경로
        // 폴더 생성
        if(!saveFile.exists()){ // 폴더 없을 경우
            saveFile.mkdir(); // 폴더 생성
        }
        try {
            long now = System.currentTimeMillis(); // 현재시간 받아오기
            Date date = new Date(now); // Date 객체 생성
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowTime = sdf.format(date);

            BufferedWriter buf = new BufferedWriter(new FileWriter(saveFile+"/CarnumData.txt", true));
            buf.append(nowTime + " "); // 날짜 쓰기
            buf.append(emotion + " ");//감정 쓰기
            buf.append(str); // 일기 쓰기
            buf.newLine(); // 개행
            buf.close();
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }



    //혜빈수정
   FragmentPagerAdapter adapterViewPager;
    public static class MyPagerAdapter extends FragmentPagerAdapter {
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
                    return com.gp.dailyrecord.FirstFragment.newInstance(0, "Page # 1");
                case 1:
                    return com.gp.dailyrecord.SecondFragment.newInstance(1, "Page # 2");
                case 2:
                    return com.gp.dailyrecord.ThirdFragment.newInstance(2, "Page # 3");
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
}




