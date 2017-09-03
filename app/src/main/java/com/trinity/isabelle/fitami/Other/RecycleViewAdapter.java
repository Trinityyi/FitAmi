package com.trinity.isabelle.fitami.Other;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trinity.isabelle.fitami.R;

public class RecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Integer steps;
    private double distance,time;
    private String top3Leaderboard;

    public RecycleViewAdapter(Long _steps, Long _distance, Long _time, String _top3Leaderboard) {
        this.steps=(int)(long)_steps;
        this.distance=(double) (_distance/1000.0);
        this.time=(double)(_time/60.0);
        this.top3Leaderboard = _top3Leaderboard;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_stats_layout,parent,false);
                return  new StatsViewHolder(view);
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_challenge_layout,parent,false);
                return  new ChallengeViewHolder(view);
            case 3:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_leaderboard_layout,parent,false);
                return  new LeaderboardViewHolder(view);
        }
        return null;
    }

    public  static  class StatsViewHolder extends RecyclerView.ViewHolder {
        ProgressBar stepsBar;
        ProgressBar distanceBar; // meters
        ProgressBar timeBar; // minutes
        TextView stepsText;
        TextView distanceText; // kilometers
        TextView timeText; // hours

        public StatsViewHolder (View itemView) {
            super(itemView);
            stepsBar = (ProgressBar) itemView.findViewById(R.id.stepsBar);
            stepsText = (TextView) itemView.findViewById(R.id.stepsText);
            distanceBar = (ProgressBar) itemView.findViewById(R.id.distanceBar);
            distanceText = (TextView) itemView.findViewById(R.id.distanceText);
            timeBar = (ProgressBar) itemView.findViewById(R.id.timeBar);
            timeText = (TextView) itemView.findViewById(R.id.timeText);

        }
    }

    public  static  class ChallengeViewHolder extends RecyclerView.ViewHolder {

        public ChallengeViewHolder (View itemView) {
            super(itemView);


        }
    }

    public  static  class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView userRank, userScore, firstNickname, firstPoints;
        TextView secondNickname, secondPoints, thirdNickname, thirdPoints;

        public LeaderboardViewHolder (View itemView) {
            super(itemView);
            userRank = (TextView) itemView.findViewById(R.id.userRank);
            userScore = (TextView) itemView.findViewById(R.id.userScore);
            firstNickname = (TextView) itemView.findViewById(R.id.firstNicknameEdit);
            firstPoints = (TextView) itemView.findViewById(R.id.firstPointsEdit);
            secondNickname = (TextView) itemView.findViewById(R.id.secondNicknameEdit);
            secondPoints = (TextView) itemView.findViewById(R.id.secondPointsEdit);
            thirdNickname = (TextView) itemView.findViewById(R.id.thirdNicknameEdit);
            thirdPoints = (TextView) itemView.findViewById(R.id.thirdPointsEdit);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 1: // Stats
                StatsViewHolder statsViewHolder = (StatsViewHolder)holder;
                statsViewHolder.stepsBar.setProgress(steps);
                statsViewHolder.stepsText.setText(steps+" steps");
                statsViewHolder.distanceBar.setProgress((int)distance);
                statsViewHolder.distanceText.setText(distance+" km");
                statsViewHolder.timeBar.setProgress((int)time);
                statsViewHolder.timeText.setText(String.format("%.1f", time)+" minutes");
                break;
            case 2: // Challenge
                break;
            case 3: // Leaderboard
                LeaderboardViewHolder leaderboardViewHolder = (LeaderboardViewHolder)holder;
                String[] items = top3Leaderboard.split(",");
                leaderboardViewHolder.firstNickname.setText(items[0]);
                leaderboardViewHolder.firstPoints.setText(items[1]+" points");
                leaderboardViewHolder.secondNickname.setText(items[2]);
                leaderboardViewHolder.secondPoints.setText(items[3]+" points");
                leaderboardViewHolder.thirdNickname.setText(items[4]);
                leaderboardViewHolder.thirdPoints.setText(items[5]+" points");
                leaderboardViewHolder.userRank.setText(items[6]);
                leaderboardViewHolder.userScore.setText(items[7]);
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {

        if (position % 3 == 0) {
            return 1;
        } else if (position % 2 == 1) {
            return 2;
        } else {
            return 3;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}