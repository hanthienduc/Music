package com.mnml.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.boswelja.lastfm.LastFMRequest;
import com.boswelja.lastfm.models.artist.Image;
import com.boswelja.lastfm.models.artist.LastFMArtist;
import com.bumptech.glide.RequestManager;
import com.mnml.music.R;
import com.mnml.music.models.Artist;
import com.mnml.music.ui.activity.ArtistDetailActivity;
import com.mnml.music.utils.Utils;
import com.mnml.music.utils.GlideUtils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private List<Artist> items;
    private final Context context;
    private RequestManager glide;

    public ArtistAdapter(Context context, List<Artist> items, RequestManager glide) {
        this.items = items;
        this.context = context;
        final int px = Utils.dpToPx(context, 72);
        this.glide = glide.applyDefaultRequestOptions(GlideUtils.glideOptions(px, true, R.drawable.default_art));
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String character = items.get(position).getName().substring(0, 1);
        if (character.matches("[a-zA-Z]")) {
            return items.get(position).getName().substring(0, 1);
        } else {
            return "\u2605";
        }
    }

    @Override
    public ArtistAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.artist, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        int albumCount = items.get(adapterPosition).getNumOfAlbums();
        int songCount = items.get(adapterPosition).getNumOfTracks();
        String artistItemsCount;
        holder.artistName.setText(items.get(adapterPosition).getName());

        if (albumCount == 1 && songCount == 1) {
            artistItemsCount =
                    (albumCount
                            + " "
                            + context.getString(R.string.album)
                            + " • "
                            + songCount
                            + " "
                            + context.getString(R.string.song));
        } else if (albumCount == 1) {
            artistItemsCount =
                    (albumCount
                            + " "
                            + context.getString(R.string.album)
                            + " • "
                            + songCount
                            + " "
                            + context.getString(R.string.songs));
        } else if (songCount == 1) {
            artistItemsCount =
                    (albumCount
                            + " "
                            + context.getString(R.string.albums)
                            + " • "
                            + songCount
                            + " "
                            + context.getString(R.string.song));
        } else {
            artistItemsCount =
                    (albumCount
                            + " "
                            + context.getString(R.string.albums)
                            + " • "
                            + songCount
                            + " "
                            + context.getString(R.string.songs));
        }
        holder.artistDesc.setText(artistItemsCount);

        holder.view.setOnClickListener(view -> {
            Intent i = new Intent(context, ArtistDetailActivity.class);
            i.putExtra("artistName", items.get(adapterPosition).getName());
            context.startActivity(i);
        });

        final String artistName = Uri.parse(items.get(adapterPosition).getName()).toString();
        new LastFMRequest()
                .setApiKey(context.getString(R.string.lastfm_api_key))
                .setQuery(artistName)
                .setArtist()
                .setCallback(new Callback<LastFMArtist>() {
                    @Override
                    public void onResponse(Call<LastFMArtist> call, Response<LastFMArtist> response) {
                        if(response.isSuccessful()) {
                            final LastFMArtist responseBody = response.body();
                            if(responseBody != null && responseBody.getArtist() != null) {
                                final List<Image> images = responseBody.getArtist().getImage();
                                if(images != null) {
                                    for (Image image : images) {
                                        if(image.getSize().equals("medium")) {
                                            glide.load(image.getText()).into(holder.artistImg);
                                            return;
                                        }
                                    }
                                }
                            }
                        } else {
                            final File file = new File(context.getCacheDir(), artistName + ".png");
                            glide.load(file).into(holder.artistImg);
                        }
                    }

                    @Override
                    public void onFailure(Call<LastFMArtist> call, Throwable throwable) {
                        File file = new File(context.getCacheDir(), artistName + ".png");
                        glide.load(file).into(holder.artistImg);
                    }
                }).build();
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    static final class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView artistName;
        private final TextView artistDesc;
        private final ImageView artistImg;
        private final View view;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            artistName = (TextView) itemView.findViewById(R.id.artist_name);
            artistDesc = (TextView) itemView.findViewById(R.id.artist_desc);
            artistImg = (ImageView) itemView.findViewById(R.id.artist_image);
            view = itemView;
        }
    }
}
