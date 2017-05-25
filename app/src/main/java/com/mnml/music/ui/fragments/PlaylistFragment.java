package com.mnml.music.ui.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import com.mnml.music.adapters.PlaylistAdapter;
import com.mnml.music.models.Playlist;
import com.mnml.music.ui.fragments.base.RVFragment;
import com.mnml.music.utils.PlaylistHelper;

import java.util.ArrayList;

public class PlaylistFragment extends RVFragment {

    @Override
    public RecyclerView.Adapter adapter() {
        final Context context = getContext();
        final PlaylistHelper playlistHelper = new PlaylistHelper(context);
        final ArrayList<Playlist> playlistList = new ArrayList<>();
        playlistList.addAll(playlistHelper.getAllPlaylist());
        return new PlaylistAdapter(context, playlistList);
    }
}
