package com.example.snake.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.snake.data.db.dao.GameRecordDao;
import com.example.snake.data.db.dao.UserDao;
import com.example.snake.data.db.entity.GameRecord;
import com.example.snake.data.db.entity.User;

/**
 * Room 数据库单例。
 * 版本：1
 * 实体：User, GameRecord
 */
@Database(
        entities = {User.class, GameRecord.class},
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract GameRecordDao gameRecordDao();

    /**
     * 获取数据库单例。
     * 使用 application Context 避免 Activity 泄漏。
     * 双重检查锁单例 — 全局只创建一个数据库连接
     */
    public static AppDatabase getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "snake.db"
                            )
                            .fallbackToDestructiveMigration()// ========== 数据库升级策略 ==========
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
