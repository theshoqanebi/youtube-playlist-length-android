package com.theshoqanebi.yt_playlist_length;

public class TotalTime {
    private final String speed;
    private final String totalTime;

    public TotalTime(String speed, String totalTime) {
        this.speed = speed;
        this.totalTime = totalTime;
    }

    public String getSpeed() {
        return speed;
    }

    public String getTotalTime() {
        return totalTime;
    }
}
