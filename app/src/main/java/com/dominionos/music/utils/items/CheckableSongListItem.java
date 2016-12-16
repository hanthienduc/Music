package com.dominionos.music.utils.items;

import android.graphics.Bitmap;

public class CheckableSongListItem {


    long id, albumId;
    String name, desc, path, albumName;
    Boolean fav;
    public Boolean isSelected;
    Bitmap art;
    int count;

    public CheckableSongListItem(long id, String name, String desc, String path,
                        Boolean fav, long albumId, String albumName, int count) {
        this.desc = desc;
        this.fav = fav;
        this.path = path;
        this.id = id;
        this.name = name;
        this.albumId = albumId;
        this.count = count;
        this.albumName = albumName;
        this.isSelected = false;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }

    public String getAlbumName() {
        return this.albumName;
    }

    public String getDesc() {
        return this.desc;
    }

    public long getAlbumId() {
        return this.albumId;
    }


    public Boolean getFav() {
        return this.fav;
    }

    public Bitmap getArt() {
        return this.art;
    }

    public int getCount() {
        return this.count;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}
