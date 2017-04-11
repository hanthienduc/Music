package com.dominionos.music.ui.activity;

import android.os.Bundle;

import com.dominionos.music.R;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;

public class AboutActivity extends ATHToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
