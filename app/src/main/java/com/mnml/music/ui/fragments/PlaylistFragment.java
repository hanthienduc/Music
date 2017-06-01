package com.mnml.music.ui.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import com.mnml.music.adapters.PlaylistAdapter;
import com.mnml.music.models.Playlist;
import com.mnml.music.base.RVFragment;
import com.mnml.music.utils.PlaylistHelper;

import java.util.ArrayList;

public class PlaylistFragment extends RVFragment {

    private PlaylistHelper helper;

    public PlaylistHelper getHelper() {
        return helper;
    }

    @Override
    public void extraCreate() {
        helper = new PlaylistHelper(getContext());
    }

    @Override
    public RecyclerView.Adapter adapter() {
        final Context context = getContext();
        final ArrayList<Playlist> playlistList = new ArrayList<>();
        playlistList.addAll(helper.getAllPlaylist());
        return new PlaylistAdapter(context, playlistList, helper);
    }
}
