package com.example.snake.util;

import android.content.Context;
import android.view.MotionEvent;

/**
 * 手势识别辅助类。
 * 检测滑动手势并转换为方向向量 [dx, dy]。
 *
 */
public class GestureHelper {

    // 最小滑动距离（像素），低于此值不触发方向变更
    private static final float MIN_SWIPE_DISTANCE_PX = 20f;

    private float startX;
    private float startY;
    private boolean isTracking;

    public GestureHelper(Context context) {
        reset();
    }

    public float[] detectVector(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录手指按下的起始坐标
                startX = event.getX();
                startY = event.getY();
                isTracking = true;
                return null;

            case MotionEvent.ACTION_MOVE:
                if (!isTracking) return null;

                float moveDx = event.getX() - startX;
                float moveDy = event.getY() - startY;
                float distance = (float) Math.sqrt(moveDx * moveDx + moveDy * moveDy);

                if (distance >= MIN_SWIPE_DISTANCE_PX) {
                    // ★ 关键：重置起点为当前位置（增量式）
                    startX = event.getX();
                    startY = event.getY();
                    return new float[]{moveDx, moveDy};
                }
                return null;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!isTracking) return null;

                float upDx = event.getX() - startX;
                float upDy = event.getY() - startY;
                float upDistance = (float) Math.sqrt(upDx * upDx + upDy * upDy);

                // 抬手时也检查阈值
                if (upDistance >= MIN_SWIPE_DISTANCE_PX) {
                    reset();
                    return new float[]{upDx, upDy};
                }
                reset();
                return null;

            default:
                return null;
        }
    }

    private void reset() {
        isTracking = false;
        startX = 0;
        startY = 0;
    }
}