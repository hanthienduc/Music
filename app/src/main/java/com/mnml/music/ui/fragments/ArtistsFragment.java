package com.mnml.music.ui.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.mnml.music.adapters.ArtistAdapter;
import com.mnml.music.models.Artist;
import com.mnml.music.ui.fragments.base.RVFragment;
import com.mnml.music.utils.Utils;

import java.util.ArrayList;

public class ArtistsFragment extends RVFragment {

    @Override
    public RecyclerView.Adapter adapter() {
        final Context context = getContext();
        final ArrayList<Artist> artistList = Utils.getArtists(context);
        return new ArtistAdapter(context, artistList, Glide.with(this));
    }

}
