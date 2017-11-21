package com.trinity.isabelle.fitami.Other;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.trinity.isabelle.fitami.Fragments.LeaderboardFragment;
import com.trinity.isabelle.fitami.R;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdaper extends BaseAdapter implements Filterable {

    Context context;
    private List<LeaderboardFragment.Leaderboard> leaderboardList;
    private List<LeaderboardFragment.Leaderboard> leaderboardFilterList;
    private LeaderboardFilter leaderboardFilter;

    public LeaderboardAdaper(Context _context, List _data){
        this.leaderboardList = _data;
        this.leaderboardFilterList= _data;
        this.context = _context;
    }

    @Override
    public int getCount() {
        return leaderboardList.size();
    }

    @Override
    public LeaderboardFragment.Leaderboard getItem(int position) {
        return leaderboardList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder{
        TextView tvRank;
        TextView tvNickname;
        TextView tvData;
        ImageView imgBadge;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        View updateView;
        ViewHolder viewHolder;

        if (view == null) {
            updateView = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_listitem, parent,false);
            viewHolder = new ViewHolder();

            viewHolder.imgBadge = (ImageView) updateView.findViewById(R.id.imgBadge);
            viewHolder.tvRank = (TextView) updateView.findViewById(R.id.listitemRank);
            viewHolder.tvNickname = (TextView) updateView.findViewById(R.id.listitemNickname);
            viewHolder.tvData = (TextView) updateView.findViewById(R.id.listitemData);

            updateView.setTag(viewHolder);

        } else {
            updateView = view;
            viewHolder = (ViewHolder) updateView.getTag();
        }

        final LeaderboardFragment.Leaderboard item = getItem(position);

        if (position==0) {
            viewHolder.imgBadge.setImageResource(R.mipmap.gold_medal);
        } else if (position ==1){
            viewHolder.imgBadge.setImageResource(R.mipmap.silver_medal);
        }else if (position == 2) {
            viewHolder.imgBadge.setImageResource(R.mipmap.bronze_medal);
        }else {
            viewHolder.imgBadge.setImageResource(R.mipmap.gray_medal);
        }

        viewHolder.tvRank.setText("#"+item.getRank());
        viewHolder.tvNickname.setText(item.getUserNickname());
        viewHolder.tvData.setText(item.getData());

        return updateView;
    }

    @Override
    public Filter getFilter() {
        if (leaderboardFilter == null) {
            leaderboardFilter = new LeaderboardFilter();
        }
        return leaderboardFilter;
    }

// InnerClass for enabling Filter feature by implementing the methods

    private class LeaderboardFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            // checks the match for the dataId and adds to the filterlist
            long dataId = Long.parseLong(constraint.toString());
            FilterResults results = new FilterResults();

            if (dataId >= 0) {
                ArrayList<LeaderboardFragment.Leaderboard> filterList = new ArrayList<>();
                Integer newData;

                    for (int i = 0; i < leaderboardFilterList.size(); i++) {
                        if ( (leaderboardFilterList.get(i).getDataId() ) == dataId) {

                            LeaderboardFragment.Leaderboard lbData = leaderboardFilterList.get(i);
                            filterList.add(lbData);
                        }
                    }

                results.count = filterList.size();
                results.values = filterList;

            } else {

                results.count = leaderboardFilterList.size();
                results.values = leaderboardFilterList;

            }
            return results;
        }

        //Publishes the matches found, i.e., the selected dataids
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            leaderboardList = (ArrayList<LeaderboardFragment.Leaderboard>)results.values;

            notifyDataSetChanged();
        }
    }

}
