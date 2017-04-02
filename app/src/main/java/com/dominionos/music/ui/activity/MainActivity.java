package com.dominionos.music.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.lapism.searchview.SearchView;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.main_toolbar) Toolbar toolbar;
    @BindView(R.id.main_viewpager) ViewPager viewPager;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingUpPanelLayout;

    private Unbinder unbinder;
    private SharedPreferences sharedPrefs;
    private boolean darkMode = false;
    private SearchView search;
    private Drawer drawer;
    private MusicService service;
    private PlayerFragment player;
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
        darkMode = sharedPrefs.getBoolean("dark_theme", false);
        setTheme(darkMode ? R.style.AppTheme_Dark : R.style.AppTheme_Main);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.main_viewpager);
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
            case R.id.search_item:
                search.open(true, item);
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
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tab_layout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);

        setDrawer();

        setDynamicShortcuts();

        setSearch();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        player = new PlayerFragment();
        transaction.replace(R.id.player_holder, player).commit();
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
        search.setTheme(darkMode ? SearchView.THEME_DARK : SearchView.THEME_LIGHT);
        search.setVersion(SearchView.VERSION_MENU_ITEM);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("dark_theme", darkMode);
                intent.putExtra("query", query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setDynamicShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();
            ShortcutInfo searchShortcut = new ShortcutInfo.Builder(this, "search")
                    .setShortLabel(getString(R.string.search))
                    .setIcon(Icon.createWithResource(MainActivity.this, R.drawable.ic_shortcut_search))
                    .setIntent(new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, SearchActivity.class))
                    .build();
            shortcuts.add(searchShortcut);
            shortcutManager.setDynamicShortcuts(shortcuts);
        }
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

    public SlidingUpPanelLayout getSlidingPanel() {
        return slidingUpPanelLayout;
    }

    private void setDrawer() {
        final PrimaryDrawerItem songs = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.songs).withIcon(MaterialDesignIconic.Icon.gmi_audio);
        final PrimaryDrawerItem albums = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.albums).withIcon(MaterialDesignIconic.Icon.gmi_collection_music);
        final PrimaryDrawerItem artists = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.artist).withIcon(MaterialDesignIconic.Icon.gmi_account_circle);
        final PrimaryDrawerItem playlist = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.playlist).withIcon(MaterialDesignIconic.Icon.gmi_playlist_audio);
        final SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(5).withName(getString(R.string.settings)).withSelectable(false).withIcon(MaterialDesignIconic.Icon.gmi_settings);
        final SecondaryDrawerItem about = new SecondaryDrawerItem().withIdentifier(6).withName(R.string.about).withSelectable(false).withIcon(MaterialDesignIconic.Icon.gmi_info);

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
                                startActivityForResult(intent, Config.SETTINGS_REQUEST_CODE);
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
        if (search.isSearchOpen()) {
            search.close(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        unbindService(serviceConnection);
    }

}
