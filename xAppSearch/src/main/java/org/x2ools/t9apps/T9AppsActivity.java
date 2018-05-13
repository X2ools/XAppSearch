package org.x2ools.t9apps;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.x2ools.t9apps.base.BaseActivity;
import org.x2ools.t9apps.base.PermissionListener;
import org.x2ools.t9apps.settings.Settings;
import org.x2ools.xappsearchlib.T9AppsView;
import org.x2ools.xappsearchlib.T9Search;

public class T9AppsActivity extends BaseActivity {

    private static final String TAG = "T9AppsActivity";

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Settings settings = new Settings(this);
        boolean enableContact = settings.isEnableContact();
        boolean enableCall = settings.isEnableCall();
        boolean qwerty = settings.getKeyboardType() == KeyboardType.QWERTY;

        T9Search.getInstance().init(T9AppsActivity.this, enableContact, enableCall);

        T9AppsView view = new T9AppsView(this);
        setContentView(view);

        if (settings.isEnableContact()) {
            checkPermission(listener, Manifest.permission.READ_CONTACTS);
        }
        if (settings.isEnableCall()) {
            checkPermission(listener, Manifest.permission.CALL_PHONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        T9Search.getInstance().destroy();
    }
}
