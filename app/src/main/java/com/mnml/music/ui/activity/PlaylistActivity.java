package com.mnml.music.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.mnml.music.R;
import com.mnml.music.adapters.CheckableSongsAdapter;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.items.CheckableSong;
import com.mnml.music.utils.MySQLiteHelper;
import com.mnml.music.utils.Utils;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.TintHelper;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    @BindView(R.id.playlist_toolbar)
    Toolbar toolbar;
    private int playlistId;
    private CheckableSongsAdapter adapter;
    private String title;
    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ATH.setActivityToolbarColorAuto(this, toolbar);
        ATH.setStatusbarColor(this, Utils.getAutoStatColor(ThemeStore.primaryColor(this)));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        unbinder = ButterKnife.bind(this);
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        title = getIntent().getStringExtra("title");
        setTitle(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_playlist_activity);
        final MySQLiteHelper helper = new MySQLiteHelper(this);
        playlistId = getIntent().getIntExtra("playlistId", -1);
        if (playlistId == -1) {
            finish();
        }
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(new SongsAdapter(this, helper.getPlayListSongs(playlistId), Glide.with(this), true));

        List<CheckableSong> songList = new ArrayList<>();
        Cursor musicCursor2;
        final String where2 = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy2 = MediaStore.Audio.Media.TITLE;
        musicCursor2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where2, null, orderBy2);
        if (musicCursor2 != null && musicCursor2.moveToFirst()) {
            int titleColumn = musicCursor2.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor2.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn =
                    musicCursor2.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor2.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor2.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor2.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int i2 = 0;
            do {
                i2++;
                songList.add(
                        new CheckableSong(
                                musicCursor2.getLong(idColumn),
                                musicCursor2.getString(titleColumn),
                                musicCursor2.getString(artistColumn),
                                musicCursor2.getString(pathColumn),
                                musicCursor2.getLong(albumIdColumn),
                                musicCursor2.getString(albumColumn),
                                i2));
            } while (musicCursor2.moveToNext());
        }
        if (musicCursor2 != null) {
            musicCursor2.close();
        }
        adapter = new CheckableSongsAdapter(songList);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.playlist_fab);
        fab.setOnClickListener(
                view -> {
                    MaterialDialog dialog =
                            new MaterialDialog.Builder(PlaylistActivity.this)
                                    .title(getString(R.string.add_to_playlist))
                                    .positiveText(getString(R.string.add))
                                    .negativeText(getString(R.string.cancel))
                                    .adapter(adapter, layoutManager)
                                    .onPositive(
                                            (dialog1, which) -> {
                                                ArrayList<CheckableSong> checkedSongs = adapter.getCheckedItems();
                                                helper.addSongs(checkedSongs, playlistId);
                                                Intent i = new Intent(PlaylistActivity.this, PlaylistActivity.class);
                                                i.putExtra("playlistId", playlistId);
                                                i.putExtra("title", title);
                                                finish();
                                                startActivity(i);
                                            })
                                    .build();
                    dialog.show();
                });
        TintHelper.setTintAuto(fab, ThemeStore.accentColor(this), true);
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
