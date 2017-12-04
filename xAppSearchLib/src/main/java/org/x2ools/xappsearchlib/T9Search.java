
package org.x2ools.xappsearchlib;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import org.reactivestreams.Publisher;
import org.x2ools.xappsearchlib.db.AppDatabase;
import org.x2ools.xappsearchlib.db.AppUsage;
import org.x2ools.xappsearchlib.db.InstalledApp;
import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.ContactItem;
import org.x2ools.xappsearchlib.model.SearchItem;
import org.x2ools.xappsearchlib.tools.IconCache;
import org.x2ools.xappsearchlib.tools.ToPinYinUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class T9Search {

    private static final boolean DEBUG = false;

    private static final String TAG = "T9Search";
    private Disposable dataDisposable;
    private AppDatabase mDb;


    private T9Search() {
    }

    private static class Holder {
        @SuppressLint("StaticFieldLeak")
        private static T9Search INSTANCE = new T9Search();
    }

    public static T9Search getInstance() {
        return Holder.INSTANCE;
    }

    private Context mContext;
    private PackageManager mPackageManager;
    private ContentResolver mResolver;
    private HashMap<String, ContactItem> mAddedContact = new HashMap<>();
    private IconCache mIconCache;
    private ArrayList<SearchItem> mSearchResult = new ArrayList<>();
    private BehaviorSubject<List<SearchItem>> mAllItemsSubject = BehaviorSubject.createDefault(Collections.<SearchItem>emptyList());
    private boolean mContactEnable = false;
    private boolean mCallPhoneEnable = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            T9Search.this.reloadData();
        }
    };

    private Publisher<List<SearchItem>> mGetAllCallable = (subscriber) -> {
        List<SearchItem> all = new ArrayList<>();
        if (mContactEnable) {
            Cursor cursor = null;
            try {
                cursor = mResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        null, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String name = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String photoUri = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                    ContactItem item = new ContactItem();
                    item.setId(id);
                    item.setName(name);
                    item.setPinyin(ToPinYinUtils.getPinYin(item.getName(), false));
                    item.setFullpinyin(ToPinYinUtils.getPinYin(item.getName(), true));
                    item.setPhoneNumber(phoneNumber);
                    mIconCache.getIcon(item, mResolver, photoUri);

                    if (mAddedContact.containsKey(name)) {
                        ContactItem added = mAddedContact.get(name);
                        if (added.getPhoneNumber().equals(phoneNumber)) {
                            continue;
                        } else {
                            item.setName(name + "(" + phoneNumber + ")");
                        }
                    }

                    all.add(item);
                    mAddedContact.put(name, item);
                }
                cursor.close();
            }
        }

        List<InstalledApp> installedApps = mDb.installedAppDao().getAll().blockingGet();
        if (installedApps != null) {
            for (InstalledApp app : installedApps) {
                AppItem appitem = new AppItem();
                appitem.setName(app.name);
                appitem.setPinyin(app.pinyin);
                appitem.setFullpinyin(app.fullpinyin);
                appitem.setPackageName(app.packageName);
                mIconCache.getIcon(appitem, app.packageName);
                all.add(appitem);
            }

            Collections.sort(all, new NameComparator());
            subscriber.onNext(new ArrayList<>(all));
        }

        List<ApplicationInfo> applications = new ArrayList<>();
        applications.addAll(mPackageManager.getInstalledApplications(0));
        for (ApplicationInfo appinfo : applications) {
            if (mPackageManager.getLaunchIntentForPackage(appinfo.packageName) == null)
                continue;
            String name = appinfo.loadLabel(mPackageManager).toString();
            String pinyin = ToPinYinUtils.getPinYin(name, false);
            String fullpinyin = ToPinYinUtils.getPinYin(name, true);
            AppItem appitem = new AppItem();
            appitem.setName(name);
            appitem.setPinyin(pinyin);
            appitem.setFullpinyin(fullpinyin);
            appitem.setPackageName(appinfo.packageName);
            mIconCache.getIcon(appitem, appinfo);
            if (all.contains(appitem)) {
                all.remove(appitem);
            }
            mDb.installedAppDao().add(new InstalledApp(appinfo.packageName, name, pinyin, fullpinyin));
            all.add(appitem);
        }

        Collections.sort(all, new NameComparator());
        subscriber.onNext(new ArrayList<>(all));
    };

    public void init(Context context, boolean contactEnable, boolean callEnable) {
        mContext = context.getApplicationContext();
        mPackageManager = context.getPackageManager();
        mResolver = context.getContentResolver();
        mDb = Room.databaseBuilder(mContext, AppDatabase.class, "app_db")
                .build();
        mIconCache = new IconCache(context);
        mContactEnable = contactEnable;
        mCallPhoneEnable = callEnable;
        reloadData();

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            mContext.registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setContactEnable(boolean enable) {
        mContactEnable = enable;
        reloadData();
    }

    public void setCallPhoneEnable(boolean callPhoneEnable) {
        this.mCallPhoneEnable = callPhoneEnable;
        reloadData();
    }

    public boolean isCallPhoneEnable() {
        return mCallPhoneEnable;
    }

    private void reloadData() {
        if (dataDisposable != null) dataDisposable.dispose();
        dataDisposable = Observable.fromPublisher(mGetAllCallable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchItems -> mAllItemsSubject.onNext(searchItems));
    }

    public List<SearchItem> getAll() {
        return mAllItemsSubject.getValue();
    }

    public BehaviorSubject<List<SearchItem>> getAllSubject() {
        return mAllItemsSubject;
    }

    public class NameComparator implements Comparator<SearchItem> {
        @Override
        public int compare(SearchItem lhs, SearchItem rhs) {
            int ltype = (lhs instanceof ContactItem) ? 1 : -1;
            int rtype = (rhs instanceof ContactItem) ? 1 : -1;
            int type = ltype - rtype;
            int name = lhs.getName().compareTo(rhs.getName());
            if (type != 0) {
                return type;
            } else {
                return name;
            }
        }

    }

    public Maybe<List<SearchItem>> getRecent() {
        return mDb.appUsageDao().getRecentApps().map(appUsages -> {
            List<SearchItem> items = new ArrayList<>();
            for (AppUsage appUsage : appUsages) {
                ApplicationInfo info = null;
                try {
                    info = mPackageManager.getApplicationInfo(appUsage.packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (info != null) {
                    AppItem item = new AppItem();
                    item.setName(info.loadLabel(mPackageManager).toString());
                    item.setPackageName(appUsage.packageName);
                    item.setIcon(info.loadIcon(mPackageManager));
                    items.add(item);
                }
            }
            return items;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void recordUsage(String packageName) {
        mDb.appUsageDao().get(packageName)
                .subscribeOn(Schedulers.io())
                .subscribe(appUsage -> {
                    appUsage.count += 1;
                    mDb.appUsageDao().add(appUsage);
                }, throwable -> {}, () -> {
                    AppUsage newAppUsage = new AppUsage(packageName);
                    newAppUsage.count = 1;
                    mDb.appUsageDao().add(newAppUsage);
                });
    }

    public Observable<List<SearchItem>> search(final String text) {
        return Observable.fromCallable((Callable<List<SearchItem>>) () -> {
            mSearchResult.clear();
            String lower = text.toLowerCase();
            int pos = 0;
            if (mAllItemsSubject.getValue() == null) return mSearchResult;
            for (SearchItem item : mAllItemsSubject.getValue()) {
                if (item == null) {
                    Log.wtf(TAG, "item null ???");
                    continue;
                }

                pos = item.getName().indexOf(lower);
                if (pos != -1) {
                    mSearchResult.add(item);
                    continue;
                }

                pos = item.getPinyin().indexOf(lower);
                if (pos != -1) {
                    mSearchResult.add(item);
                    continue;
                }

                pos = item.getFullpinyin().indexOf(lower);
                if (pos != -1) {
                    mSearchResult.add(item);
                    continue;
                }

                pos = ToPinYinUtils.getPinyinNum(item.getPinyin()).indexOf(lower);
                if (pos != -1) {
                    mSearchResult.add(item);
                    continue;
                }

                pos = ToPinYinUtils.getPinyinNum(item.getFullpinyin()).indexOf(lower);
                if (pos != -1) {
                    mSearchResult.add(item);
                    continue;
                }

                if (item instanceof AppItem) {
                    pos = ((AppItem) item).getPackageName().indexOf(lower);
                    if (pos != -1) {
                        mSearchResult.add(item);
                        continue;
                    }

                    pos = ToPinYinUtils.getPinyinNum(((AppItem) item).getPackageName()).indexOf(lower);
                    if (pos != -1) {
                        mSearchResult.add(item);
                        continue;
                    }
                }

                if (item instanceof ContactItem) {
                    if (!TextUtils.isEmpty(((ContactItem) item).getPhoneNumber())) {
                        pos = ((ContactItem) item).getPhoneNumber().indexOf(lower);
                        if (pos != -1) {
                            mSearchResult.add(item);
                            continue;
                        }
                    }
                }
            }
            return mSearchResult;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void destroy() {
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
