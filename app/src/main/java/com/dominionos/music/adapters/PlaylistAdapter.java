package com.dominionos.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;
import com.dominionos.music.ui.activity.PlaylistActivity;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.MySQLiteHelper;
import com.dominionos.music.items.Playlist;
import com.dominionos.music.utils.Utils;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.SimpleItemViewHolder> {

    private List<Playlist> items;
    private final Context context;

    final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        final TextView gridName;
        final ImageView overflow;
        final View mainView;

        SimpleItemViewHolder(View view) {
            super(view);

            gridName = (TextView) view.findViewById(R.id.playlist_name);
            overflow = (ImageView) view.findViewById(R.id.playlist_menu);
            mainView = view;
        }
    }

    public PlaylistAdapter(Context context, List<Playlist> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public PlaylistAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.playlist, parent, false);


        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        holder.gridName.setText(items.get(position).getName());
        final int finalPosition = position;
        holder.overflow.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_playlist_delete:
                        MySQLiteHelper helper = new MySQLiteHelper(context);
                        helper.removePlayList(items.get(finalPosition).getId());
                        items.remove(finalPosition);
                        notifyItemRemoved(finalPosition);
                        return true;
                    case R.id.menu_playlist_play:
                        Intent i = new Intent();
                        i.putExtra("playlistId", items.get(finalPosition).getId());
                        i.setAction(Config.PLAY_PLAYLIST);
                        context.sendBroadcast(i);
                        return true;
                    case R.id.menu_playlist_rename:
                        showRenamePlaylistPrompt(finalPosition);
                        return true;
                }
                return false;
            });
            popupMenu.inflate(R.menu.playlist_popup_menu);
            popupMenu.show();
        });
            holder.mainView.setOnClickListener(v -> {
                Intent i = new Intent(context, PlaylistActivity.class);
                i.putExtra("playlistId", items.get(finalPosition).getId());
                i.putExtra("title", items.get(finalPosition).getName());
                context.startActivity(i);
            });
    }

    private void showRenamePlaylistPrompt(final int pos) {
        new MaterialDialog.Builder(context)
                .title(R.string.rename_playlist)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(context.getString(R.string.playlist_example), null, (dialog, input) -> {
                    if(!input.toString().equals("")) {
                        MySQLiteHelper helper = new MySQLiteHelper(context);
                        helper.renamePlaylist(input.toString(), items.get(pos).getId());
                        items.get(pos).setName(input.toString());
                        notifyItemChanged(pos);
                    } else {
                        Toast.makeText(context, R.string.playlist_name_empty_warning, Toast.LENGTH_SHORT).show();
                    }
                })
                .positiveText(context.getString(R.string.done))
                .negativeText(context.getString(R.string.cancel)).show();
    }

    public void updateData(List<Playlist> newPlaylistList) {
        items = newPlaylistList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }
}

