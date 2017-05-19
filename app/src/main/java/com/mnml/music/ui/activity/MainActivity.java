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
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mnml.music.R;
import com.mnml.music.adapters.ViewPagerAdapter;
import com.mnml.music.service.MusicService;
import com.mnml.music.ui.fragments.*;
import com.mnml.music.utils.PlaylistHelper;
import com.mnml.music.utils.shortcuts.ShortcutHandler;
import com.mnml.music.utils.Utils;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import io.reactivex.disposables.Disposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AestheticActivity {

    @BindView(R.id.main_toolbar) Toolbar toolbar;
    @BindView(R.id.main_viewpager) ViewPager viewPager;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.main_tab_layout) TabLayout tabLayout;

    private Disposable accentSubscription;
    private int accentColor;
    private PrimaryDrawerItem songs, albums, artists, playlists;
    private Unbinder unbinder;
    private SharedPreferences sharedPrefs;
    private Drawer drawer;
    private int startPage, lastPage;
    private PlaylistFragment playlistFragment;
    private MusicService service;
    private PlayerFragment player;
    private final ServiceConnection serviceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    service = ((MusicService.MyBinder) binder).getService(MainActivity.this);
                    updatePlayer();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    service = null;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasGoogleServices = Utils.isGooglePlayServicesAvailable(this);
        if(!hasGoogleServices) {
            sharedPrefs.edit().putBoolean("google_services", false).apply();
        }
        super.onCreate(savedInstanceState);
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
                    .apply();
        }

        MainActivityPermissionsDispatcher.initWithCheck(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
            case R.id.action_search:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void init() {
        startPage = Integer.decode(sharedPrefs.getString("start_page", "0"));
        lastPage = sharedPrefs.getInt("last_page", 0);

        initColorSubscribers();

        initShortcuts();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        final Intent i = new Intent(MainActivity.this, MusicService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(i);

        initDrawer();

        setupViewPager();

        initPlayer();
    }

    private void initColorSubscribers() {
        accentSubscription = Aesthetic.get().colorAccent().subscribe(integer -> {
            accentColor = integer;
            if(drawer != null && accentColor != 0) updateDrawerColors();
        });
    }

    private void initShortcuts() {
        final ShortcutHandler shortcutHandler = new ShortcutHandler();
        shortcutHandler.create(this);
    }

    private void initPlayer() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        player = new PlayerFragment();
        transaction.replace(R.id.player_holder, player).commit();
    }

    private void setupViewPager() {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        playlistFragment = new PlaylistFragment();
        adapter.addFrag(new SongsFragment(), getResources().getString(R.string.songs));
        adapter.addFrag(new AlbumsFragment(), getResources().getString(R.string.albums));
        adapter.addFrag(new ArtistsFragment(), getResources().getString(R.string.artist));
        adapter.addFrag(playlistFragment, getResources().getString(R.string.playlist));
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
        fab.setOnClickListener(v ->
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.add_playlist)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(
                                getString(R.string.playlist_example),
                                null,
                                (dialog, input) -> {
                                    if (!input.toString().equals("")) {
                                        PlaylistHelper helper = new PlaylistHelper(MainActivity.this);
                                        helper.createNewPlayList(input.toString());
                                        playlistFragment.updateList();
                                    } else {
                                        Toast.makeText(
                                                MainActivity.this,
                                                R.string.playlist_name_empty_warning,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                })
                        .positiveText(getString(R.string.ok))
                        .negativeText(getString(R.string.cancel))
                        .show());
    }

    public SlidingUpPanelLayout getSlidingPanel() {
        return slidingUpPanelLayout;
    }

    private void updateDrawerColors() {
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
                .withSelectedTextColor(accentColor)
                .withSelectedIconColor(accentColor)
                .withName(R.string.songs)
                .withIcon(MaterialDesignIconic.Icon.gmi_audio);
        albums = new PrimaryDrawerItem()
                .withIdentifier(1)
                .withSelectedTextColor(accentColor)
                .withSelectedIconColor(accentColor)
                .withName(R.string.albums)
                .withIcon(MaterialDesignIconic.Icon.gmi_album);
        artists = new PrimaryDrawerItem()
                .withIdentifier(2)
                .withSelectedTextColor(accentColor)
                .withSelectedIconColor(accentColor)
                .withName(R.string.artist)
                .withIcon(MaterialDesignIconic.Icon.gmi_account);
        playlists = new PrimaryDrawerItem()
                .withIdentifier(3)
                .withSelectedTextColor(accentColor)
                .withSelectedIconColor(accentColor)
                .withName(R.string.playlist)
                .withIcon(MaterialDesignIconic.Icon.gmi_playlist_audio);
        final SecondaryDrawerItem settings = new SecondaryDrawerItem()
                .withIdentifier(5)
                .withName(getString(R.string.settings))
                .withSelectable(false)
                .withIcon(MaterialDesignIconic.Icon.gmi_settings);
        final SecondaryDrawerItem about = new SecondaryDrawerItem()
                .withIdentifier(6)
                .withName(R.string.about)
                .withSelectable(false)
                .withIcon(MaterialDesignIconic.Icon.gmi_info);
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
    }

    public void setStatusBarColor(final int color) {
        drawer.getDrawerLayout().getStatusBarBackgroundDrawable().setTint(color);
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
        if(!accentSubscription.isDisposed()) accentSubscription.dispose();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (service != null && player != null) player.updatePlayer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
