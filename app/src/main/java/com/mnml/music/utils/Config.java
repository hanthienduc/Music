package com.mnml.music.utils;

import android.graphics.Color;

public class Config {
    public static final int DONATE_REQUEST_CODE = 5963;
    public static final int SETTINGS_REQUEST_CODE = 1160;

    // In-App SKUs
    public static final String DONATE_2 = "donate2";
    public static final String DONATE_5 = "donate5";
    public static final String DONATE_10 = "donate10";

    // Button state alpha values
    public static final float BUTTON_ACTIVE_ALPHA = 1.0f;
    public static final float BUTTON_INACTIVE_ALPHA = 0.5f;

    public static final int ALBUM_CARD_WIDTH = 180;

    // Intent values
    public static final String STOP = "stop";
    public static final String PAUSE = "pause";
    public static final String PLAY = "play";
    public static final String PREV = "prev";
    public static final String NEXT = "next";
    public static final String TOGGLE_PLAY = "toggle_play";
    public static final String CANCEL_NOTIFICATION = "cancel_notification";
    public static final String PLAY_ALBUM = "play_album";
    public static final String MENU_FROM_PLAYLIST = "menu_from_playlist";
    public static final String PLAY_FROM_PLAYLIST = "play_from_playlist";
    public static final String PLAY_PLAYLIST = "play_playlist";
    public static final String PLAY_NEXT = "play_next";
    public static final String PLAY_SINGLE_SONG = "play_single_song";
    public static final String ADD_SONG_TO_PLAYLIST = "add_song_to_playlist";
    public static final String REQUEST_SONG_DETAILS = "request_song_details";
    public static final String SEEK_TO_SONG = "seek_to_song";
    public static final String SEEK_GET_SONG = "seek_get_song";
    public static final String SHUFFLE_ALL = "shuffle_all";
    public static final String PLAY_ALL = "play_all";

    // Shortcuts
    public static final String KEY_SHORTCUT_TYPE = "ShortcutType";
    public static final int SHORTCUT_TYPE_NONE = 0;
    public static final int SHORTCUT_TYPE_SHUFFLE_ALL = 1;
    public static final int SHORTCUT_TYPE_PLAY_ALL = 2;

    static final int ANIMATION_DURATION = 500;
    public static final int NOTIFICATION_ID = 596;

    public final static int[] ACCENT_COLORS = new int[]{
            Color.parseColor("#FF1744"),
            Color.parseColor("#F50057"),
            Color.parseColor("#D500F9"),
            Color.parseColor("#651FFF"),
            Color.parseColor("#3D5AFE"),
            Color.parseColor("#2979FF"),
            Color.parseColor("#00B0FF"),
            Color.parseColor("#00E5FF"),
            Color.parseColor("#1DE9B6"),
            Color.parseColor("#00E676"),
            Color.parseColor("#76FF03"),
            Color.parseColor("#C6FF00"),
            Color.parseColor("#FFEA00"),
            Color.parseColor("#FFC400"),
            Color.parseColor("#FF9100"),
            Color.parseColor("#FF3D00"),
            Color.parseColor("#000000")
    };

    // Custom colors, including the entire material palette
    public final static int[][] ACCENT_COLORS_SUB = new int[][]{
            new int[]{
                    Color.parseColor("#FF8A80"),
                    Color.parseColor("#FF5252"),
                    Color.parseColor("#FF1744"),
                    Color.parseColor("#D50000")
            },
            new int[]{
                    Color.parseColor("#FF80AB"),
                    Color.parseColor("#FF4081"),
                    Color.parseColor("#F50057"),
                    Color.parseColor("#C51162")
            },
            new int[]{
                    Color.parseColor("#EA80FC"),
                    Color.parseColor("#E040FB"),
                    Color.parseColor("#D500F9"),
                    Color.parseColor("#AA00FF")
            },
            new int[]{
                    Color.parseColor("#B388FF"),
                    Color.parseColor("#7C4DFF"),
                    Color.parseColor("#651FFF"),
                    Color.parseColor("#6200EA")
            },
            new int[]{
                    Color.parseColor("#8C9EFF"),
                    Color.parseColor("#536DFE"),
                    Color.parseColor("#3D5AFE"),
                    Color.parseColor("#304FFE")
            },
            new int[]{
                    Color.parseColor("#82B1FF"),
                    Color.parseColor("#448AFF"),
                    Color.parseColor("#2979FF"),
                    Color.parseColor("#2962FF")
            },
            new int[]{
                    Color.parseColor("#80D8FF"),
                    Color.parseColor("#40C4FF"),
                    Color.parseColor("#00B0FF"),
                    Color.parseColor("#0091EA")
            },
            new int[]{
                    Color.parseColor("#84FFFF"),
                    Color.parseColor("#18FFFF"),
                    Color.parseColor("#00E5FF"),
                    Color.parseColor("#00B8D4")
            },
            new int[]{
                    Color.parseColor("#A7FFEB"),
                    Color.parseColor("#64FFDA"),
                    Color.parseColor("#1DE9B6"),
                    Color.parseColor("#00BFA5")
            },
            new int[]{
                    Color.parseColor("#B9F6CA"),
                    Color.parseColor("#69F0AE"),
                    Color.parseColor("#00E676"),
                    Color.parseColor("#00C853")
            },
            new int[]{
                    Color.parseColor("#CCFF90"),
                    Color.parseColor("#B2FF59"),
                    Color.parseColor("#76FF03"),
                    Color.parseColor("#64DD17")
            },
            new int[]{
                    Color.parseColor("#F4FF81"),
                    Color.parseColor("#EEFF41"),
                    Color.parseColor("#C6FF00"),
                    Color.parseColor("#AEEA00")
            },
            new int[]{
                    Color.parseColor("#FFFF8D"),
                    Color.parseColor("#FFFF00"),
                    Color.parseColor("#FFEA00"),
                    Color.parseColor("#FFD600")
            },
            new int[]{
                    Color.parseColor("#FFE57F"),
                    Color.parseColor("#FFD740"),
                    Color.parseColor("#FFC400"),
                    Color.parseColor("#FFAB00")
            },
            new int[]{
                    Color.parseColor("#FFD180"),
                    Color.parseColor("#FFAB40"),
                    Color.parseColor("#FF9100"),
                    Color.parseColor("#FF6D00")
            },
            new int[]{
                    Color.parseColor("#FF9E80"),
                    Color.parseColor("#FF6E40"),
                    Color.parseColor("#FF3D00"),
                    Color.parseColor("#DD2C00")
            },
            new int[]{
                    Color.parseColor("#000000"),
                    Color.parseColor("#FFFFFF")
            }
    };
}
