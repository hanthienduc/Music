package com.mnml.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mnml.music.R;
import com.mnml.music.models.Song;
import com.mnml.music.utils.Config;

import java.util.ArrayList;

public class PlayingSongAdapter extends RecyclerView.Adapter<PlayingSongAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<Song> songs;

    public PlayingSongAdapter(
            Context context,
            ArrayList<Song> songs) {
        this.context = context;
        this.songs = songs;
        setHasStableIds(true);
    }

    public ArrayList<Song> getData() {
        return songs;
    }

    public void updateData(ArrayList<Song> newSongList) {
        songs = newSongList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition != -1) {
            holder.title.setText(songs.get(adapterPosition).getName());
            holder.desc.setText(songs.get(adapterPosition).getDesc());
        }
        holder.view.setOnClickListener(
                v -> {
                    final Song song = songs.get(adapterPosition);
                    Intent a = new Intent();
                    a.setAction(Config.PLAY_FROM_PLAYLIST);
                    a.putExtra("song", song);
                    context.sendBroadcast(a);
                    notifyDataSetChanged();
                });
    }

    @Override
    public int getItemCount() {
        return this.songs.size();
    }

    @Override
    public long getItemId(int position) {
        return songs.get(position).getId();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView title;
        private final TextView desc;

        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            desc = (TextView) itemView.findViewById(R.id.song_item_desc);
            view = itemView;
        }
    }
}
