package com.trinity.isabelle.fitami.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.trinity.isabelle.fitami.Other.LeaderboardAdaper;
import com.trinity.isabelle.fitami.R;
import com.trinity.isabelle.fitami.Other.DataFragment;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends DataFragment implements AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "TAG_LEADERBOARD";

    Context context;

    private SharedPreferences sharedPref;

    // #1 nickname, steps,..,..; #1 nickname, distance,..,..;#1 nickname, time,..,..
    // user rank, steps; user rank, distance; user rank, time
    private String top10Leaderboard, userLeaderboard;
    private List<Leaderboard> leaderboardEntityList = new ArrayList<Leaderboard>();
    private ListView listView;
    private LeaderboardAdaper adapter;
    private Spinner leaderboardSpinner;
    private TextView userRank, userData;
    private String[] items;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    public class Leaderboard {
        private long id;
        private long dataId; // 0: steps, 1: distance, 2: time
        private String rank;
        private String userNickname;
        private String data;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getDataId() {
            return dataId;
        }

        public void setDataId(long dataId) {
            this.dataId = dataId;
        }

        public String getRank() {
            return rank;
        }

        public void setRank(String rank) {
            this.rank = rank;
        }

        public String getUserNickname() {
            return userNickname;
        }

        public void setUserNickname(String userNickname) {
            this.userNickname = userNickname;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    // TODO: Rename and change types and number of parameters
    public static LeaderboardFragment newInstance(String param1, String param2) {
        LeaderboardFragment fragment = new LeaderboardFragment();
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
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        view.setTag(TAG);
        context = getActivity();

        // Get the data from shared preferences to write on the card
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        top10Leaderboard = sharedPref.getString(getString(R.string.preference_daily_leaderboard_key), "...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0;...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0;...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0");
        userLeaderboard = sharedPref.getString(getString(R.string.preference_daily_user_leaderboard_key), "1,0,1,0,1,0");

        items = userLeaderboard.split(";");

        loadLeaderboard(top10Leaderboard);

        userRank = (TextView) view.findViewById(R.id.editUserRank);
        userData = (TextView) view.findViewById(R.id.editUserData);

        leaderboardSpinner = (Spinner) view.findViewById(R.id.leaderboardSpinner);
        listView = (ListView) view.findViewById(R.id.leaderboardListView);

        adapter = new LeaderboardAdaper(context,leaderboardEntityList);
        listView.setAdapter(adapter);

        leaderboardSpinner.setOnItemSelectedListener(this);

        return view;
    }

    public void setFragmentData(){
        if(!isAdded())  return;
        // Get the data from shared preferences to write on the card
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.preference_master_key), Context.MODE_PRIVATE);
        top10Leaderboard = sharedPref.getString(getString(R.string.preference_daily_leaderboard_key), "...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0;...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0;...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0,...,0");
        userLeaderboard = sharedPref.getString(getString(R.string.preference_daily_user_leaderboard_key), "1,0,1,0,1,0");

        items = userLeaderboard.split(";");

        loadLeaderboard(top10Leaderboard);

        userRank = (TextView) getView().findViewById(R.id.editUserRank);
        userData = (TextView) getView().findViewById(R.id.editUserData);

        leaderboardSpinner = (Spinner) getView().findViewById(R.id.leaderboardSpinner);
        listView = (ListView) getView().findViewById(R.id.leaderboardListView);

        adapter = new LeaderboardAdaper(context,leaderboardEntityList);
        listView.setAdapter(adapter);

        leaderboardSpinner.setOnItemSelectedListener(this);

        Integer dataId = leaderboardSpinner.getSelectedItemPosition();
        String[] item = items[dataId].split(",");
        userRank.setText(item[0]);
        switch (dataId) {
            case 0: // steps
                userData.setText(String.format(context.getResources().getString(R.string.steps),Integer.valueOf(item[1])));
                break;
            case 1: // distance
                userData.setText(String.format(context.getResources().getString(R.string.distance),Integer.valueOf(item[1])/1000.0));
                break;
            case 2: // time
                userData.setText(String.format(context.getResources().getString(R.string.hours),Integer.valueOf(item[1])/3600.0));
                break;
        }
        adapter.getFilter().filter(Integer.toString(dataId),new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Integer dataId = leaderboardSpinner.getSelectedItemPosition();
        String[] item = items[dataId].split(",");
        userRank.setText(item[0]);
        switch (dataId) {
            case 0: // steps
                userData.setText(String.format(context.getResources().getString(R.string.steps),Integer.valueOf(item[1])));
                break;
            case 1: // distance
                userData.setText(String.format(context.getResources().getString(R.string.distance),Integer.valueOf(item[1])/1000.0));
                break;
            case 2: // time
                userData.setText(String.format(context.getResources().getString(R.string.hours),Integer.valueOf(item[1])/3600.0));
                break;
        }
        //Here we use the Filtering Feature which we implemented in our Adapter class.
        adapter.getFilter().filter(Integer.toString(dataId),new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {

            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    private List<Leaderboard> loadLeaderboard(String data){

        String[] items = data.split(";");
        leaderboardEntityList = new ArrayList<>();
        for (int i = 0; i< items.length; i++) {
            String[] item = items[i].split(",");
            for (int j = 0; j< 10; j++) {
                Leaderboard leaderboard = new Leaderboard();
                leaderboard.setId((j+1)+(i*10));
                leaderboard.setDataId(i);
                leaderboard.setRank(String.valueOf(j+1));
                leaderboard.setUserNickname(item[j+j]);
                switch (i) {
                    case 0:
                        leaderboard.setData(String.format(context.getResources().getString(R.string.steps_string),item[j+j+1]));
                        break;
                    case 1:
                        leaderboard.setData(String.format(context.getResources().getString(R.string.distance),Double.valueOf(item[j+j+1])/1000));
                        break;
                    case 2:
                        leaderboard.setData(String.format(context.getResources().getString(R.string.hours),Double.valueOf(item[j+j+1])/3600));
                        break;
                }
                leaderboardEntityList.add(leaderboard);
            }
        }

        return leaderboardEntityList;
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
