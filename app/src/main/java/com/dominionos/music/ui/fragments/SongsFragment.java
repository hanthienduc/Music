package com.dominionos.music.ui.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.async.Action;
import com.dominionos.music.R;
import com.dominionos.music.adapters.SongsAdapter;
import com.dominionos.music.items.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SongsFragment extends Fragment {

    private View mainView;
    private FastScrollRecyclerView rv;
    private boolean darkMode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        darkMode = sharedPref.getBoolean("dark_theme", false);

        View v = inflater.inflate(R.layout.fragment_songs, container, false);
        mainView = v;

        init();

        setSongList();

        return v;
    }

    private void init() {
        rv = (FastScrollRecyclerView) mainView.findViewById(R.id.songs_fragment_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mainView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);
        if(darkMode) {
            rv.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.darkWindowBackground));
        }
    }

    private void setSongList() {
        new Action<ArrayList<Song>>() {

            @NonNull
            @Override
            public String id() {
                return "song_list";
            }

            @Nullable
            @Override
            protected ArrayList<Song> run() throws InterruptedException {
                final ArrayList<Song> songList = new ArrayList<>();
                final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
                final String orderBy = MediaStore.Audio.Media.TITLE;
                Cursor musicCursor = getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
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
                        songList.add(new Song(musicCursor.getLong(idColumn),
                                musicCursor.getString(titleColumn),
                                musicCursor.getString(artistColumn),
                                musicCursor.getString(pathColumn), false,
                                musicCursor.getLong(albumIdColumn),
                                musicCursor.getString(albumColumn)));
                    }
                    while (musicCursor.moveToNext());
                }

                if (musicCursor != null) {
                    musicCursor.close();
                }
                Collections.sort(songList, new Comparator<Song>() {
                    @Override
                    public int compare(Song song, Song t1) {
                        return song.getName().compareToIgnoreCase(t1.getName());
                    }
                });
                return songList;
            }

            @Override
            protected void done(ArrayList<Song> songList) {
                if(songList.size() != 0) {
                    rv.setAdapter(new SongsAdapter(mainView.getContext(), songList, darkMode));
                } else {
                    getActivity().findViewById(R.id.no_songs).setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }
}
