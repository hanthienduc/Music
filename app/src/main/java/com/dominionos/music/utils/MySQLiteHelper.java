package com.dominionos.music.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import com.dominionos.music.items.CheckableSong;
import com.dominionos.music.items.Playlist;
import com.dominionos.music.items.Song;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MySQLiteHelper extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "PlaylistDB";
  private final Context context;

  public MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    String CREATE_PLAYLIST_TABLE =
        "CREATE TABLE playlist ( "
            + "playlist_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "playlist_title TEXT)";

    String CREATE_SONGS_FOR_PLAYLIST_TABLE =
        "CREATE TABLE song_for_playlist ("
            + "song_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "song_real_id INTEGER,"
            + "song_playlist_id INTEGER,"
            + "song_album_id INTEGER,"
            + "song_desc TEXT,"
            + "song_fav INTEGER,"
            + "song_path TEXT,"
            + "song_name TEXT,"
            + "song_count INTEGER,"
            + "song_album_name TEXT,"
            + "song_mood TEXT)";

    String CREATE_MOOD_TABLE =
        "CREATE TABLE moods ("
            + "song_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "song_artist TEXT,"
            + "song_name TEXT,"
            + "song_album TEXT,"
            + "song_mood TEXT)";

    db.execSQL(CREATE_MOOD_TABLE);
    db.execSQL(CREATE_PLAYLIST_TABLE);
    db.execSQL(CREATE_SONGS_FOR_PLAYLIST_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS playlist");
    db.execSQL("DROP TABLE IF EXISTS song_for_playlist");
    db.execSQL("DROP TABLE IF EXISTS song");

    this.onCreate(db);
  }

  private static final String TABLE_PLAYLIST = "playlist";
  private static final String TABLE_SONG_FOR_PLAYLIST = "song_for_playlist";

  private static final String PLAYLIST_KEY_ID = "playlist_id";
  private static final String PLAYLIST_KEY_TITLE = "playlist_title";

  private static final String SONG_KEY_ID = "song_id";
  private static final String SONG_KEY_REAL_ID = "song_real_id";
  private static final String SONG_KEY_PLAYLISTID = "song_playlist_id";
  private static final String SONG_KEY_ALBUMID = "song_album_id";
  private static final String SONG_KEY_DESC = "song_desc";
  private static final String SONG_KEY_FAV = "song_fav";
  private static final String SONG_KEY_PATH = "song_path";
  private static final String SONG_KEY_NAME = "song_name";
  private static final String SONG_KEY_COUNT = "song_count";
  private static final String SONG_KEY_ALBUM_NAME = "song_album_name";

  public int createNewPlayList(String name) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(PLAYLIST_KEY_TITLE, name);
    long id = db.insert(TABLE_PLAYLIST, null, values);
    db.close();
    return (int) id;
  }

  public void renamePlaylist(String newName, int playlistId) {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL(
        "UPDATE "
            + TABLE_PLAYLIST
            + " SET "
            + PLAYLIST_KEY_TITLE
            + "='"
            + newName
            + "' WHERE "
            + PLAYLIST_KEY_ID
            + "='"
            + playlistId
            + "'");
  }

  public void removePlayList(int playlistId) {
    SQLiteDatabase db = this.getWritableDatabase();
    db.execSQL(
        "DELETE FROM " + TABLE_PLAYLIST + " WHERE " + PLAYLIST_KEY_ID + "='" + playlistId + "'");
    db.execSQL(
        "DELETE FROM "
            + TABLE_SONG_FOR_PLAYLIST
            + " WHERE "
            + SONG_KEY_PLAYLISTID
            + "='"
            + playlistId
            + "'");
  }

  public List<Playlist> getAllPlaylist() {
    List<Playlist> playlists = new LinkedList<>();
    String query = "SELECT  * FROM " + TABLE_PLAYLIST;
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery(query, null);
    Playlist playlist;
    if (cursor.moveToFirst()) {
      do {
        playlist = new Playlist(Integer.parseInt(cursor.getString(0)), cursor.getString(1));
        playlists.add(playlist);
      } while (cursor.moveToNext());
    }
    cursor.close();

    return playlists;
  }

  public void addSong(Song song, int playlistId) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.putNull(SONG_KEY_ID);
    values.put(SONG_KEY_REAL_ID, (int) song.getId());
    values.put(SONG_KEY_PLAYLISTID, playlistId);
    values.put(SONG_KEY_ALBUMID, song.getAlbumId());
    values.put(SONG_KEY_DESC, song.getDesc());
    values.put(SONG_KEY_FAV, song.getFav());
    values.put(SONG_KEY_PATH, song.getPath());
    values.put(SONG_KEY_NAME, song.getName());
    values.put(SONG_KEY_ALBUM_NAME, song.getAlbumName());

    db.insert(TABLE_SONG_FOR_PLAYLIST, null, values);
    db.close();
  }

  public void addSongs(List<CheckableSong> songList, int playlistId) {
    int songListSize = songList.size();
    int currentItem = 0;
    while (currentItem < songListSize) {
      CheckableSong item = songList.get(currentItem);
      SQLiteDatabase db = this.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.putNull(SONG_KEY_ID);
      values.put(SONG_KEY_REAL_ID, (int) item.getId());
      values.put(SONG_KEY_PLAYLISTID, playlistId);
      values.put(SONG_KEY_ALBUMID, item.getAlbumId());
      values.put(SONG_KEY_DESC, item.getDesc());
      values.put(SONG_KEY_PATH, item.getPath());
      values.put(SONG_KEY_NAME, item.getName());
      values.put(SONG_KEY_COUNT, item.getCount());
      values.put(SONG_KEY_ALBUM_NAME, item.getAlbumName());
      db.insert(TABLE_SONG_FOR_PLAYLIST, null, values);
      db.close();
      currentItem++;
    }
  }

  public void addSong(String songName, int playlistId) {
    Song song = getSong(songName);
    addSong(song, playlistId);
  }

  private Song getSong(String songName) {
    final String where =
        MediaStore.Audio.Media.IS_MUSIC
            + "=1 AND "
            + MediaStore.Audio.Media.TITLE
            + "='"
            + songName
            + "'";
    final String orderBy = MediaStore.Audio.Media.TITLE;
    Cursor musicCursor =
        context
            .getContentResolver()
            .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, orderBy);
    if (musicCursor != null && musicCursor.moveToFirst()) {
      int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
      int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
      int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
      int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
      int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
      int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
      String temp = musicCursor.getString(titleColumn);
      return new Song(
          musicCursor.getLong(idColumn),
          temp,
          musicCursor.getString(artistColumn),
          musicCursor.getString(pathColumn),
          false,
          musicCursor.getLong(albumIdColumn),
          musicCursor.getString(albumColumn));
    }
    if (musicCursor != null) {
      musicCursor.close();
    }
    return null;
  }

  public ArrayList<Song> getPlayListSongs(int playlistId) {
    ArrayList<Song> songs = new ArrayList<>();
    String query =
        "SELECT  * FROM "
            + TABLE_SONG_FOR_PLAYLIST
            + " WHERE "
            + SONG_KEY_PLAYLISTID
            + "='"
            + playlistId
            + "'";
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery(query, null);
    Song song;
    if (cursor.moveToFirst()) {
      do {
        boolean fav;
        fav = !cursor.getString(4).matches("0");
        song =
            new Song(
                Long.valueOf(cursor.getString(1)),
                cursor.getString(7),
                cursor.getString(4),
                cursor.getString(6),
                fav,
                Long.parseLong(cursor.getString(3)),
                cursor.getString(9));
        songs.add(song);
      } while (cursor.moveToNext());
    }
    cursor.close();

    return songs;
  }
}
