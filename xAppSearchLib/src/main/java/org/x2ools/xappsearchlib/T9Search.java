
package org.x2ools.xappsearchlib;

import android.annotation.SuppressLint;
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

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class T9Search {

    private static final boolean DEBUG = false;

    private static final String TAG = "T9Search";


    private T9Search() {}

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
    private String mPrevInput;
    private ArrayList<SearchItem> mSearchResult = new ArrayList<>();
    private ArrayList<SearchItem> mPrevResult = new ArrayList<>();
    private BehaviorSubject<List<SearchItem>> mAllItemsSubject = BehaviorSubject.createDefault(Collections.<SearchItem>emptyList());
    private boolean mContactEnable = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            T9Search.this.reloadData();
        }
    };

    private Callable<List<SearchItem>> mGetAllCallable = new Callable<List<SearchItem>>() {
        @Override
        public List<SearchItem> call() throws Exception {
            List<SearchItem> all = new ArrayList<>();

            List<ApplicationInfo> mApplications = new ArrayList<>();
            mApplications.addAll(mPackageManager.getInstalledApplications(0));
            for (ApplicationInfo appinfo : mApplications) {
                if (mPackageManager.getLaunchIntentForPackage(appinfo.packageName) == null)
                    continue;
                AppItem appitem = new AppItem();
                appitem.setName(appinfo.loadLabel(mPackageManager).toString());
                appitem.setPinyin(ToPinYinUtils.getPinyinNum(appitem.getName(), false));
                appitem.setFullpinyin(ToPinYinUtils.getPinyinNum(appitem.getName(), true));
                appitem.setPackageName(appinfo.packageName);
                mIconCache.getIcon(appitem, appinfo);
                all.add(appitem);
            }

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
                        item.setPinyin(ToPinYinUtils.getPinyinNum(item.getName(), false));
                        item.setFullpinyin(ToPinYinUtils.getPinyinNum(item.getName(), true));
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

            Collections.sort(all, new NameComparator());
            return all;
        }
    };

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mPackageManager = context.getPackageManager();
        mResolver = context.getContentResolver();
        mIconCache = new IconCache(context);
        reloadData();

        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addDataScheme("package");
            context.registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setContactEnable(boolean enable) {
        mContactEnable = enable;
    }

    private void reloadData() {
        Observable.fromCallable(mGetAllCallable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<SearchItem>>() {
                    @Override
                    public void accept(List<SearchItem> searchItems) throws Exception {
                        mAllItemsSubject.onNext(searchItems);
                    }
                });
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

    public Observable<List<SearchItem>> search(final String number) {
        return Observable.fromCallable(new Callable<List<SearchItem>>() {
            @Override
            public List<SearchItem> call() throws Exception {
                mSearchResult.clear();
                int pos = 0;
                boolean newQuery = mPrevInput == null || number.length() <= mPrevInput.length();
                for (SearchItem item : (newQuery ? mAllItemsSubject.getValue() : mPrevResult)) {
                    pos = item.getPinyin().indexOf(number);
                    if (pos != -1) {
                        mSearchResult.add(item);
                        continue;
                    }

                    pos = item.getFullpinyin().indexOf(number);
                    if (pos != -1) {
                        mSearchResult.add(item);
                        continue;
                    }

                    if (item instanceof ContactItem) {
                        if (!TextUtils.isEmpty(((ContactItem)item).getPhoneNumber())) {
                            pos = ((ContactItem)item).getPhoneNumber().indexOf(number);
                            if (pos != -1) {
                                mSearchResult.add(item);
                                continue;
                            }
                        }
                    }
                }
                mPrevResult.clear();
                mPrevInput = number;
                if (mSearchResult.size() > 0) {
                    mPrevResult.addAll(mSearchResult);
                    return mSearchResult;
                }
                return mSearchResult;
            }
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
