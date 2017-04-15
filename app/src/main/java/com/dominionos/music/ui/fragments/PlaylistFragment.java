package com.dominionos.music.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dominionos.music.R;
import com.dominionos.music.utils.MySQLiteHelper;
import com.dominionos.music.adapters.PlaylistAdapter;
import com.dominionos.music.items.Playlist;
import com.dominionos.music.utils.Utils;

import java.util.List;

public class PlaylistFragment extends Fragment {

    private View mainView;
    private RecyclerView rv;
    private boolean darkMode;
    private MySQLiteHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlists, container, false);
        this.mainView = v;
        rv = (RecyclerView) mainView.findViewById(R.id.playlist_list);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        darkMode = sharedPref.getBoolean("dark_theme", false);

        getPlaylistList();
        Utils.setWindowColor(rv, getContext(), darkMode);

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
        rv.setAdapter(new PlaylistAdapter(mainView.getContext(), playlistList, darkMode));
    }

    public void updateList() {
        PlaylistAdapter adapter = (PlaylistAdapter) rv.getAdapter();
        if(adapter != null) {
            adapter.updateData(helper.getAllPlaylist());
        }
    }

}