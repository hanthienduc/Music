package com.dominionos.music.ui.layouts.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Transition;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.dominionos.music.R;
import com.dominionos.music.utils.adapters.AlbumSongAdapter;
import com.dominionos.music.utils.items.SongListItem;
import com.dominionos.music.service.MusicService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private ArrayList<SongListItem> songList = new ArrayList<>();
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (settingsPref.getBoolean("pref_album_status_trans", true)) {
            if (Build.VERSION.SDK_INT >= 21)
                getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                Window w = getWindow();
                w.setFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }

        if (settingsPref.getBoolean("pref_album_nav_trans", false)) {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                getWindow().setNavigationBarColor(Color.TRANSPARENT);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        overridePendingTransition(0, 0);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.collapsingtoolbarlayout_album);
        collapsingToolbarLayout.setTitle(getIntent().getStringExtra("albumName"));
        collapsingToolbarLayout.setStatusBarScrimColor(
                getAutoStatColor(((ColorDrawable) collapsingToolbarLayout.getContentScrim()).getColor()));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_album);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView albumArt = (ImageView) findViewById(R.id.activity_album_art);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbarlayout_artist);
        fab = (FloatingActionButton) findViewById(R.id.fab_album);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bro = new Intent();
                bro.setAction(MusicService.ACTION_PLAY_ALBUM);
                bro.putExtra("albumId", getIntent().getLongExtra("albumId", 0));
                sendBroadcast(bro);
//                new AddSongToPlaylist(AlbumActivity.this, songName, songArtist, songPath,
//                        songAlbum, songId, songAlbumId).execute();
            }
        });

        controlEnterAnimation();
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(getIntent().getLongExtra("albumId", 0))},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            try {
                Picasso.with(AlbumActivity.this)
                        .load(new File(imagePath))
                        .error(R.drawable.default_artwork_dark)
                        .into(albumArt);
            } catch (NullPointerException e) {
                Picasso.with(AlbumActivity.this).
                        load(R.drawable.default_artwork_dark)
                        .into(albumArt);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        Handler mainHandler = new Handler(getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                setSongList();
            }
        };
        mainHandler.post(myRunnable);

    }


    private void controlEnterAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade());
//            getWindow().setExitTransition(new Fade());
            getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    if (Build.VERSION.SDK_INT >= 12) {
                        Animation zoomIn = AnimationUtils.loadAnimation(AlbumActivity.this, R.anim.zoom_in);
                        zoomIn.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                fab.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        fab.setAnimation(zoomIn);
                        fab.animate();
                    } else {
                        fab.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
        }
    }

    private void setSongList() {
        System.gc();
        Cursor musicCursor;

        String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String whereVal[] = {getIntent().getLongExtra("albumId", 0) + ""};
        String orderBy = MediaStore.Audio.Media._ID;

        musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, whereVal, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumNameColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int count = 0;
            do {
                count++;
                songList.add(new SongListItem(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumNameColumn),
                        count));
            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_artist_activity);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);

        rv.setAdapter(new AlbumSongAdapter(AlbumActivity.this, songList));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                fab.setVisibility(View.GONE);
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        fab.setVisibility(View.GONE);
        super.onBackPressed();
    }

    public int getAutoStatColor(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
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
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, AudioManager.FLAG_SHOW_UI);
        }
        return super.onKeyDown(keyCode, event);
    }
}
