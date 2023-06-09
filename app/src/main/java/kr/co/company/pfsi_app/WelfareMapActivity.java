package kr.co.company.pfsi_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;

public class WelfareMapActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {
    private String api_key = "WDUOyZdacml2BjQXwZJL2PDCzymCNXM4U6HOF5td";
    private TMapView tMapView = null;
    private TMapGpsManager tmapgps = null;

    private TMapPoint tpoint = null;
    private Button btnSetTrackingMode;

    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welfare_map);
        mContext = this;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // T Map View
        tMapView = new TMapView(this);

        // API Key
        tMapView.setSKTMapApiKey(api_key);
        multipleMarkers();

        tMapView.setZoomLevel(17);
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        LinearLayout linearLayoutTmap = (LinearLayout)findViewById(R.id.linearLayoutTmap);
        linearLayoutTmap.addView(tMapView);

        tmapgps = new TMapGpsManager(this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);
//      tmapgps.setProvider(tmapgps.GPS_PROVIDER);

        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);

        tmapgps.OpenGps();

        // 내위치 버튼
        btnSetTrackingMode = findViewById(R.id.BtnSetTrackingMode);
        btnSetTrackingMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Toast.makeText(WelfareMapActivity.this, "내위치", Toast.LENGTH_SHORT).show();
                    tMapView.setTrackingMode(true);
                    tMapView.setSightVisible(true);
                    tMapView.setZoomLevel(17);
            }
        });
    }

    private void multipleMarkers() {
        WelfareMapApi parser = new WelfareMapApi();
        ArrayList<WelfareMapPoint> welfareMapPoint = new ArrayList<WelfareMapPoint>();
        try {
            welfareMapPoint = parser.apiParserSearch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < welfareMapPoint.size(); i++) {
            for (WelfareMapPoint entity : welfareMapPoint) {
                TMapPoint point = new TMapPoint(welfareMapPoint.get(i).getLatitude(), welfareMapPoint.get(i).getLongitude());
                TMapMarkerItem item = new TMapMarkerItem();

                // 마커 아이콘 설정
                Bitmap bitmap = null;
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.marker);
                Bitmap b=bitmapdraw.getBitmap();
                item.setIcon(b);

                item.setPosition(0.5f, 1.0f);
                item.setCalloutTitle(welfareMapPoint.get(i).getName());
                item.setCalloutSubTitle(welfareMapPoint.get(i).getPhone());
                item.setCanShowCallout(true);
                item.setAutoCalloutVisible(true);

                item.setTMapPoint(point);
                item.setName(entity.getName());
                tMapView.setCenterPoint(welfareMapPoint.get(i).getLongitude(), welfareMapPoint.get(i).getLatitude());
                tMapView.addMarkerItem("item" + i, item);

            }
        }

    }

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
    }
}