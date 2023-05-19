package com.theshoqanebi.yt_playlist_length;

public interface OnGetThumbnailListener {
    default void onSuccess(byte[] image) {

    }

    default void onFailure(Exception exception) {

    }
}
