package com.mnml.music.utils.shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.mnml.music.service.MusicService;
import com.mnml.music.utils.Config;

public class ShortcutLauncherActivity extends Activity {


    @Override
    public void onCreate(final Bundle savedInstanceState) {
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
            default:
                Toast.makeText(this, "Unknown shortcut", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void startService(final String action) {
        final Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);

        startService(intent);
    }
}