package com.dominionos.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.async.Action;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.RequestManager;
import com.dominionos.music.R;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.items.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private final List<Song> items;
    private final Context context;
    private final boolean darkMode, shouldHaveArt;
    private final Intent i;
    private final DrawableRequestBuilder<String> glideRequest;

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
        final TextView title;
        final TextView desc;
        final View view;
        final ImageView menu;
        final ImageView art;
        final View textHolder;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            desc = (TextView) itemView.findViewById(R.id.song_item_desc);
            menu = (ImageView) itemView.findViewById(R.id.playing_bar_action);
            art = (ImageView) itemView.findViewById(R.id.song_item_art);
            textHolder = itemView.findViewById(R.id.song_text_holder);
        }
    }

    public SongsAdapter(Context context, List<Song> items, boolean darkMode, RequestManager glide, boolean shouldHaveArt) {
        this.context = context;
        this.items = items;
        this.darkMode = darkMode;
        i = new Intent();
        final int px = Utils.dpToPx(context, 48);
        this.glideRequest = glide
                .fromString()
                .centerCrop()
                .override(px, px)
                .crossFade();
        this.shouldHaveArt = shouldHaveArt;
    }

    @Override
    public SongsAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.song, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, int position) {
        final int adapterPosition = holder.getAdapterPosition();
        Utils.setPrimaryTextColor(holder.title, context, darkMode);
        Utils.setSecondaryTextColor(holder.title, context, darkMode);
        Utils.setOverflowColor(holder.menu, context, darkMode);
        holder.title.setText(items.get(adapterPosition).getName());
        holder.desc.setText(items.get(adapterPosition).getDesc());
        holder.menu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_play_next:
                        i.setAction(Config.PLAY_NEXT);
                        i.putExtra("song", items.get(adapterPosition));
                        context.sendBroadcast(i);
                        return true;
                    case R.id.menu_add_playing:
                        i.setAction(Config.ADD_SONG_TO_PLAYLIST);
                        i.putExtra("song", items.get(adapterPosition));
                        context.sendBroadcast(i);
                        return true;
                    case R.id.menu_add_playlist:
                        Utils.addToPlaylistDialog(context, items.get(adapterPosition));
                        return true;
                    case R.id.menu_share:
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("audio/*");
                        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + items.get(adapterPosition).getPath()));
                        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_song)));
                        return true;
                    case R.id.menu_delete:
                        File file = new File(items.get(adapterPosition).getPath());
                        boolean deleted = file.delete();
                        if (deleted) {
                            Toast.makeText(context, R.string.song_delete_success, Toast.LENGTH_SHORT).show();
                            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                    MediaStore.MediaColumns._ID + "='" + items.get(adapterPosition).getId() + "'", null);
                            notifyItemRemoved(adapterPosition);
                        } else
                            Toast.makeText(context, R.string.song_delete_fail, Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            });
            popupMenu.inflate(R.menu.song_popup_menu);
            popupMenu.show();
        });
        holder.view.setOnClickListener(v -> {
            Intent a = new Intent();
            a.setAction(Config.PLAY_SINGLE_SONG);
            a.putExtra("song", items.get(adapterPosition));
            context.sendBroadcast(a);
        });
        if(shouldHaveArt) {
            new Action<String>() {

                @NonNull
                @Override
                public String id() {
                    return "set_song_art";
                }

                @Nullable
                @Override
                protected String run() throws InterruptedException {
                    return Utils.getAlbumArt(context, items.get(adapterPosition).getAlbumId());
                }

                @Override
                protected void done(String result) {
                    glideRequest
                            .load(result)
                            .into(holder.art);
                }
            }.execute();
            holder.art.setContentDescription(items.get(adapterPosition).getAlbumName());
        } else {
            holder.art.setVisibility(View.GONE);
            holder.textHolder.setPaddingRelative(Utils.dpToPx(context, 16), 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

}