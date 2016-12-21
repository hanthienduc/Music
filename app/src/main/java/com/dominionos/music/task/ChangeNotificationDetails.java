package com.dominionos.music.task;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;

import com.dominionos.music.R;
import com.dominionos.music.service.MusicService;

public class ChangeNotificationDetails extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final long albumId;
    private final NotificationManager notificationManager;
    private final Notification notificationCompat;

    public ChangeNotificationDetails(Context context, long albumId,
                                     NotificationManager notificationManager,
                                     Notification notificationCompat) {
        this.context = context;
        this.albumId = albumId;
        this.notificationCompat = notificationCompat;
        this.notificationManager = notificationManager;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(albumId)},
                null);
        String songArt = "";
        if (cursor != null && cursor.moveToFirst()) {
            songArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        try {
            final Bitmap albumArt = BitmapFactory.decodeFile(songArt, options);
            Palette.generateAsync(BitmapFactory.decodeFile(songArt, options),
                    new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(final Palette palette) {
                            Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bmp);
                            canvas.drawColor(palette.getDarkVibrantColor(
                                    ResourcesCompat.getColor(context.getResources(), R.color.noti_background, null)));
                            notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_color_bg,
                                    bmp);
                            notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art, albumArt);
                            notificationManager.notify(MusicService.NOTIFICATION_ID, notificationCompat);
                        }
                    }
            );
        } catch (IllegalArgumentException e) {
            notificationCompat.bigContentView.setImageViewResource(R.id.noti_album_art, R.drawable.default_artwork_dark);
            Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(ResourcesCompat.getColor(context.getResources(), R.color.noti_background, null));
            notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_color_bg,
                    bmp);
            notificationManager.notify(MusicService.NOTIFICATION_ID, notificationCompat);
            e.printStackTrace();
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

}
