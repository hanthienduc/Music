package com.mnml.music.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mnml.music.R;
import com.mnml.music.adapters.PlaylistAdapter;
import com.mnml.music.models.Playlist;
import com.mnml.music.utils.PlaylistHelper;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private View mainView;
    private RecyclerView rv;
    private PlaylistHelper helper;

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
        helper = new PlaylistHelper(mainView.getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        rv.setLayoutManager(linearLayoutManager);
        rv.setHasFixedSize(true);
        updateList();
    }

    public void updateList() {
        PlaylistAdapter adapter = (PlaylistAdapter) rv.getAdapter();
        List<Playlist> playlistList = helper.getAllPlaylist();
        if (adapter != null) {
            adapter.updateData(playlistList);
        } else {
            rv.setAdapter(new PlaylistAdapter(mainView.getContext(), playlistList));
        }
    }
}
