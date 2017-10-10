package org.x2ools.t9apps.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.x2ools.t9apps.KeyboardType;

/**
 * @author zhoubinjia
 * @date 2017/9/30
 */
public class Settings {

    public static final String KEY_CONTACT = "contact";
    public static final String KEY_KEYBOARD = "keyboard";
    public static final String KEY_CALL = "call";

    private SharedPreferences preferences;

    public Settings(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isEnableContact() {
        return preferences.getBoolean(KEY_CONTACT, false);
    }

    public KeyboardType getKeyboardType() {
        return KeyboardType.values()[preferences.getInt(KEY_KEYBOARD, 1)];
    }

    public void setEnableContact(boolean enable) {
        preferences.edit().putBoolean(KEY_CONTACT, enable).apply();
    }

    public void setKeyboardType(KeyboardType type) {
        preferences.edit().putInt(KEY_KEYBOARD, type.ordinal()).apply();
    }

    public boolean isEnableCall() {
        return preferences.getBoolean(KEY_CALL, false);
    }

    public void setEnableCall(boolean enable) {
        preferences.edit().putBoolean(KEY_CALL, enable).apply();
    }
}
