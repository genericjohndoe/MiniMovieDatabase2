package com.gjd.minimoviedatabase2;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.gjd.minimoviedatabase2.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * AsyncTask is responsible for retrieving text based movie details
 */
public class FetchMovieInfo extends AsyncTask<String, Void, Void> {

    private final Context mContext;
    int inserted;

    public FetchMovieInfo(Context context){
        mContext = context;
    }

    /**
     * info is abstracted from JSON object and saved in sqlite database
     * @param JsonString received from http request
     * @throws JSONException
     */
    private void getMovieInfoFromJSON(String JsonString, String params)
            throws JSONException {

        JSONObject Json = new JSONObject(JsonString);
        JSONArray MovieInfo = Json.getJSONArray("results");
        Vector<ContentValues> cVVector = new Vector<ContentValues>(MovieInfo.length());
        for (int i = 0; i < MovieInfo.length(); i++) {
            JSONObject movie = MovieInfo.getJSONObject(i);
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE,movie.getString("title"));
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getString("overview"));
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getString("release_date"));
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH,
                    ("https://image.tmdb.org/t/p/w185/"+ movie.getString("poster_path")));
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getDouble("vote_average"));
            movieValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movie.getString("original_language"));
            movieValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, movie.getDouble("popularity"));
            movieValues.put(MovieContract.MovieEntry.COLUMN_API_ID, movie.getString("id"));
            movieValues.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 0);
            if (params.equals(mContext.getString(R.string.search_popular))){
                movieValues.put(MovieContract.MovieEntry.COLUMN_IS_POPULAR, 1);
                movieValues.put(MovieContract.MovieEntry.COLUMN_IS_TOP_RATED, 0);
            } else {
                movieValues.put(MovieContract.MovieEntry.COLUMN_IS_POPULAR, 0);
                movieValues.put(MovieContract.MovieEntry.COLUMN_IS_TOP_RATED, 1);
            }
            cVVector.add(movieValues);
        }

        if ( cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);
        }
    }

    /**
     * retrieves JSON via http request for further processing
     * @param params is input from .execute()
     */
    @Override
    protected Void doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String JsonStr = null;
        final String MOVIE_API_URL;


        try {
            // Construct the URL for the TheMovieDb
            MOVIE_API_URL = "https://api.themoviedb.org/3/movie/"+params[0]+"?api_key=4d2fe91044a113962494d96fba3bdfd6";

            Uri builtUri = Uri.parse(MOVIE_API_URL).buildUpon()
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to TheMovieDb, and open the connection
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
            getMovieInfoFromJSON(JsonStr, params[0]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }}
