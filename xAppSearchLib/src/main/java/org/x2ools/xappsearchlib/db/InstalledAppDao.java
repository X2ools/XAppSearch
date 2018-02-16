package org.x2ools.xappsearchlib.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Maybe;

/**
 * @author zhoubinjia
 * @date 2017/12/4
 */
@Dao
public interface InstalledAppDao {
    @Query("SELECT * FROM InstalledApp")
    Maybe<List<InstalledApp>> getAll();

    @Query("SELECT * FROM InstalledApp where packageName = :packageName")
    Maybe<InstalledApp> get(String packageName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(InstalledApp app);

    @Delete
    void remove(InstalledApp app);
}
