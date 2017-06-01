package com.mnml.music.adapters;

import android.content.Context;
import android.content.Intent;
import com.mnml.music.R;
import com.mnml.music.base.BaseAdapter;
import com.mnml.music.models.Playlist;
import com.mnml.music.ui.activity.PlaylistActivity;
import com.mnml.music.utils.PlaylistHelper;

import java.util.ArrayList;

public class PlaylistAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<Playlist> items;
    private PlaylistHelper helper;

    public PlaylistAdapter(Context context, ArrayList<Playlist> items, PlaylistHelper helper) {
        this.context = context;
        this.items = items;
        this.helper = helper;
    }

    @Override
    public void setView(ViewHolder holder, int position) {
        final Playlist playlist = items.get(position);
        holder.title.setText(playlist.getName());
        holder.itemView.setOnClickListener(
                v -> {
                    Intent i = new Intent(context, PlaylistActivity.class);
                    i.putExtra("playlistId", playlist.getId());
                    i.putExtra("title", playlist.getName());
                    context.startActivity(i);
                });
        final int songCount = new PlaylistHelper(context).getPlayListSongs(playlist.getId()).size();
        holder.desc.setText(String.valueOf(songCount) + " " + context.getString(songCount == 1 ? R.string.song : R.string.songs));

        holder.itemView.setOnCreateContextMenuListener((contextMenu, view, contextMenuInfo) -> {
            contextMenu.setHeaderTitle(playlist.getName());
            contextMenu
                    .add(context.getString(R.string.rename_playlist))
                    .setOnMenuItemClickListener(menuItem -> {
                        helper.showRenamePlaylistPrompt(this, playlist, position);
                        return true;
                    });
            contextMenu
                    .add(context.getString(R.string.menu_playlist_delete))
                    .setOnMenuItemClickListener(menuItem -> {
                        helper.removePlayList(playlist, this);
                        return true;
                    });
        });
    }

    @Override
    public int layoutId() {
        return R.layout.song;
    }

    public ArrayList<Playlist> getItems() {
        return items;
    }

    public void updateData(ArrayList<Playlist> newPlaylistList) {
        items = newPlaylistList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

}
