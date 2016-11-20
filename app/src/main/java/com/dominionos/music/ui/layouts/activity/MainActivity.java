package com.dominionos.music.ui.layouts.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dominionos.music.R;
import com.dominionos.music.ui.layouts.fragments.PlaylistFragment;
import com.dominionos.music.utils.adapters.ViewPagerAdapter;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.ui.layouts.activity.settings.Settings;
import com.dominionos.music.ui.layouts.fragments.AlbumsFragment;
import com.dominionos.music.ui.layouts.fragments.ArtistsFragment;
import com.dominionos.music.ui.layouts.fragments.SongsFragment;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences settingsPref;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(0, 0);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab_main);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent requestSongDetials = new Intent();
                requestSongDetials.setAction(MusicService.ACTION_REQUEST_SONG_DETAILS);
                sendBroadcast(requestSongDetials);
            }
        });
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            init();
            setDrawer();

            Intent i = new Intent(MainActivity.this, MusicService.class);
            startService(i);

            setupViewPager(viewPager);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tablayout);
            if (settingsPref.getBoolean("pref_extend_tabs", false))
                tabLayout.setTabMode(TabLayout.MODE_FIXED);
            else
                tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            tabLayout.setupWithViewPager(viewPager);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

    private void init() {
        viewPager = (ViewPager) findViewById(R.id.main_viewPager);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        settingsPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new SongsFragment(), getResources().getString(R.string.songs));
        adapter.addFrag(new AlbumsFragment(), getResources().getString(R.string.album));
        adapter.addFrag(new ArtistsFragment(), getResources().getString(R.string.artist));
        adapter.addFrag(new PlaylistFragment(), getResources().getString(R.string.playlist));
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
    }

    public void setDrawer() {
        PrimaryDrawerItem songs = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.songs);
        PrimaryDrawerItem albums = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.album);
        PrimaryDrawerItem artists = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.artist);
        PrimaryDrawerItem playlists = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.playlist);
        SecondaryDrawerItem settings = new SecondaryDrawerItem().withIdentifier(5).withName(R.string.action_settings);

        final Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withCloseOnClick(true)
                .withHeader(R.layout.layout_header)
                .addDrawerItems(
                        songs,
                        albums,
                        artists,
                        playlists,
                        new DividerDrawerItem(),
                        settings
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {

                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        int drawerIdentifier = (int) drawerItem.getIdentifier();
                        switch(drawerIdentifier) {
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
                                startActivity(new Intent(MainActivity.this, Settings.class));
                                break;
                        }
                        return true;
                    }
                })
                .build();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                drawer.setSelectionAtPosition(position + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fab.getVisibility() != View.VISIBLE)
            fab.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, Settings.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch(requestCode) {
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                    setDrawer();

                    Intent i = new Intent(MainActivity.this, MusicService.class);
                    startService(i);

                    setupViewPager(viewPager);
                    TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tablayout);
                    if (settingsPref.getBoolean("pref_extend_tabs", false))
                        tabLayout.setTabMode(TabLayout.MODE_FIXED);
                    else
                        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                    tabLayout.setupWithViewPager(viewPager);
                } else {
                    finish();
                }
        }
    }

}
