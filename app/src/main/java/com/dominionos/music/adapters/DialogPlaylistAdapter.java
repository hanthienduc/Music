package com.dominionos.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
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
import com.dominionos.music.items.Playlist;
import com.dominionos.music.items.Song;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.MySQLiteHelper;
import java.util.List;

public class DialogPlaylistAdapter
    extends RecyclerView.Adapter<DialogPlaylistAdapter.SimpleItemViewHolder> {

  private final List<Playlist> items;
  private final Context context;
  private final Song songToAdd;

  static final class SimpleItemViewHolder extends RecyclerView.ViewHolder {
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

  public DialogPlaylistAdapter(Context context, List<Playlist> items, Song song) {
    this.context = context;
    this.items = items;
    this.songToAdd = song;
  }

  @Override
  public DialogPlaylistAdapter.SimpleItemViewHolder onCreateViewHolder(
      ViewGroup parent, int viewType) {
    View itemView =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.dialog_playlist_item, parent, false);
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
      holder.view.setOnClickListener(v -> showNewPlaylistPrompt());
    } else {
      holder.title.setText(items.get(position).getName());
      final int finalPosition = position;
      holder.menu.setOnClickListener(
          v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.setOnMenuItemClickListener(
                item -> {
                  switch (item.getItemId()) {
                    case R.id.menu_playlist_delete:
                      MySQLiteHelper helper = new MySQLiteHelper(context);
                      helper.removePlayList(items.get(finalPosition).getId());
                      items.remove(finalPosition);
                      notifyItemRemoved(finalPosition);
                      new CountDownTimer(400, 1000) {
                        public void onTick(long millisUntilFinished) {}

                        public void onFinish() {
                          notifyDataSetChanged();
                        }
                      }.start();
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
      holder.view.setOnClickListener(
          v -> {
            MySQLiteHelper helper = new MySQLiteHelper(context);
            if (songToAdd.getId() != -1)
              helper.addSong(songToAdd, items.get(finalPosition).getId());
            else helper.addSong(songToAdd.getName(), items.get(finalPosition).getId());
            Toast.makeText(
                    context,
                    "Song added to " + items.get(finalPosition).getName(),
                    Toast.LENGTH_SHORT)
                .show();
          });
    }
  }

  private void showNewPlaylistPrompt() {
    new MaterialDialog.Builder(context)
        .title(R.string.add_playlist)
        .inputType(InputType.TYPE_CLASS_TEXT)
        .input(
            context.getString(R.string.playlist_example),
            null,
            (dialog, input) -> {
              if (!input.toString().equals("")) {
                MySQLiteHelper helper = new MySQLiteHelper(context);
                items.add(
                    items.size() - 1,
                    new Playlist(helper.createNewPlayList(input.toString()), input.toString()));
                notifyItemInserted(items.size() - 2);
              } else {
                Toast.makeText(context, R.string.playlist_name_empty_warning, Toast.LENGTH_SHORT)
                    .show();
              }
            })
        .positiveText(context.getString(R.string.ok))
        .negativeText(context.getString(R.string.cancel))
        .show();
  }

  private void showRenamePlaylistPrompt(final int pos) {
    new MaterialDialog.Builder(context)
        .title(R.string.rename_playlist)
        .inputType(InputType.TYPE_CLASS_TEXT)
        .input(
            context.getString(R.string.playlist_example),
            null,
            (dialog, input) -> {
              if (!input.toString().equals("")) {
                MySQLiteHelper helper = new MySQLiteHelper(context);
                helper.renamePlaylist(input.toString(), items.get(pos).getId());
                items.get(pos).setName(input.toString());
                notifyItemChanged(pos);
              } else {
                Toast.makeText(context, R.string.playlist_name_empty_warning, Toast.LENGTH_SHORT)
                    .show();
              }
            })
        .positiveText(context.getString(R.string.done))
        .negativeText(context.getString(R.string.cancel))
        .show();
  }

  @Override
  public int getItemCount() {
    return this.items.size();
  }
}
