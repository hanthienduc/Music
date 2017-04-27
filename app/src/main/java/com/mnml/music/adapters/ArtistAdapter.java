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
import android.widget.Toast;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mnml.music.R;
import com.mnml.music.models.Artist;
import com.mnml.music.ui.activity.ArtistDetailActivity;
import com.mnml.music.utils.glide.CircleTransform;
import com.mnml.music.utils.Utils;
import com.mnml.music.utils.retrofit.Image;
import com.mnml.music.utils.retrofit.LastFMApi;
import com.mnml.music.utils.retrofit.LastFMArtist;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private final List<Artist> items;
    private final Context context;
    private final DrawableRequestBuilder<String> glideRequest;

    public ArtistAdapter(Context context, List<Artist> items, RequestManager glide) {
        this.items = items;
        this.context = context;
        final int px = Utils.dpToPx(context, 48);
        this.glideRequest = glide
                .fromString()
                .centerCrop()
                .transform(new CircleTransform(context))
                .override(px, px)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(R.drawable.default_art)
                .crossFade();
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

        //TODO Turn this into a class we can call from anywhere
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://ws.audioscrobbler.com/2.0/")
                .build();
        LastFMApi lastFMApi = retrofit.create(LastFMApi.class);
        final String artistName = Uri.parse(items.get(adapterPosition).getName()).toString();
        Call<LastFMArtist> call = lastFMApi.getArtist(artistName);
        call.enqueue(new Callback<LastFMArtist>() {
            @Override
            public void onResponse(Call<LastFMArtist> call, Response<LastFMArtist> response) {
                if(response.isSuccessful()) {
                    LastFMArtist artist = response.body();
                    if(artist != null) {
                        com.mnml.music.utils.retrofit.Artist artistInfo = artist.getArtist();
                        if(artistInfo != null) {
                            List<Image> images = artistInfo.getImage();
                            if(images != null && !images.isEmpty()) {
                                for(final Image image : images) {
                                    if(image.getSize().equals("medium")) {
                                        glideRequest
                                                .load(image.getText())
                                                .into(holder.artistImg);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<LastFMArtist> call, Throwable throwable) {
                Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    static final class SimpleItemViewHolder extends RecyclerView.ViewHolder {
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
}
