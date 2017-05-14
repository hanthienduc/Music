package com.mnml.music.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.mnml.music.R;
import com.mnml.music.models.Album;
import com.mnml.music.ui.activity.AlbumDetailActivity;
import com.mnml.music.utils.Config;
import com.mnml.music.utils.glide.PaletteBitmap;
import com.mnml.music.utils.glide.PaletteBitmapTranscoder;
import com.mnml.music.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private List<Album> items;
    private final Context context;
    private final BitmapRequestBuilder<String, PaletteBitmap> glideRequest;
    private final boolean isSearchLayout;

    public AlbumsAdapter(Context context, List<Album> items, RequestManager glide, boolean isSearchLayout) {
        this.context = context;
        this.items = items;
        final int px = Utils.dpToPx(context, Config.ALBUM_CARD_WIDTH);
        this.isSearchLayout = isSearchLayout;
        this.glideRequest = glide
                        .fromString()
                        .asBitmap()
                        .transcode(new PaletteBitmapTranscoder(context), PaletteBitmap.class)
                        .centerCrop()
                        .override(px, px)
                        .error(R.drawable.default_art);
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
    public AlbumsAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(isSearchLayout ? R.layout.album_search : R.layout.album, parent, false);

        return new SimpleItemViewHolder(itemView);
    }

    public void updateData(ArrayList<Album> newList) {
        items = newList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        final String name = items.get(adapterPosition).getName();
        holder.albumArt.setContentDescription(name);
        holder.albumName.setText(name);
        String albumDesc;
        String desc = items.get(adapterPosition).getDesc();
        int songCount = items.get(adapterPosition).getSongCount();
        if (songCount != 1) {
            albumDesc = desc + " • " + songCount + " " + context.getString(R.string.songs);
        } else {
            albumDesc = desc + " • " + songCount + " " + context.getString(R.string.song);
        }
        holder.albumDesc.setText(albumDesc);
        int backCardColor =
                ResourcesCompat.getColor(context.getResources(), R.color.cardBackground, null);
        if (((ColorDrawable) holder.textHolder.getBackground()).getColor() != backCardColor)
            holder.textHolder.setBackgroundColor(backCardColor);
        glideRequest
                .load(items.get(adapterPosition).getArtString())
                .into(
                        new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                            @Override
                            protected void setResource(PaletteBitmap resource) {
                                super.view.setImageBitmap(resource.bitmap);
                                Palette palette = resource.palette;
                                Palette.Swatch swatch;
                                if (palette.getVibrantSwatch() != null) {
                                    swatch = palette.getVibrantSwatch();
                                    holder.textHolder.setBackgroundColor(swatch.getRgb());
                                    holder.albumName.setTextColor(swatch.getTitleTextColor());
                                    holder.albumDesc.setTextColor(swatch.getBodyTextColor());
                                } else if (palette.getDominantSwatch() != null) {
                                    swatch = palette.getDominantSwatch();
                                    holder.textHolder.setBackgroundColor(swatch.getRgb());
                                    holder.albumName.setTextColor(swatch.getTitleTextColor());
                                    holder.albumDesc.setTextColor(swatch.getBodyTextColor());
                                }
                            }
                        });
        holder.background.setOnClickListener(
                v -> {
                    Intent intent = new Intent(context, AlbumDetailActivity.class);
                    intent.putExtra("albumName", items.get(adapterPosition).getName());
                    intent.putExtra("albumId", items.get(adapterPosition).getId());
                    String transitionName = "albumArt";
                    Pair albumArt = new Pair<View, String>(holder.albumArt, transitionName);
                    ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, albumArt);
                    ActivityCompat.startActivity(context, intent, options.toBundle());
                });
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    static final class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        final TextView albumName;
        final TextView albumDesc;
        final ImageView albumArt;
        final View background;
        final View textHolder;

        SimpleItemViewHolder(View view) {
            super(view);

            albumName = (TextView) view.findViewById(R.id.album_name);
            albumDesc = (TextView) view.findViewById(R.id.album_info);
            albumArt = (ImageView) view.findViewById(R.id.album_art);
            textHolder = view.findViewById(R.id.text_holder);
            background = view.findViewById(R.id.background);
        }
    }
}