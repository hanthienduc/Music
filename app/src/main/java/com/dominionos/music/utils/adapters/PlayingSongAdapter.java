package com.dominionos.music.utils.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dominionos.music.R;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.utils.items.SongListItem;

import java.util.List;

public class PlayingSongAdapter extends RecyclerView.Adapter<PlayingSongAdapter.SimpleItemViewHolder> {

    private final List<SongListItem> songs;
    private final Context context;

    final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView desc;
        public final View view;
        public final ImageView menu;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            desc = (TextView) itemView.findViewById(R.id.song_item_desc);
            view = itemView;
            menu = (ImageView) itemView.findViewById(R.id.song_item_menu);
        }
    }

    public PlayingSongAdapter(Context context, List<SongListItem> songs) {
        this.context = context;
        this.songs = songs;
    }

    @Override
    public PlayingSongAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.song_list_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        holder.title.setText(songs.get(position).getName());
        holder.desc.setText(songs.get(position).getDesc());
        final int finalPosition = position;
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent();
                a.setAction(MusicService.ACTION_PLAY_FROM_PLAYLIST);
                a.putExtra("playListId", songs.get(finalPosition).getId() + "");
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
                                a.setAction(MusicService.ACTION_MENU_FROM_PLAYLIST);
                                a.putExtra("count", finalPosition);
                                a.putExtra("action", MusicService.ACTION_MENU_PLAY_NEXT);
                                context.sendBroadcast(a);
                                return true;
                            case R.id.menu_remove_playing:
                                Intent b = new Intent();
                                b.setAction(MusicService.ACTION_MENU_FROM_PLAYLIST);
                                b.putExtra("count", finalPosition);
                                b.putExtra("action", MusicService.ACTION_MENU_REMOVE_FROM_QUEUE);
                                context.sendBroadcast(b);
                                notifyItemRemoved(finalPosition);
                                return true;
                            case R.id.menu_add_playlist:
                                addToPlaylist(finalPosition);
                                return true;
                            case R.id.menu_share:
                                Intent c = new Intent();
                                c.setAction(MusicService.ACTION_MENU_FROM_PLAYLIST);
                                c.putExtra("count", (int) songs.get(finalPosition).getId());
                                c.putExtra("action", MusicService.ACTION_MENU_SHARE);
                                context.sendBroadcast(c);
                                return true;
                            case R.id.menu_delete:
                                Intent d = new Intent();
                                d.setAction(MusicService.ACTION_MENU_FROM_PLAYLIST);
                                d.putExtra("count", finalPosition);
                                d.putExtra("action", MusicService.ACTION_MENU_DELETE);
                                context.sendBroadcast(d);
                                notifyItemRemoved(finalPosition);
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

    private void addToPlaylist(int position) {
        SongListItem item = songs.get(position);
        Utils utils = new Utils();
        utils.addToPlaylistDialog(context, item);
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
