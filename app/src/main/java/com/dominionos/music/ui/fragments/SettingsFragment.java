package com.dominionos.music.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.dominionos.music.R;

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
        Preference playbackSpeed = findPreference("playback_speed");
        playbackSpeed.setSummary(playbackSpeed.getSharedPreferences().getString("playback_speed", "1.0x"));
        playbackSpeed.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String valueString = newValue.toString().replace("x", "");
                preference.getSharedPreferences().edit().putFloat("playback_speed_float", Float.valueOf(valueString)).apply();
                preference.setSummary(newValue.toString());
                return true;
            }
        });
    }
    private void configureAppearanceSettings() {
        final String darkModeEnabled = getString(R.string.dark_mode_enabled);
        final String darkModeDisabled = getString(R.string.dark_mode_disabled);
        Preference darkMode = findPreference("dark_theme");
        darkMode.setSummary(darkMode.getSharedPreferences().getBoolean("dark_theme", false)
                ? darkModeEnabled : darkModeDisabled);
        darkMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(newValue.equals(true)
                        ? darkModeEnabled : darkModeDisabled);
                getActivity().recreate();
                return true;
            }
        });
    }
}
