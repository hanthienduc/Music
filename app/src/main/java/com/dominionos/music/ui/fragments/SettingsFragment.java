package com.dominionos.music.ui.fragments;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;

import static android.content.Context.ACTIVITY_SERVICE;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_appearance);
        configureAppearanceSettings();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addPreferencesFromResource(R.xml.prefs_playback);
            configurePlaybackSettings();
        }

        addPreferencesFromResource(R.xml.pref_general);
        configureAdvancedSettings();
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

    private void configureAdvancedSettings() {
        Preference reset = findPreference("reset_app");
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getContext())
                        .title(R.string.reset_app)
                        .content(R.string.reset_app_summary)
                        .positiveText("Yes")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ((ActivityManager)getActivity().getSystemService(ACTIVITY_SERVICE))
                                        .clearApplicationUserData();
                            }
                        })
                        .negativeText("Cancel")
                        .show();
                return true;
            }
        });
    }
}
