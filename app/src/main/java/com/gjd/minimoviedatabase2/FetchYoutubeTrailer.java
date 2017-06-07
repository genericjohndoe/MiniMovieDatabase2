package com.gjd.minimoviedatabase2;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Retrieves urls for movie trailers and clips for a specified movie
 */
public class FetchYoutubeTrailer extends AsyncTask<String, Void, String[]> {

    /**
     * Info is abstracted from JSON object, formatted into strings, and placed into an array
     * @param JsonString from http request
     * @return A string array filled with trailers and movie clips
     * @throws JSONException
     */
    private String[] getTrailerInfoFromJSON(String JsonString)
            throws JSONException {

        JSONObject Json = new JSONObject(JsonString);
        JSONArray TrailerInfo = Json.getJSONArray("results");
        String[] MovieTrailers = new String[TrailerInfo.length()];
        for (int i = 0; i < TrailerInfo.length(); i++) {
            JSONObject trailer = TrailerInfo.getJSONObject(i);
            String key = trailer.getString("key");
            MovieTrailers[i] = "https://www.youtube.com/watch?v=" + key;
        }
        return MovieTrailers;
    }

    /**
     * An http request is made to retrieve a JSON object which is processed
     * @param params is the input from .execute()
     * @return a string array to be used as input for onPostExecute()
     */
    @Override
    protected String[] doInBackground(String... params) {

        final String MOVIE_API_URL;
        OkHttpClient client = new OkHttpClient();

        MOVIE_API_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/videos?api_key=" +  BuildConfig.MOVIE_API_KEY;

        Request request = new Request.Builder()
                .url(MOVIE_API_URL)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return getTrailerInfoFromJSON(response.body().string());
        } catch (IOException | JSONException | IllegalStateException e) {
            Log.e("FetchYoutubeTrailer", e.toString());
        }
        return null;

    }

    /**
     *
     * @param result DetailFragment.trailer is set
     */
    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            DetailFragment.trailers = result;
        }
    }

}
