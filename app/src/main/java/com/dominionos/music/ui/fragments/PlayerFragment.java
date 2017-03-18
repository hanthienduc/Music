package com.dominionos.music.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dominionos.music.R;
import com.dominionos.music.adapters.PlayingSongAdapter;
import com.dominionos.music.items.Song;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.ui.activity.MainActivity;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.MusicPlayerDBHelper;
import com.dominionos.music.utils.PlayPauseDrawable;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PlayerFragment extends Fragment {

    @BindView(R.id.play) FloatingActionButton play;
    @BindView(R.id.playing_bar_action) ImageButton playingAction;
    @BindView(R.id.playing_bar) View playingBar;
    @BindView(R.id.player_view) View playerView;
    @BindView(R.id.playing_list) RecyclerView playingListView;
    @BindView(R.id.next) ImageButton next;
    @BindView(R.id.prev) ImageButton prev;
    @BindView(R.id.shuffle) ImageButton shuffle;
    @BindView(R.id.repeat) ImageButton repeat;

    private Unbinder unbinder;
    private PlayPauseDrawable playPauseDrawable;
    private PlayPauseDrawable miniPlayPauseDrawable;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MusicService service;
    private MainActivity activity;
    private Context context;

    private boolean darkMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        darkMode = sharedPrefs.getBoolean("dark_theme", false);
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        unbinder = ButterKnife.bind(this, view);
        activity = (MainActivity) getActivity();
        context = getContext();
        service = activity.getService();
        setStyle();
        setControls();
        setPlayingList();
        slidingUpPanelLayout = activity.getSlidingPanel();
        slidingUpPanelLayout.setScrollableView(playingListView);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                playingBar.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    playingBar.setVisibility(View.GONE);
                } else if(newState != SlidingUpPanelLayout.PanelState.EXPANDED) {
                    playingBar.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
    }

    private void setPlayingList() {
        MusicPlayerDBHelper helper = new MusicPlayerDBHelper(context);
        ArrayList<Song> playingList = helper.getCurrentPlayingList();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        playingListView.setLayoutManager(layoutManager);
        if(playingList.size() > 0) {
            playingListView.setAdapter(new PlayingSongAdapter(context, playingList, darkMode, playingList.get(0), Glide.with(getContext())));
        }
    }

    private void setControls() {
        playPauseDrawable = new PlayPauseDrawable(activity);
        miniPlayPauseDrawable = new PlayPauseDrawable(activity);
        playingAction.setImageDrawable(miniPlayPauseDrawable);
        playingAction.setOnClickListener(playPauseClick());
        play.setImageDrawable(playPauseDrawable);
        play.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
        play.setOnClickListener(playPauseClick());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) service.next();
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) service.prev();
            }
        });
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) {
                    boolean shuffleState = service.shuffle();
                    shuffle.setAlpha(shuffleState
                            ? Config.BUTTON_ACTIVE
                            : Config.BUTTON_INACTIVE);
                }
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) {
                    service.repeat();
                }
            }
        });
    }

    private void setStyle() {
        playerView.setBackgroundColor(darkMode
                ? ContextCompat.getColor(context, R.color.darkContentColour)
                : ContextCompat.getColor(context, R.color.contentColour));
        playingAction.setColorFilter(darkMode
                ? ContextCompat.getColor(context, R.color.primaryTextDark)
                : ContextCompat.getColor(context, R.color.primaryTextLight));
        playingBar.setBackgroundColor(darkMode
                ? ContextCompat.getColor(context, R.color.darkContentColour)
                : ContextCompat.getColor(context, R.color.windowBackground));
    }

    private View.OnClickListener playPauseClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) {
                    boolean isPlaying = service.togglePlay();
                    Toast.makeText(service, "Play toggled", Toast.LENGTH_SHORT).show();
                    setPlayingState(isPlaying);
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
    }

}
