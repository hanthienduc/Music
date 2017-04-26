package com.dominionos.music.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;
import com.dominionos.music.adapters.DialogPlaylistAdapter;
import com.dominionos.music.items.Playlist;
import com.dominionos.music.items.Song;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static boolean isSubsInstalled(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo("projekt.substratum", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return applicationInfo != null;
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static void setContentColor(View view, Context context, boolean darkMode) {
        view.setBackgroundColor(
                darkMode
                        ? ContextCompat.getColor(context, R.color.darkContentColour)
                        : ContextCompat.getColor(context, R.color.lightContentColor));
    }

    public static int getAutoStatColor(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static ArrayList<Song> getAlbumSongs(Context context, long albumId) {
        ArrayList<Song> albumSongList = new ArrayList<>();
        Cursor musicCursor;
        String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String whereVal[] = {String.valueOf(albumId)};
        String orderBy = MediaStore.Audio.Media.TRACK;

        musicCursor = context.getContentResolver()
                        .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, whereVal, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int albumNameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            do {
                albumSongList.add(
                        new Song(
                                musicCursor.getLong(idColumn),
                                musicCursor.getString(titleColumn),
                                musicCursor.getString(artistColumn),
                                musicCursor.getString(pathColumn),
                                false,
                                musicCursor.getLong(albumIdColumn),
                                musicCursor.getString(albumNameColumn)));
            } while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return albumSongList;
    }

    public static String getAlbumArt(Context context, long albumId) {
        Cursor cursor =
                context
                        .getContentResolver()
                        .query(
                                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                                MediaStore.Audio.Albums._ID + "=?",
                                new String[]{String.valueOf(albumId)},
                                null);
        String imagePath = "";
        if (cursor != null && cursor.moveToFirst()) {
            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            cursor.close();
        }
        return imagePath;
    }

    public static void addToPlaylistDialog(Context context, Song position) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(context);
        List<Playlist> playlist = sqLiteHelper.getAllPlaylist();
        playlist.add(new Playlist(-1, context.getString(R.string.create_new_playlist)));
        new MaterialDialog.Builder(context)
                .title(context.getString(R.string.add_to_playlist))
                .positiveText(context.getString(R.string.done))
                .adapter(new DialogPlaylistAdapter(context, playlist, position), layoutManager)
                .show();
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / Config.ALBUM_CARD_WIDTH);
    }
}
