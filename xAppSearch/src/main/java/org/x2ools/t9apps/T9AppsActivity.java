package org.x2ools.t9apps;

import android.Manifest;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import org.x2ools.t9apps.base.BaseActivity;
import org.x2ools.t9apps.base.PermissionListener;
import org.x2ools.xappsearchlib.T9Search;

public class T9AppsActivity extends BaseActivity {

    private static final String TAG = "T9AppsActivity";
    private Window mWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWindow = getWindow();
        translateSystemUI();
        setContentView(R.layout.t9_apps_view);
        checkPermission(new PermissionListener() {
            @Override
            public void onResult(String permission, boolean enabled) {
                if (Manifest.permission.READ_CONTACTS.equals(permission)) {
                    T9Search.getInstance().setContactEnable(enabled);
                } else if (Manifest.permission.CALL_PHONE.equals(permission)) {
                    T9Search.getInstance().setCallPhoneEnable(enabled);
                }
                T9Search.getInstance().init(T9AppsActivity.this);
            }
        }, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE);
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
