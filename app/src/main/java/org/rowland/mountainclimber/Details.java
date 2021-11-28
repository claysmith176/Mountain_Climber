package org.rowland.mountainClimber;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

public class Details extends AppCompatActivity {
    int position;
    Mountain m;
    Map<Integer,Mountain> mountainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        mountainList = Utils.loadMountains(getApplicationContext());
        Intent i = getIntent();
        String mountainId = i.getData().getPath().substring(1);
        m = mountainList.get(Integer.parseInt(mountainId));
        getSupportActionBar().setTitle(m.name);
        widgetInit();
    }

    private void widgetInit() {
        TextView elevation = (TextView) findViewById(R.id.Elavation);
        elevation.setText((m.height) + "ft");
        TextView rank = (TextView) findViewById(R.id.Rank);
        rank.setText(Utils.getRankString(m.id));

        Button b = (Button) findViewById(R.id.startClimb);
        if (m.completed) {
            b.setText("View Hike");
        }
        b.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        activityChange(v);
                    }
                }
        );
    }

    private void activityChange(View v) {
        if (m.completed) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("mountain:///"+m.id), this, HikeOverview.class);
            //Intent i = new Intent(this, HikeOverview.class);
            //i.putExtra("mountain", m);
            startActivity(i);
        }
        else {
            //Intent i = new Intent(this, HikeTracker.class);
            //i.putExtra("mountain", m);
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("mountain:///"+m.id), this, HikeTracker.class);
            startActivity(i);
        }
    }
}
