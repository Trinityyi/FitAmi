package com.trinity.isabelle.fitami.Other;

import java.util.Comparator;

/**
 * Created by chalarangelo on 3/9/2017.
 */

public class RankableUserData implements Comparable<RankableUserData> {
    private String nickname;
    private long scoringData;

    public long getScoringData(){
        return this.scoringData;
    }

    public String getNickname(){
        return this.nickname;
    }

    public RankableUserData(String nickname, long scoringData){
        this.nickname = nickname;
        this.scoringData = scoringData;
    }

    public int compareTo(RankableUserData otherUserData) {
        return (int)(otherUserData.getScoringData() - this.scoringData);
    }

    public static Comparator<RankableUserData> RankableUserDataComparator
            = new Comparator<RankableUserData>() {

        public int compare(RankableUserData user1, RankableUserData user2) {
            return user1.compareTo(user2);
        }

    };
}
