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

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String JsonStr = null;
        final String MOVIE_API_URL;


        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            MOVIE_API_URL =  "https://api.themoviedb.org/3/movie/"+params[0]+"/videos?api_key=4d2fe91044a113962494d96fba3bdfd6";



            Uri builtUri = Uri.parse(MOVIE_API_URL).buildUpon()
                    .build();

            URL url = new URL(builtUri.toString());


            // Create the request to OpenWeatherMap, and open the connection
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
            return getTrailerInfoFromJSON(JsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
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
