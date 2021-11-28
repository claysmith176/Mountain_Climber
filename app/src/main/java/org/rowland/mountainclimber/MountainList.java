package org.rowland.mountainClimber;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MountainList extends AppCompatActivity {
    List<Button> mountainButtons = new ArrayList<>(100);
    LinearLayout buttonList;
    EditText search;
    Map<Integer,Mountain> mountainMap;
    ImageView img;
    TextView percentNumber;
    ProgressBar progressBar;
    static final int MY_PERMISSIONS_REQUEST_Location = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mountain_list);
        buttonList = (LinearLayout) findViewById(R.id.buttonList);
        img = (ImageView) findViewById(R.id.imageView);
        img.setImageResource(R.drawable.mountainpicture);
        search = (EditText) findViewById(R.id.search);

        checkPermissions();
        mountainMap = Utils.loadMountains(getApplicationContext());

        mountainButtons.clear();
        for (Mountain m: mountainMap.values()) {
            buttonCreator(m);
        }

        playServices();
        otherWidgetes();
    }

    @Override
    protected void onDestroy() {
       super.onDestroy();
       mountainMap.clear();
       mountainButtons.clear();
    }

    public void buttonCreator (Mountain m) {
        Button b = new Button(this);
        mountainButtons.add(b);
        buttonList.addView(b);
        if (m.isCompleted()) {
            b.setBackground(getDrawable(R.drawable.complete_button));
        }
        else {
            b.setBackground(getDrawable(R.drawable.list_buttons));
        }
        b.setTextColor(Color.WHITE);

        b.setId(m.id);
        b.setText(m.name);

        b.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        mountainClicked(v);
                    }
                }
                );
    }
    public void otherWidgetes() {
        Button clearList = new Button(this);
        buttonList.addView(clearList);
        clearList.setBackground(getDrawable(R.drawable.list_buttons));
        clearList.setTextColor(Color.WHITE);
        clearList.setText("clear");
        clearList.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Utils.deleteMountainFile(getApplicationContext());
                        Toast.makeText(MountainList.this, "File Deleted", Toast.LENGTH_LONG).show();
                    }
                }
        );
        int totalClimbed = 0;
        for (Mountain m : mountainMap.values()) {
            if (m.isCompleted()) {
                totalClimbed++;
            }
        }
        int percent = (int) ((double)totalClimbed/(double)mountainMap.size() * 100);
        percentNumber = (TextView) findViewById(R.id.percentNumber);
        percentNumber.setText(Integer.toString(percent) + "%");
        percentNumber.setTextColor(Color.WHITE);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setProgress(percent);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(search.getText());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void mountainClicked(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("mountain:///"+v.getId()), this, Details.class);
        startActivity(i);
    }

    public void playServices() {
        GoogleApiAvailability a = GoogleApiAvailability.getInstance();
        int i = a.isGooglePlayServicesAvailable(this);
        if (i != ConnectionResult.SUCCESS) {
            if (a.isUserResolvableError(i)) {
                Dialog d = a.getErrorDialog(this, i, 0);
                d.show();
            } else {
                Toast.makeText(this, "Play Sercives dont work", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void search(CharSequence s) {
        int counter = 0;
        String[] names = new String[mountainMap.size()];
        buttonList.removeAllViews();
        for (Mountain m : mountainMap.values()) {
            if (m.name.toLowerCase().startsWith(s.toString().toLowerCase())) {
                buttonList.addView(mountainButtons.get(counter));
            }
            counter++;
        }
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(MountainList.this, Manifest.permission_group.LOCATION) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(MountainList.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_Location);
        }
        if (ContextCompat.checkSelfPermission(MountainList.this, Manifest.permission_group.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MountainList.this,
                    new String[]{Manifest.permission_group.CAMERA},
                    MY_PERMISSIONS_REQUEST_Location);
        }
        if (ContextCompat.checkSelfPermission(MountainList.this, Manifest.permission_group.STORAGE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MountainList.this,
                    new String[]{Manifest.permission_group.STORAGE},
                    MY_PERMISSIONS_REQUEST_Location);

        }
    }
}