package com.example.snake.util;

/**
 * 全局常量定义
 * 集中管理游戏模式、难度、速度参数、蛇身、食物、操控等核心常量
 */
public final class Constants {

    private Constants() {
        // 工具类禁止实例化
    }

    // ============================================================
    // 一、游戏模式
    // ============================================================
    public static final String MODE_WALL_PASS = "wall_pass";
    public static final String MODE_NO_WALL = "no_wall_pass";

    // ============================================================
    // 二、难度
    // ============================================================
    public static final String DIFFICULTY_EASY   = "easy";
    public static final String DIFFICULTY_MEDIUM = "medium";
    public static final String DIFFICULTY_HARD   = "hard";

    public static final int[][] DIFFICULTY_SPEED_PARAMS = {
            { 80, 2, 300 },
            { 100, 3, 400 },
            { 120, 4, 500 }
    };


    // ============================================================
    // 三、蛇身相关
    // ============================================================
    public static final int NODE_DIAMETER_DP = 20;

    // ============================================================
    // 四、食物相关
    // ============================================================
    /** 吃到一个食物的得分 */
    public static final int FOOD_SCORE = 10;

    /** 同时存在的食物数量 */
    public static final int FOOD_COUNT = 5;

}