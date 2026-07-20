package com.example.snake.data.db.entity;
import androidx.room.Ignore;
/**
 * 最高分详情 DTO（非 Entity）。
 * 由 GameRecordDao#getMaxScoreDetail() 查询映射。
 * 用于主页"个人最高分（含模式/难度）"展示。
 */
public class MaxScoreDTO {

    private int score;
    private String mode;
    private String difficulty;
    private long playedAt;
    @Ignore
    public MaxScoreDTO() {
    }

    public MaxScoreDTO(int score, String mode, String difficulty, long playedAt) {
        this.score = score;
        this.mode = mode;
        this.difficulty = difficulty;
        this.playedAt = playedAt;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public long getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(long playedAt) {
        this.playedAt = playedAt;
    }
}
