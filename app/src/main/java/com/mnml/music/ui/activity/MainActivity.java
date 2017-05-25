package com.mnml.music.ui.activity;

import android.Manifest;
import android.content.*;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mnml.music.R;
import com.mnml.music.adapters.ViewPagerAdapter;
import com.mnml.music.service.MusicService;
import com.mnml.music.ui.fragments.*;
import com.mnml.music.utils.shortcuts.ShortcutHandler;
import com.mnml.music.utils.Utils;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AestheticActivity {

    @BindView(R.id.main_toolbar) Toolbar toolbar;
    @BindView(R.id.main_viewpager) ViewPager viewPager;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.main_tab_layout) TabLayout tabLayout;

    private PrimaryDrawerItem songs, albums, artists, playlists;
    private Unbinder unbinder;
    private SharedPreferences sharedPrefs;
    private Drawer drawer;
    private int startPage, lastPage;
    private MusicService service;
    private PlayerFragment player;
    private final ServiceConnection serviceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    service = ((MusicService.MyBinder) binder).getService(MainActivity.this);
                    player.activatePlayer(service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    service = null;
                    player.deactivatePlayer();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasGoogleServices = Utils.isGooglePlayServicesAvailable(this);
        if(!hasGoogleServices) {
            sharedPrefs.edit().putBoolean("google_services", false).apply();
        }
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if (Aesthetic.isFirstTime()) {
            final boolean darkMode = sharedPrefs.getBoolean("dark_theme", false);
            Aesthetic.get()
                    .activityTheme(darkMode ? R.style.AppTheme_Dark : R.style.AppTheme_Light)
                    .colorPrimaryRes(R.color.colorPrimary)
                    .colorAccentRes(R.color.colorAccent)
                    .colorStatusBarAuto()
                    .isDark(darkMode)
                    .textColorPrimaryRes(darkMode ? R.color.primaryTextDark : R.color.primaryTextLight)
                    .textColorPrimaryInverseRes(darkMode ? R.color.primaryTextLight : R.color.primaryTextDark)
                    .textColorSecondaryRes(darkMode ? R.color.secondaryTextDark : R.color.secondaryTextLight)
                    .textColorSecondaryInverseRes(darkMode ? R.color.secondaryTextLight : R.color.secondaryTextDark)
                    .apply();
        }

        MainActivityPermissionsDispatcher.initWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public MusicService getService() {
        return service;
    }

    public void updatePlayer() {
        player.updatePlayer();
    }

    public void updatePlayingList() {
        player.updatePlayingList();
    }

    public void updatePlayerPlayState() {
        player.updatePlayState();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.play_all:
                if (service != null) service.playAllSongs();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void init() {
        startPage = Integer.decode(sharedPrefs.getString("start_page", "0"));
        lastPage = sharedPrefs.getInt("last_page", 0);

        initShortcuts();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        initDrawer();

        setupViewPager();

        initPlayer();
    }

    private void initShortcuts() {
        final ShortcutHandler shortcutHandler = new ShortcutHandler();
        shortcutHandler.create(this);
    }

    private void initPlayer() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        player = new PlayerFragment();
        transaction.replace(R.id.player_holder, player).commit();
        final Intent i = new Intent(MainActivity.this, MusicService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(i);
    }

    private void setupViewPager() {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new SongsFragment(), getResources().getString(R.string.songs));
        adapter.addFrag(new AlbumsFragment(), getResources().getString(R.string.albums));
        adapter.addFrag(new ArtistsFragment(), getResources().getString(R.string.artist));
        adapter.addFrag(new PlaylistFragment(), getResources().getString(R.string.playlist));
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        if (position == 3) fab.show();
                        else fab.hide();
                    }

                    @Override
                    public void onPageSelected(int position) {
                        drawer.setSelection(position, false);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {}
                });
        if(startPage != 4) {
            viewPager.setCurrentItem(startPage);
        } else {
            viewPager.setCurrentItem(lastPage);
        }
        viewPager.setOffscreenPageLimit(adapter.getCount());
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
        fab.setOnClickListener(v -> Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show());
    }

    public SlidingUpPanelLayout getSlidingPanel() {
        return slidingUpPanelLayout;
    }

    private void updateDrawerColors(final int accentColor) {
        songs
                .withSelectedIconColor(accentColor)
                .withSelectedTextColor(accentColor);
        albums
                .withSelectedIconColor(accentColor)
                .withSelectedTextColor(accentColor);
        artists
                .withSelectedIconColor(accentColor)
                .withSelectedTextColor(accentColor);
        playlists
                .withSelectedIconColor(accentColor)
                .withSelectedTextColor(accentColor);
        drawer.updateItem(songs);
        drawer.updateItem(albums);
        drawer.updateItem(artists);
        drawer.updateItem(playlists);
    }

    private void initDrawer() {
        songs = new PrimaryDrawerItem()
                .withIdentifier(0)
                .withName(R.string.songs)
                .withIcon(GoogleMaterial.Icon.gmd_audiotrack);
        albums = new PrimaryDrawerItem()
                .withIdentifier(1)
                .withName(R.string.albums)
                .withIcon(GoogleMaterial.Icon.gmd_album);
        artists = new PrimaryDrawerItem()
                .withIdentifier(2)
                .withName(R.string.artist)
                .withIcon(GoogleMaterial.Icon.gmd_person);
        playlists = new PrimaryDrawerItem()
                .withIdentifier(3)
                .withName(R.string.playlist)
                .withIcon(GoogleMaterial.Icon.gmd_playlist_play);
        final SecondaryDrawerItem settings = new SecondaryDrawerItem()
                .withIdentifier(5)
                .withName(getString(R.string.settings))
                .withSelectable(false)
                .withIcon(GoogleMaterial.Icon.gmd_settings);
        final SecondaryDrawerItem about = new SecondaryDrawerItem()
                .withIdentifier(6)
                .withName(R.string.about)
                .withSelectable(false)
                .withIcon(GoogleMaterial.Icon.gmd_info);
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.header)
                .withActionBarDrawerToggle(true)
                .withFullscreen(true)
                .withCloseOnClick(true)
                .addDrawerItems(
                        songs,
                        albums,
                        artists,
                        playlists,
                        new DividerDrawerItem(),
                        settings,
                        about)
                .withOnDrawerItemClickListener(
                        (view, position, drawerItem) -> {
                            switch ((int) drawerItem.getIdentifier()) {
                                case 0:
                                    viewPager.setCurrentItem(0);
                                    break;
                                case 1:
                                    viewPager.setCurrentItem(1);
                                    break;
                                case 2:
                                    viewPager.setCurrentItem(2);
                                    break;
                                case 3:
                                    viewPager.setCurrentItem(3);
                                    break;
                                case 5:
                                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                                    startActivity(intent);
                                    break;
                                case 6:
                                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                                    break;
                            }
                            return true;
                        })
                .build();
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        Aesthetic.get().colorAccent().take(1).subscribe(this::updateDrawerColors);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        if(viewPager != null) sharedPrefs.edit().putInt("last_page", viewPager.getCurrentItem()).apply();
        unbinder.unbind();
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) player.updatePlayer();
    }
}
