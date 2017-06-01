package com.mnml.music.ui.fragments;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mnml.music.adapters.AlbumsAdapter;
import com.mnml.music.models.Album;
import com.mnml.music.base.RVFragment;
import com.mnml.music.utils.Utils;

import java.util.ArrayList;

public class AlbumsFragment extends RVFragment {

    @Override
    public RecyclerView.Adapter adapter() {
        final Context context = getContext();
        final ArrayList<Album> albumList = Utils.getAlbums(context);
        return new AlbumsAdapter(context, albumList, Glide.with(this));
    }

    @Override
    public RecyclerView.LayoutManager layoutManager() {
        final Context context = getContext();
        GridLayoutManager layoutManager = new GridLayoutManager(context, Utils.calculateNoOfColumns(context));
        layoutManager.scrollToPosition(0);
        layoutManager.setOrientation(GridLayoutManager.VERTICAL);
        return layoutManager;
    }
}
