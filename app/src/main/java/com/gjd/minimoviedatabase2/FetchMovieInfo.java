package com.gjd.minimoviedatabase2;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.gjd.minimoviedatabase2.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Vector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
            Log.i("MMD2", "movies inserted " + inserted);
        }
    }

    /**
     * retrieves JSON via http request for further processing
     * @param params is input from .execute()
     */
    @Override
    protected Void doInBackground(String... params) {

        final String MOVIE_API_URL;
        OkHttpClient client = new OkHttpClient();

        MOVIE_API_URL = "https://api.themoviedb.org/3/movie/" + params[0] + "?api_key=" +  BuildConfig.MOVIE_API_KEY;

        Request request = new Request.Builder()
                .url(MOVIE_API_URL)
                .build();

        try {
            Response response = client.newCall(request).execute();
            getMovieInfoFromJSON(response.body().string(),params[0]);
            return null;
        } catch (IOException | JSONException | IllegalStateException e) {
            Log.e("FetchMovieInfo", e.toString());
        }
        return null;

    }}
