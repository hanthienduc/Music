package com.mnml.music.ui.activity;

import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.aesthetic.AestheticActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mnml.music.R;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.models.Song;
import com.mnml.music.service.MusicService;
import com.mnml.music.utils.Utils;

import java.util.ArrayList;

public class AlbumDetailActivity extends AestheticActivity {

    @BindView(R.id.collapsing_toolbar_album) CollapsingToolbarLayout collapsingToolbarLayout;
    private Unbinder unbinder;
    private long albumId;
    private MusicService service;
    private final ServiceConnection serviceConnection =
            new ServiceConnection() {
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

        getWindow().setStatusBarColor(Color.TRANSPARENT);

        Intent i = new Intent(this, MusicService.class);
        if (service == null) bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);

        albumId = getIntent().getLongExtra("albumId", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        unbinder = ButterKnife.bind(this);
        overridePendingTransition(0, 0);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_album);
        collapsingToolbarLayout.setTitle(getIntent().getStringExtra("albumName"));
        if ((collapsingToolbarLayout.getContentScrim()) != null) {
            collapsingToolbarLayout.setContentScrimColor(
                    ((ColorDrawable) collapsingToolbarLayout.getContentScrim()).getColor());
        }
        collapsingToolbarLayout.setStatusBarScrimColor(
                Utils.getAutoStatColor(
                        ((ColorDrawable) collapsingToolbarLayout.getContentScrim()).getColor()));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_album);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ImageView albumArt = (ImageView) findViewById(R.id.album_art);

        Cursor cursor =
                getContentResolver()
                        .query(
                                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                                MediaStore.Audio.Albums._ID + "=?",
                                new String[]{String.valueOf(albumId)},
                                null);
        if (cursor != null && cursor.moveToFirst()) {
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            Glide.with(AlbumDetailActivity.this)
                    .load(imagePath)
                    .asBitmap()
                    .error(R.drawable.default_art)
                    .listener(
                            new RequestListener<String, Bitmap>() {
                                @Override
                                public boolean onException(
                                        Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(
                                        Bitmap resource,
                                        String model,
                                        Target<Bitmap> target,
                                        boolean isFromMemoryCache,
                                        boolean isFirstResource) {
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
                                            vibrantRgb =
                                                    ContextCompat.getColor(AlbumDetailActivity.this, R.color.cardBackground);
                                            vibrantTitleText =
                                                    ContextCompat.getColor(AlbumDetailActivity.this, R.color.primaryTextDark);
                                        }
                                        collapsingToolbarLayout.setExpandedTitleColor(vibrantTitleText);
                                        collapsingToolbarLayout.setStatusBarScrimColor(
                                                Utils.getAutoStatColor(vibrantRgb));
                                        collapsingToolbarLayout.setContentScrimColor(vibrantRgb);
                                    } catch (NullPointerException e) {
                                        Log.i(
                                                "AlbumDetailActivity",
                                                "Palette.Builder could not generate swatches, falling back to default colours");
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
        ArrayList<Song> songList = Utils.getAlbumSongs(this, albumId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv_album);
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);
        rv.setAdapter(new SongsAdapter(AlbumDetailActivity.this, songList, Glide.with(this), false));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        unbindService(serviceConnection);
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
                if (service != null) service.playAlbum(albumId);
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
