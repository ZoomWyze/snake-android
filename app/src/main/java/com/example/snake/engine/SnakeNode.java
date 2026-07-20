package com.example.snake.engine;

/**
 * 蛇身节点纯数据类。
 * 每个节点可视为一个圆形区域，用于碰撞检测和渲染。
 */
public class SnakeNode {

    /** 节点中心 X 坐标（像素） */
    public float x;

    /** 节点中心 Y 坐标（像素） */
    public float y;

    /** 节点半径（像素） */
    public float radius;

    public SnakeNode(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
}
