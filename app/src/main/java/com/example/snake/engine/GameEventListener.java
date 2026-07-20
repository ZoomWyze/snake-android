package com.example.snake.engine;

/**
 * 游戏事件回调接口。
 * 由上层（如 Activity 或 GameSurfaceView）实现，接收引擎发出的游戏事件。
 */
public interface GameEventListener {

    /**
     * 蛇吃到食物时回调。
     *
     * @param newScore 吃到食物后的最新总分
     */
    void onFoodEaten(int newScore);

    /**
     * 游戏结束时回调。
     *
     * @param finalScore 本局最终得分
     */
    void onGameOver(int finalScore);

    /**
     * 分数变化时回调。
     *
     * @param score 当前分数
     */
    void onScoreChanged(int score);
}
