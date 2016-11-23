package com.gjd.minimoviedatabase2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by joeljohnson on 7/7/16.
 */
public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {
    Context mContext;


    TrailerAdapter(Context context){
        mContext = context;
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
       TextView trailer;
        public TrailerViewHolder(View view){
            super(view);
            trailer = (TextView) view.findViewById(R.id.trailer_textview);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(DetailFragment.trailers[getAdapterPosition()])));

        }
    }

    public void onBindViewHolder(TrailerViewHolder holder, int position){
        holder.trailer.setText("Trailer #" + (position + 1) + "     |");
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
