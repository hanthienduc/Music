package com.dominionos.music.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.afollestad.async.Action;
import com.bumptech.glide.Glide;
import com.dominionos.music.R;
import com.dominionos.music.adapters.ArtistAdapter;
import com.dominionos.music.items.Artist;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Comparator;

public class ArtistsFragment extends Fragment {

    private FastScrollRecyclerView rv;
    private Context context;

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);
        context = view.getContext();

        rv = (FastScrollRecyclerView) view.findViewById(R.id.artist_list);
        int accentColor = ThemeStore.accentColor(context);
        rv.setPopupBgColor(accentColor);
        rv.setThumbColor(accentColor);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        rv.setLayoutManager(linearLayoutManager);

        getArtistList();

        return view;
    }

    private void getArtistList() {
        new Action<ArrayList<Artist>>() {

            @NonNull
            @Override
            public String id() {
                return "artist_list";
            }

            @Override
            protected ArrayList<Artist> run() throws InterruptedException {
                final ArrayList<Artist> artistList = new ArrayList<>();
                final String orderBy = MediaStore.Audio.Artists.ARTIST;
                Cursor musicCursor =
                        context
                                .getContentResolver()
                                .query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

                if (musicCursor != null && musicCursor.moveToFirst()) {
                    int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
                    int numOfAlbumsColumn =
                            musicCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
                    int numOfTracksColumn =
                            musicCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
                    do {
                        artistList.add(
                                new Artist(
                                        musicCursor.getString(titleColumn),
                                        musicCursor.getInt(numOfTracksColumn),
                                        musicCursor.getInt(numOfAlbumsColumn)));
                    } while (musicCursor.moveToNext());
                }
                artistList.sort(Comparator.comparing(Artist::getName));

                if (musicCursor != null) {
                    musicCursor.close();
                }
                return artistList;
            }

            @Override
            protected void done(ArrayList<Artist> artistList) {
                if (artistList.size() != 0) {
                    rv.setAdapter(new ArtistAdapter(context, artistList, Glide.with(context)));
                } else {
                    getActivity().findViewById(R.id.no_artists).setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }
}
