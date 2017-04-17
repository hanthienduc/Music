package com.dominionos.music.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.RequestManager;
import com.dominionos.music.R;
import com.dominionos.music.ui.activity.ArtistActivity;
import com.dominionos.music.utils.ArtistImgHandler;
import com.dominionos.music.utils.CircleTransform;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.items.Artist;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private final List<Artist> items;
    private final Context context;
    private final boolean darkMode;
    private final DrawableRequestBuilder<File> glideRequest;

    @NonNull
    @Override
    public String getSectionName(int position) {
        String character = items.get(position).getName().substring(0, 1);
        if(character.matches("[a-zA-Z]")) {
            return items.get(position).getName().substring(0,1);
        } else {
            return "\u2605";
        }
    }


    final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        final TextView artistName;
        final TextView artistDesc;
        final ImageView artistImg;
        final View view;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            artistName = (TextView) itemView.findViewById(R.id.artist_name);
            artistDesc = (TextView) itemView.findViewById(R.id.artist_desc);
            artistImg = (ImageView) itemView.findViewById(R.id.artist_image);
            view = itemView;
        }
    }

    public ArtistAdapter(Context context, List<Artist> items, boolean darkMode, RequestManager glide) {
        this.items = items;
        this.context = context;
        this.darkMode = darkMode;
        final int px = Utils.dpToPx(context, 48);
        this.glideRequest = glide
                .fromFile()
                .centerCrop()
                .transform(new CircleTransform(context))
                .override(px, px);
    }

    @Override
    public ArtistAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.artist, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, int position) {
        Utils.setPrimaryTextColor(holder.artistName, context, darkMode);
        Utils.setSecondaryTextColor(holder.artistDesc, context, darkMode);
        final int adapterPosition = holder.getAdapterPosition();
        int albumCount = items.get(adapterPosition).getNumOfAlbums();
        int songCount = items.get(adapterPosition).getNumOfTracks();
        String artistItemsCount;
        holder.artistName.setText(items.get(adapterPosition).getName());
        getArtistImg(holder, adapterPosition);

        if(albumCount == 1 && songCount == 1) {
            artistItemsCount = (albumCount + " " + context.getString(R.string.album) + " • " + songCount + " " + context.getString(R.string.song));
        } else if (albumCount == 1) {
            artistItemsCount = (albumCount + " " + context.getString(R.string.album) + " • " + songCount + " " + context.getString(R.string.songs));
        } else if (songCount == 1) {
            artistItemsCount = (albumCount + " " + context.getString(R.string.albums) + " • " + songCount + " " + context.getString(R.string.song));
        } else {
            artistItemsCount = (albumCount + " " + context.getString(R.string.albums) + " • " + songCount + " " + context.getString(R.string.songs));
        }
        holder.artistDesc.setText(artistItemsCount);

        holder.view.setOnClickListener(view -> {
            Intent i = new Intent(context, ArtistActivity.class);
            i.putExtra("artistName", items.get(adapterPosition).getName());
            context.startActivity(i);
        });
    }

    private void getArtistImg(final SimpleItemViewHolder holder, int position) {
        ArtistImgHandler imgHandler = new ArtistImgHandler(context) {
            @Override
            public void onDownloadComplete(final String url) {
                if (url != null)
                    ((Activity) context).runOnUiThread(() -> setImageToView(url, holder));
            }
        };
        String path = imgHandler.getArtistImgFromDB("name");
        if (path != null && !path.matches("")) {
            setImageToView(path, holder);
        } else {
            String urlIfAny = imgHandler.getArtistArtWork(items.get(position).getName(), position);
            setImageToView(urlIfAny, holder);
        }
    }

    private void setImageToView(String url, final SimpleItemViewHolder holder) {
        try {
            glideRequest
                    .load(new File(url))
                    .into(holder.artistImg);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }
}
