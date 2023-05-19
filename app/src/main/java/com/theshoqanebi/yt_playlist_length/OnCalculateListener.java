package com.theshoqanebi.yt_playlist_length;

public interface OnCalculateListener {
    default void onSuccess(int time, int total_videos) {

    }

    default void onFailure(Exception exception) {

    }
}
