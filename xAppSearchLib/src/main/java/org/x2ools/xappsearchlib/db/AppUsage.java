package org.x2ools.xappsearchlib.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * @author zhoubinjia
 * @date 2017/10/9
 */
@Entity
public class AppUsage {
    @PrimaryKey
    public String packageName;
    public int count;

    @Ignore
    public AppUsage(String packageName) {
        this.packageName = packageName;
    }

    public AppUsage() {
    }
}
