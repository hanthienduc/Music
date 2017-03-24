package com.mnmlos.music.items;

public class Artist {

    private final String name;
    private final int numOfTracks;
    private final int numOfAlbums;


    public Artist(String name, int numOfTracks, int numOfAlbums) {
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
