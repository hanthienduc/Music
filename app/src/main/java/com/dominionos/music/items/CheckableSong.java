package com.dominionos.music.items;

public class CheckableSong {


    private final long id;
    private final long albumId;
    private final String name;
    private final String desc;
    private final String path;
    private final String albumName;
    public Boolean isSelected;
    private final int count;

    public CheckableSong(long id, String name, String desc, String path,
                         long albumId, String albumName, int count) {
        this.desc = desc;
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

    public int getCount() {
        return this.count;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }
}
