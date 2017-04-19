package com.dominionos.music.adapters;

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
import com.dominionos.music.R;
import com.dominionos.music.utils.PaletteBitmap;
import com.dominionos.music.utils.PaletteBitmapTranscoder;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.items.Album;
import com.dominionos.music.ui.activity.AlbumDetailActivity;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private final List<Album> items;
    private final Context context;
    private final BitmapRequestBuilder<String, PaletteBitmap> glideRequest;

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

    public AlbumsAdapter(Context context, List<Album> items, RequestManager glide) {
        this.context = context;
        this.items = items;
        final int px = Utils.dpToPx(context, com.dominionos.music.utils.Config.ALBUM_CARD_WIDTH);
        this.glideRequest = glide
                .fromString()
                .asBitmap()
                .transcode(new PaletteBitmapTranscoder(context), PaletteBitmap.class)
                .centerCrop()
                .override(px, px)
                .error(R.drawable.default_art);
    }

    @Override
    public AlbumsAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.album, parent, false);

        return new SimpleItemViewHolder(itemView);
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
        if(songCount != 1) {
            albumDesc = desc + " • " + songCount + " " + context.getString(R.string.songs);
        } else {
            albumDesc = desc + " • " + songCount + " " + context.getString(R.string.song);
        }
        holder.albumDesc.setText(albumDesc);
        int backCardColor = ResourcesCompat.getColor(context.getResources(), R.color.cardBackground, null);
        if (((ColorDrawable) holder.textHolder.getBackground()).getColor() != backCardColor)
            holder.textHolder.setBackgroundColor(backCardColor);
            glideRequest
                    .load(items.get(adapterPosition).getArtString())
                    .into(new ImageViewTarget<PaletteBitmap>(holder.albumArt) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            super.view.setImageBitmap(resource.bitmap);
                            Palette palette = resource.palette;
                            Palette.Swatch swatch;
                            if(palette.getVibrantSwatch() != null) {
                                swatch = palette.getVibrantSwatch();
                                holder.textHolder.setBackgroundColor(swatch.getRgb());
                                holder.albumName.setTextColor(swatch.getTitleTextColor());
                                holder.albumDesc.setTextColor(swatch.getBodyTextColor());
                            } else if(palette.getDominantSwatch() != null) {
                                swatch = palette.getDominantSwatch();
                                holder.textHolder.setBackgroundColor(swatch.getRgb());
                                holder.albumName.setTextColor(swatch.getTitleTextColor());
                                holder.albumDesc.setTextColor(swatch.getBodyTextColor());
                            }
                        }
                    });
        holder.background.setOnClickListener(v -> {
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
}