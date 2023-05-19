package com.theshoqanebi.yt_playlist_length;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaylistLengthCalculator {

    private OnCalculateListener onCalculateListener = new OnCalculateListener() {
    };
    private OnGetThumbnailListener onGetThumbnailListener = new OnGetThumbnailListener() {
    };
    private OnGetPlaylistTitleListener onGetPlaylistTitleListener = new OnGetPlaylistTitleListener() {
    };
    private final String URL1 = "https://www.googleapis.com/youtube/v3/playlistItems?part=contentDetails&maxResults=50&fields=items/contentDetails/videoId,nextPageToken&key=%s&playlistId=%s&pageToken=%s";
    private final String URL2 = "https://www.googleapis.com/youtube/v3/videos?&part=contentDetails&key=%s&id=%s&fields=items/contentDetails/duration";
    private final String API_KEY;
    private String nextPageToken = "";
    private String PLAYLIST_ID = null;
    private int total_video = 0;
    private int total_time = 0;

    private Thread calculateThread;
    private Thread thumbnailThread;
    private Thread titleThread;

    PlaylistLengthCalculator(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    public void setOnCalculateListener(OnCalculateListener onCalculateListener) {
        this.onCalculateListener = onCalculateListener;
    }

    public void setOnGetThumbnailListener(OnGetThumbnailListener onGetThumbnailListener) {
        this.onGetThumbnailListener = onGetThumbnailListener;
    }

    public void setOnGetPlaylistTitleListener(OnGetPlaylistTitleListener onGetPlaylistTitleListener) {
        this.onGetPlaylistTitleListener = onGetPlaylistTitleListener;
    }

    public void calculate(String yt_url) {
        if (calculateThread != null) {
            calculateThread.interrupt();
        }
        try {
            PLAYLIST_ID = extractPlaylistID(yt_url);
        } catch (MalformedURLException exception) {
            onCalculateListener.onFailure(new MalformedURLException());
            return;
        }
        calculateThread = new Thread(() -> {
            try {
                while (!calculateThread.isInterrupted()) {
                    HttpURLConnection getIdsConnection = (HttpURLConnection) new URL(String.format(URL1, API_KEY, PLAYLIST_ID, nextPageToken)).openConnection();
                    JSONObject idsJsonObject = new JSONObject(stream2string(getIdsConnection.getInputStream()));
                    JSONArray ids_items = idsJsonObject.getJSONArray("items");
                    ArrayList<String> ids_array = new ArrayList<>();
                    System.out.println(ids_array);
                    for (int i = 0; i < ids_items.length(); i++) {
                        JSONObject contentDetails = ids_items.getJSONObject(i).getJSONObject("contentDetails");
                        String video_id = contentDetails.getString("videoId");
                        ids_array.add(video_id);
                    }
                    String ids = String.join(",", ids_array);
                    HttpURLConnection getDurationConnection = (HttpURLConnection) new URL(String.format(URL2, API_KEY, ids)).openConnection();
                    JSONObject durationJsonObject = new JSONObject(stream2string(getDurationConnection.getInputStream()));
                    JSONArray durations_items = durationJsonObject.getJSONArray("items");
                    ArrayList<String> durations_array = new ArrayList<>();
                    for (int i = 0; i < durations_items.length(); i++) {
                        JSONObject contentDetails = durations_items.getJSONObject(i).getJSONObject("contentDetails");
                        String duration = contentDetails.getString("duration");
                        durations_array.add(duration);
                    }
                    for (String isoTime : durations_array) {
                        total_time += Duration.parse(isoTime).getSeconds();
                    }
                    total_video += ids_array.size();
                    if (idsJsonObject.isNull("nextPageToken") && !calculateThread.isInterrupted()) {
                        onCalculateListener.onSuccess(total_time, total_video);
                        break;
                    } else {
                        nextPageToken = idsJsonObject.getString("nextPageToken");
                    }
                }
            } catch (IOException | JSONException exception) {
                if (!calculateThread.isInterrupted())
                    onCalculateListener.onFailure(exception);
            }
        });
        calculateThread.start();
    }

    boolean isValidYoutubeUrl(String url) {
        Pattern pattern = Pattern.compile("^.*(youtu.be\\/|list=)([^#\\&\\?]*).*");
        Matcher matcher = pattern.matcher("https://www.youtube.com/playlist?list=PL1vJ-TMy4nwkIt9IK_JBNSxQbqAtr_sLs");
        return matcher.matches();
    }

    String stream2string(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void getPlaylistThumbnail(String yt_url) {
        if (thumbnailThread != null) {
            thumbnailThread.interrupt();
        }
        try {
            PLAYLIST_ID = extractPlaylistID(yt_url);
        } catch (MalformedURLException exception) {
            onCalculateListener.onFailure(new MalformedURLException());
            return;
        }
        thumbnailThread = new Thread(() -> {
            try {
                String  url = "https://www.googleapis.com/youtube/v3/playlists?part=snippet&key=%s&id=%s";
                HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(String.format(url, API_KEY, PLAYLIST_ID)).openConnection();
                JSONObject jsonObject = new JSONObject(stream2string(httpURLConnection.getInputStream()));
                JSONArray items = jsonObject.getJSONArray("items");
                JSONObject snippet = items.getJSONObject(0).getJSONObject("snippet");
                JSONObject thumbnails = snippet.getJSONObject("thumbnails");
                JSONObject high = thumbnails.getJSONObject("high");
                String thumbnail_url = high.getString("url");
                HttpURLConnection thumbnailConnection = (HttpURLConnection)new URL(thumbnail_url).openConnection();
                if (!thumbnailThread.isInterrupted())
                    onGetThumbnailListener.onSuccess(readFully(thumbnailConnection.getInputStream()));
            } catch (IOException|JSONException exception) {
                if (!thumbnailThread.isInterrupted())
                    onGetThumbnailListener.onFailure(exception);
            }
        });
        thumbnailThread.start();
    }

    public void getPlaylistTitle(String yt_url) {
        if (titleThread != null) {
            titleThread.interrupt();
        }
        try {
            PLAYLIST_ID = extractPlaylistID(yt_url);
        } catch (MalformedURLException exception) {
            onCalculateListener.onFailure(new MalformedURLException());
            return;
        }
        titleThread = new Thread(() -> {
            try {
                String  url = "https://www.googleapis.com/youtube/v3/playlists?key=%s&id=%s&part=id,snippet&fields=items(snippet(title))";
                HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(String.format(url, API_KEY, PLAYLIST_ID)).openConnection();
                JSONObject jsonObject = new JSONObject(stream2string(httpURLConnection.getInputStream()));
                JSONArray items = jsonObject.getJSONArray("items");
                JSONObject snippet = items.getJSONObject(0).getJSONObject("snippet");
                String title = snippet.getString("title");
                if (!titleThread.isInterrupted())
                    onGetPlaylistTitleListener.onSuccess(title);
            } catch (IOException|JSONException exception) {
                if (!titleThread.isInterrupted())
                    onGetPlaylistTitleListener.onFailure(exception);
            }
        });
        titleThread.start();
    }

    private String extractPlaylistID(String yt_url) throws MalformedURLException {
        Pattern pattern = Pattern.compile("^.*(youtu.be\\/|list=)([^#\\&\\?]*).*");
        Matcher matcher = pattern.matcher(yt_url);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            throw new MalformedURLException();
        }
    }


    public byte[] readFully(InputStream input) throws IOException
    {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }
}
