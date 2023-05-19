package com.theshoqanebi.yt_playlist_length;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theshoqanebi.yt_playlist_length.databinding.SampleTotalTimeBinding;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<ViewHolder> {
    private final ArrayList<TotalTime> speeds;
    Adapter(@NonNull ArrayList<TotalTime> speeds) {
        this.speeds = speeds;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                SampleTotalTimeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TotalTime current = speeds.get(position);
        holder.binding.speed.setText(current.getSpeed());
        holder.binding.totalTime.setText(current.getTotalTime());
    }

    @Override
    public int getItemCount() {
        return speeds.size();
    }
}
