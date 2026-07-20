package com.example.snake.engine;

/**
 * 游戏配置数据类。
 * 封装游戏区域尺寸、速度参数、模式和节点尺寸等配置。
 */
public class GameConfig {

    /** 游戏区域宽度（像素） */
    public int areaWidth;

    /** 游戏区域高度（像素） */
    public int areaHeight;

    /** 初始移动速度（像素/秒） */
    public float initialSpeed;

    /** 每吃一个食物的加速量（像素/秒/食物） */
    public float speedIncrement;

    /** 最大移动速度（像素/秒） */
    public float maxSpeed;

    /** 是否穿墙模式 */
    public boolean wallPass;

    /** 蛇身节点半径（像素） */
    public float nodeRadius;

    public GameConfig(int areaWidth, int areaHeight, float initialSpeed,
                      float speedIncrement, float maxSpeed, boolean wallPass,
                      float nodeRadius) {
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
        this.initialSpeed = initialSpeed;
        this.speedIncrement = speedIncrement;
        this.maxSpeed = maxSpeed;
        this.wallPass = wallPass;
        this.nodeRadius = nodeRadius;
    }
}
