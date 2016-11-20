package com.dominionos.music.utils.items;

public class PlaylistItem {

    private String songTitle, songDesc;

    public PlaylistItem(String songTitle, String songDesc) {
        this.songDesc = songDesc;
        this.songTitle = songTitle;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongDesc() {
        return songDesc;
    }

}
