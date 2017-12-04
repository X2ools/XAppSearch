package org.x2ools.xappsearchlib.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * @author zhoubinjia
 * @date 2017/12/4
 */
@Entity
public class InstalledApp {
    @PrimaryKey
    @NonNull
    public String packageName = "";
    public String name;
    public String pinyin;
    public String fullpinyin;

    @Ignore
    public InstalledApp(@NonNull String packageName, String name, String pinyin, String fullpinyin) {
        this.packageName = packageName;
        this.name = name;
        this.pinyin = pinyin;
        this.fullpinyin = fullpinyin;
    }

    public InstalledApp() {
    }
}
