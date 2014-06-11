
package org.x2ools.t9apps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class ConfigFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private SwitchPreference floatPreference;
    private SwitchPreference notiPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preference);

        floatPreference = (SwitchPreference) findPreference("float_window");
        notiPreference = (SwitchPreference) findPreference("notification");

        updateService();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateService();
    }

    @Override
    public void onResume() {
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
        super.onPause();
    }

    private void updateService() {
        Intent viewService = new Intent(getActivity(), ViewManagerService.class);
        viewService.putExtra(ViewManagerService.CONFIG_CHANGED, true);
        if (floatPreference.isChecked()) {
            viewService.putExtra(ViewManagerService.SHOW_FLOAT_WINDOW, true);
        } else {
            viewService.putExtra(ViewManagerService.SHOW_FLOAT_WINDOW, false);
        }

        if (notiPreference.isChecked()) {
            viewService.putExtra(ViewManagerService.SHOW_NOTIFICATION, true);
        } else {
            viewService.putExtra(ViewManagerService.SHOW_NOTIFICATION, false);
        }
        getActivity().startService(viewService);
    }

}
