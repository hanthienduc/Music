package com.mnml.music.base;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mnml.music.R;

public abstract class BaseAdapter extends RecyclerView.Adapter<BaseAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId(), parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        setView(holder, adapterPosition);
    }

    public void setView(final ViewHolder holder, final int position) {}

    public int layoutId() {
        return R.layout.song;
    }

    public void extraViewSetup(final ViewHolder holder, final View itemView) {

    }

    public final class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView title;
        public final TextView desc;
        public final ImageView art;

        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            desc = (TextView) itemView.findViewById(R.id.desc);
            art = (ImageView) itemView.findViewById(R.id.art);
            extraViewSetup(this, itemView);
        }
    }
}
