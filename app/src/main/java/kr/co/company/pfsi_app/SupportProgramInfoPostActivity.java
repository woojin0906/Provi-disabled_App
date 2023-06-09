package kr.co.company.pfsi_app;
// 장애지원정보 상세 액티비티 클래스 (2023-05-14 우진)
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SupportProgramInfoPostActivity extends AppCompatActivity {

    private TextView TVcity, TVgroup, TVpeopleState, TVpeopleStateInfo, TVcategory, TVcategoryInfo, TVprogramTitle, TVtime, TVmoney, TVaddress, TVphone, TVprogramContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_program_info_post);

        TVcity = (TextView) findViewById(R.id.city);
        TVgroup = (TextView) findViewById(R.id.group);
        TVpeopleState = (TextView) findViewById(R.id.peopleState);
        TVpeopleStateInfo = (TextView) findViewById(R.id.peopleStateInfo);
        TVcategory = (TextView) findViewById(R.id.category);
        TVcategoryInfo = (TextView) findViewById(R.id.categoryInfo);
        TVprogramTitle = (TextView) findViewById(R.id.programTitle);
        TVtime = (TextView) findViewById(R.id.time);
        TVmoney = (TextView) findViewById(R.id.money);
        TVaddress = (TextView) findViewById(R.id.address);
        TVphone = (TextView) findViewById(R.id.phone);
        TVprogramContent = (TextView) findViewById(R.id.programContent);

        Intent receiveIntent = getIntent();
        final String city = receiveIntent.getStringExtra("city");
        final String group = receiveIntent.getStringExtra("group");
        final String peopleState = receiveIntent.getStringExtra("peopleState");
        final String peopleStateInfo = receiveIntent.getStringExtra("peopleStateInfo");
        final String category = receiveIntent.getStringExtra("category");
        final String categoryInfo = receiveIntent.getStringExtra("categoryInfo");
        final String programTitle = receiveIntent.getStringExtra("programTitle");
        final String time = receiveIntent.getStringExtra("time");
        final String money = receiveIntent.getStringExtra("money");
        final String address = receiveIntent.getStringExtra("address");
        final String phone = receiveIntent.getStringExtra("phone");
        final String programContent = receiveIntent.getStringExtra("programContent");
        final String latitude = receiveIntent.getStringExtra("latitude");
        final String longitude = receiveIntent.getStringExtra("longitude");

        TVcity.setText(city);
        TVgroup.setText(group);
        TVpeopleState.setText(peopleState);
        TVpeopleStateInfo.setText(peopleStateInfo);
        TVcategory.setText(category);
        TVcategoryInfo.setText(categoryInfo);
        TVprogramTitle.setText(programTitle);
        TVtime.setText(time);
        TVmoney.setText(money);
        TVaddress.setText(address);
        TVphone.setText(phone);
        TVprogramContent.setText(programContent);


        TVphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전화 화면
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + TVphone.getText().toString()));
                startActivity(intent);
            }
        });

        TVaddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 지도 화면
                Intent intent = new Intent(SupportProgramInfoPostActivity.this, SupportInfoMapActivity.class);
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                intent.putExtra("group", group);
                intent.putExtra("phone", phone);

                startActivity(intent);
            }
        });
    }
}