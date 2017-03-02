package com.dominionos.music.ui.layouts.activity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.async.Action;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.ui.layouts.fragments.AlbumsFragment;
import com.dominionos.music.ui.layouts.fragments.ArtistsFragment;
import com.dominionos.music.ui.layouts.fragments.PlaylistFragment;
import com.dominionos.music.ui.layouts.fragments.SongsFragment;
import com.dominionos.music.utils.MusicPlayerDBHelper;
import com.dominionos.music.utils.MySQLiteHelper;
import com.dominionos.music.utils.adapters.PlayingSongAdapter;
import com.dominionos.music.utils.adapters.ViewPagerAdapter;
import com.dominionos.music.utils.items.SongListItem;
import com.lapism.searchview.SearchView;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_GET_PLAY_STATE = "get_play_state";
    public static final String ACTION_GET_PLAYING_LIST = "get_playing_list";
    public static final String ACTION_GET_PLAYING_DETAIL = "get_playing_detail";
    public static final String ACTION_UPDATE_REPEAT = "update_repeat";
    public static final String ACTION_UPDATE_SHUFFLE = "update_shuffle";

    private boolean musicStopped = true, missingDuration = true, darkMode = false;
    private RecyclerView rv;
    private Handler handler;
    private SearchView search;
    private Timer timer;
    private TextView songName, songDesc, currentTime, totalTime;
    private ImageView playToolbar, play, albumArt, miniAlbumArt, repeatButton, shuffleButton;
    private SeekBar seekBar;
    private SlidingUpPanelLayout slidingPanel;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private AudioManager audioManager;
    private FloatingActionButton fab;
    private RelativeLayout miniController, controlHolder;
    private int seekProgress;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_GET_PLAY_STATE:
                    updatePlayState(intent.getBooleanExtra("isPlaying", false));
                    break;
                case ACTION_GET_PLAYING_DETAIL:
                    SongListItem song = (SongListItem) intent.getSerializableExtra("song");
                    if (song != null) {
                        changePlayerDetails(song.getName(), song.getDesc(),
                                intent.getIntExtra("songCurrTime", 0), intent.getIntExtra("songDuration", 0),
                                song.getAlbumId());
                    }
                    break;
                case ACTION_GET_PLAYING_LIST:
                    MusicPlayerDBHelper helper = new MusicPlayerDBHelper(context);
                    PlayingSongAdapter adapter = new PlayingSongAdapter(context, helper.getCurrentPlayingList(), darkMode);
                    if (rv.getAdapter() == null) {
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context,
                                LinearLayoutManager.VERTICAL, false);
                        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        rv.setLayoutManager(layoutManager);
                        rv.addItemDecoration(new DividerItemDecoration(rv.getContext(), layoutManager.getOrientation()));
                        rv.setAdapter(adapter);
                    } else {
                        rv.swapAdapter(adapter, false);
                    }
                    break;
                case ACTION_UPDATE_REPEAT:
                    updateRepeat(intent.getStringExtra("repeat"));
                    break;
                case ACTION_UPDATE_SHUFFLE:
                    updateShuffle(intent.getBooleanExtra("shuffle", false));
                    break;
            }
        }
    };
    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        darkMode = sharedPref.getBoolean("dark_theme", false);
        if (!darkMode) {
            setTheme(R.style.AppTheme_Main);
        } else {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        if (sharedPref.getBoolean("colour_navbar", false)) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }
        setContentView(R.layout.activity_main);
        overridePendingTransition(0, 0);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        findViewById(R.id.main_appbar).setBackgroundColor(sharedPref.getInt("primary_colour", ContextCompat.getColor(this, R.color.colorPrimary)));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MEDIA_CONTENT_CONTROL,
                        Manifest.permission.INTERNET}, 1);
            }
        } else {
            init();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.play_all:
                Intent i = new Intent();
                i.setAction(MusicService.ACTION_PLAY_ALL_SONGS);
                sendBroadcast(i);
                return true;
            case R.id.search_item:
                if (search.isSearchOpen()) {
                    search.close(true);
                } else {
                    search.open(true, item);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {

        Intent i = new Intent(MainActivity.this, MusicService.class);
        startService(i);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GET_PLAY_STATE);
        filter.addAction(ACTION_GET_PLAYING_LIST);
        filter.addAction(ACTION_GET_PLAYING_DETAIL);
        filter.addAction(ACTION_UPDATE_REPEAT);
        filter.addAction(ACTION_UPDATE_SHUFFLE);
        registerReceiver(broadcastReceiver, filter);

        handler = new Handler();

        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);

        setDrawer();

        setDynamicShortcuts();

        setSearch();

        setupPlayer();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MusicService.ACTION_REQUEST_SONG_DETAILS);
                sendBroadcast(intent);
            }
        }, 1000);
    }

    private void setSearch() {
        search = (SearchView) findViewById(R.id.searchView);
        search.setArrowOnly(false);
        search.setOnMenuClickListener(new SearchView.OnMenuClickListener() {
            @Override
            public void onMenuClick() {
                search.close(true);
            }
        });
        if (darkMode) {
            search.setTheme(SearchView.THEME_DARK);
        } else {
            search.setTheme(SearchView.THEME_LIGHT);
        }
        search.setVersion(SearchView.VERSION_MENU_ITEM);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void search(String text) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        intent.putExtra("dark_theme", darkMode);
        intent.putExtra("query", text);
        startActivity(intent);
    }

    private void setDynamicShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            ShortcutInfo searchShortcut = new ShortcutInfo.Builder(this, "search")
                    .setShortLabel(getString(R.string.search))
                    .setIcon(Icon.createWithResource(MainActivity.this, R.drawable.ic_shortcut_search))
                    .setIntent(new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, SearchActivity.class))
                    .build();
            shortcutManager.setDynamicShortcuts(Collections.singletonList(searchShortcut));
        }
    }

    private void setupViewPager(final ViewPager viewPager) {
        SongsFragment songs = new SongsFragment();
        AlbumsFragment albums = new AlbumsFragment();
        ArtistsFragment artists = new ArtistsFragment();
        PlaylistFragment playlists = new PlaylistFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("dark_theme", darkMode);
        songs.setArguments(bundle);
        albums.setArguments(bundle);
        artists.setArguments(bundle);
        playlists.setArguments(bundle);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(songs, getResources().getString(R.string.songs));
        adapter.addFrag(albums, getResources().getString(R.string.album));
        adapter.addFrag(artists, getResources().getString(R.string.artist));
        adapter.addFrag(playlists, getResources().getString(R.string.playlist));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(4);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.add_playlist)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(getString(R.string.playlist_example), null, new MaterialDialog.InputCallback() {

                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (!input.toString().equals("")) {
                                    MySQLiteHelper helper = new MySQLiteHelper(MainActivity.this);
                                    helper.createNewPlayList(input.toString());
                                } else {
                                    Toast.makeText(MainActivity.this, R.string.playlist_name_empty_warning, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .positiveText(getString(R.string.ok))
                        .negativeText(getString(R.string.cancel)).show();
            }
        });
    }

    private void setDrawer() {
        final PrimaryDrawerItem songs = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.songs).withIcon(GoogleMaterial.Icon.gmd_audiotrack);
        final PrimaryDrawerItem albums = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.albums).withIcon(GoogleMaterial.Icon.gmd_library_music);
        final PrimaryDrawerItem artists = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.artist).withIcon(GoogleMaterial.Icon.gmd_account_circle);
        final PrimaryDrawerItem playlist = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.playlist).withIcon(GoogleMaterial.Icon.gmd_queue_music);
        final SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(5).withName(getString(R.string.settings)).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_settings);
        final SecondaryDrawerItem about = new SecondaryDrawerItem().withIdentifier(6).withName(R.string.about).withSelectable(false).withIcon(GoogleMaterial.Icon.gmd_info_outline);

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withCloseOnClick(true)
                .addStickyDrawerItems(settings, about)
                .addDrawerItems(
                        songs,
                        albums,
                        artists,
                        playlist,
                        new DividerDrawerItem()
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch ((int) drawerItem.getIdentifier()) {
                            case 1:
                                viewPager.setCurrentItem(0);
                                break;
                            case 2:
                                viewPager.setCurrentItem(1);
                                break;
                            case 3:
                                viewPager.setCurrentItem(2);
                                break;
                            case 4:
                                viewPager.setCurrentItem(3);
                                break;
                            case 5:
                                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                intent.putExtra("dark_theme", darkMode);
                                startActivity(intent);
                                break;
                            case 6:
                                new LibsBuilder()
                                        .withActivityTitle(getString(R.string.about))
                                        .withAboutIconShown(true)
                                        .withAboutVersionShown(true)
                                        .withAboutDescription(getString(R.string.about_text))
                                        .withActivityStyle(darkMode ? Libs.ActivityStyle.DARK : Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                        .withActivityTheme(darkMode ? R.style.AppTheme_Dark : R.style.AppTheme_Main)
                                        .start(MainActivity.this);
                                break;
                        }
                        return true;
                    }
                })
                .build();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 3)
                    fab.show();
                else
                    fab.hide();
            }

            @Override
            public void onPageSelected(int position) {
                drawer.setSelectionAtPosition(position, false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setupPlayer() {
        currentTime = (TextView) findViewById(R.id.player_current_time);
        totalTime = (TextView) findViewById(R.id.player_total_time);
        seekBar = (SeekBar) findViewById(R.id.player_seekbar);
        songName = (TextView) findViewById(R.id.song_name_toolbar);
        songDesc = (TextView) findViewById(R.id.song_desc_toolbar);
        slidingPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        playToolbar = (ImageView) findViewById(R.id.player_play_toolbar);
        ImageView rewind = (ImageView) findViewById(R.id.player_rewind);
        ImageView forward = (ImageView) findViewById(R.id.player_forward);
        repeatButton = (ImageView) findViewById(R.id.player_repeat);
        repeatButton.setAlpha(0.5f);
        rv = (RecyclerView) findViewById(R.id.playing_list);
        play = (ImageView) findViewById(R.id.player_play);
        miniController = (RelativeLayout) findViewById(R.id.mini_controller);
        controlHolder = (RelativeLayout) findViewById(R.id.control_holder);
        shuffleButton = (ImageView) findViewById(R.id.player_shuffle);
        shuffleButton.setAlpha(0.5f);
        if (darkMode) {
            rv.setBackgroundColor(ContextCompat.getColor(this, R.color.darkWindowBackground));
        }
        albumArt = (ImageView) findViewById(R.id.album_art);
        miniAlbumArt = (ImageView) findViewById(R.id.mini_album_art);
        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_SHUFFLE_PLAYLIST);
                sendBroadcast(intent);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent changeCurrentTime = new Intent(MusicService.ACTION_SEEK_TO);
                    changeCurrentTime.putExtra("changeSeek", progress);
                    sendBroadcast(changeCurrentTime);
                    musicStopped = false;
                } else {
                    if (seekBar.getProgress() == seekBar.getMax()) {
                        musicStopped = true;
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_NEXT);
                seekBar.setProgress(0);
                sendBroadcast(intent);
            }
        });
        rewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_PREV);
                seekBar.setProgress(0);
                sendBroadcast(intent);
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_TOGGLE_PLAY);
                sendBroadcast(intent);
                if (missingDuration) {
                    Intent intent1 = new Intent(MusicService.ACTION_REQUEST_SONG_DETAILS);
                    sendBroadcast(intent1);
                }
            }
        });
        playToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_TOGGLE_PLAY);
                sendBroadcast(intent);
                if (missingDuration) {
                    Intent intent1 = new Intent(MusicService.ACTION_REQUEST_SONG_DETAILS);
                    sendBroadcast(intent1);
                }
            }
        });
        slidingPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                miniController.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    miniController.setVisibility(View.GONE);
                } else {
                    miniController.setVisibility(View.VISIBLE);
                }
            }
        });
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MusicService.ACTION_REPEAT);
                sendBroadcast(intent);
            }
        });
    }

    private void changePlayerDetails(String songNameString, String songDetailsString,
                                     int currentTime, final int totalTime, final long albumId) {
        new Action<Bitmap>() {
            @NonNull
            @Override
            public String id() {
                return "get-player-art";
            }

            @Nullable
            @Override
            protected Bitmap run() throws InterruptedException {
                Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                        MediaStore.Audio.Albums._ID + "=?",
                        new String[]{String.valueOf(albumId)},
                        null);
                String songArt = null;
                if (cursor != null && cursor.moveToFirst())
                    songArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                if (cursor != null) cursor.close();
                if (songArt != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    return BitmapFactory.decodeFile(songArt, options);
                }
                return null;
            }

            @Override
            protected void done(@Nullable Bitmap result) {
                albumArt.setImageBitmap(result);
                miniAlbumArt.setImageBitmap(result);
                if (result != null) {
                    Palette.PaletteAsyncListener paletteAsyncListener = new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            if (palette.getVibrantSwatch() != null) {
                                Drawable background = controlHolder.getBackground();
                                int colorFrom = ((ColorDrawable) background).getColor();
                                int colorTo = palette.getVibrantSwatch().getRgb();
                                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                                colorAnimation.setDuration(500); // milliseconds
                                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animator) {
                                        controlHolder.setBackgroundColor(((int) animator.getAnimatedValue()));
                                    }

                                });
                                colorAnimation.start();
                            }
                        }
                    };
                    Palette.from(result).generate(paletteAsyncListener);
                }
            }
        }.execute();

        songName.setText(songNameString);
        songDesc.setText(songDetailsString);
        if (currentTime != 0 && totalTime != 0) {
            if (timer != null) timer.cancel();
            missingDuration = false;
            this.currentTime.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(currentTime)));
            this.totalTime.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(totalTime)));
            seekBar.setMax(totalTime);
            seekBar.setProgress(currentTime);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!musicStopped) {
                                seekProgress = seekBar.getProgress();
                                if (seekProgress < totalTime) {
                                    seekBar.setProgress(seekProgress + 100);
                                } else {
                                    seekBar.setProgress(100);
                                }
                                MainActivity.this.currentTime.setText(new SimpleDateFormat("mm:ss", Locale.getDefault())
                                        .format(new Date(seekBar.getProgress())));
                            }
                        }
                    });
                }
            }, 0, 100);
        } else {
            missingDuration = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MusicService.ACTION_REQUEST_SONG_DETAILS);
                    sendBroadcast(intent);
                }
            }, 500);
        }
        if (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void updatePlayState(boolean isPlaying) {
        if (isPlaying) {
            playToolbar.setImageResource(R.drawable.ic_pause);
            play.setImageResource(R.drawable.ic_pause_circle);
            musicStopped = false;
        } else {
            playToolbar.setImageResource(R.drawable.ic_play);
            play.setImageResource(R.drawable.ic_play_circle);
            musicStopped = true;
        }
    }

    private void updateShuffle(boolean shuffle) {
        if (shuffle) {
            shuffleButton.setAlpha(1.0f);
        } else {
            shuffleButton.setAlpha(0.5f);
        }
    }

    private void updateRepeat(String repeatMode) {
        switch (repeatMode) {
            case "all":
                repeatButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat_all));
                repeatButton.setAlpha(1.0f);
                break;
            case "one":
                repeatButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat_one));
                repeatButton.setAlpha(1.0f);
                break;
            case "none":
                repeatButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat_all));
                repeatButton.setAlpha(0.5f);
                break;
            default:
                repeatButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_repeat_all));
                repeatButton.getDrawable().setAlpha(140);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    finish();
                }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        if (slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (search.isSearchOpen()) {
            search.close(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
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
