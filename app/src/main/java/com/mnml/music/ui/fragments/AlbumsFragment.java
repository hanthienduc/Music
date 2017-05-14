package com.mnml.music.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
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
import com.mnml.music.adapters.AlbumsAdapter;
import com.mnml.music.models.Album;
import com.mnml.music.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class AlbumsFragment extends Fragment {

    @BindView(R.id.album_grid) FastScrollRecyclerView albumGrid;
    @BindView(R.id.no_albums) TextView noAlbums;
    private Unbinder unbinder;
    private Context context;

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_albums, container, false);
        unbinder = ButterKnife.bind(this, v);

        context = getContext();

        final int accentColor = ContextCompat.getColor(context, R.color.colorAccent);

        albumGrid.setPopupBgColor(accentColor);
        albumGrid.setThumbColor(accentColor);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, Utils.calculateNoOfColumns(context));
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        albumGrid.setLayoutManager(gridLayoutManager);
        albumGrid.setHasFixedSize(true);

        getAlbumList();

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

    private void getAlbumList() {
        final ArrayList<Album> albumList = Utils.getAlbums(context);
        albumGrid.setAdapter(new AlbumsAdapter(context, albumList, Glide.with(context), false));
        if (albumList.isEmpty()) noAlbums.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
