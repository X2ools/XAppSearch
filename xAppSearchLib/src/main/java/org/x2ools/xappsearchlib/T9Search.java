
package org.x2ools.xappsearchlib;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;
import org.reactivestreams.Publisher;
import org.x2ools.xappsearchlib.db.AppDatabase;
import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.CalcItem;
import org.x2ools.xappsearchlib.model.ContactItem;
import org.x2ools.xappsearchlib.model.SearchItem;
import org.x2ools.xappsearchlib.tools.ToPinYinUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class T9Search {

    private static final boolean DEBUG = false;

    private static final String TAG = "T9Search";
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private Symbols symbols = new Symbols();
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

        List<AppItem> installedApps = mDb.appItemDao().getAll().blockingGet();
        if (installedApps == null) {
            installedApps = new ArrayList<>();
        }
        Collections.sort(installedApps);
        subscriber.onNext(new ArrayList<>(installedApps));

        List<SearchItem> newAll = new ArrayList<>();
        List<ApplicationInfo> applicationInfoList = mPackageManager.getInstalledApplications(0);
        for (ApplicationInfo info : applicationInfoList) {
            Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(info.packageName);
            List<ResolveInfo> ris = mPackageManager.queryIntentActivities(intentToResolve, 0);
            if (ris == null || ris.size() <= 0) continue;
            for (ResolveInfo resolveInfo : ris) {
                ComponentName componentName = new ComponentName(resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                AppItem item = new AppItem(componentName.flattenToString());
                int oldIndex = installedApps.indexOf(item);
                if (oldIndex != -1) {
                    item = installedApps.get(oldIndex);
                }
                String name = resolveInfo.activityInfo.loadLabel(mPackageManager).toString();
                String pinyin = ToPinYinUtils.getPinYin(name, false);
                String fullpinyin = ToPinYinUtils.getPinYin(name, true);
                item.setPackageName(resolveInfo.activityInfo.packageName);
                item.setName(name);
                item.setPinyin(pinyin);
                item.setFullpinyin(fullpinyin);
                mDb.appItemDao().add(item);
                newAll.add(item);
            }
        }

        Collections.sort(newAll);
        if (!newAll.equals(installedApps)) {
            subscriber.onNext(new ArrayList<>(newAll));
        }
    };

    /*
    *         if (mContactEnable) {
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
    * */

    public void init(Context context, boolean contactEnable, boolean callEnable) {
        mContext = context.getApplicationContext();
        mPackageManager = context.getPackageManager();
        mResolver = context.getContentResolver();
        mDb = Room.databaseBuilder(mContext, AppDatabase.class, "app_db")
                .build();
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
        mDisposable.clear();
        mDisposable.add(Observable.fromPublisher(mGetAllCallable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchItems -> mAllItemsSubject.onNext(searchItems)));
    }

    public List<SearchItem> getAll() {
        return mAllItemsSubject.getValue();
    }

    public BehaviorSubject<List<SearchItem>> getAllSubject() {
        return mAllItemsSubject;
    }

    private double eval(String exp) throws SyntaxException {
        return symbols.eval(exp);
    }

    public void recordUsage(String componentName) {
        mDisposable.add(mDb.appItemDao().get(componentName)
                .subscribeOn(Schedulers.io())
                .subscribe(appUsage -> {
                    appUsage.setUsage(appUsage.getUsage() + 1);
                    mDb.appItemDao().add(appUsage);
                }, throwable -> {}));
    }

    public Observable<List<SearchItem>> search(final String text) {
        return Observable.fromCallable((Callable<List<SearchItem>>) () -> {
            mSearchResult.clear();
            String lower = text.toLowerCase();
            try {
                double res = eval(lower);
                mSearchResult.add(new CalcItem(res));
            } catch (SyntaxException ignored) {
            }
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
                    pos = ((AppItem) item).getComponentName().indexOf(lower);
                    if (pos != -1) {
                        mSearchResult.add(item);
                        continue;
                    }

                    pos = ToPinYinUtils.getPinyinNum(((AppItem) item).getComponentName()).indexOf(lower);
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
        mDisposable.clear();
    }

}
