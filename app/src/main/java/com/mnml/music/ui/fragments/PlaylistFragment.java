package com.mnml.music.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.mnml.music.R;
import com.mnml.music.adapters.PlaylistAdapter;
import com.mnml.music.items.Playlist;
import com.mnml.music.utils.MySQLiteHelper;
import com.mnml.music.utils.Utils;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private View mainView;
    private RecyclerView rv;
    private MySQLiteHelper helper;

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlists, container, false);
        this.mainView = v;
        rv = (RecyclerView) mainView.findViewById(R.id.playlist_list);

        getPlaylistList();

        return v;
    }

    private void getPlaylistList() {
        helper = new MySQLiteHelper(mainView.getContext());
        List<Playlist> playlistList = helper.getAllPlaylist();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        rv.setLayoutManager(linearLayoutManager);
        rv.setHasFixedSize(true);
        rv.setAdapter(new PlaylistAdapter(mainView.getContext(), playlistList));
        rv.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> Utils.setEdgeGlowColor(rv, ThemeStore.primaryColor(getContext())));
    }

    public void updateList() {
        PlaylistAdapter adapter = (PlaylistAdapter) rv.getAdapter();
        if (adapter != null) {
            adapter.updateData(helper.getAllPlaylist());
        }
    }
}
