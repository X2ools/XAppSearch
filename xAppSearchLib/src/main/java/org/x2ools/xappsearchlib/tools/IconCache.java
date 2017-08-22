
package org.x2ools.xappsearchlib.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import org.x2ools.xappsearchlib.R;
import org.x2ools.xappsearchlib.model.AppItem;
import org.x2ools.xappsearchlib.model.SearchItem;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class IconCache {

    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

    private static class CacheEntry {
        public Drawable icon;
        //public String title;
    }

    private final HashMap<String, CacheEntry> mCache =
            new HashMap<String, CacheEntry>(INITIAL_ICON_CACHE_CAPACITY);
    
    private PackageManager pm;
    private Context mContext;

    public IconCache(Context context) {
        pm = context.getPackageManager();
        mContext = context;
    }

    /**
     * Remove any records for the supplied ComponentName.
     */
    public void remove(String key) {
        synchronized (mCache) {
            mCache.remove(key);
        }
    }

    /**
     * Empty out the cache.
     */
    public void flush() {
        synchronized (mCache) {
            mCache.clear();
        }
    }
    
    public void getIcon(SearchItem item, ContentResolver resolver, String uristr) {
        synchronized (mCache) {
            String key = item.getId() + "";
            CacheEntry entry = mCache.get(key);
            if (entry == null) {
                entry = new CacheEntry();

                mCache.put(key, entry);
                //entry.title = key;
                Drawable drawable = null;
                try {
                    Uri uri = Uri.parse(uristr);
                    drawable = Drawable.createFromStream(resolver.openInputStream(uri), uristr);
                } catch (FileNotFoundException e) {
                    drawable = mContext.getResources().getDrawable(R.drawable.ic_contact_unknow);
                } catch (NullPointerException e) {
                    drawable = mContext.getResources().getDrawable(R.drawable.ic_contact_unknow);
                }
                entry.icon = drawable;
            }
            item.setIcon(entry.icon);
        }
    }

    public void getIcon(AppItem item, ApplicationInfo info) {
        synchronized (mCache) {
            String key = item.getPackageName();
            CacheEntry entry = mCache.get(key);
            if (entry == null) {
                entry = new CacheEntry();

                mCache.put(key, entry);
                //entry.title = key;
                entry.icon = info.loadIcon(pm);
            }
            item.setIcon(entry.icon);
        }
    }

    public HashMap<String, Drawable> getAllIcons() {
        synchronized (mCache) {
            HashMap<String, Drawable> set = new HashMap<String, Drawable>();
            for (String key : mCache.keySet()) {
                final CacheEntry e = mCache.get(key);
                set.put(key, e.icon);
            }
            return set;
        }
    }

}
