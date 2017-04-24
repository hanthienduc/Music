package com.dominionos.music.items;

public class Playlist {

  private final int id;
  private String name;

  public Playlist(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
