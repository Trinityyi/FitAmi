/*
TODO:
    - It will start with today's data, no questions asked
    - Calculate data in methods
    - Write to firebase whenever




 */



package com.trinity.isabelle.fitami;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FitamiBackgroundService extends IntentService implements SensorEventListener, LocationListener {

    private Handler handler = new Handler();
    private final long SENSOR_UPDATE_LATENCY = 15000; // Update frequency should be 15s
    private final long FIREBASE_UPDATE_LATENCY = 300000; // Firebase update frequency should be 300s
    private long dailyTime;
    private long dailySteps;
    private long dailyMeters;
    private long currentMeters;
    private double lastLatitude;
    private double lastLongitude;
    private long lastStepCounterNanoTime;
    private long lastStepCounterValue;
    private long lastScore;
    private String nickname;
    private String userId;
    private String currentDate;
    private DatabaseReference rootRef;


    Runnable sensorUpdate = new Runnable() {
        @Override
        public void run() {
            Log.d("Sensors", "running");
            SharedPreferences sharedPref = FitamiBackgroundService.this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
            if(currentMeters >= 10){
                dailyMeters += currentMeters;
                dailyTime += 15;
            }
            sharedPref.edit().putFloat(getString(R.string.preference_latitude_key), (float) lastLatitude).apply();
            sharedPref.edit().putFloat(getString(R.string.preference_longitude_key), (float) lastLongitude).apply();
            currentMeters = 0;
            sharedPref.edit().putLong(getString(R.string.preference_time_key), dailyTime).apply();
            sharedPref.edit().putLong(getString(R.string.preference_step_key), dailySteps).apply();
            sharedPref.edit().putLong(getString(R.string.preference_meter_key), dailyMeters).apply();

            // Message mainActivity
            Intent intent = new Intent(String.valueOf(MainActivity.class));
            intent.putExtra("com.trinity.isabelle.fitami.backgroundservice", "Data has been updated!");
            LocalBroadcastManager.getInstance(FitamiBackgroundService.this).sendBroadcast(intent);
            // Repeat
            handler.postDelayed(this, SENSOR_UPDATE_LATENCY);
        }
    };

    Runnable firebaseUpdate = new Runnable() {
        @Override
        public void run() {
            rootRef.child("days").child(currentDate).child(userId).child("activeTime").setValue(dailyTime);
            rootRef.child("days").child(currentDate).child(userId).child("distance").setValue(dailyMeters);
            rootRef.child("days").child(currentDate).child(userId).child("steps").setValue(dailySteps);
            // Repeat
            handler.postDelayed(this, FIREBASE_UPDATE_LATENCY);
        }
    };

    Runnable dateUpdate = new Runnable() {
        @Override
        public void run() {
            // Write last day's data to Firebase
            rootRef.child("days").child(currentDate).child(userId).child("activeTime").setValue(dailyTime);
            rootRef.child("days").child(currentDate).child(userId).child("distance").setValue(dailyMeters);
            rootRef.child("days").child(currentDate).child(userId).child("steps").setValue(dailySteps);
            // Initialize data for the new day
            dailyTime = 0;
            dailyMeters = 0;
            dailySteps = 0;
            currentDate = getToday();
            lastScore = 0; // TODO: Figure out score calculations
            SharedPreferences sharedPref = FitamiBackgroundService.this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
            sharedPref.edit().putString(getString(R.string.preference_date_key), currentDate).apply();
            sharedPref.edit().putLong(getString(R.string.preference_time_key), dailyTime).apply();
            sharedPref.edit().putLong(getString(R.string.preference_step_key), dailySteps).apply();
            sharedPref.edit().putLong(getString(R.string.preference_meter_key), dailyMeters).apply();
            sharedPref.edit().putString(getString(R.string.preference_points_key), String.valueOf(lastScore)).apply();
            // Write new day to Firebase
            rootRef.child("days").child(getToday()).child(userId).child("nickname").setValue(nickname);
            rootRef.child("days").child(getToday()).child(userId).child("points").setValue(lastScore);
            rootRef.child("days").child(getToday()).child(userId).child("activeTime").setValue(0);
            rootRef.child("days").child(getToday()).child(userId).child("steps").setValue(0);
            rootRef.child("days").child(getToday()).child(userId).child("distance").setValue(0);
            // TODO: Set daily medal / challenge / whatever-we-call-this-stuff
            // Message mainActivity
            Intent intent = new Intent(String.valueOf(MainActivity.class));
            intent.putExtra("com.trinity.isabelle.fitami.backgroundservice", "Data has been updated!");
            LocalBroadcastManager.getInstance(FitamiBackgroundService.this).sendBroadcast(intent);
            // Countdown until next day
            handler.postDelayed(this, getMillisUntilTomorrow());
        }
    };


    // Constructor
    public FitamiBackgroundService() {
        super("FitamiBackgroundService");
    }

    // Service startup
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        lastLatitude = sharedPref.getFloat(getString(R.string.preference_latitude_key), 0.0f);
        lastLongitude = sharedPref.getFloat(getString(R.string.preference_longitude_key), 0.0f);
        dailySteps = sharedPref.getLong(getString(R.string.preference_step_key), 0l);
        dailyTime = sharedPref.getLong(getString(R.string.preference_time_key), 0l);
        dailyMeters = sharedPref.getLong(getString(R.string.preference_meter_key), 0l);
        userId = sharedPref.getString(getString(R.string.preference_uid_key), "00000");
        nickname = sharedPref.getString(getString(R.string.preference_nickname_key), "undefined");
        currentDate = sharedPref.getString(getString(R.string.preference_date_key), "19700101");
        rootRef = FirebaseDatabase.getInstance().getReference();

        // TODO: Check if GPS is enabled and, if not, inform in main screen before starting this service.
        // Following code assumes permissions are given and the GPS is on.

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        lastStepCounterNanoTime = System.nanoTime();
        lastStepCounterValue = -1;
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Generated code to suppress errors
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // return TODO;
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500, 0, this);
        }

        sensorUpdate.run();
        firebaseUpdate.run();
        final Handler dateHandler = new Handler();
        dateHandler.postDelayed(dateUpdate, getMillisUntilTomorrow());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle Step counter
        // Time is in nanoseconds, thus 15000000000l (that's a lowercase L) is 15s.
        if(lastStepCounterValue == -1)
            lastStepCounterValue = (long) event.values[0];
        if (Objects.equals(event.sensor.getStringType(), Sensor.STRING_TYPE_STEP_COUNTER)
                && event.timestamp - lastStepCounterNanoTime >= 15000000000l ) {
            dailySteps += ((long) event.values[0] - lastStepCounterValue);
            lastStepCounterNanoTime = event.timestamp;
            lastStepCounterValue = (long) event.values[0];
        }
        // Handle other sensors here, if we use any more.
    }



    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if(!(lastLongitude == 0.0) && !(lastLatitude == 0.0))
            currentMeters = calculateDistance(latitude, longitude, lastLatitude, lastLongitude);
        lastLatitude = latitude;
        lastLongitude = longitude;
        // Geocoder does not currently work as expected
        /*
            Geocoder g = new Geocoder(this, Locale.ENGLISH);
            try {
                // This does not work as intended, probably an emulator problem from what I've found.
                String locName = g.getFromLocation(latitude,longitude, 1).get(0).getFeatureName();
                Log.d("Location", locName);
            } catch (IOException e) {
                //e.printStackTrace();  - TODO: Figure out why the Geocoder crashes and how to fix it.
            }
        */
    }

    // Helper function off of the internet gets the distance between two points. Very mathy...
    private static long calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        long distanceInMeters = Math.round(6371000 * c);
        return distanceInMeters;
    }

    // Helper function to get milliseconds until midnight
    private static long getMillisUntilTomorrow(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long howMany = (c.getTimeInMillis()-System.currentTimeMillis());
        return howMany;
    }

    // Helper function to get the current day in string format
    public static String getToday(){
        return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

