package com.mnml.music.utils.shortcuts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import com.mnml.music.R;
import com.mnml.music.utils.Config;

import java.util.ArrayList;

public class ShortcutHandler {

    private Context context;

    public void create(Context context) {
        this.context = context;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            if(shortcutManager.getDynamicShortcuts().size() != 3) {
                Icon shuffleIcon = Icon.createWithResource(context, R.drawable.ic_shortcut_shuffle);
                ShortcutInfo shuffle = new ShortcutInfo.Builder(context, "shuffle_shortcut")
                        .setShortLabel("Shuffle all")
                        .setIcon(shuffleIcon)
                        .setIntent(shortcutIntent(Config.SHORTCUT_TYPE_SHUFFLE_ALL))
                        .build();

                Icon playIcon = Icon.createWithResource(context, R.drawable.ic_shortcut_play_arrow);
                ShortcutInfo play = new ShortcutInfo.Builder(context, "play_shortcut")
                        .setShortLabel("Play all")
                        .setIcon(playIcon)
                        .setIntent(shortcutIntent(Config.SHORTCUT_TYPE_PLAY_ALL))
                        .build();

                Icon searchIcon = Icon.createWithResource(context, R.drawable.ic_shortcut_search);
                ShortcutInfo search = new ShortcutInfo.Builder(context, "search_shortcut")
                        .setShortLabel("Search")
                        .setIcon(searchIcon)
                        .setIntent(shortcutIntent(Config.SHORTCUT_TYPE_NONE))
                        .build();

                ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();
                shortcuts.add(play);
                shortcuts.add(search);
                shortcuts.add(shuffle);
                shortcutManager.setDynamicShortcuts(shortcuts);
            }
        }
    }

    private Intent shortcutIntent(final int shortcutType) {
        Intent intent = new Intent(context, ShortcutLauncherActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        Bundle bundle = new Bundle();
        bundle.putInt(Config.KEY_SHORTCUT_TYPE, shortcutType);
        intent.putExtras(bundle);
        return intent;
    }
}
