package com.gjd.minimoviedatabase2;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String JsonStr = null;
        final String MOVIE_API_URL;


        try {
            // Construct the URL

            MOVIE_API_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "/reviews?api_key=4d2fe91044a113962494d96fba3bdfd6";


            Uri builtUri = Uri.parse(MOVIE_API_URL).buildUpon()
                    .build();

            URL url = new URL(builtUri.toString());


            // Create the request, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                //return null;
            }
            JsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e("Error ", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("Error ", "Error closing stream", e);
                }
            }
        }
        try {
            return getReviewsInfoFromJSON(JsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
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

