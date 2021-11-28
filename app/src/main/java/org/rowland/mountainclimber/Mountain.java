package org.rowland.mountainClimber;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Mountain implements Serializable {
    String name;
    int id;
    int height;
    int position;
    Double Lat;
    Double Long;
    boolean completed = false;
    List<Double> lat = new ArrayList<>(10);
    List<Double> lng = new ArrayList<>(10);
    double distanceTravelled = 5;
    double averageSpeed =5;

    Mountain(JSONObject line) {
        try {
            name = line.getString("name");
            id = line.getInt("id");
            height = line.getInt("elevation");
            Lat = line.getDouble("lon");
            Long = line.getDouble("lat");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Mountain(String line) {
        String[] values = line.split(",");
        name = values[0];
        id = Integer.valueOf(values[1]);
        height = Integer.valueOf(values[2]);
        Lat = Double.valueOf(values[3]);
        Long = Double.valueOf(values[4]);
        completed = false;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}















