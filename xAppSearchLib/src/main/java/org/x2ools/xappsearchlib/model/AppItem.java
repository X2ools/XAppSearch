package org.x2ools.xappsearchlib.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @author zhoubinjia
 * @date 2017/8/22
 */
@Entity
public class AppItem extends SearchItem {

    @PrimaryKey
    @NonNull
    private String componentName = "";
    private String packageName;

    public AppItem() {}

    public AppItem(@NonNull String componentName) {
        this.componentName = componentName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @NonNull
    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(@NonNull String componentName) {
        this.componentName = componentName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppItem appItem = (AppItem) o;

        return componentName.equals(appItem.componentName);
    }

    @Override
    public int hashCode() {
        return componentName.hashCode();
    }

    @Override
    public int compareTo(@NonNull SearchItem o) {
        return super.compareTo(o);
    }
}
