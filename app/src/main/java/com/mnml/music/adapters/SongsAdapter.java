package com.mnml.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.view.View;
import com.mnml.music.R;
import com.mnml.music.base.BaseAdapter;
import com.mnml.music.models.Song;
import com.mnml.music.utils.Config;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.List;

public class SongsAdapter extends BaseAdapter implements FastScrollRecyclerView.SectionedAdapter {

    private List<Song> items;
    private final Context context;

    public SongsAdapter(
            Context context, List<Song> items) {
        this.context = context;
        this.items = items;
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
    public int layoutId() {
        return R.layout.song;
    }

    @Override
    public void setView(final ViewHolder holder, final int position) {
        holder.title.setText(items.get(position).getName());
        holder.desc.setText(items.get(position).getDesc());
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void extraViewSetup(final ViewHolder holder, final View itemView) {
        final int position = holder.getAdapterPosition();
        itemView.setOnClickListener(v -> {
            Intent a = new Intent();
            a.setAction(Config.PLAY_SINGLE_SONG);
            a.putExtra("song", items.get(position));
            context.sendBroadcast(a);
        });
        itemView.setOnCreateContextMenuListener((contextMenu, view, contextMenuInfo) -> {
            contextMenu.setHeaderTitle(holder.title.getText());
            contextMenu
                    .add(0, view.getId(), 0, context.getString(R.string.menu_play_next))
                    .setOnMenuItemClickListener(menuItem -> {
                        final Intent playNext = new Intent(Config.PLAY_NEXT)
                                .putExtra("song", items.get(position));
                        context.sendBroadcast(playNext);
                        return true;
                    });
            contextMenu
                    .add(0, view.getId(), 0, context.getString(R.string.menu_add_playing))
                    .setOnMenuItemClickListener(menuItem -> {
                        final Intent addToPlaying = new Intent(Config.ADD_SONG_TO_PLAYLIST)
                                .putExtra("song", items.get(position));
                        context.sendBroadcast(addToPlaying);
                        return true;
                    });
            contextMenu
                    .add(0, view.getId(), 0, context.getString(R.string.menu_share))
                    .setOnMenuItemClickListener(menuItem -> {
                        final Intent share = new Intent(Intent.ACTION_SEND)
                                .setType("audio/*")
                                .putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///" + items.get(position).getPath()));
                        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_song)));
                        return true;
                    });
            contextMenu
                    .add(0, view.getId(), 0, context.getString(R.string.menu_delete))
                    .setOnMenuItemClickListener(menuItem -> {
                        final File file = new File(items.get(position).getPath());
                        boolean deleted = file.delete();
                        if(deleted) {
                            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.MediaColumns._ID + "='" + items.get(position).getId() + "'", null);
                            items.remove(position);
                            notifyItemRemoved(position);
                        }
                        return true;
                    });
        });
    }
}
