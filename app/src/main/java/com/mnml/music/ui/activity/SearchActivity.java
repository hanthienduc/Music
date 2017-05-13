package com.mnml.music.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.lapism.searchview.SearchFilter;
import com.lapism.searchview.SearchView;
import com.mnml.music.R;
import com.mnml.music.adapters.AlbumsAdapter;
import com.mnml.music.adapters.ArtistAdapter;
import com.mnml.music.adapters.SongsAdapter;
import com.mnml.music.models.Album;
import com.mnml.music.models.Artist;
import com.mnml.music.models.Song;
import com.mnml.music.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends ATHToolbarActivity {

    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.songs_rv)
    RecyclerView songsRv;
    @BindView(R.id.albums_rv)
    RecyclerView albumsRv;
    @BindView(R.id.artists_rv)
    RecyclerView artistsRv;
    @BindView(R.id.song_search)
    LinearLayout songSearch;
    @BindView(R.id.album_search)
    LinearLayout albumSearch;
    @BindView(R.id.artist_search)
    LinearLayout artistSearch;
    @BindView(R.id.song_header)
    TextView songHeader;
    @BindView(R.id.album_header)
    TextView albumHeader;
    @BindView(R.id.artist_header)
    TextView artistHeader;
    private ArrayList<Song> allSongs, searchedSongs;
    private ArrayList<Album> allAlbums, searchedAlbums;
    private ArrayList<Artist> allArtists, searchedArtists;
    private SongsAdapter songsAdapter;
    private AlbumsAdapter albumsAdapter;
    private ArtistAdapter artistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        init();
    }

    private void searchLists(final String query) {
        final List<Boolean> filterState = searchView.getFiltersStates();
        if (!query.isEmpty()) {
            if (filterState.get(0)) {
                for (Song song : allSongs) {
                    final boolean containsQuery = (Utils.containsIgnoreCase(song.getName().trim(), query) || Utils.containsIgnoreCase(song.getAlbumName().trim(), query));
                    final boolean alreadyContainsSong = searchedSongs.contains(song);
                    if (containsQuery && !alreadyContainsSong) {
                        searchedSongs.add(song);
                    } else if (!containsQuery && alreadyContainsSong) {
                        searchedSongs.remove(song);
                    }
                }
                songsAdapter.updateData(searchedSongs);
                songSearch.setVisibility(View.VISIBLE);
            } else {
                songSearch.setVisibility(View.GONE);
            }
            if (filterState.get(1)) {
                for (Album album : allAlbums) {
                    final boolean containsQuery = Utils.containsIgnoreCase(album.getName().trim(), query);
                    final boolean alreadyContainsAlbum = searchedAlbums.contains(album);
                    if (containsQuery && !alreadyContainsAlbum) {
                        searchedAlbums.add(album);
                    } else if (!containsQuery && alreadyContainsAlbum) {
                        searchedAlbums.remove(album);
                    }
                }
                albumsAdapter.updateData(searchedAlbums);
                albumSearch.setVisibility(View.VISIBLE);
            } else {
                albumSearch.setVisibility(View.GONE);
            }
            if (filterState.get(2)) {
                for (Artist artist : allArtists) {
                    final boolean containsQuery = Utils.containsIgnoreCase(artist.getName().trim(), query);
                    final boolean alreadyContainsArtist = searchedArtists.contains(artist);
                    if (containsQuery && !alreadyContainsArtist) {
                        searchedArtists.add(artist);
                    } else if (!containsQuery && alreadyContainsArtist) {
                        searchedArtists.remove(artist);
                    }
                }
                artistAdapter.updateData(searchedArtists);
                artistSearch.setVisibility(View.VISIBLE);
            } else {
                artistSearch.setVisibility(View.GONE);
            }
        } else {
            resetSearchLists();
        }

    }

    private void init() {
        final int color = ThemeStore.accentColor(this);
        songHeader.setTextColor(color);
        albumHeader.setTextColor(color);
        artistHeader.setTextColor(color);

        final ArrayList<SearchFilter> searchFilters = new ArrayList<>();
        searchFilters.add(new SearchFilter(getString(R.string.songs), true));
        searchFilters.add(new SearchFilter(getString(R.string.albums), true));
        searchFilters.add(new SearchFilter(getString(R.string.artist), true));
        searchView.setFilters(searchFilters);
        searchView.setArrowOnly(false);

        allSongs = Utils.getAllSongs(this);
        searchedSongs = new ArrayList<>();
        LinearLayoutManager songsLayoutAdapter = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        songsRv.setLayoutManager(songsLayoutAdapter);
        songsRv.setNestedScrollingEnabled(false);
        songsAdapter = new SongsAdapter(this, allSongs, Glide.with(this), true);
        songsRv.setAdapter(songsAdapter);

        allAlbums = Utils.getAlbums(this);
        searchedAlbums = new ArrayList<>();
        GridLayoutManager albumsLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false);
        albumsRv.setLayoutManager(albumsLayoutManager);
        albumsRv.setNestedScrollingEnabled(false);
        albumsAdapter = new AlbumsAdapter(this, allAlbums, Glide.with(this), true);
        albumsRv.setAdapter(albumsAdapter);

        allArtists = Utils.getArtists(this);
        searchedArtists = new ArrayList<>();
        LinearLayoutManager artistsLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        artistsRv.setLayoutManager(artistsLayoutManager);
        artistsRv.setNestedScrollingEnabled(false);
        artistAdapter = new ArtistAdapter(this, allArtists, Glide.with(this));
        artistsRv.setAdapter(artistAdapter);

        resetSearchLists();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchLists(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Toast.makeText(SearchActivity.this, s, Toast.LENGTH_SHORT).show();
                searchLists(s);
                return true;
            }
        });
        searchView.setOnMenuClickListener(super::onBackPressed);
    }

    private void resetSearchLists() {
        searchedSongs.clear();
        searchedSongs.addAll(allSongs);
        searchedAlbums.clear();
        searchedAlbums.addAll(allAlbums);
        searchedArtists.clear();
        searchedArtists.addAll(allArtists);
    }
}
