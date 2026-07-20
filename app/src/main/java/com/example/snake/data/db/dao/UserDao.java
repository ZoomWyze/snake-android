package com.example.snake.data.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.snake.data.db.entity.User;
import com.example.snake.data.db.entity.UserScoreDTO;

import java.util.List;

@Dao
public interface UserDao {

    /**
     * 插入新用户，返回自增 id。
     */
    @Insert
    long insert(User user);

    /**
     * 按账号查询用户（用于注册校验 + 登录校验）。
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    /**
     * 按 id 查询用户（用于自动登录后获取昵称等信息）。
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User findById(long userId);

    /**
     * 本机全用户排行榜：
     *   - 每个用户取其历史最高分（无记录则 0）
     *   - 同一用户多次达到相同最高分时，取最近一次的难度展示
     *   - 按最高分降序排列
     *
     * 注意：列别名与 UserScoreDTO 字段名严格匹配。
     */
    @Query(
            "SELECT u.nickname AS nickname, " +
                    "COALESCE(MAX(g.score), 0) AS maxScore, " +
                    "(SELECT g2.difficulty FROM game_records g2 " +
                    " WHERE g2.user_id = u.id " +
                    "   AND g2.score = (SELECT MAX(score) FROM game_records WHERE user_id = u.id) " +
                    " ORDER BY g2.played_at DESC LIMIT 1) AS difficulty " +
                    "FROM users u " +
                    "LEFT JOIN game_records g ON u.id = g.user_id " +
                    "GROUP BY u.id, u.nickname " +
                    "ORDER BY maxScore DESC"
    )
    LiveData<List<UserScoreDTO>> getLeaderboard();
}
