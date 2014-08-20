package org.x2ools.t9apps;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.x2ools.xappsearchlib.T9AppsView;

public class T9AppsActivity extends Activity {

    private static final String TAG = "T9AppsActivity";
    private Window mWindow;
    private T9AppsView t9AppsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWindow = getWindow();
        translateSystemUI();
        setContentView(R.layout.t9_apps_view);

        t9AppsView = (T9AppsView) findViewById(R.id.t9AppsView);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void translateSystemUI() {
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    @Override
    protected void onDestroy() {
        t9AppsView.onDestroy();
        super.onDestroy();
    }

}
