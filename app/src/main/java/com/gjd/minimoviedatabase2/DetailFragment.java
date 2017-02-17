package com.gjd.minimoviedatabase2;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gjd.minimoviedatabase2.data.MovieContract;
import com.gjd.minimoviedatabase2.data.MovieDbHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

//.data.MovieContract;
//        .data.MovieContract;

/**
 * Created by joeljohnson on 4/30/16.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAIL_LOADER = 0;

    private final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_ID,
            MovieContract.MovieEntry.COLUMN_TITLE, MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE, MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY, MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, MovieContract.MovieEntry.COLUMN_API_ID,
    };

    TextView title;
    TextView release_date;
    TextView plot;
    TextView user_rating;
    ImageView movie_poster;
    CheckBox button;
    RecyclerView movieTrailers;
    ListView listView;
    static ArrayAdapter<String> mAdapter;
    int api_id;
    static String[] trailers;
    boolean favorited;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        api_id = (MainActivity.mTwoPane) ? getArguments().getInt("API") :
        getActivity().getIntent().getExtras().getInt("api_id");


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        favorited = prefs.getBoolean("Favorited " + Integer.toString(api_id), false);

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        title = (TextView) rootView.findViewById(R.id.movie_title);
        release_date = (TextView) rootView.findViewById(R.id.movie_release_date);
        plot = (TextView) rootView.findViewById(R.id.movie_plot);
        user_rating = (TextView) rootView.findViewById(R.id.movie_rating);
        movie_poster = (ImageView) rootView.findViewById(R.id.movie_poster);
        button = (CheckBox) rootView.findViewById(R.id.Fav);
        button.setChecked(favorited);
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FavoriteList(buttonView, isChecked);
            }
        });
        movieTrailers = (RecyclerView) rootView.findViewById(R.id.movie_trailer_recyclerview);
        movieTrailers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        movieTrailers.setAdapter(new TrailerAdapter(getContext()));
        listView = (ListView) rootView.findViewById(R.id.reviews);
        mAdapter = new ArrayAdapter(getActivity(), R.layout.movie_review, R.id.mrTextView, new ArrayList<String>());
        listView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchMovieReviews movieReviews = new FetchMovieReviews();
        movieReviews.execute(Integer.toString(api_id));
        try {
            FetchYoutubeTrailer movieTrailers = new FetchYoutubeTrailer();
            movieTrailers.execute(Integer.toString(api_id));
            movieTrailers.get();
        } catch (InterruptedException e) {
            Log.i("InterruptedException", "yes");
        } catch (ExecutionException e) {
            Log.i("execution exception", "yes");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("Favorited " + Integer.toString(api_id), favorited);
        editor.commit();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (!MainActivity.mTwoPane) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        String selection = "api_id = ?";
        String[] selectionArgs = new String[]{Integer.toString(api_id)};
        data = getContext().getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, MOVIE_COLUMNS, selection, selectionArgs, null);
        if (data != null && data.moveToFirst()) {
            title.setText("Title: " + data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
            release_date.setText("Release Date: " +
                    data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
            plot.setText("Plot: \n" +
                    data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
            user_rating.setText("Average Rating: " +
                    Double.toString(data.getDouble(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE))) + "/10.00");
            Picasso.with(getContext()).load(data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH))).into(movie_poster);
            data.close();
        } else {
            CharSequence text = "Movie Details Not Found.";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void addFavorite() {
        SQLiteDatabase db = new MovieDbHelper(getContext()).getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 1);
        String selection = MovieContract.MovieEntry.COLUMN_API_ID + " = " + api_id;
        //String[] selectionArgs = {MovieContract.MovieEntry.COLUMN_IS_FAVORITE};
        int count = db.update(MovieContract.MovieEntry.TABLE_NAME,values,selection, null);

        if (count == 1) {
            CharSequence text = "Added to Favorites";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();
            favorited = true;
        }
    }

    public void deleteFavorite() {
        SQLiteDatabase db = new MovieDbHelper(getContext()).getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 0);
        String selection = MovieContract.MovieEntry.COLUMN_API_ID + " = " + api_id;
        //String[] selectionArgs = {MovieContract.MovieEntry.COLUMN_IS_FAVORITE};
        int count = db.update(MovieContract.MovieEntry.TABLE_NAME,values,selection, null);

        if (count == 1) {
            CharSequence text = "Deleted from Favorites";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();
            favorited = true;
        }
    }

    public void FavoriteList(View v, boolean checked) {
        if (checked) {
            addFavorite();
        } else {
            deleteFavorite();
        }
    }
}
