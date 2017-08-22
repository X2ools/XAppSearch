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


        T9Search.getInstance().getAllSubject().subscribe(new Consumer<List<SearchItem>>() {
            @Override
            public void accept(List<SearchItem> searchItems) throws Exception {
                if (!TextUtils.isEmpty(mFilterStr)) {
                    filter(mFilterStr);
                } else if (mAllMode) {
                    setAllApplicationsData();
                }
            }
        });
    }

    public void setDelegate(T9ViewDelegate delegate) {
        mDelegate = delegate;
    }

    public void setApplicationsData() {
        observeRecentApps().subscribe(new Consumer<List<SearchItem>>() {
            @Override
            public void accept(List<SearchItem> searchItems) throws Exception {
                if (mDelegate != null) {
                    mDelegate.showItems(searchItems);
                }
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
            setApplicationsData();
            return;
        }
        T9Search.getInstance().search(string).subscribe(new Consumer<List<SearchItem>>() {
            @Override
            public void accept(List<SearchItem> searchItems) throws Exception {
                if (mDelegate != null) {
                    mDelegate.showItems(searchItems);
                }
            }
        });
        mAllMode = false;
    }

    private Observable<List<SearchItem>> observeRecentApps() {
        return Observable.fromCallable(new Callable<List<SearchItem>>() {
            @Override
            public List<SearchItem> call() throws Exception {
                return getRecentApps();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private List<SearchItem> getRecentApps() {
        List<ActivityManager.RecentTaskInfo> recentTasks = mActivityManager.getRecentTasks(9,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE | ActivityManager.RECENT_WITH_EXCLUDED);
        Set<AppItem> recents = new HashSet<>();
        if (recentTasks != null) {
            for (ActivityManager.RecentTaskInfo recentInfo : recentTasks) {
                try {
                    ApplicationInfo info = mPackageManager.getApplicationInfo(recentInfo.baseIntent
                            .getComponent().getPackageName(), 0);
                    if (mPackageManager.getLaunchIntentForPackage(info.packageName) == null)
                        continue;
                    if ((recentInfo.baseIntent != null)
                            && ((recentInfo.baseIntent.getFlags() & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) != 0)) {
                        Log.d(TAG, "This task has flag = FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
                        continue;
                    }

                    AppItem item = new AppItem();
                    item.setName(info.loadLabel(mPackageManager).toString());
                    item.setPackageName(info.packageName);
                    item.setId(recentInfo.id);
                    item.setBaseIntent(recentInfo.baseIntent);
                    item.setIcon(info.loadIcon(mPackageManager));
                    recents.add(item);
                } catch (PackageManager.NameNotFoundException e) {
                    // Log.e(TAG, "cannot find package", e);
                }
            }
        }

        return new ArrayList<SearchItem>(recents);
    }

    public Intent getLaunchIntent(SearchItem item) {
        Intent intent = null;
        if (item instanceof ContactItem) {
            intent = new Intent("android.intent.action.CALL", Uri.parse("tel:"
                    + ((ContactItem)item).getPhoneNumber()));
        } else if (item instanceof AppItem) {
            if (item.getId() >= 0 && isTaskInRecentList((AppItem) item)) {
                mActivityManager.moveTaskToFront(item.getId(),
                        ActivityManager.MOVE_TASK_WITH_HOME);
                Log.v(TAG, "Move Task To Front for " + item.getId());
            } else if (((AppItem) item).getBaseIntent() != null) {
                intent = ((AppItem) item).getBaseIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                        | Intent.FLAG_ACTIVITY_TASK_ON_HOME
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                Log.v(TAG, "Starting activity " + intent);
                if (intent.resolveActivity(mPackageManager) == null) {
                    intent = mPackageManager.getLaunchIntentForPackage(((AppItem) item).getPackageName());
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                }
            } else {
                intent = mPackageManager.getLaunchIntentForPackage(((AppItem) item).getPackageName());
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
            }
        }
        return intent;
    }

    private boolean isTaskInRecentList(AppItem item) {
        final int taskId = item.getId();
        final Intent intent = item.getBaseIntent();
        if ((intent != null)
                && ((intent.getFlags() & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) != 0)) {
            // / M: Don't care exclude-from-recent app.
            Log.d(TAG, "This task has flag = FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
            return true;
        }
        final List<ActivityManager.RecentTaskInfo> recentTasks = mActivityManager.getRecentTasks(20,
                ActivityManager.RECENT_IGNORE_UNAVAILABLE);

        for (int i = 0; i < recentTasks.size(); ++i) {
            final ActivityManager.RecentTaskInfo info = recentTasks.get(i);
            if (info.id == taskId) {
                return true;
            }
        }

        Log.d(TAG, "This task is not in recent list for " + taskId);

        return false;
    }
}
