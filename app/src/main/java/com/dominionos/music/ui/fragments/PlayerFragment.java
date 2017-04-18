package com.dominionos.music.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.async.Action;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.dominionos.music.R;
import com.dominionos.music.adapters.PlayingSongAdapter;
import com.dominionos.music.items.Song;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.ui.activity.MainActivity;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.PaletteBitmap;
import com.dominionos.music.utils.PaletteBitmapTranscoder;
import com.dominionos.music.utils.PlayPauseDrawable;
import com.dominionos.music.utils.Utils;
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PlayerFragment extends Fragment {

    @BindView(R.id.play) FloatingActionButton play;
    @BindView(R.id.playing_bar_action) ImageButton playingAction;
    @BindView(R.id.player_view) View playerView;
    @BindView(R.id.playing_list) RecyclerView playingListView;
    @BindView(R.id.next) ImageButton next;
    @BindView(R.id.prev) ImageButton prev;
    @BindView(R.id.shuffle) ImageButton shuffle;
    @BindView(R.id.repeat) ImageButton repeat;
    @BindView(R.id.art) ImageView playingArt;
    @BindView(R.id.control_holder) View controlHolder;
    @BindView(R.id.player_seekbar) SeekBar playerSeekBar;

    @BindView(R.id.playing_bar) View playingBar;
    @BindView(R.id.playing_song_name) TextView currentSongName;
    @BindView(R.id.playing_song_desc) TextView currentSongDesc;
    @BindView(R.id.playing_art) ImageView playingSongArt;

    private Unbinder unbinder;
    private PlayPauseDrawable playPauseDrawable;
    private PlayPauseDrawable miniPlayPauseDrawable;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MusicService service;
    private MainActivity activity;
    private Context context;
    private Song currentPlaying;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;
    private PlayingSongAdapter adapter;
    private MediaPlayer mediaPlayer;

    private boolean darkMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        darkMode = sharedPrefs.getBoolean("dark_theme", false);
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        unbinder = ButterKnife.bind(this, view);
        activity = (MainActivity) getActivity();


        setControls();
        setStyle();

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        DraggableItemAnimator animator = new DraggableItemAnimator();
        animator.setSupportsChangeAnimations(false);
        playingListView.setLayoutManager(layoutManager);
        playingListView.setItemAnimator(animator);

        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
        recyclerViewDragDropManager.setInitiateOnLongPress(true);
        recyclerViewDragDropManager.setInitiateOnMove(false);

        slidingUpPanelLayout = activity.getSlidingPanel();
        slidingUpPanelLayout.setScrollableView(playingListView);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(playingBar != null) playingBar.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    if(playingBar != null) playingBar.setVisibility(View.GONE);
                } else {
                    if(playingBar != null) playingBar.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
    }

    public void updatePlayer() {
        if(service == null) service = activity.getService();
        currentPlaying = service.getCurrentSong();
        if(currentPlaying != null) {
            updatePlayingList();
            updatePlayState();
            setShuffleState(service.getShuffleState());
            setRepeatState(service.getRepeatState());
            updateSeekBar();
            if(slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN)
                slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    public void updatePlayState() {
        setPlayingState(service.isPlaying());
    }

    public void updatePlayingList() {
        if(currentPlaying != null) {
            ArrayList<Song> playingList = service.getPlayingList();
            if(playingList.size() > 0) {
                if(adapter == null) {
                    adapter = new PlayingSongAdapter(context, playingList, darkMode, currentPlaying, Glide.with(context), activity);
                    RecyclerView.Adapter wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(adapter);
                    playingListView.setAdapter(wrappedAdapter);
                    recyclerViewDragDropManager.attachRecyclerView(playingListView);
                    playingListView.scrollToPosition(playingList.indexOf(currentPlaying));
                } else {
                    adapter.updateData(playingList, currentPlaying);
                    if(playingListView != null) playingListView.scrollToPosition(playingList.indexOf(currentPlaying));
                }
            }
            if(currentSongName != null) currentSongName.setText(currentPlaying.getName());
            if(currentSongDesc != null) currentSongDesc.setText(currentPlaying.getDesc());
            setArt();
        }
    }

    private void setControls() {
        playPauseDrawable = new PlayPauseDrawable(activity);
        miniPlayPauseDrawable = new PlayPauseDrawable(activity);
        playingAction.setImageDrawable(miniPlayPauseDrawable);
        playingAction.setOnClickListener(playPauseClick(true));
        play.setImageDrawable(playPauseDrawable);
        play.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
        play.setOnClickListener(playPauseClick(false));
        next.setOnClickListener(v -> {
            if(service == null) service = activity.getService();
            if(service != null) service.next();
        });
        prev.setOnClickListener(v -> {
            if(service == null) service = activity.getService();
            if(service != null) service.prev();
        });
        shuffle.setOnClickListener(v -> {
            if(service == null) service = activity.getService();
            if(service != null) {
                setShuffleState(service.shuffle());
                updatePlayingList();
            }
        });
        repeat.setOnClickListener(v -> {
            if(service != null) {
                setRepeatState(service.repeat());
            }
        });
        playerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer == null && service != null) mediaPlayer = service.getMediaPlayer();
                if(fromUser && mediaPlayer != null) mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setShuffleState(boolean shuffleState) {
        if(shuffle != null) {
            shuffle.setAlpha(shuffleState
                    ? Config.BUTTON_ACTIVE_ALPHA
                    : Config.BUTTON_INACTIVE_ALPHA);
        }
    }

    private void setRepeatState(int repeatState) {
        if(repeat != null) {
            switch (repeatState) {
                case 0:
                    repeat.setImageResource(R.drawable.ic_repeat_all);
                    repeat.setAlpha(Config.BUTTON_INACTIVE_ALPHA);
                    break;
                case 1:
                    repeat.setImageResource(R.drawable.ic_repeat_one);
                    repeat.setAlpha(Config.BUTTON_ACTIVE_ALPHA);
                    break;
                case 2:
                    repeat.setImageResource(R.drawable.ic_repeat_all);
                    repeat.setAlpha(Config.BUTTON_ACTIVE_ALPHA);
                    break;
            }
        }
    }

    private void updateSeekBar() {
        mediaPlayer = service.getMediaPlayer();
        if(mediaPlayer != null && service.isPlaying()) {
            playerSeekBar.setMax(mediaPlayer.getDuration());
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(() -> {
                    if(mediaPlayer == null) mediaPlayer = service.getMediaPlayer();
                    if(mediaPlayer != null
                            && playerSeekBar != null
                            && service.isPlaying()) {
                        int seekProgress = mediaPlayer.getCurrentPosition();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            playerSeekBar.setProgress(seekProgress, true);
                        } else {
                            playerSeekBar.setProgress(seekProgress);
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    private void setArt() {
        new Action<String>() {

            @NonNull
            @Override
            public String id() {
                return "get-playing-art";
            }

            @Nullable
            @Override
            protected String run() throws InterruptedException {
                if(service != null && service.getCurrentSong() != null) {
                    long albumId = service.getCurrentSong().getAlbumId();
                    return Utils.getAlbumArt(context, albumId);
                }
                return null;
            }

            @Override
            protected void done(String result) {
                try {
                    Glide.with(context)
                            .load(result)
                            .asBitmap()
                            .transcode(new PaletteBitmapTranscoder(activity), PaletteBitmap.class)
                            .centerCrop()
                            .error(R.drawable.default_art)
                            .into(new ImageViewTarget<PaletteBitmap>(playingArt) {
                                @Override
                                protected void setResource(PaletteBitmap resource) {
                                    super.view.setImageBitmap(resource.bitmap);
                                    Palette palette = resource.palette;
                                    Palette.Swatch swatch;
                                    if(palette.getVibrantSwatch() != null) {
                                        swatch = palette.getVibrantSwatch();
                                        controlHolder.setBackgroundColor(swatch.getRgb());
                                        play.setColorFilter(swatch.getRgb());
                                    } else if(palette.getDominantSwatch() != null) {
                                        swatch = palette.getDominantSwatch();
                                        controlHolder.setBackgroundColor(swatch.getRgb());
                                        play.setColorFilter(swatch.getRgb());
                                    }
                                }
                            });
                    Glide.with(context)
                            .load(result)
                            .error(R.drawable.default_art)
                            .into(playingSongArt);
                    playingSongArt.setContentDescription(currentPlaying.getAlbumName());
                } catch(IllegalArgumentException e) {
                    e.printStackTrace();
                    if(playingSongArt != null) playingSongArt.setImageResource(R.drawable.default_art);
                    if(playingArt != null) playingArt.setImageResource(R.drawable.default_art);
                }
            }
        }.execute();
    }

    private void setStyle() {
        Utils.setWindowColor(playerView, context, darkMode);
        Utils.setContentColor(playingBar, context, darkMode);
        Utils.setPrimaryTextColor(currentSongName, context, darkMode);
        Utils.setSecondaryTextColor(currentSongDesc, context, darkMode);
        playingAction.setColorFilter(darkMode
                ? ContextCompat.getColor(context, R.color.primaryTextDark)
                : ContextCompat.getColor(context, R.color.primaryTextLight));
    }

    private View.OnClickListener playPauseClick(boolean fromPlayingBar) {
        return v -> {
            if(service == null) service = activity.getService();
            if(service != null) {
                if(service.togglePlay()) {
                    playPauseDrawable.setPause(!fromPlayingBar);
                    miniPlayPauseDrawable.setPause(fromPlayingBar);
                } else {
                    playPauseDrawable.setPlay(!fromPlayingBar);
                    miniPlayPauseDrawable.setPlay(fromPlayingBar);
                }
            }
        };
    }

    private void setPlayingState(boolean isPlaying) {
        if(isPlaying) {
            playPauseDrawable.setPause(true);
            miniPlayPauseDrawable.setPause(true);
        } else {
            playPauseDrawable.setPlay(true);
            miniPlayPauseDrawable.setPlay(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        recyclerViewDragDropManager.release();
    }

}