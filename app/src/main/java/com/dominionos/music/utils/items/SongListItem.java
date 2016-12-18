package com.dominionos.music.utils.items;

public class SongListItem {

    private final long id;
    private final long albumId;
    private final String name;
    private final String desc;
    private final String path;
    private final String albumName;
    private final Boolean fav;
    private final int count;

    public SongListItem(long id, String name, String desc, String path,
                        Boolean fav, long albumId, String albumName, int count) {
        this.desc = desc;
        this.fav = fav;
        this.path = path;
        this.id = id;
        this.name = name;
        this.albumId = albumId;
        this.count = count;
        this.albumName = albumName;
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

    public int getCount() {
        return this.count;
    }

}
