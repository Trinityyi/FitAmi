package com.trinity.isabelle.fitami.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trinity.isabelle.fitami.R;
import com.trinity.isabelle.fitami.Other.DataFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends DataFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "TAG_PROFILE";


    Spinner spinnerGender;
    EditText editNickname;
    TextView editBirth, editWeight, editHeight;
    Button btnSave;
    private String nickname,gender,dateOfBirth,height,weight;
    FirebaseDatabase database;
    DatabaseReference rootRef,userRef;
    String userId;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        view.setTag(TAG);

        editNickname = (EditText) view.findViewById(R.id.feditNickname);
        spinnerGender = (Spinner) view.findViewById(R.id.fspinnerGender);
        editBirth = (TextView) view.findViewById(R.id.feditBirth);
        editHeight = (TextView) view.findViewById(R.id.feditHeight);
        editWeight = (TextView) view.findViewById(R.id.feditWeight);
        btnSave = (Button) view.findViewById(R.id.btnSaveProfile);

        // Get the data from shared preferences to write on the card
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        userId = sharedPref.getString(getString(R.string.preference_uid_key), "");

        //database reference pointing to root of database
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference();
        userRef = rootRef.child("users").child(userId);

        editBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
                View mView = layoutInflaterAndroid.inflate(R.layout.dialogbox_birth, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
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
        editHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
                View mView = layoutInflaterAndroid.inflate(R.layout.dialogbox_height, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
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

        editWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
                View mView = layoutInflaterAndroid.inflate(R.layout.dialogbox_weight, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
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

        rootRef.child("users/" + userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nickname = dataSnapshot.child("nickname").getValue(String.class);
                gender = dataSnapshot.child("gender").getValue(String.class);
                dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);
                height = dataSnapshot.child("height").getValue(String.class);
                weight = dataSnapshot.child("weight").getValue(String.class);

                editNickname.setText(nickname);
                spinnerGender.setSelection(((ArrayAdapter)spinnerGender.getAdapter()).getPosition(gender));
                editBirth.setText(dateOfBirth);
                editHeight.setText(height);
                editWeight.setText(weight);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Save data
                String nickname = editNickname.getText().toString();
                String gender = String.valueOf(spinnerGender.getSelectedItem());
                String dateOfBirth = editBirth.getText().toString();
                String height = editHeight.getText().toString();
                String weight = editWeight.getText().toString();

                // check form
                if(TextUtils.isEmpty(nickname)) {
                    editNickname.setError(getString(R.string.required_field));
                    Toast.makeText(getActivity(), getString(R.string.no_nickname), Toast.LENGTH_SHORT).show();
                    return;
                }else if (nickname.contains(",")|| nickname.contains(";")) {
                    Toast.makeText(getActivity(), getString(R.string.nickname_invalid), Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(gender)) {
                    Toast.makeText(getActivity(), getString(R.string.no_gender), Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(dateOfBirth)) {
                    Toast.makeText(getActivity(), getString(R.string.no_birth), Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(height)) {
                    Toast.makeText(getActivity(), getString(R.string.no_height), Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(weight)) {
                    Toast.makeText(getActivity(), getString(R.string.no_weight), Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    //push creates a unique id in database
                    userRef.child("nickname").setValue(nickname);
                    userRef.child("gender").setValue(gender);
                    userRef.child("dateOfBirth").setValue(dateOfBirth);
                    userRef.child("height").setValue(height);
                    userRef.child("weight").setValue(weight);

                    getActivity().onBackPressed();
                }
            }
        });

        return view;
    }

    public void setFragmentData(){

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
