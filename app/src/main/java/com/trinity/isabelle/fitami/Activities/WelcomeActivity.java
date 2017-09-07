package com.trinity.isabelle.fitami.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinity.isabelle.fitami.R;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private int[] layouts;
    private Button btnNext;
    Spinner spinnerGender;
    EditText editNickname;
    TextView editBirth, editWeight, editHeight;
    DatabaseReference rootRef,userRef;
    final Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //database reference pointing to root of database
        rootRef = FirebaseDatabase.getInstance().getReference();
        //database reference pointing to user node
        userRef = rootRef.child("users").child(userId);

        rootRef.child("users/" + userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) { // if user not new, case where the data already exists
                    SharedPreferences sharedPref = WelcomeActivity.this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
                    sharedPref.edit().putString(getString(R.string.preference_uid_key), userId).apply();
                    startActivity(new Intent(WelcomeActivity.this, TutorialActivity.class));
                    finish();
                }
                else { // if user new, case where the data does not yet exist
                    setContentView(R.layout.activity_welcome);

                    viewPager = (ViewPager) findViewById(R.id.viewPagerWelcome);
                    btnNext = (Button) findViewById(R.id.btnWelcomeNext);

                    // layouts of all welcome sliders
                    layouts = new int[]{
                            R.layout.welcome_slide,
                            R.layout.activity_settings
                    };

                    WelcomeActivity.MyViewPagerAdapter myViewPagerAdapter = new WelcomeActivity.MyViewPagerAdapter();
                    viewPager.setAdapter(myViewPagerAdapter);

                    viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

                    SharedPreferences sharedPref = WelcomeActivity.this.getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
                    sharedPref.edit().putString(getString(R.string.preference_uid_key), userId).apply();


                    btnNext.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // checking for last page
                            // if last page tutorial screen will be launched
                            int current = getItem(+1);
                            if (current < layouts.length) {
                                // move to next screen
                                viewPager.setCurrentItem(current);
                            }
                            else {

                                launchTutorialScreen();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchTutorialScreen() {
        // Save data
        String nickname = editNickname.getText().toString();
        String gender = String.valueOf(spinnerGender.getSelectedItem());
        String dateOfBirth = editBirth.getText().toString();
        String height = editHeight.getText().toString();
        String weight = editWeight.getText().toString();

        // check form
        if(TextUtils.isEmpty(nickname)) {
            editNickname.setError(getString(R.string.required_field));
            Toast.makeText(this, getString(R.string.no_nickname), Toast.LENGTH_SHORT).show();
            return;
        }else if (nickname.contains(",")|| nickname.contains(";")) {
            Toast.makeText(this, getString(R.string.nickname_invalid), Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, getString(R.string.no_gender), Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(dateOfBirth)) {
            Toast.makeText(this, getString(R.string.no_birth), Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(height)) {
            Toast.makeText(this, getString(R.string.no_height), Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(weight)) {
            Toast.makeText(this, getString(R.string.no_weight), Toast.LENGTH_SHORT).show();
            return;
        } else {
            //push creates a unique id in database
            userRef.child("nickname").setValue(nickname);
            userRef.child("gender").setValue(gender);
            userRef.child("dateOfBirth").setValue(dateOfBirth);
            userRef.child("height").setValue(height);
            userRef.child("weight").setValue(weight);
            for(int i = 0; i < 24; i++){
                userRef.child("medals").child(String.valueOf(i)).setValue(0);
            }
            userRef.child("score").setValue(0);
            startActivity(new Intent(WelcomeActivity.this, TutorialActivity.class));
            finish();
        }
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            // changing the next button text 'NEXT' / 'START'
            if (position == layouts.length - 1) {
                // last page. make button text to START
                btnNext.setText(getString(R.string.start));

            }
            else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
            }

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * View pager adapter
     */
    private class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        private MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            if (position==layouts.length-1){
                editNickname = (EditText) view.findViewById(R.id.editNickname);
                spinnerGender = (Spinner) view.findViewById(R.id.spinnerGender);
                editBirth = (TextView) view.findViewById(R.id.editBirth);
                editHeight = (TextView) view.findViewById(R.id.editHeight);
                editWeight = (TextView) view.findViewById(R.id.editWeight);

                // dialog for date of birth field
                editBirth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
                        View mView = layoutInflaterAndroid.inflate(R.layout.dialogbox_birth, null);
                        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
                        alertDialogBuilderUserInput.setView(mView);

                        final NumberPicker year = (NumberPicker) mView.findViewById(R.id.yearPicker);
                        final NumberPicker month = (NumberPicker) mView.findViewById(R.id.monthPicker);
                        alertDialogBuilderUserInput
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        String content = month.getValue()+"/"+year.getValue(); //gets you the contents of edit text
                                        editBirth.setText(content); //displays it in a textview..
                                    }
                                })

                                .setNegativeButton(getString(R.string.dialog_cancel),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogBox, int id) {
                                                dialogBox.cancel();
                                            }
                                        });

                        year.setMinValue(1920);
                        year.setMaxValue(2050);
                        year.setValue(1990);

                        month.setMinValue(1);
                        month.setMaxValue(12);
                        month.setValue(6);


                        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                        alertDialogAndroid.show();
                    }
                });

                // dialog for height field
                editHeight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
                        View mView = layoutInflaterAndroid.inflate(R.layout.dialogbox_height, null);
                        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
                        alertDialogBuilderUserInput.setView(mView);

                        final NumberPicker cm = (NumberPicker) mView.findViewById(R.id.heightPicker);
                        alertDialogBuilderUserInput
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        String content = cm.getValue()+" cm"; //gets you the contents of edit text
                                        editHeight.setText(content); //displays it in a textview..
                                    }
                                })

                                .setNegativeButton(getString(R.string.dialog_cancel),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogBox, int id) {
                                                dialogBox.cancel();
                                            }
                                        });

                        cm.setMinValue(120);
                        cm.setMaxValue(230);
                        cm.setValue(170);


                        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                        alertDialogAndroid.show();
                    }
                });

                // dialog for weight field
                editWeight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
                        View mView = layoutInflaterAndroid.inflate(R.layout.dialogbox_weight, null);
                        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
                        alertDialogBuilderUserInput.setView(mView);

                        final NumberPicker kilo = (NumberPicker) mView.findViewById(R.id.kiloPicker);
                        final NumberPicker gram = (NumberPicker) mView.findViewById(R.id.gramPicker);
                        alertDialogBuilderUserInput
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        String content = kilo.getValue()+"."+gram.getValue()+" kg"; //gets you the contents of edit text
                                        editWeight.setText(content); //displays it in a textview..
                                    }
                                })

                                .setNegativeButton(getString(R.string.dialog_cancel),
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogBox, int id) {
                                                dialogBox.cancel();
                                            }
                                        });

                        kilo.setMinValue(20);
                        kilo.setMaxValue(299);
                        kilo.setValue(70);

                        gram.setMinValue(0);
                        gram.setMaxValue(9);
                        gram.setValue(0);

                        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                        alertDialogAndroid.show();
                    }
                });
            }

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
