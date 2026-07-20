package com.example.snake.engine;

import java.util.List;
import java.util.Random;

/**
 * 食物实体。
 * 在游戏区域内随机生成，与蛇身和其它食物不重叠。
 * 被蛇头触碰后触发得分和生长。
 */
public class Food {

    /** 食物中心 X 坐标（像素） */
    public float x;

    /** 食物中心 Y 坐标（像素） */
    public float y;

    /** 食物半径（像素） */
    public float radius;

    private final Random random;

    public Food() {
        this.random = new Random();
    }

    /**
     * 在游戏区域内随机生成食物。
     * 食物中心距边界 ≥ nodeRadius，且与蛇身所有节点、其它食物距离 ≥ nodeRadius * 2。
     * 最多尝试 100 次，若仍无法找到合法位置则放置在区域中心并返回 false。
     *
     * @param areaWidth   游戏区域宽度（像素）
     * @param areaHeight  游戏区域高度（像素）
     * @param nodeRadius  蛇身节点半径（像素）
     * @param snakeNodes  蛇身节点列表（用于碰撞检测）
     * @param otherFoods  已存在的其它食物列表（用于避让），可为 null
     * @return true 表示找到了合法位置
     */
    public boolean generate(int areaWidth, int areaHeight, float nodeRadius,
                            List<SnakeNode> snakeNodes, List<Food> otherFoods) {
        this.radius = nodeRadius;

        float usableWidth = areaWidth - 2 * nodeRadius;
        float usableHeight = areaHeight - 2 * nodeRadius;

        // 区域过小，直接放置在中心
        if (usableWidth <= 0 || usableHeight <= 0) {
            this.x = areaWidth / 2f;
            this.y = areaHeight / 2f;
            return true;
        }

        float minDistance = nodeRadius * 2;       // 最小允许距离（节点直径）
        float minDistanceSq = minDistance * minDistance;

        for (int attempt = 0; attempt < 100; attempt++) {
            float fx = nodeRadius + random.nextFloat() * usableWidth;
            float fy = nodeRadius + random.nextFloat() * usableHeight;

            boolean overlaps = false;

            // 检查与蛇身节点的重叠
            for (SnakeNode node : snakeNodes) {
                float dx = node.x - fx;
                float dy = node.y - fy;
                if (dx * dx + dy * dy < minDistanceSq) {
                    overlaps = true;
                    break;
                }
            }

            // 检查与其它食物的重叠
            if (!overlaps && otherFoods != null) {
                for (Food other : otherFoods) {
                    if (other == this) continue; // 跳过自身
                    float dx = other.x - fx;
                    float dy = other.y - fy;
                    if (dx * dx + dy * dy < minDistanceSq) {
                        overlaps = true;
                        break;
                    }
                }
            }

            if (!overlaps) {
                this.x = fx;
                this.y = fy;
                return true;
            }
        }

        // 100 次仍未找到合法位置，放置在中心作为兜底
        this.x = areaWidth / 2f;
        this.y = areaHeight / 2f;
        return false;
    }

    /**
     * 判断食物是否被蛇头吃掉。
     * 当蛇头中心与食物中心距离小于两者半径之和时判定为吃到。
     *
     * @param head 蛇头节点
     * @return true 表示被吃掉
     */
    public boolean isEaten(SnakeNode head) {
        float dx = head.x - this.x;
        float dy = head.y - this.y;
        float threshold = head.radius + this.radius;
        return (dx * dx + dy * dy) < (threshold * threshold);
    }
}