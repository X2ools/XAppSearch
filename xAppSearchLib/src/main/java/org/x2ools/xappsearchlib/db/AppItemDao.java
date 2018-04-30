package org.x2ools.xappsearchlib.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.x2ools.xappsearchlib.model.AppItem;

import java.util.List;

import io.reactivex.Maybe;

/**
 * @author zhoubinjia
 * @date 2017/12/4
 */
@Dao
public interface AppItemDao {
    @Query("SELECT * FROM AppItem")
    Maybe<List<AppItem>> getAll();

    @Query("SELECT * FROM AppItem where componentName = :componentName")
    Maybe<AppItem> get(String componentName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(AppItem app);

    @Delete
    void remove(AppItem app);
}
