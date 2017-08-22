
package org.x2ools.xappsearchlib.model;

import android.graphics.drawable.Drawable;

public class SearchItem {
    private int id;
    private String name;
    private String pinyin;
    private String fullpinyin;
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
}
