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
 * Retrieves reviews for specific movies
 */
public class FetchMovieReviews extends AsyncTask<String, Void, String[]> {
    /**
     * Reviews abstracted from JSON object, formatted into strings and placed in array
     * @param JsonString received from https request
     * @return a string array of movie reviews
     * @throws JSONException
     */
    private String[] getReviewsInfoFromJSON(String JsonString)
            throws JSONException {

        JSONObject Json = new JSONObject(JsonString);
        JSONArray ReviewInfo = Json.getJSONArray("results");
        String[] MovieReviews = new String[ReviewInfo.length()];
        for (int i = 0; i < ReviewInfo.length(); i++) {
            JSONObject review = ReviewInfo.getJSONObject(i);
            String reviewStr = review.getString("content");
            MovieReviews[i] = ("Review #"+(i+1) + "\n" +reviewStr);
        }
        return MovieReviews;
    }

    /**
     *
     * @param params input from .execute()
     * @return
     */
    @Override
    protected String[] doInBackground(String... params) {

        final String MOVIE_API_URL;
        OkHttpClient client = new OkHttpClient();

        MOVIE_API_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/reviews?api_key=" +  BuildConfig.MOVIE_API_KEY;

        Request request = new Request.Builder()
                .url(MOVIE_API_URL)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return getReviewsInfoFromJSON(response.body().string());
        } catch (IOException | JSONException | IllegalStateException e) {
            Log.e("FetchMovieReviws", e.toString());
        }
        return null;

    }

    /**
     *
     * @param result contains strings to be added to ArrayAdapter (detailFragment.mAapter)
     */
    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            DetailFragment.mAdapter.clear();
            for (String reviewStr : result) {
                DetailFragment.mAdapter.add(reviewStr);
            }
        }
    }
}

