package org.x2ools.t9apps;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import org.x2ools.t9apps.base.BaseActivity;
import org.x2ools.t9apps.base.PermissionListener;
import org.x2ools.t9apps.settings.Settings;
import org.x2ools.xappsearchlib.T9AppsView;
import org.x2ools.xappsearchlib.T9Search;

public class T9AppsActivity extends BaseActivity {

    private static final String TAG = "T9AppsActivity";
    private Window mWindow;

    private PermissionListener listener = (permission, enabled) -> {
        if (Manifest.permission.READ_CONTACTS.equals(permission)) {
            T9Search.getInstance().setContactEnable(enabled);
        } else if (Manifest.permission.CALL_PHONE.equals(permission)) {
            T9Search.getInstance().setCallPhoneEnable(enabled);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWindow = getWindow();
        translateSystemUI();
        Settings settings = new Settings(this);
        boolean enableContact = settings.isEnableContact();
        boolean enableCall = settings.isEnableCall();
        boolean qwerty = settings.getKeyboardType() == KeyboardType.QWERTY;

        T9Search.getInstance().init(T9AppsActivity.this, enableContact, enableCall);

        T9AppsView view = new T9AppsView(this);
        view.setFitsSystemWindows(true);
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.translucent));
        view.setQwerty(qwerty, false);
        setContentView(view);

        if (settings.isEnableContact()) {
            checkPermission(listener, Manifest.permission.READ_CONTACTS);
        }
        if (settings.isEnableCall()) {
            checkPermission(listener, Manifest.permission.CALL_PHONE);
        }
    }

    private void translateSystemUI() {
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        T9Search.getInstance().destroy();
    }
}
