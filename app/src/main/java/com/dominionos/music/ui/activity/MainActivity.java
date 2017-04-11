package com.dominionos.music.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;
import com.dominionos.music.adapters.ViewPagerAdapter;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.ui.fragments.AlbumsFragment;
import com.dominionos.music.ui.fragments.ArtistsFragment;
import com.dominionos.music.ui.fragments.PlayerFragment;
import com.dominionos.music.ui.fragments.PlaylistFragment;
import com.dominionos.music.ui.fragments.SongsFragment;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.MySQLiteHelper;
import com.dominionos.music.utils.Utils;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends ATHToolbarActivity {

    @BindView(R.id.main_toolbar) Toolbar toolbar;
    @BindView(R.id.main_viewpager) ViewPager viewPager;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.main_tab_layout) TabLayout tabLayout;

    private Unbinder unbinder;
    private SharedPreferences sharedPrefs;
    private boolean darkMode = false;
    private Drawer drawer;
    private MusicService service;
    private PlayerFragment player;
    private int primaryColor, accentColor;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
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
        boolean subsTheme = sharedPrefs.getBoolean("substratum_theme", false);
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .activityTheme(R.style.AppTheme_Light)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .commit();
        }
        if(subsTheme) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .commit();
        }
        primaryColor = ThemeStore.primaryColor(this);
        accentColor = ThemeStore.accentColor(this);
        darkMode = sharedPrefs.getBoolean("dark_theme", false);
        ATH.setLightStatusbarAuto(this, Utils.getAutoStatColor(primaryColor));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        toolbar.setBackgroundColor(primaryColor);
        ATH.setActivityToolbarColorAuto(this, toolbar);
        tabLayout.setTabTextColors(ToolbarContentTintHelper.toolbarSubtitleColor(this, primaryColor), ToolbarContentTintHelper.toolbarTitleColor(this, primaryColor));
        tabLayout.setBackgroundColor(primaryColor);
        tabLayout.setSelectedTabIndicatorColor(accentColor);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        TintHelper.setTintAuto(fab, accentColor, true);

        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MEDIA_CONTENT_CONTROL,
                    Manifest.permission.INTERNET}, 1);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.play_all:
                if(service != null) service.playAllSongs();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(service != null) player.updatePlayer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Config.SETTINGS_REQUEST_CODE) {
            if (darkMode != sharedPrefs.getBoolean("dark_theme", false)) recreate();
        }
    }

    private void init() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Intent i = new Intent(MainActivity.this, MusicService.class);
        bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(i);

        setupViewPager();
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
        TintHelper.setTintAuto(tabLayout, primaryColor, true);

        setDrawer();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        player = new PlayerFragment();
        transaction.replace(R.id.player_holder, player).commit();

        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);

    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new SongsFragment(), getResources().getString(R.string.songs));
        adapter.addFrag(new AlbumsFragment(), getResources().getString(R.string.album));
        adapter.addFrag(new ArtistsFragment(), getResources().getString(R.string.artist));
        adapter.addFrag(new PlaylistFragment(), getResources().getString(R.string.playlist));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(4);
        fab.setOnClickListener(v -> new MaterialDialog.Builder(MainActivity.this)
                .title(R.string.add_playlist)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(getString(R.string.playlist_example), null, (dialog, input) -> {
                    if (!input.toString().equals("")) {
                        MySQLiteHelper helper = new MySQLiteHelper(MainActivity.this);
                        helper.createNewPlayList(input.toString());
                    } else {
                        Toast.makeText(MainActivity.this, R.string.playlist_name_empty_warning, Toast.LENGTH_SHORT).show();
                    }
                })
                .positiveText(getString(R.string.ok))
                .negativeText(getString(R.string.cancel)).show());
    }

    public SlidingUpPanelLayout getSlidingPanel() {
        return slidingUpPanelLayout;
    }

    private void setDrawer() {
        final PrimaryDrawerItem songs = new PrimaryDrawerItem().withSelectedTextColor(accentColor).withSelectedIconColor(accentColor).withIdentifier(0).withName(R.string.songs).withIcon(MaterialDesignIconic.Icon.gmi_audio);
        final PrimaryDrawerItem albums = new PrimaryDrawerItem().withSelectedTextColor(accentColor).withSelectedIconColor(accentColor).withIdentifier(1).withName(R.string.albums).withIcon(MaterialDesignIconic.Icon.gmi_collection_music);
        final PrimaryDrawerItem artists = new PrimaryDrawerItem().withSelectedTextColor(accentColor).withSelectedIconColor(accentColor).withIdentifier(2).withName(R.string.artist).withIcon(MaterialDesignIconic.Icon.gmi_account_circle);
        final PrimaryDrawerItem playlist = new PrimaryDrawerItem().withSelectedTextColor(accentColor).withSelectedIconColor(accentColor).withIdentifier(3).withName(R.string.playlist).withIcon(MaterialDesignIconic.Icon.gmi_playlist_audio);
        final SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(5).withName(getString(R.string.settings)).withSelectable(false).withIcon(MaterialDesignIconic.Icon.gmi_settings);
        final SecondaryDrawerItem about = new SecondaryDrawerItem().withIdentifier(6).withName(R.string.about).withSelectable(false).withIcon(MaterialDesignIconic.Icon.gmi_info);
        final SectionDrawerItem librarySection = new SectionDrawerItem().withName("Library").withIsExpanded(true).withDivider(false).withSubItems(songs, albums, artists, playlist);
        final SectionDrawerItem appSection = new SectionDrawerItem().withDivider(true).withName("App").withSubItems(settings, about).withIsExpanded(true);
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHeader(R.layout.header)
                .withActionBarDrawerToggle(true)
                .withCloseOnClick(true)
                .addDrawerItems(
                        librarySection,
                        appSection
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
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
                            startActivityForResult(intent, Config.SETTINGS_REQUEST_CODE);
                            break;
                        case 6:
                            startActivity(new Intent(MainActivity.this, AboutActivity.class));
                            break;
                    }
                    return true;
                })
                .build();
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        drawer.getHeader().findViewById(R.id.header).setBackgroundColor(primaryColor);
        drawer.getDrawerLayout().setStatusBarBackgroundColor(Utils.getAutoStatColor(primaryColor));
        drawer.getActionBarDrawerToggle().getDrawerArrowDrawable().setColor(ToolbarContentTintHelper.toolbarContentColor(this, primaryColor));
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
                drawer.setSelection(position, false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        drawer.setSelectionAtPosition(2);
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        unbindService(serviceConnection);
    }

}
