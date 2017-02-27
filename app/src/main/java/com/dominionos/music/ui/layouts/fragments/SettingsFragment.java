package com.dominionos.music.ui.layouts.fragments;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.dominionos.music.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);
        Preference darkMode = findPreference("dark_theme");
        if(darkMode.getSharedPreferences().getBoolean("dark_theme", false)) {
            darkMode.setSummary("Dark theme enabled");
        } else {
            darkMode.setSummary("Dark theme disabled");
        }
        findPreference("colour_navbar").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(getContext(), "Restart app to see changes", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        darkMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.equals(true)) {
                    preference.setSummary("Dark theme enabled");
                } else {
                    preference.setSummary("Dark theme disabled");
                }
                Toast.makeText(getContext(), "Restart app to see changes", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }
}
