package com.trinity.isabelle.fitami.Other;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.trinity.isabelle.fitami.Activities.MainActivity;
import com.trinity.isabelle.fitami.Activities.WelcomeActivity;
import com.trinity.isabelle.fitami.R;

import java.util.Arrays;

/*

 https://theengineerscafe.com/email-and-google-authentication-in-android-using-firebase/

 */

public class Login extends AppCompatActivity {

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
            sharedPref.edit().putString(getString(R.string.preference_uid_key), auth.getCurrentUser().getUid()).apply();
            sharedPref.edit().putString(getString(R.string.preference_email_key), auth.getCurrentUser().getEmail()).apply();
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();

        } else {

            // not signed in
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setLogo(R.mipmap.ic_launcher_round)
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                            ))
                            .build(),
                    RC_SIGN_IN);

        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                startActivity(new Intent(Login.this,WelcomeActivity.class));
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e("Login","Login canceled by User");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.e("Login","No Internet Connection");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Log.e("Login","Unknown Error");
                    return;
                }
            }

            Log.e("Login","Unknown sign in response");
        }
    }
}