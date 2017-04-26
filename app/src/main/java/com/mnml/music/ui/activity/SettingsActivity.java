package com.mnml.music.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.mnml.music.R;
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


    public static class SettingsFragment extends ATEPreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.prefs_appearance);
            configureAppearanceSettings();

            addPreferencesFromResource(R.xml.prefs_playback);
            configurePlaybackSettings();
        }

        private void configurePlaybackSettings() {
            final SeekBarPreference playbackSpeed = (SeekBarPreference) findPreference("playback_speed");
            playbackSpeed.setSummary((playbackSpeed.getValue() / 10.0f) + "x");
            playbackSpeed.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        float value = ((int) newValue) / 10.0f;
                        preference.getSharedPreferences().edit().putFloat("playback_speed_float", value).apply();
                        preference.setSummary(value + "x");
                        return true;
                    });
        }

        private void configureAppearanceSettings() {
            Preference substratumTheme = findPreference("substratum_theme");

            final String darkModeEnabled = getString(R.string.dark_mode_enabled);
            final String darkModeDisabled = getString(R.string.dark_mode_disabled);
            Preference darkMode = findPreference("dark_theme");
            darkMode.setSummary(
                    darkMode.getSharedPreferences().getBoolean("dark_theme", false)
                            ? darkModeEnabled
                            : darkModeDisabled);
            darkMode.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        preference.setSummary(newValue.equals(true) ? darkModeEnabled : darkModeDisabled);
                        ThemeStore.editTheme(getActivity())
                                .activityTheme(((Boolean) newValue) ? R.style.AppTheme_Dark : R.style.AppTheme_Light)
                                .commit();
                        getActivity().recreate();
                        return true;
                    });

            boolean subsTheme =
                    substratumTheme.getSharedPreferences().getBoolean("substratum_theme", false);
            ATEColorPreference colorPrimaryPref = (ATEColorPreference) findPreference("primary_color");
            colorPrimaryPref.setEnabled(!subsTheme);
            colorPrimaryPref.setColor(
                    ThemeStore.primaryColor(getActivity()), ThemeStore.primaryColor(getActivity()));
            colorPrimaryPref.setOnPreferenceClickListener(
                    preference -> {
                        new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                                .preselect(ThemeStore.primaryColor(getActivity()))
                                .accentMode(false)
                                .allowUserColorInput(true)
                                .allowUserColorInputAlpha(false)
                                .show();
                        return true;
                    });

            ATEColorPreference colorAccentPref = (ATEColorPreference) findPreference("accent_color");
            colorAccentPref.setEnabled(!subsTheme);
            colorAccentPref.setColor(
                    ThemeStore.accentColor(getActivity()), ThemeStore.accentColor(getActivity()));
            colorAccentPref.setOnPreferenceClickListener(
                    preference -> {
                        new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
                                .preselect(ThemeStore.accentColor(getActivity()))
                                .accentMode(true)
                                .allowUserColorInput(true)
                                .allowUserColorInputAlpha(false)
                                .show();
                        return true;
                    });

            boolean isSubstratumInstalled = Utils.isSubsInstalled(getContext());
            substratumTheme.setEnabled(isSubstratumInstalled);
            substratumTheme.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        colorPrimaryPref.setEnabled(!(boolean) newValue);
                        colorAccentPref.setEnabled(!(boolean) newValue);
                        if ((boolean) newValue) {
                            ThemeStore.editTheme(getContext())
                                    .primaryColorRes(R.color.colorPrimary)
                                    .accentColorRes(R.color.colorAccent)
                                    .commit();
                            getActivity().recreate();
                        }
                        return true;
                    });
        }
    }

}
