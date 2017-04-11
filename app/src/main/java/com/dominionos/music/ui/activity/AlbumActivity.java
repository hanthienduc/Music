package com.dominionos.music.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dominionos.music.R;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.adapters.SongsAdapter;
import com.dominionos.music.items.Song;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AlbumActivity extends AppCompatActivity {

    @BindView(R.id.collapsing_toolbar_album) CollapsingToolbarLayout collapsingToolbarLayout;

    private Unbinder unbinder;
    private final ArrayList<Song> songList = new ArrayList<>();
    private boolean darkMode = false;
    private long albumId;
    private MusicService service;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = ((MusicService.MyBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        darkMode = sharedPrefs.getBoolean("dark_theme", false);

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Intent i = new Intent(this, MusicService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

        albumId = getIntent().getLongExtra("albumId", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        unbinder = ButterKnife.bind(this);
        overridePendingTransition(0, 0);
        collapsingToolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar_album);
        collapsingToolbarLayout.setTitle(getIntent().getStringExtra("albumName"));
        if ((collapsingToolbarLayout.getContentScrim()) != null) {
            collapsingToolbarLayout.setContentScrimColor(((ColorDrawable) collapsingToolbarLayout.getContentScrim()).getColor());
        }
        collapsingToolbarLayout.setStatusBarScrimColor(
                Utils.getAutoStatColor(((ColorDrawable) collapsingToolbarLayout.getContentScrim()).getColor()));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_album);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ImageView albumArt = (ImageView) findViewById(R.id.album_art);

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(albumId)},
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
                                collapsingToolbarLayout.setExpandedTitleColor(vibrantTitleText);
                                collapsingToolbarLayout.setStatusBarScrimColor(Utils.getAutoStatColor(vibrantRgb));
                                collapsingToolbarLayout.setContentScrimColor(vibrantRgb);
                                ToolbarContentTintHelper.setToolbarContentColorBasedOnToolbarColor(AlbumActivity.this, toolbar, vibrantRgb);
                                int toolbarColor = ToolbarContentTintHelper.toolbarTitleColor(AlbumActivity.this, vibrantRgb);
                                collapsingToolbarLayout.setCollapsedTitleTextColor(toolbarColor);
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
                : ContextCompat.getColor(this, R.color.lightWindowBackground));

        rv.setAdapter(new SongsAdapter(AlbumActivity.this, songList, darkMode, Glide.with(this), false));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.play_all:
                if(service != null) service.playAlbum(albumId);
                return true;
            case R.id.shuffle_all:
                service.shuffleAlbum(albumId);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
