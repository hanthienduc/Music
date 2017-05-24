package com.mnml.music.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mnml.music.R;
import com.mnml.music.models.Song;
import com.mnml.music.utils.Config;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SimpleItemViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

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
    public SongsAdapter.SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, int position) {
        final int absolutePosition = holder.getAdapterPosition();
        holder.title.setText(items.get(absolutePosition).getName());
        holder.desc.setText(items.get(absolutePosition).getDesc());
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

    final class SimpleItemViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {
        private final TextView title;
        private final TextView desc;
        private final View view;

        SimpleItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            title = (TextView) itemView.findViewById(R.id.song_item_name);
            desc = (TextView) itemView.findViewById(R.id.song_item_desc);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            final int position = SimpleItemViewHolder.this.getAdapterPosition();

            contextMenu.setHeaderTitle(title.getText());
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
        }
    }
}
