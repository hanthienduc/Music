package com.dominionos.music.utils.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.utils.MySQLiteHelper;
import com.dominionos.music.utils.items.Playlist;
import com.dominionos.music.utils.items.SongListItem;

import java.util.List;

class DialogPlaylistAdapter extends RecyclerView.Adapter<DialogPlaylistAdapter.SimpleItemViewHolder> {

    private final List<Playlist> items;
    private final Context context;
    private final SongListItem songToAdd;
    private final AlertDialog dialog;

    final static class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView add;
        public final View view;
        public final ImageView menu;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (TextView) itemView.findViewById(R.id.playlist_item_name);
            menu = (ImageView) itemView.findViewById(R.id.playlist_item_menu);
            add = (TextView) itemView.findViewById(R.id.playlist_item_add_new);
        }
    }

    DialogPlaylistAdapter(Context context, List<Playlist> items,
                          SongListItem songListItem, AlertDialog dialog) {
        this.context = context;
        this.items = items;
        this.songToAdd = songListItem;
        this.dialog = dialog;
    }

    @Override
    public DialogPlaylistAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.dialog_playlist_item, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        if (items.get(position).getId() == -1) {
            holder.title.setVisibility(View.GONE);
            holder.menu.setVisibility(View.GONE);
            holder.add.setVisibility(View.VISIBLE);
            holder.add.setText(items.get(position).getName());
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNewPlaylistPrompt();
                }
            });
        } else {
            holder.title.setText(items.get(position).getName());
            final int finalPosition = position;
            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu_playlist_delete:
                                    MySQLiteHelper helper = new MySQLiteHelper(context);
                                    helper.removePlayList(items.get(finalPosition).getId());
                                    items.remove(finalPosition);
                                    notifyItemRemoved(finalPosition);
                                    new CountDownTimer(400, 1000) {
                                        public void onTick(long millisUntilFinished) {
                                        }

                                        public void onFinish() {
                                            notifyDataSetChanged();
                                        }
                                    }.start();
                                    return true;
                                case R.id.menu_playlist_play:
                                    Intent i = new Intent();
                                    i.putExtra("playlistId", items.get(finalPosition).getId());
                                    i.setAction(MusicService.ACTION_PLAY_PLAYLIST);
                                    context.sendBroadcast(i);
                                    return true;
                                case R.id.menu_playlist_rename:
                                    showRenamePlaylistPrompt(finalPosition);
                                    return true;
                            }
                            return false;
                        }
                    });
                    popupMenu.inflate(R.menu.playlist_popup_menu);
                    popupMenu.show();
                }
            });
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MySQLiteHelper helper = new MySQLiteHelper(context);
                    if (songToAdd.getId() != -1)
                        helper.addSong(songToAdd, items.get(finalPosition).getId());
                    else
                        helper.addSong(songToAdd.getName(), items.get(finalPosition).getId());
                    dialog.dismiss();
                    Toast.makeText(context, "Song added to playlist", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showNewPlaylistPrompt() {
        new MaterialDialog.Builder(context)
                .title("Create playlist")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("e.g. Favourites", null, new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if(!input.toString().equals("")) {
                            MySQLiteHelper helper = new MySQLiteHelper(context);
                            items.add(items.size() - 1, new Playlist(helper.createNewPlayList(
                                    input.toString()), input.toString()));
                            notifyItemInserted(items.size() - 2);
                        } else {
                            Toast.makeText(context, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    private void showRenamePlaylistPrompt(final int pos) {
        new MaterialDialog.Builder(context)
                .title("Rename playlist")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("e.g. Favourites", null, new MaterialDialog.InputCallback() {

                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if(!input.toString().equals("")) {
                            MySQLiteHelper helper = new MySQLiteHelper(context);
                            helper.renamePlaylist(input.toString(), items.get(pos).getId());
                            items.get(pos).setName(input.toString());
                            notifyItemChanged(pos);
                        } else {
                            Toast.makeText(context, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }
}
