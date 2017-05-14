package com.mnml.music.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.ComponentName;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import com.afollestad.async.Action;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mnml.music.R;
import com.mnml.music.models.Song;
import com.mnml.music.ui.activity.MainActivity;
import com.mnml.music.utils.Config;
import com.mnml.music.utils.MusicPlayerDBHelper;
import com.mnml.music.utils.PlaylistHelper;
import com.mnml.music.utils.NotificationHandler;
import com.mnml.music.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MusicService extends Service {

    private final IBinder binder = new MyBinder();
    private MediaPlayer mediaPlayer;
    private boolean repeatOne = false, repeatAll = false, shuffle = false;
    private float playbackSpeed = 1.0f;
    private MusicPlayerDBHelper playerDBHelper;
    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private ArrayList<Song> preShuffle, playingList;
    private NotificationHandler notificationHandler;
    private Song currentSong;
    private MainActivity activity;
    private SharedPreferences sharedPrefs;
    private PowerManager.WakeLock wakeLock;
    private boolean isPlaying = false, pausedByFocus = false;
    private final AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener() {
                public void onAudioFocusChange(int focusChange) {
                    if (mediaPlayer != null) {
                        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                            if (isPlaying) {
                                mediaPlayer.pause();
                                pausedByFocus = true;
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                            if (isPlaying) playerDuck(true);
                        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                            if (!isPlaying && pausedByFocus) {
                                mediaPlayer.start();
                                pausedByFocus = false;
                            } else if (isPlaying && !pausedByFocus) {
                                playerDuck(false);
                            }
                        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                            audioManager.abandonAudioFocus(afChangeListener);
                            stopMusic();
                        }
                    }
                }
            };
    private final BroadcastReceiver musicPlayer =
            new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    Song song;
                    switch (intent.getAction()) {
                        case Config.TOGGLE_PLAY:
                            togglePlay();
                            break;
                        case Config.PLAY_SINGLE_SONG:
                            song = (Song) intent.getSerializableExtra("song");
                            playingList.clear();
                            playingList.add(song);
                            playerDBHelper.overwriteStoredList(playingList);
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
                            playerDBHelper.overwriteStoredList(playingList);
                            break;
                        case Config.ADD_SONG_TO_PLAYLIST:
                            song = (Song) intent.getSerializableExtra("song");
                            playingList.add(song);
                            playerDBHelper.overwriteStoredList(playingList);
                            break;
                        case Config.PLAY_FROM_PLAYLIST:
                            song = (Song) intent.getSerializableExtra("song");
                            playMusic(song);
                            updateCurrentPlaying();
                            break;
                        case Config.PLAY_PLAYLIST:
                            PlaylistHelper helper = new PlaylistHelper(context);
                            playingList = helper.getPlayListSongs(intent.getIntExtra("playlistId", -1));
                            playerDBHelper.overwriteStoredList(playingList);
                            playMusic(playingList.get(0));
                            requestSongDetails = new Intent();
                            requestSongDetails.setAction(Config.REQUEST_SONG_DETAILS);
                            sendBroadcast(requestSongDetails);
                            break;
                        case Config.STOP:
                            stopMusic();
                            break;
                        case Config.PAUSE:
                            pause();
                            break;
                        case Config.PLAY:
                            play();
                            break;
                    }
                }
            };

    private void playerDuck(boolean duck) {
        // Reduce the volume by half when ducking - otherwise play at full volume.
        mediaPlayer.setVolume(duck ? 0.5f : 1.0f, duck ? 0.5f : 1.0f);
    }

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
        if (!repeatAll && !repeatOne) {
            return 0;
        } else if (repeatAll && !repeatOne) {
            return 2;
        } else if (!repeatAll) {
            return 1;
        }
        return 0;
    }

    private void updateCurrentPlaying() {
        updatePlayState();
        if (activity != null) activity.updatePlayingList();
        if (currentSong != null) updateSessionMetadata();
    }

    private void updatePlayState() {
        if (activity != null) activity.updatePlayerPlayState();
        if (mediaPlayer != null && currentSong != null) updateNotification();
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
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        } else if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            final float playbackSpeed = sharedPrefs.getFloat("playback_speed_float", 1.0f);
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
            mediaPlayer.start();
            isPlaying = true;
        } else if (mediaPlayer == null && playingList.size() != 0) {
            playMusic(playingList.get(0));
            isPlaying = true;
        }
        updatePlayState();
        return isPlaying;
    }

    private void pause() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    private void play() {
        if(mediaPlayer != null && !isPlaying && currentSong != null) {
            playbackSpeed = sharedPrefs.getFloat("playback_speed_float", 1.0f);
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
            mediaPlayer.start();
            isPlaying = true;
        } else if (currentSong == null && !playingList.isEmpty()) {
            currentSong = playingList.get(0);
            playMusic(currentSong);
        } else {
            Toast.makeText(this, "Nothing to play", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean shuffle() {
        if (playingList.size() > 1) {
            if (!shuffle) {
                preShuffle = playingList;
                Collections.shuffle(playingList);
                shuffle = true;
            } else {
                if (preShuffle != null) {
                    playingList = preShuffle;
                    playerDBHelper.overwriteStoredList(playingList);
                }
                shuffle = false;
            }
        }
        return shuffle;
    }

    public int repeat() {
        int repeatState;
        if (!repeatAll && !repeatOne) {
            repeatAll = true;
            repeatOne = false;
            repeatState = 2;
        } else if (repeatAll) {
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
        if (playingList.size() > 0) {
            playMusic(playingList.get(playingList.indexOf(currentSong) + 1));
            updateCurrentPlaying();
        }
    }

    public void prev() {
        if (mediaPlayer.getCurrentPosition() < 5000) {
            mediaPlayer.seekTo(0);
        } else {
            int currentPos = playingList.indexOf(currentSong);
            final Song song = playingList.get(currentPos - 1);
            if (isPlaying) stopMusic();
            if (song != null) playMusic(song);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    private void stopMusic() {
        if (mediaPlayer != null) {
            if (isPlaying) mediaPlayer.stop();
            updatePlayState();
            updateSessionState();
            mediaPlayer.release();
        }
        playerDBHelper.overwriteStoredList(playingList);
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
        int result = audioManager.requestAudioFocus(
                afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            currentSong = song;
            try {
                stopMusic();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(song.getPath());
                playbackSpeed = sharedPrefs.getFloat("playback_speed_float", 1.0f);
                mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(playbackSpeed));
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    mp.stop();
                    return false;
                });
                mediaPlayer.setOnPreparedListener(mp -> {
                    isPlaying = true;
                    mediaPlayer.start();
                    updateSessionState();
                    updateSessionMetadata();
                    startForeground(Config.NOTIFICATION_ID, notificationHandler.startNotification(currentSong));
                    if(activity != null) activity.updatePlayer();
                });
                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    updateSessionMetadata();
                    updateSessionState();
                    if (!repeatOne && !repeatAll) {
                        try {
                            Song nextSong = playingList.get(playingList.indexOf(currentSong) + 1);
                            if (nextSong != null) {
                                playMusic(nextSong);
                            } else {
                                stopMusic();
                            }
                        } catch (IndexOutOfBoundsException ignored) {
                            stopMusic();
                        }
                        updateCurrentPlaying();
                    } else if (repeatOne) {
                        playMusic(song);
                    } else {
                        try {
                            Song nextSong = playingList.get(playingList.indexOf(currentSong) + 1);
                            if (nextSong != null) {
                                playMusic(nextSong);
                            } else {
                                playMusic(playingList.get(0));
                            }
                        } catch (IndexOutOfBoundsException ignored) {
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
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        notificationHandler = new NotificationHandler();
        notificationHandler.init(this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        playerDBHelper = new MusicPlayerDBHelper(this);
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(Config.TOGGLE_PLAY);
        commandFilter.addAction(Config.STOP);
        commandFilter.addAction(Config.PAUSE);
        commandFilter.addAction(Config.PLAY);
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

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        ComponentName mediaButtonReceiverComponentName = new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mediaButtonReceiverComponentName);

        PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);
        mediaSession = new MediaSessionCompat(this, "MusicService", mediaButtonReceiverComponentName, mediaButtonReceiverPendingIntent);
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
            public void onSkipToNext() {
                next();
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                prev();
                super.onSkipToPrevious();
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return MediaButtonIntentReceiver.handleIntent(MusicService.this, mediaButtonEvent);
            }
        });
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
        mediaSession.setActive(true);

        playingList = playerDBHelper.getStoredList();
        if (!playingList.isEmpty()) {
            currentSong = playingList.get(0);
        }
        updateSessionState();

        mediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if(intent != null &&  intent.getAction() != null) {
            switch(intent.getAction()) {
                case Config.PLAY:
                    play();
                    break;
                case Config.PAUSE:
                    pause();
                    break;
                case Config.TOGGLE_PLAY:
                    togglePlay();
                    break;
                case Config.NEXT:
                    next();
                    break;
                case Config.PREV:
                    prev();
                    break;
                case Config.SHUFFLE_ALL:
                    playingList = Utils.getAllSongs(this);
                    Collections.shuffle(playingList);
                    shuffle = true;
                    if(!playingList.isEmpty()) playMusic(playingList.get(0));
                    break;
                case Config.PLAY_ALL:
                    playingList = Utils.getAllSongs(this);
                    if(!playingList.isEmpty()) playMusic(playingList.get(0));
            }
        }
        return START_STICKY;
    }

    private void updateSessionState() {
        int playState =
                isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        long playBackStateActions =
                PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;

        playbackSpeed = sharedPrefs.getFloat("playback_speed_float", 1.0f);
        int currentPosition;
        try {
            currentPosition = mediaPlayer.getCurrentPosition();
        } catch (NullPointerException | IllegalStateException ignored) {
            currentPosition = 0;
        }
        mediaSession.setPlaybackState(
                new PlaybackStateCompat.Builder()
                        .setActions(playBackStateActions)
                        .setState(
                                playState,
                                currentPosition,
                                playbackSpeed)
                        .build());
    }

    private void updateSessionMetadata() {

        boolean showAlbumArtOnLockScreen = sharedPrefs.getBoolean("lock_screen_art", true);

        mediaSession.setMetadata(
                new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, currentSong.getAlbumName())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getName())
                        .putBitmap(
                                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                                showAlbumArtOnLockScreen ? getAlbumArt() : null)
                        .build());
    }

    public void playAllSongs() {
        MaterialDialog progressDialog =
                new MaterialDialog.Builder(activity)
                        .title("Please wait")
                        .progress(true, 0)
                        .show();
        new Action<ArrayList<Song>>() {

            @NonNull
            @Override
            public String id() {
                return "play_all";
            }

            @Override
            protected ArrayList<Song> run() throws InterruptedException {
                return Utils.getAllSongs(MusicService.this);
            }

            @Override
            protected void done(@Nullable ArrayList<Song> result) {
                super.done(result);
                if (result != null && !result.isEmpty()) {
                    playingList = result;
                    playMusic(playingList.get(0));
                    updateCurrentPlaying();
                    updatePlayState();
                    activity.updatePlayingList();
                    activity.updatePlayer();
                }
                progressDialog.dismiss();
            }
        }.execute();
    }

    private Bitmap getAlbumArt() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap albumArt;
        Cursor cursor =
                getContentResolver()
                        .query(
                                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
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
        if (cursor != null) cursor.close();
        return albumArt;
    }

    private void updateNotification() {
        notificationHandler.changeNotification(currentSong);
    }

    private void stopNotification() {
        stopMusic();
        notificationHandler.destroyNotification();
        stopForeground(true);
        if (activity != null) activity.updatePlayer();
    }

    public void playAlbum(long albumId) {
        ArrayList<Song> albumSongs = Utils.getAlbumSongs(this, albumId);
        if (!albumSongs.isEmpty()) {
            stopMusic();
            playingList.clear();
            playingList = albumSongs;
            currentSong = playingList.get(0);
            playMusic(currentSong);
            updateCurrentPlaying();
        }
    }

    public void shuffleAlbum(long albumId) {
        ArrayList<Song> albumSongs = Utils.getAlbumSongs(this, albumId);
        if (!albumSongs.isEmpty()) {
            Collections.shuffle(albumSongs);
            stopMusic();
            playingList.clear();
            playingList = albumSongs;
            currentSong = playingList.get(0);
            playMusic(currentSong);
            updateCurrentPlaying();
            shuffle = true;
        }
    }
    public MediaSessionCompat.Token getSessionToken() {
        if(mediaSession != null) {
            return mediaSession.getSessionToken();
        } else {
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playerDBHelper.overwriteStoredList(playingList);
        audioManager.abandonAudioFocus(afChangeListener);
        if (mediaSession != null) {
            mediaSession.setActive(false);
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        stopForeground(true);
        notificationHandler.destroyNotification();
        wakeLock.release();
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
