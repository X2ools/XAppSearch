package org.x2ools.xappsearchlib;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.ContactItem;
import org.x2ools.xappsearchlib.model.SearchItem;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * @author zhoubinjia
 * @date 2017/8/21
 */
public class T9ViewModel {
    private static final String TAG = T9ViewModel.class.getSimpleName();
    private String mFilterStr = null;
    private T9ViewDelegate mDelegate;
    private PackageManager mPackageManager;
    private ActivityManager mActivityManager;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    public T9ViewModel(Context context) {
        mPackageManager = context.getPackageManager();
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        mDisposables.add(T9Search.getInstance().getAllSubject()
                .subscribe(searchItems -> filter(mFilterStr)));
    }

    public void setDelegate(T9ViewDelegate delegate) {
        mDelegate = delegate;
    }

    public void setAllApplicationsData() {
        List<SearchItem> allApps = T9Search.getInstance().getAll();
        if (mDelegate != null) {
            mDelegate.showItems(allApps);
        }
    }

    public void filter(String string) {
        mFilterStr = string;
        if (TextUtils.isEmpty(string)) {
            setAllApplicationsData();
            return;
        }
        mDisposables.add(T9Search.getInstance().search(string).subscribe(searchItems -> {
            if (mDelegate != null) {
                mDelegate.showItems(searchItems);
            }
        }));
    }

    public Intent getLaunchIntent(SearchItem item) {
        Intent intent = null;
        if (item instanceof ContactItem) {
            intent = new Intent("android.intent.action.CALL", Uri.parse("tel:"
                    + ((ContactItem)item).getPhoneNumber()));
        } else if (item instanceof AppItem) {
            ComponentName name = ComponentName.unflattenFromString(((AppItem) item).getComponentName());
            if (name == null) return null;
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setPackage(name.getPackageName());
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(name);
        }
        return intent;
    }

    public void recordUsage(SearchItem item) {
        if (item instanceof AppItem) {
            T9Search.getInstance().recordUsage(((AppItem) item).getComponentName());
        }
    }

    public void destroy() {
        mDisposables.clear();
    }
}
