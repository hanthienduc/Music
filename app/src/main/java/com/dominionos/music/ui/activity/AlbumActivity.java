package com.dominionos.music.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dominionos.music.R;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.adapters.SongsAdapter;
import com.dominionos.music.items.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private final ArrayList<Song> songList = new ArrayList<>();
    private AudioManager audioManager;
    private boolean darkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        darkMode = sharedPrefs.getBoolean("dark_theme", false);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setTheme(darkMode ? R.style.AppTheme_Dark : R.style.AppTheme_Main);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        overridePendingTransition(0, 0);
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar_album);
        collapsingToolbarLayout.setTitle(getIntent().getStringExtra("albumName"));
        if ((collapsingToolbarLayout.getContentScrim()) != null) {
            collapsingToolbarLayout.setContentScrimColor(((ColorDrawable) collapsingToolbarLayout.getContentScrim()).getColor());
        }
        collapsingToolbarLayout.setStatusBarScrimColor(
                Utils.getAutoStatColor(((ColorDrawable) collapsingToolbarLayout.getContentScrim()).getColor()));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_album);
        final Drawable upButton = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back, null);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(upButton);
        }
        ImageView albumArt = (ImageView) findViewById(R.id.album_art);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        final View toolbarBackground = findViewById(R.id.title_background);
        fab = (FloatingActionButton) findViewById(R.id.fab_album);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bro = new Intent();
                bro.setAction(Config.PLAY_ALBUM);
                bro.putExtra("albumId", getIntent().getLongExtra("albumId", 0));
                sendBroadcast(bro);
            }
        });

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(getIntent().getLongExtra("albumId", 0))},
                null);
        if (cursor != null && cursor.moveToFirst()) {
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            Glide.with(AlbumActivity.this)
                    .load(imagePath)
                    .asBitmap()
                    .error(R.drawable.default_art)
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Palette palette = new Palette.Builder(resource).generate();
                            try {
                                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                                Palette.Swatch altSwatch = palette.getDominantSwatch();
                                int vibrantRgb;
                                int vibrantTitleText;
                                if (vibrantSwatch != null) {
                                    vibrantRgb = vibrantSwatch.getRgb();
                                    vibrantTitleText = vibrantSwatch.getTitleTextColor();
                                } else if (altSwatch != null) {
                                    vibrantRgb = altSwatch.getRgb();
                                    vibrantTitleText = altSwatch.getTitleTextColor();
                                } else {
                                    vibrantRgb = ContextCompat.getColor(AlbumActivity.this, R.color.cardBackground);
                                    vibrantTitleText = ContextCompat.getColor(AlbumActivity.this, R.color.primaryTextDark);
                                }
                                toolbarBackground.setBackgroundColor(vibrantRgb);
                                collapsingToolbarLayout.setStatusBarScrimColor(Utils.getAutoStatColor(vibrantRgb));
                                collapsingToolbarLayout.setContentScrimColor(vibrantRgb);
                                collapsingToolbarLayout.setExpandedTitleColor(vibrantTitleText);
                                collapsingToolbarLayout.setCollapsedTitleTextColor(vibrantTitleText);
                                collapsingToolbarLayout.setBackgroundColor(vibrantRgb);
                                if (upButton != null) {
                                    upButton.setTintList(ColorStateList.valueOf(vibrantTitleText));
                                }
                            } catch (NullPointerException e) {
                                Log.i("AlbumActivity", "Palette.Builder could not generate swatches, falling back to default colours");
                            }
                            return false;
                        }
                    })
                    .into(albumArt);
        }
        if (cursor != null) cursor.close();

        setSongList();
    }

    private void setSongList() {
        Cursor musicCursor;

        String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String whereVal[] = {getIntent().getLongExtra("albumId", 0) + ""};
        String orderBy = MediaStore.Audio.Media.TRACK;

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
            do {
                songList.add(new Song(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumNameColumn)));
            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        FastScrollRecyclerView rv = (FastScrollRecyclerView) findViewById(R.id.rv_album);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        rv.setBackgroundColor(darkMode
                ? ContextCompat.getColor(this, R.color.darkWindowBackground)
                : ContextCompat.getColor(this, R.color.windowBackground));

        rv.setAdapter(new SongsAdapter(AlbumActivity.this, songList, darkMode));
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
