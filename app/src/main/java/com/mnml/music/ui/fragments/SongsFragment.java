package com.mnml.music.ui.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.models.Song;
import com.mnml.music.base.RVFragment;
import com.mnml.music.utils.Utils;

import java.util.ArrayList;

public class SongsFragment extends RVFragment {

    @Override
    public RecyclerView.Adapter adapter() {
        final Context context = getContext();
        final ArrayList<Song> songList = Utils.getAllSongs(context);
        return new SongsAdapter(context, songList);
    }
}
