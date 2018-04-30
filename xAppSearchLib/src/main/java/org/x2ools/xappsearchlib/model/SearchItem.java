
package org.x2ools.xappsearchlib.model;

import android.arch.persistence.room.Ignore;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class SearchItem implements Comparable<SearchItem> {
    private int id;
    private String name;
    private String pinyin;
    private String fullpinyin;
    private int usage;
    @Ignore
    private Drawable icon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getFullpinyin() {
        return fullpinyin;
    }

    public void setFullpinyin(String fullpinyin) {
        this.fullpinyin = fullpinyin;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    @Override
    public int compareTo(@NonNull SearchItem o) {
        if (o instanceof CalcItem) return -1;
        if (o instanceof ContactItem) return -1;
        int compare = - Integer.compare(usage, o.usage);
        if (compare == 0) {
            compare = name.compareTo(o.name);
        }
        return compare;
    }
}
