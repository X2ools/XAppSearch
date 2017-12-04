package org.x2ools.xappsearchlib.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * @author zhoubinjia
 * @date 2017/10/9
 */
@Database(entities = {AppUsage.class, InstalledApp.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppUsageDao appUsageDao();
    public abstract InstalledAppDao installedAppDao();
}
