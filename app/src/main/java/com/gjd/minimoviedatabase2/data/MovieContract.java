package com.gjd.minimoviedatabase2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Class holds string relevant to CRUD in sqlite datanase
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.gjd.minimoviedatabase2.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_MOVIE_INFO = "Movie_Info";
    public static final String PATH_FAVORITES = "Favorites";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_INFO).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_INFO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE_INFO;

        // Table name
        public static final String TABLE_NAME = "Movie_Info";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_RELEASE_DATE = "release_date";


        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_POPULARITY = "popularity";

        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        public static final String COLUMN_API_ID = "api_id";

        public static final String COLUMN_IS_FAVORITE = "is_favorite";

        public static final String COLUMN_IS_POPULAR = "is_popular";

        public static final String COLUMN_IS_TOP_RATED = "is_top_rated";


        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}

