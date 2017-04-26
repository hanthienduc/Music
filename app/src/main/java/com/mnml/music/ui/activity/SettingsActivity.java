package com.mnml.music.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mnml.music.R;
import com.mnml.music.ui.fragments.SettingsFragment;
import com.mnml.music.utils.Utils;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;

public class SettingsActivity extends ATHToolbarActivity
        implements ColorChooserDialog.ColorCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ATH.setActivityToolbarColorAuto(this, getATHToolbar());
        ATH.setStatusbarColor(this, Utils.getAutoStatColor(ThemeStore.primaryColor(this)));
        setTheme(ThemeStore.activityTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_holder, new SettingsFragment());
        transaction.commit();

        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, int i) {
        if (!colorChooserDialog.isAccentMode()) {
            ThemeStore.editTheme(this).primaryColor(i).commit();
            recreate();
        } else {
            ThemeStore.editTheme(this).accentColor(i).commit();
            recreate();
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog colorChooserDialog) {
    }
}
