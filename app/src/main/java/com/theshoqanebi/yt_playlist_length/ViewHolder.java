package com.theshoqanebi.yt_playlist_length;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theshoqanebi.yt_playlist_length.databinding.SampleTotalTimeBinding;

public class ViewHolder extends RecyclerView.ViewHolder {
    public SampleTotalTimeBinding binding;
    ViewHolder(@NonNull SampleTotalTimeBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
