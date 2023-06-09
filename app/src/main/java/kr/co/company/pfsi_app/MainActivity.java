package kr.co.company.pfsi_app;
// 메인 액티비티 클래스 (2023-05-10 우진)
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button ChatVoiceBtn, MyInfoBtn, UrgentContactInfoBtn, SupportProgramInfoBtn, WelfareMapBtn;
    private DatabaseOpenHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 설정
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }

        // loding activity 실행
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);

        // DatabaseOpenHelper 객체 생성
        dbHelper = new DatabaseOpenHelper(this, "mydatabase.db", null, 1);
        db = dbHelper.getWritableDatabase();

        ChatVoiceBtn = findViewById(R.id.ChatVoiceBtn);
        MyInfoBtn = findViewById(R.id.MyInfoBtn);
        UrgentContactInfoBtn = findViewById(R.id.UrgentContactInfoBtn);
        SupportProgramInfoBtn = findViewById(R.id.SupportProgramInfoBtn);
        WelfareMapBtn = findViewById(R.id.WelfareMapBtn);

        ChatVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, ChatVoiceActivity.class);
                startActivity(intent);
            }
        });

        MyInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, MyInfoActivity.class);
                startActivity(intent);
            }
        });

        UrgentContactInfoBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("Range")
            @Override
            public void onClick(View v) {
                // 전화 화면
                // *자신의 정보 등록 메뉴에서 저장된 보호자 연락처 사용
                Cursor cursor = dbHelper.selectInfo(db);
                String gardianPhone = "";

                // 결과 처리
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        gardianPhone = cursor.getString(cursor.getColumnIndex("gardianPhone"));

                    } while (cursor.moveToNext());
                    cursor.close();
                }

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+gardianPhone));
                startActivity(intent);
            }
        });

        SupportProgramInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, SupportProgramInfoActivity.class);
                startActivity(intent);
            }
        });

        WelfareMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(MainActivity.this, WelfareMapActivity.class);
                startActivity(intent);
            }
        });

    }
}