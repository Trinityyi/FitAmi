package com.trinity.isabelle.fitami.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.trinity.isabelle.fitami.Fragments.HomeFragment;
import com.trinity.isabelle.fitami.Fragments.LeaderboardFragment;
import com.trinity.isabelle.fitami.Fragments.ProfileFragment;
import com.trinity.isabelle.fitami.Fragments.TrophyFragment;
import com.trinity.isabelle.fitami.Other.DataFragment;
import com.trinity.isabelle.fitami.Other.FitamiBackgroundService;
import com.trinity.isabelle.fitami.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private long lastTime,lastSteps,lastMeters;
    private String nickname,email;

    private NavigationView navigationView;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private View navHeader;
    private TextView txtName, txtEmail;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_TROPHY = "trophy";
    private static final String TAG_LEADERBOARD = "leaderboard";
    public static String CURRENT_TAG = TAG_HOME;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    // Broadcast Receiver for getting messages from background service
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String messageReceived = intent.getStringExtra(String.valueOf(R.string.intent_service_string_extra));
            Log.d("Received ", messageReceived);
            // If the broadcast was about the GPS not being enabled, deal with it
            if(Objects.equals(messageReceived, "The GPS is not enabled!"))  {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 7431);
            }
            else {
                SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
                float lastLatitude = sharedPref.getFloat(getString(R.string.preference_latitude_key), 0.0f);
                float lastLongitude = sharedPref.getFloat(getString(R.string.preference_longitude_key), 0.0f);
                lastTime = sharedPref.getLong(getString(R.string.preference_time_key), 0l);
                lastSteps = sharedPref.getLong(getString(R.string.preference_step_key), 0l);
                lastMeters = sharedPref.getLong(getString(R.string.preference_meter_key), 0l);
                nickname = sharedPref.getString(getString(R.string.preference_nickname_key), "...");
                //getHomeFragment().setFragmentData();
                if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
                    ((DataFragment)getSupportFragmentManager().findFragmentByTag(CURRENT_TAG)).setFragmentData();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Layout stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        //Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtEmail = (TextView) navHeader.findViewById(R.id.email);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        // Register broadcast receiver for messages from the background service
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(String.valueOf(MainActivity.class)));

        // Start the background service
        Intent backgroundService = new Intent(this, FitamiBackgroundService.class);
        startService(backgroundService);

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }

    }

    /***
     * Load navigation menu header information
     */
    private void loadNavHeader() {
        // name, email
        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        nickname = sharedPref.getString(getString(R.string.preference_nickname_key), "Fitami User");
        String email = sharedPref.getString(getString(R.string.preference_email_key), "user@mail.com");
        txtName.setText(nickname);
        txtEmail.setText(email);
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }


        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                // profile fragment
                ProfileFragment profileFragment = new ProfileFragment();
                return profileFragment;
            case 2:
                // trophy room fragment
                TrophyFragment trophyFragment = new TrophyFragment();
                return trophyFragment;
            case 3:
                // leaderboard fragment
                LeaderboardFragment leaderboardFragment = new LeaderboardFragment();
                return leaderboardFragment;
            default:
                return new HomeFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }
    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with selected fragment;
                    case R.id.nav_home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_profile:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_PROFILE;
                        break;
                    case R.id.nav_trophy:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_TROPHY;
                        break;
                    case R.id.nav_leaderboard:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_LEADERBOARD;
                        break;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer,toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }


    // Deal with permissions for GPS being given or not
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 7431:
                if (! (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}, 7431);
                }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_home) {
            // Handle the profile action
        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_trophy) {

        } else if (id == R.id.nav_leaderboard) {

        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
