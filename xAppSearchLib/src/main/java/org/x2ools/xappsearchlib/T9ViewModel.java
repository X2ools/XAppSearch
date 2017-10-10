package org.x2ools.xappsearchlib;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.ContactItem;
import org.x2ools.xappsearchlib.model.SearchItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author zhoubinjia
 * @date 2017/8/21
 */
public class T9ViewModel {
    private static final String TAG = T9ViewModel.class.getSimpleName();
    private String mFilterStr = null;
    private boolean mAllMode = false;
    private T9ViewDelegate mDelegate;
    private PackageManager mPackageManager;
    private ActivityManager mActivityManager;

    public T9ViewModel(Context context) {
        mPackageManager = context.getPackageManager();
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        T9Search.getInstance().getAllSubject().subscribe(searchItems -> filter(mFilterStr));
    }

    public void setDelegate(T9ViewDelegate delegate) {
        mDelegate = delegate;
    }

    public void setApplicationsData() {
        observeRecentApps().subscribe(searchItems -> {
            if (mDelegate != null) {
                mDelegate.showItems(searchItems);
            }
        });
        mAllMode = false;
    }

    public void setAllApplicationsData() {
        List<SearchItem> allApps = T9Search.getInstance().getAll();
        if (mDelegate != null) {
            mDelegate.showItems(allApps);
        }
        mAllMode = true;
    }

    public boolean isAllMode() {
        return mAllMode;
    }

    public void filter(String string) {
        mFilterStr = string;
        if (TextUtils.isEmpty(string)) {
            if (isAllMode()) {
                setAllApplicationsData();
            } else {
                setApplicationsData();
            }
            return;
        }
        if ("*".equals(string)) {
            setAllApplicationsData();
            return;
        }
        T9Search.getInstance().search(string).subscribe(searchItems -> {
            if (mDelegate != null) {
                mDelegate.showItems(searchItems);
            }
        });
        mAllMode = false;
    }

    private Maybe<List<SearchItem>> observeRecentApps() {
        return T9Search.getInstance().getRecent();
    }

    public Intent getLaunchIntent(SearchItem item) {
        Intent intent = null;
        if (item instanceof ContactItem) {
            intent = new Intent("android.intent.action.CALL", Uri.parse("tel:"
                    + ((ContactItem)item).getPhoneNumber()));
        } else if (item instanceof AppItem) {
            intent = mPackageManager.getLaunchIntentForPackage(((AppItem) item).getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        }
        return intent;
    }

    public void recordUsage(SearchItem item) {
        if (item instanceof AppItem) {
            T9Search.getInstance().recordUsage(((AppItem) item).getPackageName());
        }
    }
}
