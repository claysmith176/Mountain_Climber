package org.rowland.mountainClimber;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.ArrayList;
import java.util.Map;

public class HikeTracker extends FragmentActivity implements OnMapReadyCallback  {
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    Location oldLocation;
    Location newLocation;
    Polyline path;
    ArrayList<LatLng> points = new ArrayList<>(10);
    PolylineOptions pLOptions;
    boolean drawPolyline = false;
    private static final int COLOR_BLUE_ARGB = 0xff699BF3;
    TextView distanceTravelled;
    TextView distanceLeft;
    TextView currentElevation;
    TextView averageSpeed;
    double averageTotal;
    int numberofValues;
    double totalDistance;
    LatLng summit;
    Location lastLocation;
    Button checkIn;
    Button resume;
    Button stop;
    Button pause;
    boolean updateInfo = true;
    SupportMapFragment mapFragment;
    double altitude;
    double distanceSummit;
    private static final String TAG = "BroadcastTest";
    private Intent intervalIntent;
    private PendingIntent intervalPendingIntent;
    private int testTimer = 0;
    private Mountain mountain;
    Map<Integer,Mountain> mountainList;
    long startTime;
    double speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_tracker);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                  onLocationChange(location);
                }
            };
        };

        mountainList = Utils.loadMountains(getApplicationContext());
        Intent i = getIntent();
        String mountainId = i.getData().getPath().substring(1);
        mountain = mountainList.get(Integer.parseInt(mountainId));

        setLayoutVariables();
        summit = new LatLng(mountain.Lat, mountain.Long);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
               .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChange(location);
                        }
                    }
                });

        intervalIntent = new Intent(this, LocationUpdatePendingIntent.class);
        intervalPendingIntent = PendingIntent.getService(this, 12,intervalIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public void setLayoutVariables() {
        distanceTravelled = (TextView) findViewById(R.id.distanceTraelled);
        distanceLeft = (TextView) findViewById(R.id.distanceToSummit);
        currentElevation = (TextView) findViewById(R.id.currentElevation);
        averageSpeed = (TextView) findViewById(R.id.averageSpeed);
        pause = (Button) findViewById(R.id.pause);
        resume = (Button) findViewById(R.id.Resume);
        stop = (Button) findViewById(R.id.stop);
        checkIn = (Button) findViewById(R.id.checkIn);
        pause.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        pauseActivity();
                    }
                }
        );
        resume.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        unPauseActivity();
                    }
                }
        );
        resume.setClickable(false);
        stop.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        mountain.distanceTravelled = 0;
                        mountain.averageSpeed = 0;
                        mountain.lat.clear();
                        mountain.lng.clear();
                        finish();
                    }
                }
        );
        stop.setClickable(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String s;
        getResources();
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
               createLocationRequest();

        } else {
            Toast.makeText(this, "Location is needed", Toast.LENGTH_LONG).show();
        }
        Color.argb(50,0,100,255);
        mMap.addMarker(new MarkerOptions().position(summit)
                .title("Summit of " + mountain.name)
                .snippet(Integer.toString(mountain.height) + "ft"));
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(summit)
                .radius(50)
                .strokeColor(Color.argb(50,0,0,255))
                .fillColor(Color.argb(50,0,100,255)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(summit, 10));
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        path = mMap.addPolyline(new PolylineOptions().color(COLOR_BLUE_ARGB).width(20));
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, intervalPendingIntent);
    }

    private void onLocationChange(Location l) {
        float[] result = new float[1];
        testTimer++;
        if (lastLocation == null) {
            lastLocation = l;
            updateInfo = true;
            startTime = l.getTime();
        }
        if (l.distanceTo(lastLocation) > lastLocation.getAccuracy() /*&& l.distanceTo(lastLocation) < 500*/ || updateInfo) {
            LatLng a = new LatLng(l.getLatitude(), l.getLongitude());
            points.add(a);
            path.setPoints(points);
            drawPolyline = true;
            averageTotal = averageTotal + (l.getSpeed() / .44704f);


            Location.distanceBetween(l.getLatitude(), l.getLongitude(), summit.latitude, summit.longitude, result);
            distanceSummit = meters2Miles(result[0]);
            distanceSummit = (double)Math.round(distanceSummit * 100d) / 100d;
            distanceLeft.setText("You have " +Double.toString(distanceSummit) + " miles to go");

            if (l.getAltitude() != 0) {
                currentElevation.setText("Elevation " + (double) Math.round((meters2Feet(l.getAltitude())) * 100d) / 100d + "ft");
            } else {
                currentElevation.setText("No Elevation Data Available");
            }

            totalDistance = totalDistance + (meters2Miles(lastLocation.distanceTo(l)));
            distanceTravelled.setText("You have  gone " + Math.round(totalDistance * 100d) / 100d + " miles");

            speed = totalDistance/miliseconds2hours(l.getTime() - startTime);
            if (totalDistance == 0 || l.getTime() - startTime == 0) {
                averageSpeed.setText("Average 0.0 mph");
            }
            else {
                averageSpeed.setText("Average " + Math.round(speed * 100d) / 100d + " mph");
            }

            if (distanceSummit < .2 && .2 > (l.getAccuracy() / 1609.344f)) {
                reachedTop();
            }
            numberofValues++;
            lastLocation = l;
            updateInfo = false;
        }
    }

    public double meters2Feet(double meters) {
        return meters * 3.2808;
    }

    public double meters2Miles(double meters) {
        return meters / 1609.344;
    }

    public double miliseconds2hours(double time) {
        return time * 2.7777778 *Math.pow(10, -7);
    }



    public void reachedTop() {
        checkIn.setVisibility(View.VISIBLE);
        checkIn.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        updateMtnCalculations();
                        mountain.setCompleted(true);
                        Utils.saveMountains(getApplicationContext(), mountainList);
                        activityChanger(HikeOverview.class);
                    }
                }
        );
    }

    private void activityChanger(Class c) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("mountain:///"+mountain.id), this, HikeOverview.class);
        startActivity(i);
    }

    public void pauseActivity() {
        stopLocationUpdates();
        pause.setClickable(false);
        pause.setVisibility(View.INVISIBLE);
        resume.setClickable(true);
        resume.setVisibility(View.VISIBLE);
        stop.setClickable(true);
        stop.setVisibility(View.VISIBLE);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        updateMtnCalculations();
        Utils.saveMountains(getApplicationContext(), mountainList);
    }

    public void unPauseActivity() {
        startLocationUpdates();
        pause.setClickable(true);
        pause.setVisibility(View.VISIBLE);
        resume.setClickable(false);
        resume.setVisibility(View.INVISIBLE);
        stop.setClickable(false);
        stop.setVisibility(View.INVISIBLE);
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("points", points);
        state.putDouble("averageTotal", averageTotal);
        state.putDouble("totalDistance", totalDistance);
        state.putInt("numberOfValues", numberofValues);
        state.putInt("test", testTimer);
        state.putDouble("altitude", altitude);
        state.putDouble("distanceSummit", distanceSummit);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        points = (ArrayList<LatLng>) savedInstanceState.getSerializable("points");
        averageTotal = savedInstanceState.getDouble("averageTotal");
        numberofValues = savedInstanceState.getInt("numberOfValues");
        totalDistance = savedInstanceState.getDouble("totalDistance");
        distanceSummit = savedInstanceState.getDouble("distancSummit");
        altitude = savedInstanceState.getDouble("altitude");
        testTimer = savedInstanceState.getInt("test");
        if (Double.isNaN(averageTotal / numberofValues)) {
            averageSpeed.setText("Average 0.0 mph");
        }else {
            averageSpeed.setText("Average " + (averageTotal / numberofValues) + " mph");
        }
        distanceTravelled.setText("You have  gone " + totalDistance + "miles");
        distanceLeft.setText(Double.toString(distanceSummit));
        currentElevation.setText("Elevation " + (double)Math.round((altitude * 3.2808d)* 100d) / 100d + "ft");
    }

    private void updateMtnCalculations() {
        for (LatLng p: points) {
            mountain.lat.add(p.latitude);
            mountain.lng.add(p.longitude);
        }
        mountain.averageSpeed = speed;
        mountain.distanceTravelled = totalDistance;
    }

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }

}
