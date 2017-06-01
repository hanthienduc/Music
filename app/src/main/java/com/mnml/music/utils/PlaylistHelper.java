package com.mnml.music.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mnml.music.adapters.PlaylistAdapter;
import com.mnml.music.models.Playlist;
import com.mnml.music.models.Song;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PlaylistHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "PlaylistDB";
    private static final String TABLE_PLAYLIST = "playlist";
    private static final String TABLE_SONG_FOR_PLAYLIST = "song_for_playlist";
    private static final String PLAYLIST_KEY_ID = "playlist_id";
    private static final String PLAYLIST_KEY_TITLE = "playlist_title";
    private static final String SONG_KEY_ID = "song_id";
    private static final String SONG_KEY_REAL_ID = "song_real_id";
    private static final String SONG_KEY_PLAYLISTID = "song_playlist_id";
    private static final String SONG_KEY_ALBUMID = "song_album_id";
    private static final String SONG_KEY_DESC = "song_desc";
    private static final String SONG_KEY_PATH = "song_path";
    private static final String SONG_KEY_NAME = "song_name";
    private static final String SONG_KEY_ALBUM_NAME = "song_album_name";
    private Context context;
    public PlaylistHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLAYLIST_TABLE =
                "CREATE TABLE " + TABLE_PLAYLIST + " ( "
                        + PLAYLIST_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + PLAYLIST_KEY_TITLE + " TEXT)";

        String CREATE_SONGS_FOR_PLAYLIST_TABLE =
                "CREATE TABLE song_for_playlist ("
                        + SONG_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + SONG_KEY_REAL_ID + " INTEGER,"
                        + SONG_KEY_PLAYLISTID + " INTEGER,"
                        + SONG_KEY_ALBUMID + " INTEGER,"
                        + SONG_KEY_DESC + " TEXT,"
                        + SONG_KEY_PATH + " TEXT,"
                        + SONG_KEY_NAME + " TEXT,"
                        + SONG_KEY_ALBUM_NAME + " TEXT)";

        db.execSQL(CREATE_PLAYLIST_TABLE);
        db.execSQL(CREATE_SONGS_FOR_PLAYLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // We probably shouldn't delete all playlists on db upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONG_FOR_PLAYLIST);

        this.onCreate(db);
    }

    int createNewPlayList(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PLAYLIST_KEY_TITLE, name);
        long id = db.insert(TABLE_PLAYLIST, null, values);
        db.close();
        return (int) id;
    }

    void renamePlaylist(String newName, int playlistId) {
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

    public void removePlayList(final Playlist playlist, final PlaylistAdapter adapter) {
        new MaterialDialog.Builder(context)
                .title("Delete playlist?")
                .content("Are you sure you want to delete this playlist?")
                .positiveText("Yes")
                .negativeText("Cancel")
                .onPositive((materialDialog, dialogAction) -> {
                    SQLiteDatabase db = PlaylistHelper.this.getWritableDatabase();
                    db.execSQL(
                            "DELETE FROM " + TABLE_PLAYLIST + " WHERE " + PLAYLIST_KEY_ID + "='" + playlist.getId() + "'");
                    db.execSQL(
                            "DELETE FROM "
                                    + TABLE_SONG_FOR_PLAYLIST
                                    + " WHERE "
                                    + SONG_KEY_PLAYLISTID
                                    + "='"
                                    + playlist.getId()
                                    + "'");
                    final ArrayList<Playlist> items = adapter.getItems();
                    items.remove(playlist);
                    adapter.updateData(items);
                })
                .show();
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
        values.put(SONG_KEY_PATH, song.getPath());
        values.put(SONG_KEY_NAME, song.getName());
        values.put(SONG_KEY_ALBUM_NAME, song.getAlbumName());

        db.insert(TABLE_SONG_FOR_PLAYLIST, null, values);
        db.close();
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
        int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
        int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
        int artistColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
        int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        if (cursor.moveToFirst()) {
            do {
                song = new Song(
                        cursor.getLong(idColumn),
                        cursor.getString(titleColumn),
                        cursor.getString(artistColumn),
                        cursor.getString(pathColumn),
                        cursor.getLong(albumIdColumn),
                        cursor.getString(albumColumn));
                songs.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return songs;
    }
}
