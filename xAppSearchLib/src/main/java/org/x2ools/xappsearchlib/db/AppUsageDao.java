package org.x2ools.xappsearchlib.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Maybe;

/**
 * @author zhoubinjia
 * @date 2017/10/9
 */
@Dao
public interface AppUsageDao {
    @Query("SELECT * FROM AppUsage")
    Maybe<List<AppUsage>> getAll();

    @Query("SELECT * FROM AppUsage ORDER BY count DESC LIMIT 9")
    Maybe<List<AppUsage>> getRecent9Apps();

    @Query("SELECT * FROM AppUsage where packageName = :packageName")
    Maybe<AppUsage> get(String packageName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(AppUsage appUsage);
}
