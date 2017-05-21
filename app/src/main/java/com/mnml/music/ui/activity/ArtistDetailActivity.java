package com.mnml.music.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.aesthetic.AestheticActivity;
import com.bumptech.glide.Glide;
import com.mnml.music.R;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.models.Song;
import com.mnml.music.utils.Utils;

import java.util.ArrayList;

public class ArtistDetailActivity extends AestheticActivity {

    @BindView(R.id.artist_toolbar) Toolbar toolbar;
    @BindView(R.id.artist_recycler_view) RecyclerView rv;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        unbinder = ButterKnife.bind(this);

        String artistName = getIntent().getStringExtra("artistName");

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(artistName);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);

        final ArrayList<Song> artistSongs = Utils.getArtistSongs(this, artistName);
        rv.setAdapter(new SongsAdapter(this, artistSongs));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
