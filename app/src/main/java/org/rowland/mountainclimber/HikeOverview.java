package org.rowland.mountainClimber;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Map;

public class HikeOverview extends AppCompatActivity implements OnMapReadyCallback {
    Mountain m;
    Map<Integer,Mountain> mountainList;
    ArrayList<LatLng> points = new ArrayList<>();
    private static final int COLOR_BLUE_ARGB = 0xff699BF3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_summit_data);
        mountainList = Utils.loadMountains(getApplicationContext());
        Intent i = getIntent();
        String mountainId = i.getData().getPath().substring(1);
        m = mountainList.get(Integer.parseInt(mountainId));

        m.completed = true;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initWidgets(m.averageSpeed, m.distanceTravelled);
    }

    public void initWidgets(Double speed, Double distance) {
        TextView averageSpeedView = (TextView) findViewById(R.id.averageSpeed);
        TextView distanceView = (TextView) findViewById(R.id.totalDistanceText);
        distanceView.setText("You Travelled " + Math.round(distance * 100d) / 100d + " miles");
        averageSpeedView.setText("Speed: " + Math.round(speed * 100d) / 100d + " mph");

        Button b = (Button) findViewById(R.id.finish);
        b.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        activityChange(v);
                    }
                }
        );
    }

    public void activityChange(View v) {
        Intent i = new Intent(this, MountainList.class);
        startActivity(i);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String s;
        getResources();
        LatLng summit = new LatLng(m.Lat, m.Long);
        Color.argb(50,0,100,255);
        googleMap.addMarker(new MarkerOptions().position(summit)
                .title("Summit of " + m.name)
                .snippet(Integer.toString(m.height) + "ft"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(summit, 13));
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        Polyline path = googleMap.addPolyline(new PolylineOptions()
                .color(COLOR_BLUE_ARGB)
                .width(20));
        for (int i = 0; i < m.lng.size(); i++) {
            points.add(new LatLng(m.lat.get(i), m.lng.get(i)));
        }
        path.setPoints(points);
    }
}
