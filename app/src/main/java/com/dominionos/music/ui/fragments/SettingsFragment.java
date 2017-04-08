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

        addPreferencesFromResource(R.xml.prefs_playback);
        configurePlaybackSettings();
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
        Preference substratumTheme = findPreference("substratum_theme");

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

        boolean subsTheme = substratumTheme.getSharedPreferences().getBoolean("substratum_theme", false);
        ATEColorPreference colorPrimaryPref = (ATEColorPreference) findPreference("primary_color");
        colorPrimaryPref.setEnabled(!subsTheme);
        colorPrimaryPref.setColor(ThemeStore.primaryColor(getActivity()), ContextCompat.getColor(getContext(), R.color.colorPrimary));
        colorPrimaryPref.setOnPreferenceClickListener(preference -> {
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
        colorAccentPref.setColor(ThemeStore.accentColor(getActivity()), ContextCompat.getColor(getContext(), R.color.colorAccent));
        colorAccentPref.setOnPreferenceClickListener(preference -> {
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
        substratumTheme.setOnPreferenceChangeListener((preference, newValue) -> {
            colorPrimaryPref.setEnabled(!(boolean) newValue);
            colorAccentPref.setEnabled(!(boolean) newValue);
            if((boolean) newValue) {
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
