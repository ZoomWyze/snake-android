package com.example.snake.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.snake.data.db.entity.GameRecord;
import com.example.snake.data.db.entity.MaxScoreDTO;

import java.util.List;

@Dao
public interface GameRecordDao {

    /**
     * 插入一条游戏记录。
     */
    @Insert
    void insert(GameRecord record);

    /**
     * 查询某用户的全部游戏记录，按时间倒序（历史记录页）。
     */
    @Query("SELECT * FROM game_records WHERE user_id = :userId ORDER BY played_at DESC")
    LiveData<List<GameRecord>> getByUserId(long userId);

    /**
     * 查询某用户的最高分（用于主页持续观察）。
     * 当用户没有任何记录时，LiveData 中会收到 null。
     */
    @Query("SELECT MAX(score) FROM game_records WHERE user_id = :userId")
    LiveData<Integer> getMaxScore(long userId);

    /**
     * 同步查询某用户的最高分（用于结算页一次性判定"新纪录"）。
     * 必须在后台线程调用。
     */
    @Query("SELECT MAX(score) FROM game_records WHERE user_id = :userId")
    Integer getMaxScoreSync(long userId);

    /**
     * 查询某用户的最高分详情：
     *   - 取该用户最高分
     *   - 若多次达到最高分，取最近一次
     *   - 返回 score/mode/difficulty/playedAt 四个字段
     *
     * 用于主页"个人最高分（含模式/难度）"展示。
     */
    @Query(
            "SELECT score AS score, mode AS mode, difficulty AS difficulty, played_at AS playedAt " +
                    "FROM game_records " +
                    "WHERE user_id = :userId " +
                    "  AND score = (SELECT MAX(score) FROM game_records WHERE user_id = :userId) " +
                    "ORDER BY played_at DESC " +
                    "LIMIT 1"
    )
    LiveData<MaxScoreDTO> getMaxScoreDetail(long userId);
}
