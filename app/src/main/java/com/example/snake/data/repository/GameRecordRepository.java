package com.example.snake.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.example.snake.data.db.AppDatabase;
import com.example.snake.data.db.dao.GameRecordDao;
import com.example.snake.data.db.entity.GameRecord;
import com.example.snake.data.db.entity.MaxScoreDTO;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 游戏记录数据仓库。
 *   - 写操作在单线程后台执行
 *   - LiveData 查询直接暴露
 *   - 一次性查询通过 Callback 在主线程回调
 */
public class GameRecordRepository {

    private final GameRecordDao recordDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public GameRecordRepository(@NonNull Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.recordDao = db.gameRecordDao();
    }

    // ------------------------------------------------------------------
    // 写操作
    // ------------------------------------------------------------------

    public interface InsertCallback {
        void onInserted();

        void onError(Throwable t);
    }

    /**
     * 异步插入一局游戏记录。
     */
    public void insert(@NonNull GameRecord record, @Nullable InsertCallback callback) {
        executor.execute(() -> {
            try {
                recordDao.insert(record);
                if (callback != null) {
                    mainHandler.post(callback::onInserted);
                }
            } catch (Throwable t) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(t));
                }
            }
        });
    }

    // ------------------------------------------------------------------
    // 一次性查询（回调式，主线程回调）
    // ------------------------------------------------------------------

    public interface MaxScoreCallback {
        void onResult(@Nullable Integer maxScore);
    }

    /**
     * 【新增】异步查询当前用户历史最高分（用于结算判断是否破纪录）。
     * 主线程回调，如果用户无记录则回调 null。
     */
    public void getMaxScoreForCompare(long userId, @NonNull MaxScoreCallback callback) {
        executor.execute(() -> {
            Integer max = recordDao.getMaxScoreSync(userId);
            mainHandler.post(() -> callback.onResult(max));
        });
    }

    // ------------------------------------------------------------------
    // LiveData 查询
    // ------------------------------------------------------------------

    /**
     * 当前用户的历史记录列表（时间倒序）。
     */
    public LiveData<List<GameRecord>> getByUserId(long userId) {
        return recordDao.getByUserId(userId);
    }

    /**
     * 当前用户的最高分（无记录时为 null）。
     */
    public LiveData<Integer> getMaxScore(long userId) {
        return recordDao.getMaxScore(userId);
    }

    /**
     * 当前用户的最高分详情（含模式/难度/时间）。
     */
    public LiveData<MaxScoreDTO> getMaxScoreDetail(long userId) {
        return recordDao.getMaxScoreDetail(userId);
    }
}
