package com.gjd.minimoviedatabase2;

import android.content.ContentResolver;
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
import android.util.Log;
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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemSelectedListener {
    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;
    private static final int MOVIE_LOADER = 0;
    private int mPosition = RecyclerView.NO_POSITION;
    static Set<String> favorites;
    static Integer favCount;
    int spinnerState;
    Spinner spinner;
    String spinnerString;



    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_ID,
            MovieContract.MovieEntry.COLUMN_TITLE, MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE, MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_POPULARITY, MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, MovieContract.MovieEntry.COLUMN_API_ID,
    };


    public MainActivityFragment() {
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri Uri, int api);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        favorites = prefs.getStringSet("Favorites", new LinkedHashSet<String>());
        spinnerState = prefs.getInt("Spinner State", 0);
        spinnerString = prefs.getString("Default", "Popularity");

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
            public void onClick(long id , MovieAdapter.MovieViewHolder vh, int api) {
                ((Callback) getActivity())
                        .onItemSelected(MovieContract.MovieEntry.buildMovieUri(id+1), api);
            }
        }, emptyView);

        // specify an adapter (see also next example)
        mRecyclerView.setAdapter(mAdapter);

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
        try {
            FetchMovieInfo movieTask = new FetchMovieInfo(getContext());
            movieTask.execute();
            movieTask.get();
        } catch (InterruptedException e) {
            Log.i("InterruptedException", "yes");
        } catch (ExecutionException e) {
            Log.i("execution exception", "yes");
        }
        CharSequence text = "UpdateSort called";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getContext(), text, duration);
        toast.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (favCount == null){
            updateSort();
            favCount = new Integer(1);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("Favorites",favorites);
        editor.putInt("Spinner State",  spinner.getSelectedItemPosition());
        editor.putString("Default", spinnerString);
        editor.commit();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){
        return new CursorLoader(getContext(), MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        ContentResolver resolver = getContext().getContentResolver();

        if (spinnerString.equals(getString(R.string.pref_popularity))){
            data = resolver.query(MovieContract.MovieEntry.CONTENT_URI, MOVIE_COLUMNS,null, null, null);
        }
        else if  (spinnerString.equals(getString(R.string.pref_highest_rated))){
            String sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
            data = resolver.query(MovieContract.MovieEntry.CONTENT_URI, MOVIE_COLUMNS,null, null, sortOrder);
        } else {
            String selection = MovieContract.MovieEntry.COLUMN_API_ID + " in (" + (toString(favorites) +")");
            data = resolver.query(MovieContract.MovieEntry.CONTENT_URI, MOVIE_COLUMNS, selection, null, null);
            if (favorites.isEmpty()) {
                CharSequence text = "No movies have been favorited.";
                Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
            }

        }
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);;
    }

    public int numberOfColumns(){
        final int posterWidth = 105;
        final int DIPperInch= 160;
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int orientation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        if (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180) {
            return Math.round(metrics.widthPixels / metrics.ydpi * DIPperInch / posterWidth);
        }else{
            return Math.round(metrics.widthPixels / metrics.xdpi * DIPperInch / posterWidth);
        }
    }

    public String toString(Set<String> StringList){
        String returnString ="";
        int Comma = 0;
        for (String string : StringList){
            returnString += string;
            Comma += 1;
            if (Comma < (StringList.size())){
                returnString += ", ";
            }

        }
        return returnString;
    }
    private boolean returnScreenSize(){
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width=dm.widthPixels;
        int height=dm.heightPixels;
        int dens=dm.densityDpi;
        double wi=(double)width/(double)dens;
        double hi=(double)height/(double)dens;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi,2);
        return Math.sqrt(x+y) >= 6.7;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spinnerString = parent.getItemAtPosition(position).toString();
        getLoaderManager().restartLoader(0, null, this);
    }

    public void onNothingSelected(AdapterView<?> parent) {}

}


