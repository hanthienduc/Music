package com.mnml.music.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATESwitchPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.mnml.music.R;
import com.mnml.music.utils.Config;
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


    public static class SettingsFragment extends ATEPreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private static final String KEY_DARK_MODE = "dark_theme";
        private static final String KEY_RESET_THEME = "reset_theme";
        private static final String KEY_COLOR_PRIMARY = "primary_color";
        private static final String KEY_COLOR_ACCENT = "accent_color";
        private static final String KEY_PLAYBACK_SPEED = "playback_speed";
        private static final String KEY_GOOGLE_SERVICES = "google_services";

        private Context context;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            context = getContext();
            addPreferencesFromResource(R.xml.prefs_appearance);
            configureAppearanceSettings();

            addPreferencesFromResource(R.xml.prefs_general);
            configureGeneralSettings();

            addPreferencesFromResource(R.xml.prefs_playback);
            configurePlaybackSettings();
        }

        private void configureGeneralSettings() {
            final ATESwitchPreference googleServices = (ATESwitchPreference) findPreference(KEY_GOOGLE_SERVICES);
            final boolean isGoogleServicesAvailable = Utils.isGooglePlayServicesAvailable(context);
            if(!isGoogleServicesAvailable) {
                googleServices.getSharedPreferences().edit().putBoolean(KEY_GOOGLE_SERVICES, false).apply();
            }
            googleServices.setEnabled(isGoogleServicesAvailable);
        }

        private void configurePlaybackSettings() {
            final SeekBarPreference playbackSpeed = (SeekBarPreference) findPreference(KEY_PLAYBACK_SPEED);
            playbackSpeed.setOnPreferenceChangeListener(this);
            playbackSpeed.callChangeListener(playbackSpeed.getValue());
        }

        private void configureAppearanceSettings() {
            final Preference darkMode = findPreference(KEY_DARK_MODE);
            darkMode.setOnPreferenceChangeListener(this);
            darkMode.callChangeListener(darkMode.getSharedPreferences().getBoolean(KEY_DARK_MODE, false));

            final ATEColorPreference colorPrimaryPref = (ATEColorPreference) findPreference(KEY_COLOR_PRIMARY);
            colorPrimaryPref.setColor(ThemeStore.primaryColor(getActivity()), ThemeStore.primaryColor(getActivity()));
            colorPrimaryPref.setOnPreferenceClickListener(this);

            final ATEColorPreference colorAccentPref = (ATEColorPreference) findPreference(KEY_COLOR_ACCENT);
            colorAccentPref.setColor(ThemeStore.accentColor(getActivity()), ThemeStore.accentColor(getActivity()));
            colorAccentPref.setOnPreferenceClickListener(this);

            final Preference resetTheme = findPreference(KEY_RESET_THEME);
            boolean isSubstratumInstalled = Utils.isSubsInstalled(context);
            resetTheme.setTitle(isSubstratumInstalled ? getString(R.string.substratum_theme) : getString(R.string.reset_theme));
            resetTheme.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch(preference.getKey()) {
                case KEY_DARK_MODE:
                    preference.setSummary(newValue.equals(true) ? getString(R.string.dark_mode_enabled) : getString(R.string.dark_mode_disabled));
                    if(!newValue.equals(preference.getSharedPreferences().getBoolean(KEY_DARK_MODE, false))) {
                        ThemeStore.editTheme(getActivity())
                                .activityTheme(((Boolean) newValue) ? R.style.AppTheme_Dark : R.style.AppTheme_Light)
                                .commit();
                        getActivity().recreate();
                    }
                    return true;
                case KEY_PLAYBACK_SPEED:
                    float value = ((int) newValue) / 10.0f;
                    preference.getSharedPreferences().edit().putFloat("playback_speed_float", value).apply();
                    preference.setSummary(value + "x");
                    return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch(preference.getKey()) {
                case KEY_COLOR_PRIMARY:
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                            .preselect(ThemeStore.primaryColor(getActivity()))
                            .accentMode(false)
                            .allowUserColorInput(true)
                            .dynamicButtonColor(false)
                            .allowUserColorInputAlpha(false)
                            .show();
                    return true;
                case KEY_COLOR_ACCENT:
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
                            .preselect(ThemeStore.accentColor(getActivity()))
                            .accentMode(true)
                            .allowUserColorInput(true)
                            .customColors(Config.ACCENT_COLORS, Config.ACCENT_COLORS_SUB)
                            .dynamicButtonColor(false)
                            .allowUserColorInputAlpha(false)
                            .show();
                    return true;
                case KEY_RESET_THEME:
                    new MaterialDialog.Builder(context)
                            .title(preference.getTitle())
                            .content(getString(R.string.reset_theme_confirmation))
                            .positiveText(getString(R.string.yes))
                            .negativeText(getString(R.string.cancel))
                            .onPositive((materialDialog, dialogAction) -> {
                                ThemeStore.editTheme(context)
                                        .primaryColor(context.getColor(R.color.colorPrimary))
                                        .accentColor(context.getColor(R.color.colorAccent))
                                        .commit();
                                getActivity().recreate();
                            })
                            .show();
                    return true;
            }
            return false;
        }
    }

}
