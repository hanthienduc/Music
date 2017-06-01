package com.mnml.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.ImageView;
import com.boswelja.lastfm.LastFMRequest;
import com.boswelja.lastfm.models.artist.Image;
import com.boswelja.lastfm.models.artist.LastFMArtist;
import com.bumptech.glide.RequestManager;
import com.mnml.music.R;
import com.mnml.music.base.BaseAdapter;
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

public class ArtistAdapter extends BaseAdapter
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
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public void setView(ViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        int albumCount = items.get(adapterPosition).getNumOfAlbums();
        int songCount = items.get(adapterPosition).getNumOfTracks();
        holder.title.setText(items.get(adapterPosition).getName());

        String descBuilder = String.valueOf(albumCount) +
                " " +
                context.getString(albumCount == 1 ? R.string.album : R.string.albums) +
                " \u2022 " +
                String.valueOf(songCount) +
                " " +
                context.getString(songCount == 1 ? R.string.song : R.string.songs);
        holder.desc.setText(descBuilder);

        holder.itemView.setOnClickListener(view -> {
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
                                            glide.load(image.getText()).into(holder.art);
                                            return;
                                        }
                                    }
                                }
                            }
                        } else {
                            loadFromCache(artistName, holder.art);
                        }
                    }

                    @Override
                    public void onFailure(Call<LastFMArtist> call, Throwable throwable) {
                        loadFromCache(artistName, holder.art);
                    }
                }).build();
    }

    private void loadFromCache(final String artistName, final ImageView art) {
        final File file = new File(context.getCacheDir(), artistName + ".png");
        glide.load(file).into(art);
    }

    @Override
    public int layoutId() {
        return R.layout.artist;
    }
}
