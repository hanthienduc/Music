package com.dominionos.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.afollestad.async.Action;
import com.dominionos.music.R;
import com.dominionos.music.ui.activity.MainActivity;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.MusicPlayerDBHelper;
import com.dominionos.music.utils.MySQLiteHelper;
import com.dominionos.music.items.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;

    private boolean repeatOne = false, repeatAll = false, shuffle = false;
    private int currentPlaylistSongId = -1, pausedSongSeek;
    private Song pausedSong;
    private MusicPlayerDBHelper playList;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private ArrayList<Song> songList, preShuffle, playingList;
    private NotificationManagerCompat notificationManager;
    private Song currentSong;
    private SharedPreferences prefs;
    private IBinder binder = new MyBinder();

    private final AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if(mediaPlayer != null) {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            if(mediaPlayer.isPlaying()) {
                                mediaPlayer.pause();
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            if(!mediaPlayer.isPlaying()) {
                                mediaPlayer.start();
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            audioManager.abandonAudioFocus(afChangeListener);
                            stopMusic();
                        }
                    }
                }
            };

    private static final int NOTIFICATION_ID = 596;

    private final BroadcastReceiver musicPlayer = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Song song;
            switch(intent.getAction()) {
                case Config.TOGGLE_PLAY:
                    togglePlay();
                    break;
                case Config.PLAY_SINGLE_SONG:
                    pausedSongSeek = 0;
                    song = (Song) intent.getSerializableExtra("song");
                    currentPlaylistSongId = 0;
                    playingList.clear();
                    playingList.add(song);
                    playList.overwriteStoredList(playingList);
                    playSingle(song);
                    Intent requestSongDetails = new Intent();
                    requestSongDetails.setAction(Config.REQUEST_SONG_DETAILS);
                    sendBroadcast(requestSongDetails);
                    break;
                case Config.CANCEL_NOTIFICATION:
                    stopNotification();
                    break;
                case Config.PLAY_ALBUM:
                    pausedSongSeek = 0;
                    playingList.clear();
                    Cursor musicCursor;
                    String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
                    String whereVal[] = {intent.getLongExtra("albumId", 0) + ""};
                    String orderBy = MediaStore.Audio.Media._ID;

                    musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            null, where, whereVal, orderBy);
                    if (musicCursor != null && musicCursor.moveToFirst()) {
                        //get columns
                        int titleColumn = musicCursor.getColumnIndex
                                (android.provider.MediaStore.Audio.Media.TITLE);
                        int idColumn = musicCursor.getColumnIndex
                                (android.provider.MediaStore.Audio.Media._ID);
                        int artistColumn = musicCursor.getColumnIndex
                                (android.provider.MediaStore.Audio.Media.ARTIST);
                        int pathColumn = musicCursor.getColumnIndex
                                (MediaStore.Audio.Media.DATA);
                        int albumIdColumn = musicCursor.getColumnIndex
                                (MediaStore.Audio.Media.ALBUM_ID);
                        int albumNameColumn = musicCursor.getColumnIndex
                                (MediaStore.Audio.Media.ALBUM);
                        do {
                            playingList.add(new Song(musicCursor.getLong(idColumn),
                                    musicCursor.getString(titleColumn),
                                    musicCursor.getString(artistColumn),
                                    musicCursor.getString(pathColumn), false,
                                    musicCursor.getLong(albumIdColumn),
                                    musicCursor.getString(albumNameColumn)));
                        }
                        while (musicCursor.moveToNext());
                    }
                    if (musicCursor != null) {
                        musicCursor.close();
                    }
                    playMusic(playingList.get(0));
                    requestSongDetails = new Intent();
                    requestSongDetails.setAction(Config.REQUEST_SONG_DETAILS);
                    sendBroadcast(requestSongDetails);
                    break;
                case Config.NEXT:
                    next();
                    break;
                case Config.PREV:
                    prev();
                    break;
                case Config.REQUEST_SONG_DETAILS:
                    updateCurrentPlaying();
                    updatePlaylist();
                    break;
                case Config.SEEK_TO_SONG:
                    try {
                        mediaPlayer.seekTo(intent.getIntExtra("changeSeek", 0));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        pausedSongSeek = intent.getIntExtra("changeSeek", 0);
                        playMusic(pausedSong);
                        pausedSongSeek = 0;
                    }
                    break;
                case Config.SHUFFLE_PLAYLIST:
                    shuffle();
                    break;
                case Config.PLAY_NEXT:
                    int insertPos = playingList.indexOf(currentSong) + 1;
                    song = (Song) intent.getSerializableExtra("song");
                    playingList.add(insertPos, song);
                    playList.overwriteStoredList(playingList);
                    updatePlaylist();
                    break;
                case Config.ADD_SONG_TO_PLAYLIST:
                    song = (Song) intent.getSerializableExtra("song");
                    if (playList.getPlaybackTableSize() != 0 && currentPlaylistSongId != -1) {
                        playingList.add(song);
                        playList.overwriteStoredList(playingList);
                        updatePlaylist();
                    } else {
                        intent.setAction(Config.PLAY_SINGLE_SONG);
                        sendBroadcast(intent);
                    }
                    break;
                case Config.PLAY_FROM_PLAYLIST:
                    pausedSongSeek = 0;
                    song = (Song) intent.getSerializableExtra("song");
                    playMusic(song);
                    updateCurrentPlaying();
                    requestSongDetails = new Intent();
                    requestSongDetails.setAction(Config.REQUEST_SONG_DETAILS);
                    sendBroadcast(requestSongDetails);
                    break;
                case Config.PLAY_PLAYLIST:
                    MySQLiteHelper helper = new MySQLiteHelper(context);
                    playingList = helper.getPlayListSongs(intent.getIntExtra("playlistId", -1));
                    playList.overwriteStoredList(playingList);
                    playMusic(playingList.get(0));
                    requestSongDetails = new Intent();
                    requestSongDetails.setAction(Config.REQUEST_SONG_DETAILS);
                    sendBroadcast(requestSongDetails);
                    break;
                case Config.PLAY_ALL_SONGS:
                    if(songList != null) {
                        playList.overwriteStoredList(songList);
                        playingList = songList;
                        pausedSongSeek = 0;
                        playMusic(playingList.get(0));
                        requestSongDetails = new Intent();
                        requestSongDetails.setAction(Config.REQUEST_SONG_DETAILS);
                        sendBroadcast(requestSongDetails);
                    } else {
                        Toast.makeText(context, getString(R.string.service_generate_list_warning), Toast.LENGTH_LONG).show();
                    }
                    break;
                case Config.REPEAT:
                    repeat();
                    break;
            }
        }

    };

    public ArrayList<Song> getPlayingList() {
        return playingList;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    private void updateRepeat() {
        Intent intent = new Intent(Config.UPDATE_REPEAT);
        if(repeatAll) {
            intent.putExtra("repeat", "all");
        } else if (repeatOne) {
            intent.putExtra("repeat", "one");
        } else {
            intent.putExtra("repeat", "none");
        }
        sendBroadcast(intent);
    }

    private void updateCurrentPlaying() {
        Intent intent = new Intent(Config.GET_PLAYING_DETAIL);
        if(currentSong != null) {
            intent.putExtra("song", currentSong);
        } else if(playingList.size() > 0){
            intent.putExtra("song", playingList.get(0));
        }
        if(mediaPlayer != null) {
            intent.putExtra("songDuration", mediaPlayer.getDuration());
            intent.putExtra("songCurrTime", mediaPlayer.getCurrentPosition());
        }
        sendBroadcast(intent);
        updatePlayState();
        if(currentSong != null) updateSession("metadata");
    }

    private void updatePlayState() {
        Intent intent = new Intent(Config.GET_PLAY_STATE);
        intent.putExtra("isPlaying", mediaPlayer != null && mediaPlayer.isPlaying());
        sendBroadcast(intent);
        if(mediaPlayer != null && currentSong != null) updateNotification();
        updateSession("state");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean togglePlay() {
        boolean isPlaying = false;
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        } else if(mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
        } else if(mediaPlayer == null && playingList.size() != 0) {
            playMusic(playingList.get(0));
        }
        updatePlayState();
        Toast.makeText(this, "Play toggled", Toast.LENGTH_LONG).show();
        return isPlaying;
    }

    public boolean shuffle() {
        if(playList.getPlaybackTableSize() > 1) {
            if(!shuffle) {
                preShuffle = playingList;
                Collections.shuffle(playingList);
                updatePlaylist();
                shuffle = true;
            } else {
                if(preShuffle != null) {
                    playingList = preShuffle;
                    playList.overwriteStoredList(playingList);
                    updatePlaylist();
                }
                shuffle = false;
            }
        }
        return shuffle;
    }

    public void repeat() {
        if(!repeatAll && !repeatOne) {
            repeatAll = true;
            repeatOne = false;
        } else if(repeatAll) {
            repeatAll = false;
            repeatOne = true;
        } else {
            repeatAll = false;
            repeatOne = false;
        }
        updateRepeat();
    }

    public void next() {
        playMusic(playingList.get(playingList.indexOf(currentSong) + 1));
        updateCurrentPlaying();
    }

    public void prev() {
        int currentPos = playingList.indexOf(currentSong);
        if(currentPos != 0) {
            playMusic(playingList.get(currentPos - 1));
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    private void updatePlaylist() {
        Intent playlistIntent = new Intent(Config.GET_PLAYING_LIST);
        sendBroadcast(playlistIntent);
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        updateCurrentPlaying();
        updateSession("state");
    }

    private void playSingle(Song song) {
        playingList.clear();
        playingList.add(song);
        playMusic(song);
    }

    private void playMusic(final Song song) {

        int result = audioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            currentSong = song;
            try {
                stopMusic();
                mediaPlayer = new MediaPlayer();
                notificationManager = NotificationManagerCompat.from(this);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(song.getPath());
                mediaPlayer.prepare();
                currentPlaylistSongId =  (int) song.getId();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if(!repeatOne && !repeatAll) {
                            if(playingList.size() == 1) {
                                currentPlaylistSongId = -1;
                                pausedSong = song;
                                pausedSongSeek = 0;
                                stopMusic();
                            } else {
                                pausedSongSeek = 0;
                                Song song = playingList.get(playingList.indexOf(currentSong) + 1);
                                playMusic(song);
                                updateCurrentPlaying();
                            }
                        } else if(repeatOne) {
                            playMusic(song);
                        } else {
                            if(playingList.size() == 1) {
                                playMusic(song);
                                updateCurrentPlaying();
                            } else if(playingList.size() != 1) {
                                pausedSongSeek = 0;
                                Song song = playingList.get(playingList.indexOf(currentSong) + 1);
                                playMusic(song);
                                updateCurrentPlaying();
                            } else if(playingList.size() == playingList.indexOf(song)) {
                                playMusic(playingList.get(0));
                            }
                        }
                        updateSession("metadata");
                    }
                });
                mediaPlayer.start();
                mediaPlayer.seekTo(pausedSongSeek);
                startForeground(NOTIFICATION_ID, createNotification());
                updateSession("state");
                updateSession("metadata");
            } catch (IOException e) {
                Toast.makeText(MusicService.this, getString(R.string.file_invalid), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MusicService.this, getString(R.string.unable_gain_focus), Toast.LENGTH_SHORT).show();
        }

        Intent intent = new Intent(Config.GET_PLAY_STATE);
        intent.putExtra("isPlaying", mediaPlayer != null && mediaPlayer.isPlaying());
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        playList = new MusicPlayerDBHelper(this);
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(Config.TOGGLE_PLAY);
        commandFilter.addAction(Config.CANCEL_NOTIFICATION);
        commandFilter.addAction(Config.PLAY_SINGLE_SONG);
        commandFilter.addAction(Config.PLAY_ALL_SONGS);
        commandFilter.addAction(Config.ADD_SONG_TO_PLAYLIST);
        commandFilter.addAction(Config.REQUEST_SONG_DETAILS);
        commandFilter.addAction(Config.SEEK_TO_SONG);
        commandFilter.addAction(Config.SEEK_GET_SONG);
        commandFilter.addAction(Config.NEXT);
        commandFilter.addAction(Config.MENU_FROM_PLAYLIST);
        commandFilter.addAction(Config.PLAY_NEXT);
        commandFilter.addAction(Config.PLAY_FROM_PLAYLIST);
        commandFilter.addAction(Config.PLAY_PLAYLIST);
        commandFilter.addAction(Config.PREV);
        commandFilter.addAction(Config.PLAY_ALBUM);
        commandFilter.addAction(Config.SHUFFLE_PLAYLIST);
        commandFilter.addAction(Config.REPEAT);
        registerReceiver(musicPlayer, commandFilter);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaSession = new MediaSessionCompat(this, "MusicService");
        updateSession("state");
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                Intent intent = new Intent(Config.TOGGLE_PLAY);
                sendBroadcast(intent);
                super.onPlay();
            }
            @Override
            public void onPause() {
                Intent intent = new Intent(Config.TOGGLE_PLAY);
                sendBroadcast(intent);
                super.onPause();
            }
            @Override
            public void onStop() {
                stopNotification();
                super.onStop();
            }
            @Override
            public void onFastForward() {
                Intent intent = new Intent(Config.NEXT);
                sendBroadcast(intent);
                super.onFastForward();
            }
            @Override
            public void onRewind() {
                Intent intent = new Intent(Config.PREV);
                sendBroadcast(intent);
                super.onRewind();
            }
            @Override
            public void onSkipToNext() {
                Intent intent = new Intent(Config.NEXT);
                sendBroadcast(intent);
                super.onSkipToNext();
            }
            @Override
            public void onSkipToPrevious() {
                Intent intent = new Intent(Config.PREV);
                sendBroadcast(intent);
                super.onSkipToPrevious();
            }
        });
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);

        ArrayList<Song> databaseList = playList.getStoredList();
        if(databaseList.size() != 0) {
            playingList = databaseList;
            currentSong = playingList.get(0);
        } else {
            playingList = new ArrayList<>();
        }

        new Action<ArrayList<Song>>() {

            @NonNull
            @Override
            public String id() {
                return "get_all_songs";
            }

            @Nullable
            @Override
            protected ArrayList<Song> run() throws InterruptedException {
                songList = new ArrayList<>();
                Cursor musicCursor2;
                final String where2 = MediaStore.Audio.Media.IS_MUSIC + "=1";
                final String orderBy2 = MediaStore.Audio.Media.TITLE;
                musicCursor2 = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, where2, null, orderBy2);
                if (musicCursor2 != null && musicCursor2.moveToFirst()) {
                    int titleColumn = musicCursor2.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.TITLE);
                    int idColumn = musicCursor2.getColumnIndex
                            (android.provider.MediaStore.Audio.Media._ID);
                    int artistColumn = musicCursor2.getColumnIndex
                            (android.provider.MediaStore.Audio.Media.ARTIST);
                    int pathColumn = musicCursor2.getColumnIndex
                            (MediaStore.Audio.Media.DATA);
                    int albumIdColumn = musicCursor2.getColumnIndex
                            (MediaStore.Audio.Media.ALBUM_ID);
                    int albumColumn = musicCursor2.getColumnIndex
                            (MediaStore.Audio.Media.ALBUM);
                    do {
                        songList.add(new Song(musicCursor2.getLong(idColumn),
                                musicCursor2.getString(titleColumn),
                                musicCursor2.getString(artistColumn),
                                musicCursor2.getString(pathColumn), false,
                                musicCursor2.getLong(albumIdColumn),
                                musicCursor2.getString(albumColumn)));
                    }
                    while (musicCursor2.moveToNext());
                }
                if (musicCursor2 != null) {
                    musicCursor2.close();
                }
                return songList;
            }
        }.execute();
        return START_STICKY;
    }

    private void updateSession(String changed) {

        boolean showAlbumArtOnLockscreen = prefs.getBoolean("lock_screen_art", true);

        int playState = mediaPlayer != null && mediaPlayer.isPlaying()
                ? PlaybackStateCompat.STATE_PLAYING
                : PlaybackStateCompat.STATE_PAUSED;

        long playBackStateActions = PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        if(changed.equals("metadata")) {
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.getAlbumName())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getName())
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, showAlbumArtOnLockscreen
                            ? getAlbumArt() : null)
                    .build());
        } else if(changed.equals("state")) {
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setActions(playBackStateActions)
                    .setState(playState, mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0, 1.0f)
                    .build());
        }
    }

    private Bitmap getAlbumArt() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap albumArt;
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(currentSong.getAlbumId())},
                null);
        String songArt = "";
        if (cursor != null && cursor.moveToFirst()) {
            songArt = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }
        try {
            albumArt = BitmapFactory.decodeFile(songArt, options);
        } catch (IllegalArgumentException e) {
            albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_art, options);
        }
        if(cursor != null) cursor.close();
        return albumArt;
    }

    private Notification createNotification() {
        PendingIntent playIntent = PendingIntent.getBroadcast(this, 100,
                new Intent(Config.TOGGLE_PLAY).setPackage(getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent prevIntent = PendingIntent.getBroadcast(this, 100,
                new Intent(Config.PREV).setPackage(getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent nextIntent = PendingIntent.getBroadcast(this, 100,
                new Intent(Config.NEXT).setPackage(getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(this, 100,
                new Intent(Config.CANCEL_NOTIFICATION).setPackage(getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent launchAppIntent = PendingIntent.getActivity(this, 100,
                new Intent(getApplicationContext(), MainActivity.class).setPackage(getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(1))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentIntent(launchAppIntent)
                .setSmallIcon(R.drawable.ic_audiotrack)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(currentSong.getName())
                .setContentText(currentSong.getDesc())
                .setLargeIcon(getAlbumArt());
        NotificationCompat.Action prev = new NotificationCompat.Action(R.drawable.ic_skip_previous, getString(R.string.previous), prevIntent);
        NotificationCompat.Action playPause;
        notificationBuilder.addAction(prev);
        if(mediaPlayer.isPlaying()) {
            playPause = new NotificationCompat.Action(R.drawable.ic_pause, getString(R.string.play), playIntent);
        } else {
            playPause = new NotificationCompat.Action(R.drawable.ic_play, getString(R.string.play), playIntent);
        }
        notificationBuilder.addAction(playPause);
        if(playingList.size() > 1) {
            NotificationCompat.Action next = new NotificationCompat.Action(R.drawable.ic_skip_next, getString(R.string.next), nextIntent);
            notificationBuilder.addAction(next);
        }
        NotificationCompat.Action cancel = new NotificationCompat.Action(R.drawable.ic_remove, getString(R.string.cancel), cancelIntent);
        notificationBuilder.addAction(cancel);
        return notificationBuilder.build();
    }

    private void updateNotification() {
        notificationManager.notify(NOTIFICATION_ID, createNotification());
    }

    private void stopNotification() {
        stopMusic();
        notificationManager.cancelAll();
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioManager.abandonAudioFocus(afChangeListener);
        if(mediaSession != null) {
            mediaSession.release();
        }
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
        notificationManager.cancelAll();
        stopForeground(true);
    }

    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
