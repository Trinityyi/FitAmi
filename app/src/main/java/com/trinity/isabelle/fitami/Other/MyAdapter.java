package com.trinity.isabelle.fitami.Other;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.trinity.isabelle.fitami.R;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private Integer steps;
    private double distance,time;

    public MyAdapter(Long _steps,Long _distance, Long _time) {
        this.steps=(int)(long)_steps;
        this.distance=(double) (_distance/1000.0);
        this.time=(double)(_time/60.0);
    }

    public  static  class MyViewHolder extends RecyclerView.ViewHolder {
        ProgressBar stepsBar;
        ProgressBar distanceBar; // meters
        ProgressBar timeBar; // minutes
        TextView stepsText;
        TextView distanceText; // kilometers
        TextView timeText; // hours

        public MyViewHolder (View itemView) {
            super(itemView);
            this.stepsBar = (ProgressBar) itemView.findViewById(R.id.stepsBar);
            this.stepsText = (TextView) itemView.findViewById(R.id.stepsText);
            this.distanceBar = (ProgressBar) itemView.findViewById(R.id.distanceBar);
            this.distanceText = (TextView) itemView.findViewById(R.id.distanceText);
            this.timeBar = (ProgressBar) itemView.findViewById(R.id.timeBar);
            this.timeText = (TextView) itemView.findViewById(R.id.timeText);

        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout,parent,false);

        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.stepsBar.setProgress(steps);
        holder.stepsText.setText(steps+" steps");
        holder.distanceBar.setProgress((int)distance);
        holder.distanceText.setText(distance+" km");
        holder.timeBar.setProgress((int)time);
        holder.timeText.setText(time+" minutes");
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
