package com.example.snake.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * dp / px 单位转换工具类
 * 适配不同屏幕密度
 */
public final class DpPxUtil {

    private DpPxUtil() {
        // 工具类禁止实例化
    }

    /**
     * dp 转 px
     * 使用系统 displayMetrics，保证与 Android 渲染流程一致
     *
     * @param context 上下文
     * @param dp      dp 值
     * @return 对应的 px 值（向下取整为 int）
     */
    public static int dp2px(Context context, float dp) {
        if (context == null) {
            return (int) dp;
        }
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    /**
     * px 转 dp
     *
     * @param context 上下文
     * @param px      px 值
     * @return 对应的 dp 值
     */
    public static float px2dp(Context context, float px) {
        if (context == null) {
            return px;
        }
        return px / context.getResources().getDisplayMetrics().density;
    }
}
