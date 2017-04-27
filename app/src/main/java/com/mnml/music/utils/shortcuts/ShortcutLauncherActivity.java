package com.mnml.music.utils.shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import com.mnml.music.models.Song;
import com.mnml.music.service.MusicService;
import com.mnml.music.utils.Config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class ShortcutLauncherActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int shortcutType = Config.SHORTCUT_TYPE_NONE;

        //Set shortcutType from the intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //noinspection WrongConstant
            shortcutType = extras.getInt(Config.KEY_SHORTCUT_TYPE, Config.SHORTCUT_TYPE_NONE);
        }

        switch (shortcutType) {
            case Config.SHORTCUT_TYPE_SHUFFLE_ALL:
                startService(Config.SHUFFLE_ALL);
                break;
            case Config.SHORTCUT_TYPE_PLAY_ALL:
                startService(Config.PLAY_ALL);
                break;
        }

        finish();
    }

    private void startService(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);

        startService(intent);
    }
}