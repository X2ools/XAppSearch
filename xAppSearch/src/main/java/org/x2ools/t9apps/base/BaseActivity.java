package org.x2ools.t9apps.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhoubinjia on 16/6/8.
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    private final int PERMISSION_REQUEST_CODE = new Random().nextInt(65535);
    private PermissionListener mListener;
    private String TAG = getClass().getSimpleName();

    public void checkPermission(PermissionListener listener, String... permissions) {
        mListener = listener;
        List<String> needCheckPermissions = new ArrayList<>();
        for (String permission : permissions) {
            boolean granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
            boolean shouldRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
            if (!granted) {
                needCheckPermissions.add(permission);
            } else {
                mListener.onResult(permission, true);
            }
        }
        if (needCheckPermissions.size() == 0) return;
        ActivityCompat.requestPermissions(this, needCheckPermissions.toArray(new String[needCheckPermissions.size()]), PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (mListener != null) {
                for (int i = 0; i < permissions.length; i++) {
                    mListener.onResult(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public boolean hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return false;
    }
}