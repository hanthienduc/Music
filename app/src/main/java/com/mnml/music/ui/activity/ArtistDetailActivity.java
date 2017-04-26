package com.mnml.music.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.mnml.music.R;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.items.Song;
import com.mnml.music.utils.Utils;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;

import java.util.ArrayList;

public class ArtistDetailActivity extends ATHToolbarActivity {

    @BindView(R.id.artist_toolbar) Toolbar toolbar;
    @BindView(R.id.artist_recycler_view) RecyclerView rv;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ATH.setActivityToolbarColorAuto(this, toolbar);
        ATH.setStatusbarColor(this, Utils.getAutoStatColor(ThemeStore.primaryColor(this)));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        unbinder = ButterKnife.bind(this);

        String artistName = getIntent().getStringExtra("artistName");

        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
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
        rv.setAdapter(new SongsAdapter(this, artistSongs, Glide.with(this), true));
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
