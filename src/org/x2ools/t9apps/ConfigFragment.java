
package org.x2ools.t9apps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.Toast;

public class ConfigFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private SwitchPreference floatPreference;
    private SwitchPreference notiPreference;
    private Preference shortcutPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preference);

        floatPreference = (SwitchPreference) findPreference("float_window");
        notiPreference = (SwitchPreference) findPreference("notification");
        shortcutPreference = findPreference("shortcut");

        updateService();

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == shortcutPreference) {
            addShortcut();
            Toast.makeText(getActivity(), R.string.shortcut_installed, Toast.LENGTH_SHORT).show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private void addShortcut() {
        Intent shortcutIntent = new Intent(getActivity(), T9AppsActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "T9Search");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.ic_launcher));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getActivity().sendBroadcast(addIntent);
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
