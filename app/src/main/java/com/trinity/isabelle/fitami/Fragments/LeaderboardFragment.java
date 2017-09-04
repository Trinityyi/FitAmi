package com.trinity.isabelle.fitami.Fragments;

import android.content.Context;
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

import com.trinity.isabelle.fitami.Other.LeaderboardAdaper;
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

    private List<Leaderboard> leaderboardEntityList = new ArrayList<Leaderboard>();
    private ListView listView;
    private LeaderboardAdaper adapter;
    private Spinner leaderboardSpinner;

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

        loadDummyLeaderboard();
        //loadLeaderboard();

        leaderboardSpinner = (Spinner) view.findViewById(R.id.leaderboardSpinner);
        listView = (ListView) view.findViewById(R.id.leaderboardListView);

        adapter = new LeaderboardAdaper(leaderboardEntityList);
        listView.setAdapter(adapter);

        leaderboardSpinner.setOnItemSelectedListener(this);

        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Integer dataId = leaderboardSpinner.getSelectedItemPosition();
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

    private List<Leaderboard> loadDummyLeaderboard(){

        leaderboardEntityList = new ArrayList<>();
        Leaderboard leaderboard1 = new Leaderboard();
        leaderboard1.setId(1);
        leaderboard1.setDataId(0);
        leaderboard1.setRank("1");
        leaderboard1.setUserNickname("Superman");
        leaderboard1.setData("15000");
        leaderboardEntityList.add(leaderboard1);

        Leaderboard leaderboard2 = new Leaderboard();
        leaderboard2.setId(2);
        leaderboard2.setDataId(0);
        leaderboard2.setRank("2");
        leaderboard2.setUserNickname("Batman");
        leaderboard2.setData("10000");
        leaderboardEntityList.add(leaderboard2);

        Leaderboard leaderboard3 = new Leaderboard();
        leaderboard3.setId(3);
        leaderboard3.setDataId(0);
        leaderboard3.setRank("3");
        leaderboard3.setUserNickname("Foufoutos");
        leaderboard3.setData("8000");
        leaderboardEntityList.add(leaderboard3);

        Leaderboard leaderboard4 = new Leaderboard();
        leaderboard4.setId(4);
        leaderboard4.setDataId(1);
        leaderboard4.setRank("1");
        leaderboard4.setUserNickname("Superman");
        leaderboard4.setData("1500");
        leaderboardEntityList.add(leaderboard4);

        Leaderboard leaderboard5 = new Leaderboard();
        leaderboard5.setId(5);
        leaderboard5.setDataId(2);
        leaderboard5.setRank("1");
        leaderboard5.setUserNickname("Superman");
        leaderboard5.setData("7200");
        leaderboardEntityList.add(leaderboard5);

        Leaderboard leaderboard6 = new Leaderboard();
        leaderboard6.setId(6);
        leaderboard6.setDataId(2);
        leaderboard6.setRank("2");
        leaderboard6.setUserNickname("Batman");
        leaderboard6.setData("5400");
        leaderboardEntityList.add(leaderboard6);

        Leaderboard leaderboard7 = new Leaderboard();
        leaderboard7.setId(7);
        leaderboard7.setDataId(3);
        leaderboard7.setRank("1");
        leaderboard7.setUserNickname("Superman");
        leaderboard7.setData("80");
        leaderboardEntityList.add(leaderboard7);

        Leaderboard leaderboard8 = new Leaderboard();
        leaderboard8.setId(8);
        leaderboard8.setDataId(3);
        leaderboard8.setRank("2");
        leaderboard8.setUserNickname("Batman");
        leaderboard8.setData("50");
        leaderboardEntityList.add(leaderboard8);

        return leaderboardEntityList;
    }

    public void setFragmentData(){
        // TODO
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
