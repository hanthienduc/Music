package com.dominionos.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.async.Action;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.RequestManager;
import com.dominionos.music.R;
import com.dominionos.music.utils.CircleTransform;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.items.Song;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.ArrayList;
import java.util.List;


public class PlayingSongAdapter extends RecyclerView.Adapter<PlayingSongAdapter.ViewHolder>
        implements DraggableItemAdapter<PlayingSongAdapter.ViewHolder>{

    private List<Song> songs;
    private final Context context;
    private final boolean darkMode;
    private Song currentSong;
    private final DrawableRequestBuilder<String> glideRequest;

    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        return true;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        Song movedSong = songs.get(fromPosition);
        songs.remove(movedSong);
        songs.add(toPosition, movedSong);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    final static class ViewHolder extends AbstractDraggableItemViewHolder {
        final TextView title;
        final TextView desc;
        public final View view;
        public final ImageView menu;
        final ImageView art;

        ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            desc = (TextView) itemView.findViewById(R.id.song_item_desc);
            view = itemView;
            menu = (ImageView) itemView.findViewById(R.id.playing_bar_action);
            art = (ImageView) itemView.findViewById(R.id.song_item_art);
        }
    }

    public void updateData(ArrayList<Song> newSongList, Song currentSong) {
        songs = newSongList;
        this.currentSong = currentSong;
        notifyDataSetChanged();
    }

    public PlayingSongAdapter(Context context, List<Song> songs, boolean darkMode, Song currentSong, RequestManager glide) {
        this.context = context;
        this.songs = songs;
        this.darkMode = darkMode;
        this.currentSong = currentSong;
        final int px = Utils.dpToPx(context, 72);
        this.glideRequest = glide
                .fromString()
                .centerCrop()
                .crossFade()
                .transform(new CircleTransform(context))
                .override(px, px);
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.song, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final int adapterPosition = holder.getAdapterPosition();
        if(darkMode) {
            holder.title.setTextColor(ContextCompat.getColor(context, R.color.primaryTextDark));
            holder.desc.setTextColor(ContextCompat.getColor(context, R.color.secondaryTextDark));
            holder.menu.setColorFilter(ContextCompat.getColor(context, R.color.primaryTextDark));
        }
        if(adapterPosition != -1) {
            holder.title.setText(songs.get(adapterPosition).getName());
            holder.desc.setText(songs.get(adapterPosition).getDesc());
            if(songs.get(adapterPosition) != currentSong) {
                new Action<String>() {

                    @NonNull
                    @Override
                    public String id() {
                        return "song_art";
                    }

                    @Nullable
                    @Override
                    protected String run() throws InterruptedException {
                        return Utils.getAlbumArt(context, songs.get(adapterPosition).getAlbumId());
                    }

                    @Override
                    protected void done(String result) {
                        holder.art.clearColorFilter();
                        glideRequest
                                .load(result)
                                .into(holder.art);
                    }
                }.execute();
            } else {
                holder.art.setImageResource(R.drawable.ic_audiotrack);
                holder.art.setColorFilter(darkMode
                        ? ContextCompat.getColor(context, R.color.primaryTextDark)
                        : ContextCompat.getColor(context, R.color.primaryTextLight));
            }
        }
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent();
                a.setAction(Config.PLAY_FROM_PLAYLIST);
                a.putExtra("song", songs.get(adapterPosition));
                context.sendBroadcast(a);
            }
        });
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_play_next:
                                Intent a = new Intent();
                                a.setAction(Config.MENU_FROM_PLAYLIST);
                                a.putExtra("count", adapterPosition);
                                a.putExtra("action", Config.MENU_PLAY_NEXT);
                                context.sendBroadcast(a);
                                return true;
                            case R.id.menu_remove_playing:
                                Intent b = new Intent();
                                b.setAction(Config.MENU_FROM_PLAYLIST);
                                b.putExtra("count", adapterPosition);
                                b.putExtra("action", Config.MENU_REMOVE_FROM_QUEUE);
                                context.sendBroadcast(b);
                                notifyItemRemoved(adapterPosition);
                                return true;
                            case R.id.menu_add_playlist:
                                Song song = songs.get(adapterPosition);
                                Utils.addToPlaylistDialog(context, song);
                                return true;
                            case R.id.menu_share:
                                Intent c = new Intent();
                                c.setAction(Config.MENU_FROM_PLAYLIST);
                                c.putExtra("count", (int) songs.get(adapterPosition).getId());
                                c.putExtra("action", Config.MENU_SHARE);
                                context.sendBroadcast(c);
                                return true;
                            case R.id.menu_delete:
                                Intent d = new Intent();
                                d.setAction(Config.MENU_FROM_PLAYLIST);
                                d.putExtra("count", adapterPosition);
                                d.putExtra("action", Config.MENU_DELETE);
                                context.sendBroadcast(d);
                                notifyItemRemoved(adapterPosition);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.playing_popup_menu);
                popupMenu.show();
            }
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
}
