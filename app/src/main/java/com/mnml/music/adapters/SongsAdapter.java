package com.mnml.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.mnml.music.R;
import com.mnml.music.models.Song;
import com.mnml.music.utils.Config;
import com.mnml.music.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private List<Song> items;
    private final Context context;
    private final Intent intent;

    public SongsAdapter(
            Context context, List<Song> items) {
        this.context = context;
        this.items = items;
        intent = new Intent();
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

    public void updateData(ArrayList<Song> newList) {
        items = newList;
        notifyDataSetChanged();
    }

    @Override
    public SongsAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, int position) {
        final int absolutePosition = holder.getAdapterPosition();
        holder.title.setText(items.get(absolutePosition).getName());
        holder.desc.setText(items.get(absolutePosition).getDesc());
        holder.menu.setOnClickListener(
                v -> {
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    popupMenu.setOnMenuItemClickListener(
                            item -> {
                                switch (item.getItemId()) {
                                    case R.id.menu_play_next:
                                        intent.setAction(Config.PLAY_NEXT);
                                        intent.putExtra("song", items.get(absolutePosition));
                                        context.sendBroadcast(intent);
                                        return true;
                                    case R.id.menu_add_playing:
                                        intent.setAction(Config.ADD_SONG_TO_PLAYLIST);
                                        intent.putExtra("song", items.get(absolutePosition));
                                        context.sendBroadcast(intent);
                                        return true;
                                    case R.id.menu_add_playlist:
                                        Utils.addToPlaylistDialog(context, items.get(absolutePosition));
                                        return true;
                                    case R.id.menu_share:
                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("audio/*");
                                        share.putExtra(
                                                Intent.EXTRA_STREAM,
                                                Uri.parse("file:///" + items.get(absolutePosition).getPath()));
                                        context.startActivity(
                                                Intent.createChooser(share, context.getString(R.string.share_song)));
                                        return true;
                                    case R.id.menu_delete:
                                        File file = new File(items.get(absolutePosition).getPath());
                                        boolean deleted = file.delete();
                                        if (deleted) {
                                            context.getContentResolver()
                                                    .delete(
                                                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                                            MediaStore.MediaColumns._ID
                                                                    + "='"
                                                                    + items.get(absolutePosition).getId()
                                                                    + "'",
                                                            null);
                                            notifyItemRemoved(absolutePosition);
                                            Toast.makeText(context, R.string.song_delete_success, Toast.LENGTH_SHORT)
                                                    .show();
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
            a.putExtra("song", items.get(absolutePosition));
            context.sendBroadcast(a);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class SimpleItemViewHolder
            extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView desc;
        final View view;
        final ImageView menu;
        final View textHolder;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            desc = (TextView) itemView.findViewById(R.id.song_item_desc);
            menu = (ImageView) itemView.findViewById(R.id.song_overflow);
            textHolder = itemView.findViewById(R.id.song_text_holder);
        }
    }
}
