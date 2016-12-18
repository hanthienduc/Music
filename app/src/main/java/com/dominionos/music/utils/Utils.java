package com.dominionos.music.utils;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.utils.adapters.DialogPlaylistAdapter;
import com.dominionos.music.utils.items.Playlist;
import com.dominionos.music.utils.items.SongListItem;

import java.util.List;

public class Utils {

    public void addToPlaylistDialog(Context context, SongListItem position) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(context);
        List<Playlist> playlist = sqLiteHelper.getAllPlaylist();
        playlist.add(new Playlist(-1, "Create Playlist"));
        new MaterialDialog.Builder(context)
                .title("Add to Playlist")
                .positiveText("Done")
                .adapter(new DialogPlaylistAdapter(context, playlist, position), layoutManager)
                .show();
    }
}
