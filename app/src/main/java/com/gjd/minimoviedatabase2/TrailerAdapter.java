package com.gjd.minimoviedatabase2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Used to format trailers in details fragment
 */
public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
    private final Context mContext;


    TrailerAdapter(Context context){
        mContext = context;
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
       final ImageButton trailer;
        public TrailerViewHolder(View view){
            super(view);
            trailer = (ImageButton) view.findViewById(R.id.trailer_button);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(DetailFragment.trailers[getAdapterPosition()])));

        }
    }

    public void onBindViewHolder(TrailerViewHolder holder, int position){
        holder.trailer.setImageResource(R.drawable.ic_local_movies_black_24dp);
    }

    public TrailerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.trailer_textview, null);
        view.setFocusable(true);
        return new TrailerViewHolder(view);
    }

    public int getItemCount(){
        if (DetailFragment.trailers != null) {return DetailFragment.trailers.length;}
        else {return 0;}
    }
}
