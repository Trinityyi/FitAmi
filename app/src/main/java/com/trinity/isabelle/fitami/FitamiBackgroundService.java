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
import java.util.Random;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class FitamiBackgroundService extends IntentService implements SensorEventListener, LocationListener {

    private Handler handler = new Handler();
    private final long SENSOR_UPDATE_LATENCY = 15000; // Update frequency should be 15s
    private final long FIREBASE_UPDATE_LATENCY = 300000; // Firebase update frequency should be 300s
    private long dailyTime, dailySteps, dailyMeters, currentMeters, lastStepCounterNanoTime, lastStepCounterValue, lastScore;
    private double lastLatitude, lastLongitude;
    private String nickname, userId, currentDate;
    private int dailyMedal;
    private boolean lastStepIndicator;
    private DatabaseReference rootRef;
    private SharedPreferences sharedPref;

    // Update sensor data in preferences
    Runnable sensorUpdate = new Runnable() {
        @Override
        public void run() {
            if(currentMeters >= 10){
                dailyMeters += currentMeters;
                dailyTime += 15;
                if (lastStepIndicator == false) {
                    dailySteps += Math.round(currentMeters * 1.3123359580052);
                }
            }
            updatePreferenceFloat(R.string.preference_latitude_key, (float) lastLatitude);
            updatePreferenceFloat(R.string.preference_longitude_key, (float) lastLongitude);
            currentMeters = 0;
            updatePreferenceLong(R.string.preference_time_key, dailyTime);
            updatePreferenceLong(R.string.preference_step_key, dailySteps);
            updatePreferenceLong(R.string.preference_meter_key, dailyMeters);
            checkForMedals();

            // Message mainActivity - TODO: Encapsulate as a method
            Intent intent = new Intent(String.valueOf(MainActivity.class));
            intent.putExtra(String.valueOf(R.string.intent_service_string_extra), "Data has been updated!");
            LocalBroadcastManager.getInstance(FitamiBackgroundService.this).sendBroadcast(intent);
            // ----------------------------------------------------

            handler.postDelayed(this, SENSOR_UPDATE_LATENCY); // Repeat
        }
    };

    // Write data to Firebase
    Runnable firebaseUpdate = new Runnable() {
        @Override
        public void run() {
            updatePreferenceLong(R.string.preference_timestamp_key, getMillis());
            writeDayDataToFirebase();
            handler.postDelayed(this, FIREBASE_UPDATE_LATENCY); // Repeat
        }
    };

    // Initialize a new day
    Runnable dateUpdate = new Runnable() {
        @Override
        public void run() {
            writePreviousDayDataToFirebase();
            initializeNewDay();
            handler.postDelayed(this, getMillisUntilTomorrow());    // Set this to run in a day from now
        }
    };

    // Read users/user data from Firebase
    final ValueEventListener userListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            nickname = String.valueOf(dataSnapshot.child("nickname").getValue(String.class));
            lastScore = Long.valueOf(dataSnapshot.child("score").getValue(Long.class));
            updatePreferenceString(R.string.preference_nickname_key, nickname);
            updatePreferenceLong(R.string.preference_points_key, lastScore);
            for(int i = 0; i<24; i++){
                updatePreferenceIndexedLong(R.string.preference_medal_key, i, Long.valueOf(dataSnapshot.child("medals/"+i).getValue(Long.class)));
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("User Listener", "Something went horribly wrong!");
        }
    };

    // Read or write data for days/day/user from/to Firebase
    final ValueEventListener dayUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(Long.valueOf(dataSnapshot.child("timestamp").getValue(Long.class)) >= retrievePreferenceLong(R.string.preference_timestamp_key, 0l)){
                dailyTime = Long.valueOf(dataSnapshot.child("activeTime").getValue(Long.class));
                dailyMeters = Long.valueOf(dataSnapshot.child("distance").getValue(Long.class));
                dailySteps = Long.valueOf(dataSnapshot.child("steps").getValue(Long.class));
                updatePreferenceLong(R.string.preference_time_key, dailyTime);
                updatePreferenceLong(R.string.preference_meter_key, dailyMeters);
                updatePreferenceLong(R.string.preference_step_key, dailySteps);
                updatePreferenceLong(R.string.preference_timestamp_key, Long.valueOf(dataSnapshot.child("timestamp").getValue(Long.class)));
            }
            else {
                writeDayDataToFirebase();
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("Day User Listener", "Something went horribly wrong!");
        }
    };

    final ValueEventListener dayUserInitialListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(Long.valueOf(dataSnapshot.child("timestamp").getValue(Long.class)) >= retrievePreferenceLong(R.string.preference_timestamp_key, 0l)){
                dailyTime = Long.valueOf(dataSnapshot.child("activeTime").getValue(Long.class));
                dailyMeters = Long.valueOf(dataSnapshot.child("distance").getValue(Long.class));
                dailySteps = Long.valueOf(dataSnapshot.child("steps").getValue(Long.class));
                updatePreferenceLong(R.string.preference_time_key, dailyTime);
                updatePreferenceLong(R.string.preference_meter_key, dailyMeters);
                updatePreferenceLong(R.string.preference_step_key, dailySteps);
                updatePreferenceLong(R.string.preference_timestamp_key, Long.valueOf(dataSnapshot.child("timestamp").getValue(Long.class)));
            }
            else {
                writeDayDataToFirebase();
            }
            // Start running repeating tasks
            firebaseUpdate.run();
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("Day User Listener", "Something went horribly wrong!");
        }
    };


    // Constructor
    public FitamiBackgroundService() {
        super("FitamiBackgroundService");
    }

    // Service startup
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d("Random", ""+dailyMedal);
        // Initialize shared preferences and firebase variables
        sharedPref = this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        catch (Exception e){
            Log.e("Firebase persistence", "Cannot set persistence");
        }
        rootRef = FirebaseDatabase.getInstance().getReference();
        userId = retrievePreferenceString(R.string.preference_uid_key, "00000");                    // This is known
        nickname = retrievePreferenceString(R.string.preference_nickname_key, "undefined");         // This is known
        lastScore = retrievePreferenceLong(R.string.preference_points_key, 0l);                   // This is known
        // Note: If, for some reason, the data above is wrong or not up-to-date, when the data is read from Firebase, it will be updated
        currentDate = getCurrentDate();
        // Check if currentDate is different from the date stored in preferences and possibly write to Firebase for the previous one
        if(!Objects.equals(currentDate, retrievePreferenceString(R.string.preference_date_key, "19700101"))
                && !Objects.equals(retrievePreferenceString(R.string.preference_date_key, "19700101"), "19700101")){
            writePreviousDayDataToFirebase();
            // After writing to Firebase, initialize the new date data and write a new entry for the date in Firebase
            initializeNewDay();
        }
        // Register listeners that will get data from Firebase
        rootRef.child("days/" + currentDate + "/" + userId).addListenerForSingleValueEvent(dayUserInitialListener);
        rootRef.child("users/" + userId).addValueEventListener(userListener);
        rootRef.child("days/" + currentDate + "/" + userId).addValueEventListener(dayUserListener);
        // TODO: Add a listener for the day and all users to get leaderboards
        // Initialize step counter sensor
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        lastStepCounterNanoTime = System.nanoTime();
        lastStepCounterValue = -1;
        lastStepIndicator = false;
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        // Initialize GPS sensor
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        lastLatitude = 0.0f;
        lastLongitude = 0.0f;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Handle errors from GPS not being enabled - prompt user to enable by messaging MainActivity
            Log.e("GPS Error", "The GPS is not enabled!");
            Intent mainIntent = new Intent(String.valueOf(MainActivity.class));
            mainIntent.putExtra(String.valueOf(R.string.intent_service_string_extra), "The GPS is not enabled!");
            LocalBroadcastManager.getInstance(FitamiBackgroundService.this).sendBroadcast(mainIntent);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500, 0, this);
        }
        dailyMedal = retrievePreferenceInt(R.string.preference_daily_challenge_key, 0);
        // Start running repeating tasks
        sensorUpdate.run();
        // Set date update task to run when the date changes
        final Handler dateHandler = new Handler();
        dateHandler.postDelayed(dateUpdate, getMillisUntilTomorrow());
        // Call super.onStartCommand()
        return super.onStartCommand(intent, flags, startId);
    }

    // Step counter handling (time is in nanoseconds)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(lastStepCounterValue == -1)  lastStepCounterValue = (long) event.values[0];
        if (Objects.equals(event.sensor.getStringType(), Sensor.STRING_TYPE_STEP_COUNTER)
                && event.timestamp - lastStepCounterNanoTime >= SENSOR_UPDATE_LATENCY * 1000000l ) {
            dailySteps += ((long) event.values[0] - lastStepCounterValue);
            lastStepCounterNanoTime = event.timestamp;
            lastStepCounterValue = (long) event.values[0];
            lastStepIndicator = true;
        }
        // Handle other sensors here, if we use any more.
    }

    // GPS handling
    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if(!(lastLongitude == 0.0) && !(lastLatitude == 0.0))
            currentMeters = getDistance(latitude, longitude, lastLatitude, lastLongitude);
        lastLatitude = latitude;
        lastLongitude = longitude;
    }

    // Calculate distance in meters between two locations
    private long getDistance(double lat1, double lng1, double lat2, double lng2) {
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

    // Get milliseconds until next day at 00:00
    private long getMillisUntilTomorrow(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long howMany = (c.getTimeInMillis()-System.currentTimeMillis());
        return howMany;
    }

    // Get the textual representation of the current date
    public String getCurrentDate(){
        return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    }

    // Get current time
    public long getMillis(){
        return System.currentTimeMillis();
    }

    // Check if user has gotten any medals
    public void checkForMedals(){
        // Check steps (medals 0,1,2,3)
        checkMedal(dailySteps, 5000, 0, 5);
        checkMedal(dailySteps, 10000, 1, 10);
        checkMedal(dailySteps, 20000, 2, 15);
        checkMedal(dailySteps, 25000, 3, 20);
        // Check meters medals (4,5,6,7)
        checkMedal(dailyMeters, 5000, 4, 5);
        checkMedal(dailyMeters, 10000, 5, 10);
        checkMedal(dailyMeters, 20000, 6, 15);
        checkMedal(dailyMeters, 25000, 7, 20);
        // Check time medals (8,9,10,11)
        checkMedal(dailyTime, 1800, 8, 5);
        checkMedal(dailyTime, 3600, 9, 10);
        checkMedal(dailyTime, 5400, 10, 15);
        checkMedal(dailyTime, 7200, 11, 20);
    }

    // Check if user has gotten a medal and update preferences and Firebase
    public void checkMedal(long measurement, long threshold, int index, long pointValue){
        if(measurement >= threshold
                && retrievePreferenceIndexedLong(R.string.preference_daily_medal_key, index, 0l) < 1
                && retrievePreferenceIndexedLong(R.string.preference_daily_medal_key, index + 12, 0l) < 1){
            // Update medal (not daily challenge) and score
            if(dailyMedal != index) {
                updatePreferenceIndexedLong(R.string.preference_daily_medal_key, index, 1);
                updatePreferenceIndexedLong(R.string.preference_medal_key, index, retrievePreferenceIndexedLong(R.string.preference_medal_key, index, 0l) + 1);
                rootRef.child("users/" + userId + "/medals/" + index).setValue(retrievePreferenceIndexedLong(R.string.preference_medal_key, index, 0l));
                updatePreferenceLong(R.string.preference_points_key, retrievePreferenceLong(R.string.preference_points_key, lastScore) + pointValue);
                rootRef.child("users/" + userId + "/score").setValue(retrievePreferenceLong(R.string.preference_points_key, lastScore));
            }
            // Update medal (daily challenge) and score
            else {
                updatePreferenceIndexedLong(R.string.preference_daily_medal_key, index + 12, 1);
                updatePreferenceIndexedLong(R.string.preference_medal_key, index + 12, retrievePreferenceIndexedLong(R.string.preference_medal_key, index + 12, 0l) + 1);
                rootRef.child("users/" + userId + "/medals/" + (index + 12)).setValue(retrievePreferenceIndexedLong(R.string.preference_medal_key, index + 12, 0l));
                updatePreferenceLong(R.string.preference_points_key, retrievePreferenceLong(R.string.preference_points_key, lastScore) + 25);
                rootRef.child("users/" + userId + "/score").setValue(retrievePreferenceLong(R.string.preference_points_key, lastScore));
            }
        }
    }

    // Initialize a new day and write it to Firebase
    public void initializeNewDay(){
        currentDate = getCurrentDate();
        lastStepCounterNanoTime = System.nanoTime();
        lastStepCounterValue = -1;
        lastLatitude = 0.0f;
        lastLongitude = 0.0f;
        dailyTime = 0l;
        dailyMeters = 0l;
        dailySteps = 0l;
        rootRef.child("days/" + currentDate + "/" + userId + "/nickname").setValue(nickname);
        rootRef.child("days/" + currentDate + "/" + userId + "/points").setValue(lastScore);
        rootRef.child("days/" + currentDate + "/" + userId + "/timestamp").setValue(getMillis());
        rootRef.child("days/" + currentDate + "/" + userId + "/activeTime").setValue(dailyTime);
        rootRef.child("days/" + currentDate + "/" + userId + "/distance").setValue(dailyMeters);
        rootRef.child("days/" + currentDate + "/" + userId + "/steps").setValue(dailySteps);
        // Register new listener
        rootRef.child("days/" + currentDate + "/" + userId).addValueEventListener(dayUserListener);
        // Set daily challenge, cleanup daily medals
        dailyMedal = new Random().nextInt(12);
        updatePreferenceInt(R.string.preference_daily_challenge_key, dailyMedal);
        for (int i = 0; i<24; i++){
            updatePreferenceIndexedLong(R.string.preference_daily_medal_key, i, 0);
        }
        // TODO: Add a listener for the day and all users to get leaderboards
    }

    // Writes data from a previously stored day to Firebase
    public void writePreviousDayDataToFirebase(){
        String previousDate = retrievePreferenceString(R.string.preference_date_key, "19700101");
        long previousTime = retrievePreferenceLong(R.string.preference_time_key, 0l);
        long previousMeters = retrievePreferenceLong(R.string.preference_meter_key, 0l);
        long previousSteps = retrievePreferenceLong(R.string.preference_step_key, 0l);
        rootRef.child("days/" + previousDate + "/" + userId + "/activeTime").setValue(previousTime);
        rootRef.child("days/" + previousDate + "/" + userId + "/distance").setValue(previousMeters);
        rootRef.child("days/" + previousDate + "/" + userId + "/steps").setValue(previousSteps);
        // Unregister previous listener
        try {
            rootRef.child("days/" + previousDate + "/" + userId).removeEventListener(dayUserListener);
        }
        catch (Exception e) {
            Log.e("Writing previous day", "Something went horribly wrong");
        }
    }

    // Writes the day data to Firebase
    public void writeDayDataToFirebase(){
        rootRef.child("days/" + currentDate + "/" + userId + "/nickname").setValue(nickname);
        rootRef.child("days/" + currentDate + "/" + userId + "/points").setValue(lastScore);
        rootRef.child("days/" + currentDate + "/" + userId + "/timestamp").setValue(getMillis());
        rootRef.child("days/" + currentDate + "/" + userId + "/activeTime").setValue(dailyTime);
        rootRef.child("days/" + currentDate + "/" + userId + "/distance").setValue(dailyMeters);
        rootRef.child("days/" + currentDate + "/" + userId + "/steps").setValue(dailySteps);
    }

    // Helper methods for reading and writing to shared preferences (just for visual clarity in the code)
    public void updatePreferenceInt(int key, int value){
        sharedPref.edit().putInt(getString(key), value).apply();
    }

    public void updatePreferenceFloat(int key, float value){
        sharedPref.edit().putFloat(getString(key), value).apply();
    }

    public void updatePreferenceLong(int key, long value){
        sharedPref.edit().putLong(getString(key), value).apply();
    }

    public void updatePreferenceIndexedLong(int key, int index, long value){
        sharedPref.edit().putLong(getString(key)+index, value).apply();
    }

    public void updatePreferenceString(int key, String value){
        sharedPref.edit().putString(getString(key), value).apply();
    }

    public int retrievePreferenceInt(int key, int defaultValue){
        return sharedPref.getInt(getString(key), defaultValue);
    }

    public float retrievePreferenceFloat(int key, float defaultValue){
        return sharedPref.getFloat(getString(key), defaultValue);
    }

    public long retrievePreferenceLong(int key, long defaultValue){
        return sharedPref.getLong(getString(key), defaultValue);
    }

    public long retrievePreferenceIndexedLong(int key, int index, long defaultValue){
        return sharedPref.getLong(getString(key) + index, defaultValue);
    }

    public String retrievePreferenceString(int key, String defaultValue){
        return sharedPref.getString(getString(key), defaultValue);
    }

    // Misc overrides (required)
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    @Override
    protected void onHandleIntent(Intent intent) { }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onProviderDisabled(String provider) { }
}

