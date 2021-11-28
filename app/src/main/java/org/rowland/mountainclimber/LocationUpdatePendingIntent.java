package org.rowland.mountainClimber;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.Nullable;

public class LocationUpdatePendingIntent extends IntentService {

        public LocationUpdatePendingIntent() {
            super(LocationUpdatePendingIntent.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            Location location = intent.getParcelableExtra("com.google.android.location.LOCATION");
            if(location !=null)
            {
                //our location based code
            }
        }


}
