package com.mnml.music.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.aesthetic.AestheticActivity;
import com.bumptech.glide.Glide;
import com.mnml.music.R;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.utils.PlaylistHelper;

public class PlaylistActivity extends AestheticActivity {
    @BindView(R.id.playlist_toolbar) Toolbar toolbar;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        unbinder = ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        String title = getIntent().getStringExtra("title");
        setTitle(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_playlist_activity);
        final PlaylistHelper helper = new PlaylistHelper(this);
        int playlistId = getIntent().getIntExtra("playlistId", -1);
        if (playlistId == -1) {
            finish();
        }
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(new SongsAdapter(this, helper.getPlayListSongs(playlistId)));

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.playlist_fab);
        fab.setOnClickListener(view -> Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
