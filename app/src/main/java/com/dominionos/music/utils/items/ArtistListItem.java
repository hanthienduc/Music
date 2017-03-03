package com.dominionos.music.utils.items;

public class ArtistListItem {

    private final long id;
    private final String name;
    private final int numOfTracks;
    private final int numOfAlbums;


    public ArtistListItem(long id, String name, int numOfTracks, int numOfAlbums) {
        this.id = id;
        this.name = name;
        this.numOfTracks = numOfTracks;
        this.numOfAlbums = numOfAlbums;
    }

    public int getNumOfAlbums() {
        return numOfAlbums;
    }

    public int getNumOfTracks() {
        return numOfTracks;
    }

    public String getName() {
        return this.name;
    }

}
