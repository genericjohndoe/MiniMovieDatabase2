package com.gjd.minimoviedatabase2;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gjd.minimoviedatabase2.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * this class formats cells for recyclerview
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private final Context mContext;
    final private MovieOnClickHandler mClickHandler;
    private Cursor mCursor;
    private int apiID;
    private View emptyView;

    MovieAdapter(Context context, MovieOnClickHandler dh, View emptyView){
        mContext = context;
        mClickHandler = dh;
        this.emptyView = emptyView;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView poster;

        public MovieViewHolder(View view) {
            super(view);
            poster = (ImageView) view.findViewById(R.id.poster);
            view.setOnClickListener(this);
            view.setTag(this);
        }
        @Override
        public void onClick(View view){
            int adapterPosition = getAdapterPosition();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                poster.setTransitionName(mContext.getString(R.string.transition));
            mCursor.moveToPosition(adapterPosition);
            apiID = mCursor.getInt(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_API_ID));
            String title = mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE));
            poster.setContentDescription(title);
            mClickHandler.onClick(adapterPosition, this, apiID, poster);
        }
    }
    public interface MovieOnClickHandler {
        void onClick(long id, MovieViewHolder vh, int api, View view);
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.poster_image_view, null);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder viewHolder, int position){
        mCursor.moveToPosition(position);
        Picasso.with(mContext)
                .load(mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH)))
                .into(viewHolder.poster);
    }

    @Override
    public int getItemCount() {
        return (null != mCursor ? mCursor.getCount() : 0);
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }
}
