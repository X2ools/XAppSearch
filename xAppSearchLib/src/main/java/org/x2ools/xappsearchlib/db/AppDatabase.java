package org.x2ools.xappsearchlib.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import org.x2ools.xappsearchlib.model.AppItem;

/**
 * @author zhoubinjia
 * @date 2017/10/9
 */
@Database(entities = {AppItem.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AppItemDao appItemDao();
}
