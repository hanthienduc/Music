
package com.boswelja.lastfm.models.artist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LastFMArtist {

    @SerializedName("artist")
    @Expose
    private Artist artist;

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

}
