package com.dominionos.music.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SeekBarPreference;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.dominionos.music.R;
import com.dominionos.music.ui.activity.SettingsActivity;
import com.dominionos.music.utils.Utils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_appearance);
        configureAppearanceSettings();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addPreferencesFromResource(R.xml.prefs_playback);
            configurePlaybackSettings();
        }
    }

    private void configurePlaybackSettings() {
        final SeekBarPreference playbackSpeed = (SeekBarPreference) findPreference("playback_speed");
        playbackSpeed.setSummary((playbackSpeed.getValue() / 10.0f) + "x");
        playbackSpeed.setOnPreferenceChangeListener((preference, newValue) -> {
            float value = ((int) newValue) / 10.0f;
            preference.getSharedPreferences().edit().putFloat("playback_speed_float", value).apply();
            preference.setSummary(value + "x");
            return true;
        });
    }
    private void configureAppearanceSettings() {
        final String darkModeEnabled = getString(R.string.dark_mode_enabled);
        final String darkModeDisabled = getString(R.string.dark_mode_disabled);
        Preference darkMode = findPreference("dark_theme");
        darkMode.setSummary(darkMode.getSharedPreferences().getBoolean("dark_theme", false)
                ? darkModeEnabled : darkModeDisabled);
        darkMode.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(newValue.equals(true)
                    ? darkModeEnabled : darkModeDisabled);
            ThemeStore.editTheme(getActivity())
                    .activityTheme(((Boolean) newValue) ? R.style.AppTheme_Dark : R.style.AppTheme_Light)
                    .commit();
            getActivity().recreate();
            return true;
        });

        boolean isSubstratumInstalled = Utils.isSubsInstalled(getContext());
        findPreference("substratum_theme").setEnabled(isSubstratumInstalled);

        ATEColorPreference colorPrimaryPref = (ATEColorPreference) findPreference("primary_color");
        colorPrimaryPref.setColor(ThemeStore.primaryColor(getActivity()), ContextCompat.getColor(getContext(), R.color.colorPrimary));
        colorPrimaryPref.setOnPreferenceClickListener(preference -> {
            new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                    .preselect(ThemeStore.primaryColor(getActivity()))
                    .show();
            return true;
        });
    }
}
