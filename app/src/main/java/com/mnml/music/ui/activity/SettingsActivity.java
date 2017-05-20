package com.mnml.music.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.AestheticActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mnml.music.R;
import com.mnml.music.utils.Config;
import com.mnml.music.utils.Utils;

public class SettingsActivity extends AestheticActivity
        implements ColorChooserDialog.ColorCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_holder, new SettingsFragment());
        transaction.commit();

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
            Aesthetic.get()
                    .colorPrimary(i)
                    .colorStatusBarAuto()
                    .apply();
            recreate();
        } else {
            Aesthetic.get()
                    .colorAccent(i)
                    .apply();
            recreate();
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog colorChooserDialog) {
    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
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
            final SwitchPreferenceCompat googleServices = (SwitchPreferenceCompat) findPreference(KEY_GOOGLE_SERVICES);
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

            final Preference colorPrimaryPref = findPreference(KEY_COLOR_PRIMARY);
            colorPrimaryPref.setOnPreferenceClickListener(this);

            final Preference colorAccentPref = findPreference(KEY_COLOR_ACCENT);
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
                    final boolean darkMode = (Boolean) newValue;
                    preference.setSummary(darkMode ? getString(R.string.dark_mode_enabled) : getString(R.string.dark_mode_disabled));
                    Aesthetic.get()
                            .activityTheme(darkMode ? R.style.AppTheme_Dark : R.style.AppTheme_Light)
                            .textColorPrimaryRes(darkMode ? R.color.primaryTextDark : R.color.primaryTextLight)
                            .textColorPrimaryInverseRes(darkMode ? R.color.primaryTextLight : R.color.primaryTextDark)
                            .textColorSecondaryRes(darkMode ? R.color.secondaryTextDark : R.color.secondaryTextLight)
                            .textColorSecondaryInverseRes(darkMode ? R.color.secondaryTextLight : R.color.secondaryTextDark)
                            .colorWindowBackgroundRes(darkMode ? R.color.darkWindowBackground : R.color.lightWindowBackground)
                            .isDark(darkMode)
                            .apply();
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
                            .accentMode(false)
                            .allowUserColorInput(true)
                            .dynamicButtonColor(false)
                            .allowUserColorInputAlpha(false)
                            .show();
                    return true;
                case KEY_COLOR_ACCENT:
                    new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
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
                            .onPositive((materialDialog, dialogAction) -> Aesthetic.get()
                                    .isDark(false)
                                    .activityTheme(R.style.AppTheme_Light)
                                    .colorPrimaryRes(R.color.colorPrimary)
                                    .colorStatusBarAuto()
                                    .colorAccentRes(R.color.colorAccent)
                                    .apply())
                            .show();
                    return true;
            }
            return false;
        }
    }

}
