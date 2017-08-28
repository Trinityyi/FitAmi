package com.trinity.isabelle.fitami.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.trinity.isabelle.fitami.R;

public class SettingsActivity extends AppCompatActivity {

    private TextView editGender, editBirth, editWeight, editHeight;
    final Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

//        List age = new ArrayList<Integer>();
//        for (int i = 1; i <= 100; i++) {
//            age.add(Integer.toString(i));
//        }
//        ArrayAdapter<Integer> spinnerArrayAdapter = new ArrayAdapter<Integer>(
//                this, android.R.layout.simple_spinner_item, age);
//        spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
//
//        Spinner spinner = (Spinner)findViewById(R.id.spinner);
//        spinner.setAdapter(spinnerArrayAdapter);


        editBirth = (TextView) findViewById(R.id.editBirth);
        editHeight = (TextView) findViewById(R.id.editHeight);
        editWeight = (TextView) findViewById(R.id.editWeight);

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
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                // ToDo get user input here
                                String content = kilo.getValue()+"."+gram.getValue()+" kg"; //gets you the contents of edit text
                                editWeight.setText(content); //displays it in a textview..
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
            }
        });
    }
}
