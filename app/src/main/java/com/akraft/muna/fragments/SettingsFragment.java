package com.akraft.muna.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;

import com.akraft.muna.R;
import com.akraft.muna.Utils;
import com.akraft.muna.activities.SigninActivity;
import com.facebook.login.LoginManager;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference logoutItem = findPreference("logout");
        logoutItem.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(Utils.AUTH_PREF, 0).edit();
                editor.clear();
                editor.apply();

                LoginManager.getInstance().logOut();
                Intent intent = new Intent(getActivity(), SigninActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("nearby_marks_notifications")) {
            CheckBoxPreference preference = (CheckBoxPreference) findPreference(key);
            if (preference.isChecked())
                getActivity().sendBroadcast(new Intent("com.akraft.muna.action.START_MARKS_DETECTOR"));
            else
                getActivity().sendBroadcast(new Intent("com.akraft.muna.action.DISABLE_MARKS_DETECTOR"));
        }
    }

}
