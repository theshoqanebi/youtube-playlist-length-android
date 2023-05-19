package com.theshoqanebi.yt_playlist_length;

public interface OnGetPlaylistTitleListener {
    default void onSuccess(String title) {

    }

    default void onFailure(Exception exception) {

    }
}
