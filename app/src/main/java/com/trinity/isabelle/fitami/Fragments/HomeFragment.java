package com.trinity.isabelle.fitami.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trinity.isabelle.fitami.Other.RecycleViewAdapter;
import com.trinity.isabelle.fitami.R;
import com.trinity.isabelle.fitami.Other.DataFragment;

public class HomeFragment extends DataFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "TAG_HOME";

    Context context;

    private SharedPreferences sharedPref;

    private long lastTime,lastSteps,lastMeters;
    private int dailyMedal;
    // #1 nickname, points , #2 nickname, points , #3 nickname, points, user rank, points
    private String top3Leaderboard, dailyChallenge;
    private RecycleViewAdapter recycleViewAdapter;
    protected RecyclerView recyclerView;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        setFragmentData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        view.setTag(TAG);
        context = getActivity();

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Get the data from shared preferences to write on the card
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        lastTime = sharedPref.getLong(getString(R.string.preference_time_key), 0l);
        lastSteps = sharedPref.getLong(getString(R.string.preference_step_key), 0l);
        lastMeters = sharedPref.getLong(getString(R.string.preference_meter_key), 0l);
        dailyMedal = sharedPref.getInt(getString(R.string.preference_daily_challenge_key), 0);
        top3Leaderboard = sharedPref.getString(getString(R.string.preference_total_score_leaderboard_key), "...,0,...,0,...,0,1,0");
        dailyChallenge = String.format(getResources().getString(R.string.daily_challenge_medal),getResources().getStringArray(R.array.badge_array)[dailyMedal]);
        // TODO: add completed to dailyChallenge
        if(sharedPref.getLong(getString(R.string.preference_daily_medal_key) + dailyMedal + 12, 0l) > 0l){
            dailyChallenge +=  getResources().getString(R.string.daily_challenge_medal_completed);
        }
        RecyclerView.Adapter adapter = new RecycleViewAdapter(context,lastSteps, lastMeters, lastTime, dailyChallenge, top3Leaderboard);
        recyclerView.setAdapter(adapter);
        recycleViewAdapter = (RecycleViewAdapter) adapter;

        return view;
    }

    public void setFragmentData(){
        if(!isAdded())  return;

        // Get the data from shared preferences to write on the card
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        lastTime = sharedPref.getLong(getString(R.string.preference_time_key), 0l);
        lastSteps = sharedPref.getLong(getString(R.string.preference_step_key), 0l);
        lastMeters = sharedPref.getLong(getString(R.string.preference_meter_key), 0l);
        dailyMedal = sharedPref.getInt(getString(R.string.preference_daily_challenge_key), 0);
        top3Leaderboard = sharedPref.getString(getString(R.string.preference_total_score_leaderboard_key), "...,0,...,0,...,0,1,0");
        dailyChallenge = String.format(getResources().getString(R.string.daily_challenge_medal),getResources().getStringArray(R.array.badge_array)[dailyMedal]);
        // TODO: add completed to dailyChallenge
        if(sharedPref.getLong(getString(R.string.preference_daily_medal_key) + dailyMedal + 12, 0l) > 0l){
            dailyChallenge += getResources().getString(R.string.daily_challenge_medal_completed);
        }
        recycleViewAdapter.updateData(context,lastSteps, lastMeters, lastTime, dailyChallenge, top3Leaderboard);
        recycleViewAdapter.notifyDataSetChanged();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
