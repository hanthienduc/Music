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
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

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
    private MusicPlayerDBHelper playList;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private ArrayList<Song> preShuffle, playingList;
    private NotificationManagerCompat notificationManager;
    private Song currentSong;
    private SharedPreferences prefs;
    private final IBinder binder = new MyBinder();
    private MainActivity activity;
    private SharedPreferences sharedPrefs;
    private boolean isPlaying = false, pausedByFocus = false;

    private final AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if(mediaPlayer != null) {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            if (isPlaying) {
                                mediaPlayer.pause();
                                pausedByFocus = true;
                            }
                        } else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            if(isPlaying) playerDuck(true);
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            if(!isPlaying && pausedByFocus) {
                                mediaPlayer.start();
                                pausedByFocus = false;
                            } else if(isPlaying && !pausedByFocus) {
                                playerDuck(false);
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            audioManager.abandonAudioFocus(afChangeListener);
                            stopMusic();
                        }
                    }
                }
            };

    public synchronized void playerDuck(boolean duck) {
        // Reduce the volume by half when ducking - otherwise play at full volume.
        mediaPlayer.setVolume(duck ? 0.5f : 1.0f, duck ? 0.5f : 1.0f);
    }

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
                    song = (Song) intent.getSerializableExtra("song");
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
                    playAlbum(intent.getLongExtra("albumId", 0));
                    break;
                case Config.NEXT:
                    next();
                    break;
                case Config.PREV:
                    prev();
                    break;
                case Config.REQUEST_SONG_DETAILS:
                    updateCurrentPlaying();
                    break;
                case Config.SEEK_TO_SONG:
                    try {
                        mediaPlayer.seekTo(intent.getIntExtra("changeSeek", 0));
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        playMusic(currentSong);
                    }
                    break;
                case Config.PLAY_NEXT:
                    int insertPos = playingList.indexOf(currentSong) + 1;
                    song = (Song) intent.getSerializableExtra("song");
                    playingList.add(insertPos, song);
                    playList.overwriteStoredList(playingList);
                    break;
                case Config.ADD_SONG_TO_PLAYLIST:
                    song = (Song) intent.getSerializableExtra("song");
                    if (playList.getPlaybackTableSize() != 0) {
                        playingList.add(song);
                        playList.overwriteStoredList(playingList);
                    } else {
                        intent.setAction(Config.PLAY_SINGLE_SONG);
                        sendBroadcast(intent);
                    }
                    break;
                case Config.PLAY_FROM_PLAYLIST:
                    song = (Song) intent.getSerializableExtra("song");
                    playMusic(song);
                    updateCurrentPlaying();
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
            }
        }

    };

    public ArrayList<Song> getPlayingList() {
        return playingList;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public boolean getShuffleState() {
        return shuffle;
    }

    public int getRepeatState() {
        if(!repeatAll && !repeatOne) {
            return 0;
        } else if(repeatAll && !repeatOne) {
            return 2;
        } else if(!repeatAll) {
            return 1;
        }
        return 0;
    }

    private void updateCurrentPlaying() {
        updatePlayState();
        if(activity != null) activity.updatePlayingList();
        if(currentSong != null) updateSessionMetadata();
    }

    public void changePlayingList(ArrayList<Song> changedList) {
        playingList = changedList;
    }

    private void updatePlayState() {
        if(activity != null) activity.updatePlayerPlayState();
        if(mediaPlayer != null && currentSong != null) updateNotification();
        updateSessionState();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public boolean togglePlay() {
        isPlaying = false;
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        } else if(mediaPlayer != null && !mediaPlayer.isPlaying()) {
            final float playbackSpeed = sharedPrefs.getFloat("playback_speed_float", 1.0f);
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
            mediaPlayer.start();
            isPlaying = true;
        } else if(mediaPlayer == null && playingList.size() != 0) {
            playMusic(playingList.get(0));
            isPlaying = true;
        }
        updatePlayState();
        return isPlaying;
    }

    public boolean shuffle() {
        if(playingList.size() > 1) {
            if(!shuffle) {
                preShuffle = playingList;
                Collections.shuffle(playingList);
                shuffle = true;
            } else {
                if(preShuffle != null) {
                    playingList = preShuffle;
                    playList.overwriteStoredList(playingList);
                }
                shuffle = false;
            }
        }
        return shuffle;
    }

    public int repeat() {
        int repeatState;
        if(!repeatAll && !repeatOne) {
            repeatAll = true;
            repeatOne = false;
            repeatState = 2;
        } else if(repeatAll) {
            repeatAll = false;
            repeatOne = true;
            repeatState = 1;
        } else {
            repeatAll = false;
            repeatOne = false;
            repeatState = 0;
        }
        return repeatState;
    }

    public void next() {
        if(playingList.size() > 0) {
            playMusic(playingList.get(playingList.indexOf(currentSong) + 1));
            updateCurrentPlaying();
        }
    }

    public void prev() {
        int currentPos = playingList.indexOf(currentSong);
        if(mediaPlayer.getCurrentPosition() < 5000) {
            mediaPlayer.seekTo(0);
        } else if(currentPos != 0 && playingList.size() > 0) {
            playMusic(playingList.get(currentPos - 1));
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            updatePlayState();
            updateSessionState();
            mediaPlayer.release();
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private void playSingle(Song song) {
        playingList.clear();
        playingList.add(song);
        playMusic(song);
    }

    private void playMusic(final Song song) {

        int result = audioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            currentSong = song;
            if(activity != null) activity.updatePlayer();
            try {
                stopMusic();
                mediaPlayer = new MediaPlayer();
                notificationManager = NotificationManagerCompat.from(this);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(song.getPath());
                final Float playbackSpeed = sharedPrefs.getFloat("playback_speed_float", 1.0f);
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start();
                    isPlaying = true;
                    activity.updatePlayer();
                    startForeground(NOTIFICATION_ID, createNotification());
                    updateSessionState();
                    updateSessionMetadata();
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    updateSessionMetadata();
                    updateSessionState();
                    if(!repeatOne && !repeatAll) {
                        if(playingList.size() == 1) {
                            stopMusic();
                        } else {
                            Song song1 = playingList.get(playingList.indexOf(currentSong) + 1);
                            if(song1 != null) {
                                playMusic(song1);
                            } else {
                                stopMusic();
                            }
                            updateCurrentPlaying();
                        }
                    } else if(repeatOne) {
                        playMusic(song);
                    } else {
                        Song song1 = playingList.get(playingList.indexOf(currentSong) + 1);
                        if(song1 != null) {
                            playMusic(song1);
                        } else {
                            playMusic(playingList.get(0));
                        }
                        updateCurrentPlaying();
                    }
                });
                mediaPlayer.prepareAsync();
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
        sharedPrefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this);
        playList = new MusicPlayerDBHelper(this);
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(Config.TOGGLE_PLAY);
        commandFilter.addAction(Config.CANCEL_NOTIFICATION);
        commandFilter.addAction(Config.PLAY_SINGLE_SONG);
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
        registerReceiver(musicPlayer, commandFilter);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaSession = new MediaSessionCompat(this, "MusicService");
        updateSessionState();
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                togglePlay();
                super.onPlay();
            }
            @Override
            public void onPause() {
                togglePlay();
                super.onPause();
            }
            @Override
            public void onStop() {
                stopNotification();
                super.onStop();
            }
            @Override
            public void onFastForward() {
                next();
                super.onFastForward();
            }
            @Override
            public void onRewind() {
                prev();
                super.onRewind();
            }
            @Override
            public void onSkipToNext() {
                next();
                super.onSkipToNext();
            }
            @Override
            public void onSkipToPrevious() {
                prev();
                super.onSkipToPrevious();
            }
        });
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);

        playingList = playList.getStoredList();
        if(!playingList.isEmpty()) {
            currentSong = playingList.get(0);
        }
        return START_STICKY;
    }

    private void updateSessionState() {
        int playState = isPlaying
                ? PlaybackStateCompat.STATE_PLAYING
                : PlaybackStateCompat.STATE_PAUSED;

        long playBackStateActions = PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_REWIND |
                PlaybackStateCompat.ACTION_FAST_FORWARD |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;

        float playbackSpeed = 1.0f;
        if(mediaPlayer != null)  playbackSpeed = mediaPlayer.getPlaybackParams().getSpeed();
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(playBackStateActions)
                .setState(playState, mediaPlayer != null
                        ? mediaPlayer.getCurrentPosition()
                        : 0, playbackSpeed)
                .build());
    }
    private void updateSessionMetadata() {

        boolean showAlbumArtOnLockscreen = prefs.getBoolean("lock_screen_art", true);

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.getAlbumName())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getName())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, showAlbumArtOnLockscreen
                        ? getAlbumArt() : null)
                .build());

    }

    public void playAllSongs() {
        final ArrayList<Song> songList = new ArrayList<>();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
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
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            do {
                songList.add(new Song(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn)));
            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        playingList = songList;
        if(!playingList.isEmpty()) playMusic(playingList.get(0));
        updateCurrentPlaying();
        updatePlayState();
        activity.updatePlayingList();
        activity.updatePlayer();
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
                .setShowWhen(false)
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
        if(isPlaying) {
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
        if(activity != null) activity.updatePlayer();
    }

    public ArrayList<Song> getAlbumSongs(long albumId) {
        ArrayList<Song> albumSongList = new ArrayList<>();
        Cursor musicCursor;
        String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String whereVal[] = {String.valueOf(albumId)};
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
                albumSongList.add(new Song(musicCursor.getLong(idColumn),
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
        return albumSongList;
    }

    public void playAlbum(long albumId) {
        ArrayList<Song> albumSongs = getAlbumSongs(albumId);
        if(!albumSongs.isEmpty()){
            stopMusic();
            playingList.clear();
            playingList = albumSongs;
            currentSong = playingList.get(0);
            playMusic(currentSong);
            updateCurrentPlaying();
        }
    }

    public void shuffleAlbum(long albumId) {
        ArrayList<Song> albumSongs = getAlbumSongs(albumId);
        if(!albumSongs.isEmpty()){
            Collections.shuffle(albumSongs);
            stopMusic();
            playingList.clear();
            playingList = albumSongs;
            currentSong = playingList.get(0);
            playMusic(currentSong);
            updateCurrentPlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playList.overwriteStoredList(playingList);
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
        public MusicService getService(MainActivity activity) {
            MusicService.this.activity = activity;
            return MusicService.this;
        }

        public MusicService getService() {
            return MusicService.this;
        }
    }
}
