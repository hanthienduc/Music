package com.mnml.music.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.mnml.music.R;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.models.Song;
import com.mnml.music.utils.Utils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class SongsFragment extends Fragment {

    private Context context;
    private Unbinder unbinder;
    @BindView(R.id.songs_fragment_list) FastScrollRecyclerView rv;

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
        unbinder.unbind();
    }

    private void init() {
        int accentColor = ThemeStore.accentColor(context);
        rv.setPopupBgColor(accentColor);
        rv.setThumbColor(accentColor);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        final ArrayList<Song> songList = Utils.getAllSongs(context);
        rv.setAdapter(new SongsAdapter(context, songList, Glide.with(context), true));
    }
}
