package com.mnml.music.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.mnml.music.R;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.models.Song;
import com.mnml.music.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class SongsFragment extends Fragment {

    private Context context;
    private Unbinder unbinder;
    @BindView(R.id.songs_fragment_list) FastScrollRecyclerView rv;
    @BindView(R.id.no_songs) TextView noSongs;

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getContext();
        View v = inflater.inflate(R.layout.fragment_songs, container, false);
        unbinder = ButterKnife.bind(this, v);
        init();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unbinder.unbind();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        int accentColor = ContextCompat.getColor(context, R.color.colorAccent);
        rv.setPopupBgColor(accentColor);
        rv.setThumbColor(accentColor);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        final ArrayList<Song> songList = Utils.getAllSongs(context);
        if(songList.isEmpty()) noSongs.setVisibility(View.VISIBLE);
        rv.setAdapter(new SongsAdapter(context, songList, Glide.with(context), true));
    }
}
