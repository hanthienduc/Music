package com.dominionos.music.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.dominionos.music.items.Song;

import java.util.ArrayList;

public class MusicPlayerDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MusicPlayingDB";
    private static final String TABLE_PLAYBACK = "playback";
    private static final String SONG_KEY_ID = "song_id";
    private static final String SONG_KEY_REAL_ID = "song_real_id";
    private static final String SONG_KEY_ALBUMID = "song_album_id";
    private static final String SONG_KEY_DESC = "song_desc";
    private static final String SONG_KEY_FAV = "song_fav";
    private static final String SONG_KEY_PATH = "song_path";
    private static final String SONG_KEY_NAME = "song_name";
    private static final String SONG_KEY_ALBUM_NAME = "song_album_name";
    private static final String SONG_KEY_PLAYING = "song_playing";
    public MusicPlayerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLAYBACK_TABLE =
                "CREATE TABLE playback ("
                        + "song_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "song_real_id INTEGER,"
                        + "song_album_id INTEGER,"
                        + "song_desc TEXT,"
                        + "song_fav INTEGER,"
                        + "song_path TEXT,"
                        + "song_name TEXT,"
                        + "song_count INTEGER,"
                        + "song_album_name TEXT,"
                        + "song_mood TEXT,"
                        + "song_playing INTEGER)";
        db.execSQL(CREATE_PLAYBACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS playback");
        this.onCreate(db);
    }

    public ArrayList<Song> getStoredList() {
        ArrayList<Song> songs = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_PLAYBACK;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Song song;
        if (cursor.moveToFirst()) {
            do {
                song =
                        new Song(
                                Long.valueOf(cursor.getString(1)),
                                cursor.getString(6),
                                cursor.getString(3),
                                cursor.getString(5),
                                false,
                                Long.parseLong(cursor.getString(2)),
                                cursor.getString(8));
                songs.add(song);
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return songs;
    }

    public int getPlaybackTableSize() {
        String query = "SELECT  * FROM " + TABLE_PLAYBACK;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public void overwriteStoredList(ArrayList<Song> playList) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYBACK);
        for (int i = 0; i < playList.size(); i++) {
            Song song = playList.get(i);
            ContentValues values = new ContentValues();
            Log.v("sname", song.getName());
            values.putNull(SONG_KEY_ID);
            values.put(SONG_KEY_REAL_ID, (int) song.getId());
            values.put(SONG_KEY_ALBUMID, song.getAlbumId());
            values.put(SONG_KEY_DESC, song.getDesc());
            values.put(SONG_KEY_FAV, song.getFav());
            values.put(SONG_KEY_PATH, song.getPath());
            values.put(SONG_KEY_NAME, song.getName());
            values.put(SONG_KEY_ALBUM_NAME, song.getAlbumName());
            values.put(SONG_KEY_PLAYING, 0);
            db.insert(TABLE_PLAYBACK, null, values);
        }
    }
}
