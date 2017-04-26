package com.mnml.music.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EdgeEffect;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mnml.music.R;
import com.mnml.music.adapters.DialogPlaylistAdapter;
import com.mnml.music.items.Playlist;
import com.mnml.music.items.Song;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Utils {

    public static boolean isSubsInstalled(final Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo("projekt.substratum", 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return applicationInfo != null;
    }

    public static boolean isGooglePlayServicesAvailable(final Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static void setContentColor(final View view, final Context context, final boolean darkMode) {
        view.setBackgroundColor(
                darkMode
                        ? ContextCompat.getColor(context, R.color.darkContentColour)
                        : ContextCompat.getColor(context, R.color.lightContentColor));
    }

    public static int getAutoStatColor(final int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static ArrayList<Song> getArtistSongs(final Context context, final String artistName) {
        final ArrayList<Song> list = new ArrayList<>();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            do {
                if (artistName.equals(musicCursor.getString(artistColumn))) {
                    list.add(
                            new Song(
                                    musicCursor.getLong(idColumn),
                                    musicCursor.getString(titleColumn),
                                    musicCursor.getString(artistColumn),
                                    musicCursor.getString(pathColumn),
                                    false,
                                    musicCursor.getLong(albumIdColumn),
                                    musicCursor.getString(albumColumn)));
                }
            } while (musicCursor.moveToNext());
            list.sort(Comparator.comparing(Song::getName));
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return list;
    }

    public static ArrayList<Song> getAllSongs(final Context context) {
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, null, orderBy);
        final ArrayList<Song> songList = getSongs(musicCursor);
        songList.sort(Comparator.comparing(Song::getName));
        return songList;
    }

    public static ArrayList<Song> getAlbumSongs(final Context context, final long albumId) {
        Cursor musicCursor;
        String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String whereVal[] = {String.valueOf(albumId)};
        String orderBy = MediaStore.Audio.Media.TRACK;

        musicCursor = context.getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, where, whereVal, orderBy);

        return getSongs(musicCursor);
    }

    private static ArrayList<Song> getSongs(final Cursor cursor) {
        final ArrayList<Song> list = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int albumNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            do {
                list.add(
                        new Song(
                                cursor.getLong(idColumn),
                                cursor.getString(titleColumn),
                                cursor.getString(artistColumn),
                                cursor.getString(pathColumn),
                                false,
                                cursor.getLong(albumIdColumn),
                                cursor.getString(albumNameColumn)));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public static String getAlbumArt(final Context context, final long albumId) {
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

    public static void addToPlaylistDialog(final Context context, final Song position) {
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

    public static int dpToPx(final Context context, final int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int calculateNoOfColumns(final Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / Config.ALBUM_CARD_WIDTH);
    }

    public static void setEdgeGlowColor(final RecyclerView recyclerView, final int color) {
        try {
            final Class<?> clazz = RecyclerView.class;
            for (final String name : new String[] {"ensureTopGlow", "ensureBottomGlow"}) {
                Method method = clazz.getDeclaredMethod(name);
                method.setAccessible(true);
                method.invoke(recyclerView);
            }
            for (final String name : new String[] {"mTopGlow", "mBottomGlow"}) {
                final Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                final Object edge = field.get(recyclerView); // android.support.v4.widget.EdgeEffectCompat
                final Field fEdgeEffect = edge.getClass().getDeclaredField("mEdgeEffect");
                fEdgeEffect.setAccessible(true);
                ((EdgeEffect) fEdgeEffect.get(edge)).setColor(color);
            }
        } catch (final Exception ignored) {}

    }
}
