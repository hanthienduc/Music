package com.mnml.music.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;

import com.mnml.music.R;
import com.mnml.music.models.Song;
import com.mnml.music.service.MusicService;
import com.mnml.music.ui.activity.MainActivity;
import com.kabouzeid.appthemehelper.ThemeStore;

import java.io.File;

public class NotificationHandler {
    private NotificationManagerCompat notificationManager;
    private MusicService service;
    private PendingIntent launchAppIntent;
    private NotificationCompat.Action prev, next, play, pause, cancel;
    private Song currentSong;

    public void init(MusicService service) {
        this.service = service;
        notificationManager = NotificationManagerCompat.from(service);

        final String packageName = service.getPackageName();
        PendingIntent playIntent = PendingIntent.getBroadcast(
                service,
                100,
                new Intent(Config.TOGGLE_PLAY).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent prevIntent = PendingIntent.getBroadcast(
                service,
                100,
                new Intent(Config.PREV).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nextIntent = PendingIntent.getBroadcast(
                service,
                100,
                new Intent(Config.NEXT).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(
                service,
                100,
                new Intent(Config.CANCEL_NOTIFICATION).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT);
        launchAppIntent = PendingIntent.getActivity(
                service,
                100,
                new Intent(service.getApplicationContext(), MainActivity.class).setPackage(packageName),
                PendingIntent.FLAG_UPDATE_CURRENT);

        prev = new NotificationCompat.Action(R.drawable.ic_skip_previous, service.getString(R.string.previous), prevIntent);
        pause = new NotificationCompat.Action(R.drawable.ic_pause, service.getString(R.string.play), playIntent);
        play = new NotificationCompat.Action(R.drawable.ic_play, service.getString(R.string.play), playIntent);

        next = new NotificationCompat.Action(R.drawable.ic_skip_next, service.getString(R.string.next), nextIntent);
        cancel = new NotificationCompat.Action(R.drawable.ic_remove, service.getString(R.string.cancel), cancelIntent);
    }

    public Notification startNotification(Song currentSong) {
        this.currentSong = currentSong;
        return createNotification();
    }

    public void changeNotification(Song currentSong) {
        this.currentSong = currentSong;
        notificationManager.notify(Config.NOTIFICATION_ID, createNotification());
    }

    private Notification createNotification() {
        File image = new File(Utils.getAlbumArt(service, currentSong.getAlbumId()));
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
        bitmap = Bitmap.createBitmap(bitmap);
        Palette p = Palette.from(bitmap).generate();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(service);
        notificationBuilder
                .setStyle(
                        new NotificationCompat.MediaStyle()
                                .setMediaSession(service.getSessionToken())
                                .setShowActionsInCompactView(1))
                .setShowWhen(false)
                .setColor(p.getVibrantColor(ThemeStore.accentColor(service)))
                .setContentIntent(launchAppIntent)
                .setSmallIcon(R.drawable.ic_audiotrack)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(currentSong.getName())
                .setContentText(currentSong.getDesc())
                .setLargeIcon(bitmap);
        notificationBuilder.addAction(prev);
        notificationBuilder.addAction(service.isPlaying() ? pause : play);
        if (service.getPlayingList().size() > 1) {
            notificationBuilder.addAction(next);
        }
        notificationBuilder.addAction(cancel);
        return notificationBuilder.build();
    }

    public void destroyNotification() {
        notificationManager.cancel(Config.NOTIFICATION_ID);
    }
}
