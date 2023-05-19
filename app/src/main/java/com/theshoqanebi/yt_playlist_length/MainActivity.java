package com.theshoqanebi.yt_playlist_length;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.theshoqanebi.yt_playlist_length.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private final OnCalculateListener onCalculateListener = new OnCalculateListener() {
        @Override
        public void onSuccess(int time, int total_videos) {
            runOnUiThread(() -> {
                ArrayList<TotalTime> speeds = new ArrayList<>();
                speeds.add(new TotalTime("1.00X", formatTime(time)));
                speeds.add(new TotalTime("1.25X", formatTime((int) ((time * 1f) / 1.25))));
                speeds.add(new TotalTime("1.50X", formatTime((int) ((time * 1f) / 1.50))));
                speeds.add(new TotalTime("1.75X", formatTime((int) ((time * 1f) / 1.75))));
                speeds.add(new TotalTime("2.00X", formatTime((int) ((time * 1f) / 2.00))));
                Adapter adapter = new Adapter(speeds);
                binding.listTotalTime.setAdapter(adapter);
                binding.listTotalTime.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                binding.listTotalTime.setVisibility(View.VISIBLE);
                binding.totalTimeProgressCircular.setVisibility(View.INVISIBLE);
                binding.videosCount.setText(String.valueOf(total_videos));
            });
        }

        @Override
        public void onFailure(Exception exception) {

        }
    };

    private final OnGetThumbnailListener onGetThumbnailListener = new OnGetThumbnailListener() {
        @Override
        public void onSuccess(byte[] image) {
            runOnUiThread(() -> {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                binding.thumbnail.setImageBitmap(bitmap);
                binding.playlistShape.setVisibility(View.VISIBLE);
                binding.thumbnail.setVisibility(View.VISIBLE);
                binding.thumbnailProgressCircular.setVisibility(View.INVISIBLE);
                binding.totalTimeProgressCircular.setVisibility(View.INVISIBLE);
            });
        }

        @Override
        public void onFailure(Exception exception) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this, exception.toString(), Toast.LENGTH_LONG).show());
        }
    };

    private final OnGetPlaylistTitleListener onGetPlaylistTitleListener = new OnGetPlaylistTitleListener() {
        @Override
        public void onSuccess(String title) {
            runOnUiThread(() -> binding.title.setText(title));
        }

        @Override
        public void onFailure(Exception exception) {
            OnGetPlaylistTitleListener.super.onFailure(exception);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.go.setOnClickListener(v -> {
            resetUI();
            binding.thumbnailProgressCircular.setVisibility(View.VISIBLE);
            binding.totalTimeProgressCircular.setVisibility(View.VISIBLE);
            String API_KEY = getString(R.string.API_KEY);
            String url = binding.url.getText().toString();
            PlaylistLengthCalculator playlistLengthCalculator = new PlaylistLengthCalculator(API_KEY);
            playlistLengthCalculator.setOnCalculateListener(onCalculateListener);
            playlistLengthCalculator.setOnGetThumbnailListener(onGetThumbnailListener);
            playlistLengthCalculator.setOnGetPlaylistTitleListener(onGetPlaylistTitleListener);
            playlistLengthCalculator.calculate(url);
            playlistLengthCalculator.getPlaylistThumbnail(url);
            playlistLengthCalculator.getPlaylistTitle(url);
        });
    }

    private void resetUI() {
        binding.playlistShape.setVisibility(View.INVISIBLE);
        binding.thumbnail.setVisibility(View.INVISIBLE);
        binding.totalTimeProgressCircular.setVisibility(View.INVISIBLE);
        binding.thumbnailProgressCircular.setVisibility(View.INVISIBLE);
        binding.listTotalTime.setAdapter(null);
        binding.listTotalTime.setLayoutManager(null);
        binding.title.setText("");
    }

    public String formatTime(int time) {
        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = (time % 3600) % 60;
        String hoursFormatted = String.valueOf(hours).length() == 1 ? "0" + hours : String.valueOf(hours);
        String minutesFormatted = String.valueOf(minutes).length() == 1 ? "0" + minutes : String.valueOf(minutes);
        String secondsFormatted = String.valueOf(seconds).length() == 1 ? "0" + seconds : String.valueOf(seconds);
        if (hours > 0) {
            return String.format("%s:%s:%s", hoursFormatted, minutesFormatted, secondsFormatted);
        }
        return String.format("%s:%s", minutesFormatted, secondsFormatted);
    }
}