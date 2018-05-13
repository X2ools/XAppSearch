package org.x2ools.xappsearchlib.tools;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

public class IconCache extends LruCache<String, Drawable> {

    private IconCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public void resize(int maxSize) {
        super.resize(maxSize);
    }

    @Override
    public void trimToSize(int maxSize) {
        super.trimToSize(maxSize);
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Drawable oldValue, Drawable newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);
    }

    @Override
    protected Drawable create(String key) {
        ComponentName componentName = ComponentName.unflattenFromString(key);
        Drawable icon;
        try {
            icon = AppContext.get().getPackageManager().getActivityIcon(componentName);
        } catch (PackageManager.NameNotFoundException e) {
            icon = AppContext.get().getPackageManager().getDefaultActivityIcon();
        }
        return icon;
    }

    @Override
    protected int sizeOf(String key, Drawable value) {
        if (value.getIntrinsicHeight() <= 0 || value.getIntrinsicWidth() <= 0) {
            return 48 * 48;
        } else {
            return value.getIntrinsicWidth() * value.getIntrinsicHeight();
        }
    }

    private static class Holder {
        private static IconCache INSTANCE = new IconCache(200 * 48 * 48);
    }

    public static IconCache get() {
        return Holder.INSTANCE;
    }
}
