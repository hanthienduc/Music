package com.mnml.music.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.mnml.music.models.Song;

import java.util.ArrayList;

public class MusicPlayerDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "MusicPlayingDB";
    private static final String TABLE_PLAYBACK = "playback";
    private static final String SONG_KEY_ID = "song_id";
    private static final String SONG_KEY_REAL_ID = "song_real_id";
    private static final String SONG_KEY_ALBUMID = "song_album_id";
    private static final String SONG_KEY_DESC = "song_desc";
    private static final String SONG_KEY_PATH = "song_path";
    private static final String SONG_KEY_NAME = "song_name";
    private static final String SONG_KEY_ALBUM_NAME = "song_album_name";
    public MusicPlayerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLAYBACK_TABLE =
                "CREATE TABLE " + TABLE_PLAYBACK + " ("
                        + SONG_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + SONG_KEY_REAL_ID + " INTEGER,"
                        + SONG_KEY_ALBUMID + " INTEGER,"
                        + SONG_KEY_DESC + " TEXT,"
                        + SONG_KEY_PATH + " TEXT,"
                        + SONG_KEY_NAME + " TEXT,"
                        + SONG_KEY_ALBUM_NAME + " TEXT)";
        db.execSQL(CREATE_PLAYBACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYBACK);
        this.onCreate(db);
    }

    public ArrayList<Song> getStoredList() {
        ArrayList<Song> songs = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_PLAYBACK;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Song song;
        if (cursor.moveToFirst()) {
            int songNameIndex = cursor.getColumnIndex(SONG_KEY_NAME);
            int songDescIndex = cursor.getColumnIndex(SONG_KEY_DESC);
            int songIdIndex = cursor.getColumnIndex(SONG_KEY_REAL_ID);
            int songPathIndex = cursor.getColumnIndex(SONG_KEY_PATH);
            int albumNameIndex = cursor.getColumnIndex(SONG_KEY_ALBUM_NAME);
            int albumIdIndex = cursor.getColumnIndex(SONG_KEY_ALBUMID);
            do {
                song = new Song(
                        Long.valueOf(cursor.getString(songIdIndex)),
                        cursor.getString(songNameIndex),
                        cursor.getString(songDescIndex),
                        cursor.getString(songPathIndex),
                        Long.parseLong(cursor.getString(albumIdIndex)),
                        cursor.getString(albumNameIndex));
                songs.add(song);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return songs;
    }

    public void overwriteStoredList(ArrayList<Song> playList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYBACK);
        for (Song song : playList) {
            ContentValues values = new ContentValues();
            Log.v("sname", song.getName());
            values.putNull(SONG_KEY_ID);
            values.put(SONG_KEY_REAL_ID, (int) song.getId());
            values.put(SONG_KEY_ALBUMID, song.getAlbumId());
            values.put(SONG_KEY_DESC, song.getDesc());
            values.put(SONG_KEY_PATH, song.getPath());
            values.put(SONG_KEY_NAME, song.getName());
            values.put(SONG_KEY_ALBUM_NAME, song.getAlbumName());
            db.insert(TABLE_PLAYBACK, null, values);
        }
    }
}
