package com.example.snake.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
/**
 * 游戏记录表实体。
 * 表名：game_records
 * 外键：user_id -> users.id，删除用户时级联删除其游戏记录。
 */
@Entity(
        tableName = "game_records",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE// 删用户时自动删其所有游戏记录
        ),
        indices = {@Index(value = "user_id")}
)
public class GameRecord {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "score")
    private int score;

    @ColumnInfo(name = "mode")
    private String mode; // "wall_pass" | "no_wall_pass"

    @ColumnInfo(name = "difficulty")
    private String difficulty; // "easy" | "medium" | "hard"

    @ColumnInfo(name = "played_at")
    private long playedAt;
    @Ignore
    public GameRecord() {
    }

    public GameRecord(long userId, int score, String mode, String difficulty, long playedAt) {
        this.userId = userId;
        this.score = score;
        this.mode = mode;
        this.difficulty = difficulty;
        this.playedAt = playedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
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
    @NonNull
    public void setMode(String mode) {
        this.mode = mode;
    }
    @NonNull
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
