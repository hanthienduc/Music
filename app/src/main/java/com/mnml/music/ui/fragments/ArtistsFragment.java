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
import com.mnml.music.adapters.ArtistAdapter;
import com.mnml.music.models.Artist;
import com.mnml.music.utils.Utils;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class ArtistsFragment extends Fragment {

    private Context context;
    private Unbinder unbinder;

    @BindView(R.id.artist_list) FastScrollRecyclerView rv;
    @BindView(R.id.no_artists) TextView noArtists;

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);
        unbinder = ButterKnife.bind(this, view);

        context = view.getContext();

        final int accentColor = ContextCompat.getColor(context, R.color.colorAccent);

        rv.setPopupBgColor(accentColor);
        rv.setThumbColor(accentColor);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        rv.setLayoutManager(linearLayoutManager);

        getArtistList();

        return view;
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

    private void getArtistList() {
        final ArrayList<Artist> artistList = Utils.getArtists(context);
        rv.setAdapter(new ArtistAdapter(context, artistList, Glide.with(context)));
        if(artistList.isEmpty()) noArtists.setVisibility(View.VISIBLE);
    }
}
