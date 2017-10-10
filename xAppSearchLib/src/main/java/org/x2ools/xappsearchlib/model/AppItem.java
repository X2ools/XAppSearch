package org.x2ools.xappsearchlib.model;

import android.content.Intent;

/**
 * @author zhoubinjia
 * @date 2017/8/22
 */
public class AppItem extends SearchItem {

    private String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppItem appItem = (AppItem) o;

        return packageName != null ? packageName.equals(appItem.packageName) : appItem.packageName == null;
    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }
}
