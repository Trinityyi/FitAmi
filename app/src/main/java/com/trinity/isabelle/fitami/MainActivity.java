package com.trinity.isabelle.fitami;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Used for message handling.
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
                float lastLatitude = sharedPref.getFloat(getString(R.string.preference_latitude_key), 0.0f);
                float lastLongitude = sharedPref.getFloat(getString(R.string.preference_longitude_key), 0.0f);
                long lastTime = sharedPref.getLong(getString(R.string.preference_time_key), 0l);
                long  lastSteps = sharedPref.getLong(getString(R.string.preference_step_key), 0l);
                long lastMeters = sharedPref.getLong(getString(R.string.preference_meter_key), 0l);
                // TODO: Maybe we should turn this into a string resource.
                Log.d("Received ", intent.getStringExtra("com.trinity.isabelle.fitami.backgroundservice"));
                TextView logger = (TextView) findViewById(R.id.logger);
                logger.setText("Location: "+lastLatitude+" , "+lastLongitude+" - Time: "+lastTime+" - Steps: "+lastSteps+" - Meters: "+lastMeters);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(String.valueOf(MainActivity.class)));

        final SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        final String userId = sharedPref.getString(getString(R.string.preference_uid_key), "00000");
        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();
        sharedPref.edit().putString(getString(R.string.preference_date_key), getToday()).apply();

        final ValueEventListener userScoreListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(Objects.equals(String.valueOf(sharedPref.getString(getString(R.string.preference_points_key), "undefined")), "undefined")){
                    rootRef.child("days").child(getToday()).child(userId).child("points").setValue(String.valueOf(dataSnapshot.child("score").getValue(Integer.class)));
                    sharedPref.edit().putString(getString(R.string.preference_points_key), String.valueOf(dataSnapshot.child("score").getValue(Integer.class))).apply();
                }
                rootRef.child("users").child(userId).removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                                // ...
            }
        };


        final ValueEventListener usernameListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {

                    sharedPref.edit().putString(getString(R.string.preference_nickname_key), String.valueOf(dataSnapshot.child(userId).child("nickname").getValue(String.class))).apply();
                    sharedPref.edit().putString(getString(R.string.preference_points_key), String.valueOf(dataSnapshot.child(userId).child("points").getValue(String.class))).apply();
                    sharedPref.edit().putLong(getString(R.string.preference_time_key), Long.valueOf(dataSnapshot.child(userId).child("activeTime").getValue(Long.class))).apply();
                    sharedPref.edit().putLong(getString(R.string.preference_step_key), Long.valueOf(dataSnapshot.child(userId).child("steps").getValue(Long.class))).apply();
                    sharedPref.edit().putLong(getString(R.string.preference_meter_key), Long.valueOf(dataSnapshot.child(userId).child("distance").getValue(Long.class))).apply();

                    // Only kill the event listener here, as this event will fire even if the data does
                    // not exist (right after it's added by the else statement below).
                    rootRef.child("days").child(getToday()).removeEventListener(this);
                }
                else{
                    rootRef.child("days").child(getToday()).child(userId).child("nickname").setValue("todo");
                    rootRef.child("days").child(getToday()).child(userId).child("points").setValue("undefined");
                    rootRef.child("days").child(getToday()).child(userId).child("activeTime").setValue(0);
                    rootRef.child("days").child(getToday()).child(userId).child("steps").setValue(0);
                    rootRef.child("days").child(getToday()).child(userId).child("distance").setValue(0);
                    // TODO: Initialize shared prefs for the day
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
//                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        rootRef.child("users").child(userId).addValueEventListener(userScoreListener);
        rootRef.child("days").child(getToday()).addValueEventListener(usernameListener);

        // Write the last updated time as "-1" in the shared preferences, so that the service
        // knows this is a cold start.

//        sharedPref.edit().putInt(getString(R.string.preference_time_key), -1).apply();
        // TODO: Open service, it will know the existing data
        // Start the service and let it do its thing
        Intent backgroundService = new Intent(this, FitamiBackgroundService.class);
        startService(backgroundService);
    }

    public String getToday(){
        return new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
