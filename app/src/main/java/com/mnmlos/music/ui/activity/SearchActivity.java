package com.mnmlos.music.ui.activity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.mnmlos.music.R;
import com.mnmlos.music.adapters.SongsAdapter;
import com.mnmlos.music.items.Song;
import com.lapism.searchview.SearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RecyclerView searchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean darkMode = sharedPref.getBoolean("dark_theme", false);

        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.darkWindowBackground));
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        searchList = (RecyclerView) findViewById(R.id.search_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        searchList.setLayoutManager(layoutManager);
        searchList.setBackgroundColor(darkMode ?
                ContextCompat.getColor(this, R.color.darkWindowBackground) : ContextCompat.getColor(this, R.color.windowBackground));


        final ArrayList<Song> songs = new ArrayList<>();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
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
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            do {
                songs.add(new Song(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn)));
            }
            while (musicCursor.moveToNext());
            Collections.sort(songs, new Comparator<Song>() {
                @Override
                public int compare(Song song, Song t1) {
                    return song.getName().compareTo(t1.getName());
                }
            });
            musicCursor.close();
            final List<Song> searchResults = new ArrayList<>();
            SearchView search = (SearchView) findViewById(R.id.searchView);
            if(darkMode) {
                search.setTheme(SearchView.THEME_DARK);
            } else {
                search.setTheme(SearchView.THEME_LIGHT);
            }
            search.setVersion(SearchView.VERSION_TOOLBAR);
            search.setArrowOnly(false);
            search.setOnMenuClickListener(new SearchView.OnMenuClickListener() {
                @Override
                public void onMenuClick() {
                    finish();
                }
            });
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchResults.clear();
                    for (Song song : songs) {
                        if (song.getName().toLowerCase().contains(query.toLowerCase())) {
                            searchResults.add(song);
                        }
                        searchList.setAdapter(new SongsAdapter(SearchActivity.this, searchResults, darkMode, Glide.with(SearchActivity.this)));
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            search.setQuery(getIntent().getStringExtra("query"), true);
        }
    }
}
