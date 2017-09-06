package com.trinity.isabelle.fitami.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.trinity.isabelle.fitami.R;
import com.trinity.isabelle.fitami.Other.DataFragment;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BadgeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BadgeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BadgeFragment extends DataFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "TAG_BADGE";

    private SharedPreferences sharedPref;
    int dailyChallenge;
    String[] badges = new String[24];
    ImageView imgBadge, imgBadgeDaily;


    public BadgeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BadgeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BadgeFragment newInstance(String param1, String param2) {
        BadgeFragment fragment = new BadgeFragment();
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
        View view = inflater.inflate(R.layout.fragment_badge, container, false);
        view.setTag(TAG);

        // TODO: change layout background image if user has gotten a medal

        // Get the data from shared preferences to write on the card
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        dailyChallenge = sharedPref.getInt(getString(R.string.preference_daily_challenge_key),0);
        for (int i=0; i<24; i++) {
            badges[i]= String.valueOf(sharedPref.getLong(getString(R.string.preference_daily_medal_key)+i,0l));
        }

        for(int i=0; i<12; i++) {
            String imgID = "imgBadge"+i;
            String imgDailyID = "imgBadgeDaily"+i;
            int resID = getResources().getIdentifier(imgID, "id", "com.trinity.isabelle.fitami");
            int resDailyID = getResources().getIdentifier(imgDailyID, "id", "com.trinity.isabelle.fitami");
            imgBadge = ((ImageView) view.findViewById(resID));
            imgBadgeDaily = ((ImageView) view.findViewById(resDailyID));
            if (dailyChallenge == i){
                imgBadgeDaily.setVisibility(View.VISIBLE);
            }
            if(badges[i].equals("1") || badges[i+12].equals("1")) {
                imgBadge.setAlpha(1.0f);
            }
            else {
                imgBadge.setAlpha(0.3f);
            }
        }


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setFragmentData(){
        if(!isAdded())  return;
        // Get the data from shared preferences to write on the card
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        dailyChallenge = sharedPref.getInt(getString(R.string.preference_daily_challenge_key),0);
        for (int i=0; i<24; i++) {
            badges[i]= String.valueOf(sharedPref.getLong(getString(R.string.preference_daily_medal_key)+i,0l));
        }

        for(int i=0; i<12; i++) {
            String imgID = "imgBadge"+i;
            String imgDailyID = "imgBadgeDaily"+i;
            int resID = getResources().getIdentifier(imgID, "id", "com.trinity.isabelle.fitami");
            int resDailyID = getResources().getIdentifier(imgDailyID, "id", "com.trinity.isabelle.fitami");
            imgBadge = ((ImageView) getView().findViewById(resID));
            imgBadgeDaily = ((ImageView) getView().findViewById(resDailyID));
            if (dailyChallenge == i){
                imgBadgeDaily.setVisibility(View.VISIBLE);
            }
            if(badges[i].equals("1") || badges[i+12].equals("1")) {
                imgBadge.setAlpha(1.0f);
            }
            else {
                imgBadge.setAlpha(0.3f);
            }
        }
    }

    @Override
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
