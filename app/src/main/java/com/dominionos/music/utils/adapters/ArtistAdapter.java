package com.dominionos.music.utils.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dominionos.music.R;
import com.dominionos.music.utils.items.ArtistListItem;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.SimpleItemViewHolder> {

    private final List<ArtistListItem> items;

    final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        TextView artistName, artistDesc;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            artistName = (TextView) itemView.findViewById(R.id.artist_name);
            artistDesc = (TextView) itemView.findViewById(R.id.artist_desc);
        }
    }

    public ArtistAdapter(List<ArtistListItem> items) {
        this.items = items;
    }

    @Override
    public ArtistAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.artist_list_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, int position) {
        int albumCount = items.get(position).getNumOfAlbums();
        int songCount = items.get(position).getNumOfTracks();
        String artistItemsCount = null;
        holder.artistName.setText(items.get(position).getName());

        if(albumCount == 1 && songCount == 1) {
            artistItemsCount = (albumCount + " Album • " + songCount + " Song");
        } else if (albumCount == 1 && songCount != 1) {
            artistItemsCount = (albumCount + " Album • " + songCount + " Songs");
        } else if (albumCount != 1 && songCount == 1) {
            artistItemsCount = (albumCount + " Albums • " + songCount + " Song");
        } else {
            artistItemsCount = (albumCount + " Albums • " + songCount + " Songs");
        }
        holder.artistDesc.setText(artistItemsCount);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }
}
