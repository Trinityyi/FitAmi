/*
TODO:
    - It will start with today's data, no questions asked
    - Calculate data in methods
    - Write to firebase whenever




 */



//package com.trinity.isabelle.fitami;
//
//import android.app.IntentService;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.location.Geocoder;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//// These two cannot be resolved, why?
////import com.google.android.gms.location.ActivityRecognitionResult;
////import com.google.android.gms.location.DetectedActivity;
//
//import java.io.IOException;
//import java.util.Locale;
//import java.util.Objects;
//
///**
// * An {@link IntentService} subclass for handling asynchronous task requests in
// * a service on a separate handler thread.
// * <p>
// * TODO: Customize class - update intent actions, extra parameters and static
// * helper methods.
// */
//public class FitamiBackgroundService extends IntentService implements SensorEventListener, LocationListener {
//
//    private Handler handler = new Handler();
//    private final long SENSOR_UPDATE_LATENCY = 15000; // Update frequency should be 15s
//    private final long FIREBASE_UPDATE_LATENCY = 300000; // Firebase update frequency should be 300s
//    private int dailyTime;
//    private int dailySteps;
//    private int dailyMeters;
//    private double lastLatitude;
//    private double lastLongitude;
//    private long lastStepCounterNanoTime;
//
//
//    Runnable sensorUpdate = new Runnable() {
//        @Override
//        public void run() {
//            // TODO: Use the stuff from here: https://developers.google.com/android/reference/com/google/android/gms/location/DetectedActivity
//            // to detect activity and store in lastDetectedActivity.
//            // DetectedActivity detectedActivity;
//            Log.d("Sensors", "running");
//            SharedPreferences sharedPref = FitamiBackgroundService.this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
//            sharedPref.edit().putLong(getString(R.string.preference_time_key), lastTime).apply();
//            sharedPref.edit().putFloat(getString(R.string.preference_latitude_key), (float) lastLatitude).apply();
//            sharedPref.edit().putFloat(getString(R.string.preference_longitude_key), (float) lastLongitude).apply();
//            sharedPref.edit().putLong(getString(R.string.preference_step_key), lastSteps).apply();
//            sharedPref.edit().putInt(getString(R.string.preference_activity_key), lastDetectedActivity).apply();
//
//            // Message mainActivity
//            Intent intent = new Intent(String.valueOf(MainActivity.class));
//            intent.putExtra("com.trinity.isabelle.fitami.backgroundservice", "Data has been updated!");
//            LocalBroadcastManager.getInstance(FitamiBackgroundService.this).sendBroadcast(intent);
//            // Repeat
//            handler.postDelayed(this, SENSOR_UPDATE_LATENCY);
//        }
//    };
//
//    Runnable firebaseUpdate = new Runnable() {
//        @Override
//        public void run() {
//            // TODO: Log all sensor data to Firebase, based on the predefined patterns.
//            Log.d("Firebase", "data");
//            // Repeat
//            handler.postDelayed(this, FIREBASE_UPDATE_LATENCY);
//        }
//    };
//
//
//    // Constructor
//    public FitamiBackgroundService() {
//        super("FitamiBackgroundService");
//    }
//
//    // Service startup
//    @Override
//    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
//        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
//        long time = sharedPref.getLong(getString(R.string.preference_time_key), -1);
//        if (time == -1) {
//            lastTime = System.currentTimeMillis();
//            lastSteps = 0;
//            lastLatitude = 0.0;
//            lastLongitude = 0.0;
//            lastDetectedActivity = 4;
//        } else {
//            lastTime = time;
//            lastSteps = sharedPref.getLong(getString(R.string.preference_step_key), 0);
//            lastLatitude = sharedPref.getFloat(getString(R.string.preference_latitude_key), 0.0f);
//            lastLongitude = sharedPref.getFloat(getString(R.string.preference_longitude_key), 0.0f);
//            lastDetectedActivity = sharedPref.getInt(getString(R.string.preference_activity_key), 4);
//        }
//        // TODO: Check if GPS is enabled and, if not, inform in main screen before starting this service.
//        // Following code assumes permissions are given and the GPS is on.
//
//        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        Sensor stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//        lastStepCounterNanoTime = System.nanoTime();
//        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
//
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        // Generated code to suppress errors
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            // return TODO;
//        }
//        else {
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500, 0, this);
//        }
//
//        sensorUpdate.run();
//        firebaseUpdate.run();
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        // Time is in nanoseconds, thus 10000000000l (that's a lowercase L) is 10s.
//        if (Objects.equals(event.sensor.getStringType(), Sensor.STRING_TYPE_STEP_COUNTER)
//                && event.timestamp - lastStepCounterNanoTime >= 10000000000l ) {
//            Log.d("Steps", String.valueOf(event.values[0]));
//            lastSteps += event.values[0];
//            lastStepCounterNanoTime = event.timestamp;
//        }
//        // Handle other sensors here, if we use any more.
//    }
//
//
//
//    @Override
//    public void onLocationChanged(Location location) {
//        double latitude = location.getLatitude();
//        double longitude = location.getLongitude();
//        long distance = calculateDistance(latitude, longitude, lastLatitude, lastLongitude);
//        float speed = distance/ (System.currentTimeMillis() - lastTime); // Is this meter/milliseconds? Probably, right?
//        // TODO: Possibly save the topSpeed of every run or something, I don't know.
//        Log.d("Speed", String.valueOf(speed));
//        lastLatitude = latitude;
//        lastLongitude = longitude;
//        lastTime = System.currentTimeMillis();
//        Geocoder g = new Geocoder(this, Locale.ENGLISH);
//        try {
//            // This does not work as intended, probably an emulator problem from what I've found.
//            String locName = g.getFromLocation(latitude,longitude, 1).get(0).getFeatureName();
//            Log.d("Location", locName);
//        } catch (IOException e) {
//            //e.printStackTrace();  - TODO: Figure out why the Geocoder crashes and how to fix it.
//        }
//    }
//
//    // Helper function off of the internet gets the distance between two points. Very mathy...
//    private static long calculateDistance(double lat1, double lng1, double lat2, double lng2) {
//        double dLat = Math.toRadians(lat2 - lat1);
//        double dLon = Math.toRadians(lng2 - lng1);
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
//                + Math.cos(Math.toRadians(lat1))
//                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
//                * Math.sin(dLon / 2);
//        double c = 2 * Math.asin(Math.sqrt(a));
//        long distanceInMeters = Math.round(6371000 * c);
//        return distanceInMeters;
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//    }
//
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
//}
//
