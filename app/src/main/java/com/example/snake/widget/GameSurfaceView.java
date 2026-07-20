package com.example.snake.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.snake.engine.Food;
import com.example.snake.engine.GameEngine;
import com.example.snake.engine.Snake;
import com.example.snake.engine.SnakeNode;
import com.example.snake.R;
import com.example.snake.util.GestureHelper;

import java.util.List;

/**
 * 游戏画面渲染视图。
 * 负责在子线程中驱动 GameEngine 并进行 Canvas 绘制。
 */
public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private GameEngine engine;
    private RenderThread renderThread;// 渲染线程
    private final GestureHelper gestureHelper;
    private Path snakePath;
    // 渲染相关 Paint
    private Paint paintBackground;
    private Paint paintSnakeBody;
    private Paint paintSnakeHead;
    private Paint paintSnakeStroke;
    private Paint paintFood;
    private Paint paintFoodGlow;

    // 是否正在运行
    private volatile boolean isRunning = false;

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        this.gestureHelper = new GestureHelper(context);
        initPaints();
    }

    public GameSurfaceView(Context context, GameEngine engine) {
        super(context);
        getHolder().addCallback(this);
        this.engine = engine;
        this.gestureHelper = new GestureHelper(context);
        initPaints();
    }

    private void initPaints() {
        // 抗锯齿
        paintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSnakeBody = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintSnakeHead = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintSnakeStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFood = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintFoodGlow = new Paint(Paint.ANTI_ALIAS_FLAG);

        // 背景
        paintBackground.setColor(Color.WHITE);

        // 蛇身：霓虹绿 #39FF14
        int colorSnake = ContextCompat.getColor(getContext(), R.color.neon_green);
        paintSnakeBody.setColor(colorSnake);
        paintSnakeHead.setColor(colorSnake);
        paintSnakeBody.setStrokeCap(Paint.Cap.ROUND);
        paintSnakeBody.setStrokeJoin(Paint.Join.ROUND);

        // 蛇头边框：白色
        paintSnakeStroke.setColor(Color.WHITE);
        paintSnakeStroke.setStyle(Paint.Style.STROKE);
        paintSnakeStroke.setStrokeWidth(4f);

        // 食物：霓虹粉 #FF6EC7
        int colorFood = ContextCompat.getColor(getContext(), R.color.neon_pink);
        paintFood.setColor(colorFood);

        // 食物发光：半透明霓虹粉
        int colorFoodSoft = ContextCompat.getColor(getContext(), R.color.neon_pink_soft);
        paintFoodGlow.setColor(colorFoodSoft);

        // ★ 复用 Path 对象，避免每帧创建
        snakePath = new Path();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        startRenderThread();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopRenderThread();
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // 尺寸变化时无需特殊处理，GameConfig 已在创建时固定
    }

    /**
     * 处理触摸事件，传递给 GestureHelper 解析方向
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float[] vector = gestureHelper.detectVector(event);
        if (vector != null && engine != null) {
            // 直接传递原始滑动向量，引擎内部归一化
            engine.setDirection(vector[0], vector[1]);
        }
        return true;
    }

    /**
     * 暂停渲染循环
     */
    public void pauseRendering() {
        stopRenderThread();
    }

    /**
     * 恢复渲染循环
     */
    public void resumeRendering() {
        if (renderThread == null || !renderThread.isAlive()) {
            if (getHolder().getSurface().isValid()) {
                startRenderThread();
            }
        }
    }

    /**
     * ★ 安全启动渲染线程
     */
    private void startRenderThread() {
        if (renderThread != null && renderThread.isAlive()) {
            // 已有线程在运行，不重复创建
            return;
        }
        isRunning = true;
        renderThread = new RenderThread();
        renderThread.start();
    }

    /**
     * 安全停止渲染线程：设置标志位 → 等待线程退出
     */
    private void stopRenderThread() {
        isRunning = false;
        if (renderThread != null) {
            try {
                renderThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            renderThread = null;
        }
    }
    /**
     * 渲染线程
     */
    private class RenderThread extends Thread {
        private long lastTime;

        @Override
        public void run() {
            lastTime = System.currentTimeMillis();
            while (isRunning) {
                long now = System.currentTimeMillis();
                long deltaTimeMs = now - lastTime;
                lastTime = now;

                // 1. 更新游戏逻辑
                if (engine != null) {
                    engine.update(deltaTimeMs);
                }

                // 2. 绘制画面
                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    try {
                        drawGame(canvas);
                    } finally {
                        getHolder().unlockCanvasAndPost(canvas);
                    }
                }

                // 3. 控制帧率 ~60fps
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 核心绘制方法
     */
    private void drawGame(Canvas canvas) {
        if (engine == null) return;

        // 1. 绘制背景
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paintBackground);

        // 2. 绘制食物（多个）
        List<Food> foods = engine.getFoods();
        if (foods != null) {
            for (Food food : foods) {
                if (food != null) {
                    // 发光效果
                    canvas.drawCircle(food.x, food.y, food.radius * 1.5f, paintFoodGlow);
                    // 实体
                    canvas.drawCircle(food.x, food.y, food.radius, paintFood);
                }
            }
        }

        // 3. 绘制蛇
        Snake snake = engine.getSnake();
        List<SnakeNode> nodes = snake.getNodes();
        if (nodes == null || nodes.isEmpty()) return;

        // 绘制连线 (节点之间连线)
        snakePath.reset();
        snakePath.moveTo(nodes.get(0).x, nodes.get(0).y);
        for (int i = 1; i < nodes.size(); i++) {
            SnakeNode node = nodes.get(i);
            canvas.drawCircle(node.x, node.y, node.radius, paintSnakeBody);
        }
        // 用 STROKE 模式画这条线，宽度等于节点直径 → 看起来像粗粗的蛇身
        paintSnakeBody.setStyle(Paint.Style.STROKE);
        paintSnakeBody.setStrokeWidth(nodes.get(0).radius * 2);
        canvas.drawPath(snakePath, paintSnakeBody);

        // 绘制节点 (覆盖连线上，确保圆润)
        paintSnakeBody.setStyle(Paint.Style.FILL);
        for (int i = 0; i < nodes.size(); i++) {
            SnakeNode node = nodes.get(i);
            canvas.drawCircle(node.x, node.y, node.radius, paintSnakeBody);
        }

        // 4. 绘制蛇头 (加白色边框)
        SnakeNode head = nodes.get(0);
        canvas.drawCircle(head.x, head.y, head.radius, paintSnakeHead);
        // 边框半径稍大一点，确保完全包裹
        canvas.drawCircle(head.x, head.y, head.radius, paintSnakeStroke);
    }
}
