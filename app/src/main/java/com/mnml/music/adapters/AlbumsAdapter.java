package com.mnml.music.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mnml.music.R;
import com.mnml.music.models.Album;
import com.mnml.music.ui.activity.AlbumDetailActivity;
import com.mnml.music.utils.Config;
import com.mnml.music.utils.Utils;
import com.mnml.music.utils.GlideUtils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private List<Album> items;
    private Context context;
    private final RequestManager glide;
    private int px;

    public AlbumsAdapter(Context context, List<Album> items, RequestManager glide) {
        this.context = context;
        this.items = items;
        px = Utils.dpToPx(context, Config.ALBUM_CARD_WIDTH);
        this.glide = glide.applyDefaultRequestOptions(GlideUtils.glideOptions(px, false, R.drawable.default_art));
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album, parent, false);

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
        glide
                .load(items.get(adapterPosition).getArtString())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Drawable> target, boolean b) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable drawable, Object o, Target<Drawable> target, DataSource dataSource, boolean b) {
                        final Palette.Builder palette = Palette.from(GlideUtils.convertToBitmap(drawable, px, px));
                        palette.generate(palette1 ->
                                holder.background.setBackgroundColor(
                                        palette1.getVibrantColor(
                                                palette1.getDominantColor(
                                                        palette1.getMutedColor(
                                                                context.getColor(R.color.colorAccent))))
                        ));
                        return false;
                    }
                })
                .into(holder.albumArt);
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    final class SimpleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView albumName;
        private final TextView albumDesc;
        private final ImageView albumArt;
        private final View background;
        private final View textHolder;

        SimpleItemViewHolder(View view) {
            super(view);

            albumName = (TextView) view.findViewById(R.id.album_name);
            albumDesc = (TextView) view.findViewById(R.id.album_info);
            albumArt = (ImageView) view.findViewById(R.id.album_art);
            textHolder = view.findViewById(R.id.text_holder);
            background = view.findViewById(R.id.background);
        }

        @Override
        public void onClick(View view) {
            final int adapterPosition = this.getAdapterPosition();
            final Intent intent = new Intent(context, AlbumDetailActivity.class);
            intent.putExtra("albumName", items.get(adapterPosition).getName());
            intent.putExtra("albumId", items.get(adapterPosition).getId());
            final String transitionName = "albumArt";
            Pair albumArtPair = new Pair<View, String>(albumArt, transitionName);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, albumArtPair);
            ActivityCompat.startActivity(context, intent, options.toBundle());
        }
    }
}
