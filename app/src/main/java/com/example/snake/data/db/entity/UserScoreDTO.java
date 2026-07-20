package com.example.snake.data.db.entity;
import androidx.room.Ignore;
/**
 * 排行榜查询结果映射类（非 Entity）。
 * 由 UserDao#getLeaderboard() 的 SQL 查询直接映射。
 * 列别名需与字段名严格对应：
 *   - nickname
 *   - maxScore
 *   - difficulty
 */
public class UserScoreDTO {

    private String nickname;
    private int maxScore;
    private String difficulty;
    @Ignore
    public UserScoreDTO() {
    }

    public UserScoreDTO(String nickname, int maxScore, String difficulty) {
        this.nickname = nickname;
        this.maxScore = maxScore;
        this.difficulty = difficulty;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
