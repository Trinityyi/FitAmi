package com.trinity.isabelle.fitami.Fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.trinity.isabelle.fitami.Other.RecycleViewAdapter;
import com.trinity.isabelle.fitami.R;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "TAG_HOME";

    private SharedPreferences sharedPref;

    private long lastTime,lastSteps,lastMeters;
    // #1 nickname, points , #2 nickname, points , #3 nickname, points, user rank, points
    private String top3Leaderboard = "Tamila,50,Pazareva,45,Skata,35,23,15";;
    protected RecyclerView recyclerView;

    public HomeFragment() {
        // Required empty public constructor
    }

    // Broadcast Receiver for getting messages from background service
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String messageReceived = intent.getStringExtra(String.valueOf(R.string.intent_service_string_extra));
            Log.d("Received ", messageReceived);
            // If the broadcast was about the GPS not being enabled, deal with it
            if(Objects.equals(messageReceived, "The GPS is not enabled!"))  {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, 7431);
            }
            else {
                sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
                lastTime = sharedPref.getLong(getString(R.string.preference_time_key), 0l);
                lastSteps = sharedPref.getLong(getString(R.string.preference_step_key), 0l);
                lastMeters = sharedPref.getLong(getString(R.string.preference_meter_key), 0l);

                recyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
                RecyclerView.Adapter adapter = new RecycleViewAdapter(lastSteps, lastMeters, lastTime, top3Leaderboard);
                recyclerView.setAdapter(adapter);
            }
        }
    };

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        view.setTag(TAG);

        // Register broadcast receiver for messages from the background service
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(broadcastReceiver, new IntentFilter(String.valueOf(HomeFragment.class)));
//
//        // Start the background service
//        Intent backgroundService = new Intent(getActivity(), FitamiBackgroundService.class);
//        getActivity().startService(backgroundService);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Get the data from shared preferences to write on the card
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        lastTime = sharedPref.getLong(getString(R.string.preference_time_key), 0l);
        lastSteps = sharedPref.getLong(getString(R.string.preference_step_key), 0l);
        lastMeters = sharedPref.getLong(getString(R.string.preference_meter_key), 0l);

        RecyclerView.Adapter adapter = new RecycleViewAdapter(lastSteps, lastMeters, lastTime, top3Leaderboard);
        recyclerView.setAdapter(adapter);

        return view;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        }
//      else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

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
