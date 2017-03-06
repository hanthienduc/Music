package com.dominionos.music.ui.layouts.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;
import com.dominionos.music.utils.MySQLiteHelper;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.utils.adapters.CheckableSongsAdapter;
import com.dominionos.music.utils.adapters.PlaylistActivityAdapter;
import com.dominionos.music.utils.items.CheckableSongListItem;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private int playlistId;
    private CheckableSongsAdapter adapter;
    private String title;
    private boolean darkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        darkMode = sharedPrefs.getBoolean("dark_theme", false);

        setTheme(darkMode ? R.style.AppTheme_Dark : R.style.AppTheme_Main);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        setSupportActionBar((Toolbar) findViewById(R.id.playlist_toolbar));
        title = getIntent().getStringExtra("title");
        setTitle(title);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_playlist_activity);
        MySQLiteHelper helper = new MySQLiteHelper(this);
        playlistId = getIntent().getIntExtra("playlistId", -1);
        if (playlistId == -1) {
            finish();
        }
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setBackgroundColor(darkMode
                ? Utils.getColor(this, R.color.darkWindowBackground)
                : Utils.getColor(this, R.color.windowBackground));
        rv.setAdapter(new PlaylistActivityAdapter(
                this, helper.getPlayListSongs(playlistId),
                playlistId, darkMode));

        List<CheckableSongListItem> songList = new ArrayList<>();
        Cursor musicCursor2;
        final String where2 = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy2 = MediaStore.Audio.Media.TITLE;
        musicCursor2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where2, null, orderBy2);
        if (musicCursor2 != null && musicCursor2.moveToFirst()) {
            int titleColumn = musicCursor2.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor2.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor2.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor2.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor2.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor2.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int i2 = 0;
            do {
                i2++;
                songList.add(new CheckableSongListItem(musicCursor2.getLong(idColumn),
                        musicCursor2.getString(titleColumn),
                        musicCursor2.getString(artistColumn),
                        musicCursor2.getString(pathColumn),
                        musicCursor2.getLong(albumIdColumn),
                        musicCursor2.getString(albumColumn),
                        i2));
            }
            while (musicCursor2.moveToNext());
        }
        if (musicCursor2 != null) {
            musicCursor2.close();
        }
        adapter = new CheckableSongsAdapter(songList);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.playlist_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDialog dialog = new MaterialDialog.Builder(PlaylistActivity.this)
                        .title(getString(R.string.add_to_playlist))
                        .positiveText(getString(R.string.add))
                        .negativeText(getString(R.string.cancel))
                        .adapter(adapter, layoutManager)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ArrayList<CheckableSongListItem> checkedSongs = adapter.getCheckedItems();
                                MySQLiteHelper helper = new MySQLiteHelper(PlaylistActivity.this);
                                helper.addSongs(checkedSongs, playlistId);
                                Intent i = new Intent(PlaylistActivity.this, PlaylistActivity.class);
                                i.putExtra("playlistId", playlistId);
                                i.putExtra("title", title);
                                finish();
                                startActivity(i);
                            }
                        }).build();
                dialog.show();
            }
        });
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                break;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI);
                }
        }
        return super.onKeyDown(keyCode, event);
    }

}
