package com.trinity.isabelle.fitami.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.trinity.isabelle.fitami.Other.LeaderboardAdaper;
import com.trinity.isabelle.fitami.Other.RecycleViewAdapter;
import com.trinity.isabelle.fitami.R;
import com.trinity.isabelle.fitami.Other.DataFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LeaderboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LeaderboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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

    private SharedPreferences sharedPref;

    // #1 nickname, steps,..,..; #1 nickname, distance,..,..;#1 nickname, time,..,..; user rank, points
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
        private long dataId; // 0: steps, 1: distance, 2: time, 3: score
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LeaderboardFragment.
     */
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

        adapter = new LeaderboardAdaper(leaderboardEntityList);
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

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Integer dataId = leaderboardSpinner.getSelectedItemPosition();
        String[] item = items[dataId].split(",");
        userRank.setText(item[0]);
        userData.setText(item[1]);
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
    private List<Leaderboard> loadLeaderboard( String data){

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
                leaderboard.setData(item[j+j+1]);
                leaderboardEntityList.add(leaderboard);
            }
        }

        return leaderboardEntityList;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
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
