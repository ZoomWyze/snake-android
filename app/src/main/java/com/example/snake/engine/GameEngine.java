package com.example.snake.engine;

import com.example.snake.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏核心引擎。
 * <p>
 * 负责管理蛇、食物（多个）、分数、碰撞检测和游戏状态。
 * 由外部游戏循环（如 SurfaceView 子线程）驱动，每帧调用 update()。
 * 蛇的移动方向使用单位向量 (dx, dy) 控制，支持任意角度自由移动。
 * <p>
 */
public class GameEngine {

    // ================================================================
    // 游戏状态
    // ================================================================

    private Snake snake;

    /** 多个食物实体 */
    private List<Food> foods;

    private final GameConfig config;

    /** 当前分数 */
    private int score;

    /** 已吃食物数量（用于速度递增计算） */
    private int eatenCount;

    /** 游戏是否结束 */
    private boolean gameOver;

    /** 游戏是否暂停 */
    private boolean paused;

    /** 事件回调监听器 */
    private GameEventListener listener;

    // ================================================================
    // 构造与初始化
    // ================================================================

    /**
     * 构造游戏引擎，初始化蛇和多个食物。
     * 蛇初始位于游戏区域中央，长度 3 节，朝右。
     *
     * @param config 游戏配置
     */
    public GameEngine(GameConfig config) {
        this.config = config;
        this.score = 0;
        this.eatenCount = 0;
        this.gameOver = false;
        this.paused = false;
        this.listener = null;

        // 蛇初始位置：区域中央
        float startX = config.areaWidth / 2f;
        float startY = config.areaHeight / 2f;
        this.snake = new Snake(startX, startY, config.nodeRadius);

        // 初始化多个食物
        this.foods = new ArrayList<>();
        generateAllFoods();
    }

    /**
     * 生成所有食物。每个食物避开蛇身和已生成的其它食物。
     */
    private void generateAllFoods() {
        foods.clear();
        for (int i = 0; i < Constants.FOOD_COUNT; i++) {
            Food f = new Food();
            f.generate(config.areaWidth, config.areaHeight,
                    config.nodeRadius, snake.getNodes(), foods);
            foods.add(f);
        }
    }

    /**
     * 设置事件监听器。
     *
     * @param listener 监听器实例
     */
    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    // ================================================================
    // 主更新逻辑
    // ================================================================

    /**
     * 每帧更新游戏状态。
     * 计算本帧移动距离，移动蛇，并依次检测食物碰撞、墙壁碰撞和自身碰撞。
     *
     * @param deltaTimeMs 距离上一帧的时间间隔（毫秒）
     */
    public void update(long deltaTimeMs) {
        if (paused || gameOver) {
            return;
        }

        // 计算当前速度（像素/秒）
        float speed = config.initialSpeed + eatenCount * config.speedIncrement;
        speed = Math.min(speed, config.maxSpeed);

        // 转换为本帧移动距离（像素）
        float distance = speed * deltaTimeMs / 1000f;

        // 移动蛇
        snake.move(distance);

        // 碰撞检测（顺序：墙壁 → 自身→ 食物 ）
        checkWallCollision();
        checkSelfCollision();
        checkFoodCollision();

    }

    // ================================================================
    // 碰撞检测
    // ================================================================

    /**
     * 检测蛇头是否吃到任一食物。
     * 吃到后：分数 +10，吃食计数 +1，蛇生长，该食物重新生成。
     * 每帧最多处理一次吃食事件。
     */
    private void checkFoodCollision() {
        for (int i = 0; i < foods.size(); i++) {
            if (foods.get(i).isEaten(snake.getHead())) {
                score += Constants.FOOD_SCORE;
                eatenCount++;
                snake.grow();

                // 构建「除当前食物外」的列表，用于重新生成时避让
                List<Food> others = new ArrayList<>(foods);
                others.remove(i);

                boolean generated = foods.get(i).generate(
                        config.areaWidth, config.areaHeight,
                        config.nodeRadius, snake.getNodes(), others);

                if (!generated) {
                    // 无法生成新食物（极端情况），游戏结束
                    gameOver = true;
                    if (listener != null) {
                        listener.onFoodEaten(score);
                        listener.onScoreChanged(score);
                        listener.onGameOver(score);
                    }
                    return;
                }

                if (listener != null) {
                    listener.onFoodEaten(score);
                    listener.onScoreChanged(score);
                }
                break; // 每帧只处理一次吃食
            }
        }
    }

    /**
     * 检测蛇头与墙壁的碰撞。
     * <p>
     * 穿墙模式：蛇头越过边界时 wrap 到对侧，同步平移所有轨迹点保持连续。
     * 不穿墙模式：蛇头越界即判定游戏结束。
     */
    private void checkWallCollision() {
        SnakeNode head = snake.getHead();

        if (config.wallPass) {
            float dx = 0;
            float dy = 0;

            if (head.x < 0) {
                dx = config.areaWidth;
            } else if (head.x > config.areaWidth) {
                dx = -config.areaWidth;
            }

            if (head.y < 0) {
                dy = config.areaHeight;
            } else if (head.y > config.areaHeight) {
                dy = -config.areaHeight;
            }

            if (dx != 0 || dy != 0) {
                snake.applyWrap(dx, dy);
            }
        } else {
            if (head.x < 0 || head.x > config.areaWidth
                    || head.y < 0 || head.y > config.areaHeight) {
                gameOver = true;
                if (listener != null) {
                    listener.onGameOver(score);
                }
            }
        }
    }

    /**
     * 检测蛇头与蛇身的碰撞。
     * 跳过蛇头附近前 3 个节点，若蛇头与任一节点中心距离小于节点直径，判定碰撞。
     */
    private void checkSelfCollision() {
        SnakeNode head = snake.getHead();
        float diameter = head.radius * 2;
        float diameterSq = diameter * diameter;

        for (SnakeNode node : snake.getBodyForCollisionCheck()) {
            float dx = head.x - node.x;
            float dy = head.y - node.y;
            if (dx * dx + dy * dy < diameterSq) {
                gameOver = true;
                if (listener != null) {
                    listener.onGameOver(score);
                }
                return;
            }
        }
    }

    // ================================================================
    // 控制
    // ================================================================

    /**
     * 设置蛇移动方向（任意角度）。
     * 传入滑动向量的 dx/dy 分量，内部归一化后设置为移动方向。
     *
     * @param dx 水平分量（右为正）
     * @param dy 垂直分量（下为正）
     */
    public void setDirection(float dx, float dy) {
        snake.setDirection(dx, dy);
    }

    /** 暂停游戏 */
    public void pause() {
        paused = true;
    }

    /** 恢复游戏 */
    public void resume() {
        paused = false;
    }

    /**
     * 重置游戏到初始状态。
     * 分数归零，蛇回到区域中央，所有食物重新生成。
     */
    public void reset() {
        score = 0;
        eatenCount = 0;
        gameOver = false;
        paused = false;

        float startX = config.areaWidth / 2f;
        float startY = config.areaHeight / 2f;
        snake.reset(startX, startY);
        generateAllFoods();
    }

    // ================================================================
    // 状态查询
    // ================================================================

    public Snake getSnake() {
        return snake;
    }

    /**
     * 获取所有食物列表（用于渲染多个食物）。
     */
    public List<Food> getFoods() {
        return foods;
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return paused;
    }

}