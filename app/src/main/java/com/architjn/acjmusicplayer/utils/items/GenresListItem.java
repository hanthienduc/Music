package com.architjn.acjmusicplayer.utils.items;

public class GenresListItem {

    long id;
    String name;

    public GenresListItem(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

}
