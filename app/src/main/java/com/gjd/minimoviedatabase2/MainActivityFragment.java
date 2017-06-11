package com.gjd.minimoviedatabase2;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.gjd.minimoviedatabase2.data.MovieContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {
    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    private static final int MOVIE_LOADER = 0;
    private int spinnerState;
    private Spinner spinner;
    private String spinnerString;


    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_ID,
            MovieContract.MovieEntry.COLUMN_TITLE, MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE, MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY, MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, MovieContract.MovieEntry.COLUMN_API_ID
    };


    public MainActivityFragment() {
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected. used to pass nformation from
         * fragment to activity
         */
        void onItemSelected(Uri Uri, int api, View view);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        spinnerState = prefs.getInt(getString(R.string.spinner_state), 0);
        spinnerString = prefs.getString(getString(R.string.Default), getString(R.string.pref_popularity));

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a grid layout manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), (returnScreenSize()) ? 2 : numberOfColumns()));

        View emptyView = rootView.findViewById(R.id.recyclerview_empty);

        mAdapter = new MovieAdapter(getActivity(), new MovieAdapter.MovieOnClickHandler() {
            @Override
            public void onClick(long id, MovieAdapter.MovieViewHolder vh, int api, View view) {
                ((Callback) getActivity())
                        .onItemSelected(MovieContract.MovieEntry.buildMovieUri(id + 1), api, view);
            }
        }, emptyView);

        mRecyclerView.setAdapter(mAdapter);

        // specify an adapter (see also next exam

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.pref_sort_options, R.layout.spinner_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        if (spinner != null) spinner.setSelection(spinnerState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateSort() {
        FetchMovieInfo movieTask = new FetchMovieInfo(getContext());
        String search = spinnerString.equals(getString(R.string.pref_popularity)) ? getString(R.string.search_popular) : getString(R.string.search_top_rated);
        movieTask.execute(search);
    }

    private void updateSortTop() {
        //this method was add to ensure the top rated movies were in the database by the time
        //they needed to be shown on the UI
        FetchMovieInfo movieTask = new FetchMovieInfo(getContext());
        movieTask.execute(getString(R.string.search_top_rated));
    }

    @Override
    public void onStart() {
        super.onStart();
        updateSort();
        updateSortTop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(getString(R.string.spinner_state), spinner.getSelectedItemPosition());
        editor.putString(getString(R.string.Default), spinnerString);
        editor.apply();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (spinnerString.equals(getString(R.string.pref_popularity))) {
            String selection = MovieContract.MovieEntry.COLUMN_IS_POPULAR + " = 1";
            return new CursorLoader(getContext(), MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS, selection, null, null);
        } else if (spinnerString.equals(getString(R.string.pref_highest_rated))) {
            String selection = MovieContract.MovieEntry.COLUMN_IS_TOP_RATED + " = 1";
            return new CursorLoader(getContext(), MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS, selection, null, null);
        } else {
            return new CursorLoader(getContext(), MovieContract.MovieEntry.CONTENT_URI,
                    MOVIE_COLUMNS, MovieContract.MovieEntry.COLUMN_IS_FAVORITE + "= 1", null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        data.setNotificationUri(getActivity().getContentResolver(), MovieContract.MovieEntry.CONTENT_URI);

        if (data.getCount() == 0 && spinnerString.equals(getString(R.string.Fav))) {
            CharSequence text = getString(R.string.no_fav);
            Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
        }
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    /**
     * this app returns the number of columns based on the width of the phone, the orientation, and
     * the size of the image populating the recyclerview cell
     * @return number of columns
     */

    private int numberOfColumns() {
        final int posterWidth = 105;
        final int DIPperInch = 160;
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int orientation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        if (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180) {
            return Math.round(metrics.widthPixels / metrics.ydpi * DIPperInch / posterWidth);
        } else {
            return Math.round(metrics.widthPixels / metrics.xdpi * DIPperInch / posterWidth);
        }
    }

    /**
     *
     * @return true if the diagonal is greater than 6.7 inches
     */

    private boolean returnScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dens = dm.densityDpi;
        double wi = (double) width / (double) dens;
        double hi = (double) height / (double) dens;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        return Math.sqrt(x + y) >= 6.7;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerString = parent.getItemAtPosition(position).toString();
        getLoaderManager().restartLoader(0, null, this);
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

}


