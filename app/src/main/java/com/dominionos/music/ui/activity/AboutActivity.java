package com.dominionos.music.ui.activity;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.dominionos.music.R;
import com.dominionos.music.utils.Utils;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AboutActivity extends ATHToolbarActivity {

    @BindView(R.id.about_toolbar) Toolbar toolbar;
    @BindView(R.id.app_version) TextView appVersion;
    @BindView(R.id.libs_used_title) TextView libsUsedTitle;

    private Unbinder unbinder;
    private int primaryColor, accentColor;
    private boolean darkMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        darkMode = sharedPrefs.getBoolean("dark_mode", false);
        primaryColor = ThemeStore.primaryColor(this);
        accentColor = ThemeStore.accentColor(this);
        ATH.setStatusbarColor(this, Utils.getAutoStatColor(primaryColor));
        setContentView(R.layout.activity_about);
        unbinder = ButterKnife.bind(this);
        ATH.setActivityToolbarColorAuto(this, toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setAppInfo();
        setLibs();
    }

    public void setAppInfo() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            String appNameWithVersion = getString(R.string.app_name) + " " + version;
            appVersion.setText(appNameWithVersion);
            appVersion.setTextColor(darkMode
                    ? getColor(R.color.primaryTextDark)
                    : getColor(R.color.primaryTextLight));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setLibs() {
        libsUsedTitle.setTextColor(accentColor);
        LibsFragment fragment = new LibsBuilder()
                .withFields(R.string.class.getFields())
                .withActivityStyle(darkMode
                        ? Libs.ActivityStyle.DARK
                        : Libs.ActivityStyle.LIGHT)
                .withActivityTheme(ThemeStore.activityTheme(AboutActivity.this))
                .withSortEnabled(true)
                .withShowLoadingProgress(true)
                .fragment();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_holder, fragment);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
