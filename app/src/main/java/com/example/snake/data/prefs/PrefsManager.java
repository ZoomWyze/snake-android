package com.example.snake.data.prefs;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * SharedPreferences 统一管理（文件名 "snake_prefs"）。
 *
 * 使用方式：
 *   1. 在自定义 Application（如 App.java）的 onCreate() 中调用 {@link #init(Application)}
 *   2. 之后所有 getter 可无参调用（内部使用 sApp），setter 接受 Context
 *   3. setter 也接受 Context 参数，以便在任何 UI 组件中调用
 *
 * 默认值：
 *   - userId              = -1L
 *   - bgm_enabled         = true
 *   - bgm_volume          = 0.7f
 *   - sfx_enabled         = true
 *   - sfx_volume          = 0.7f
 *   - selected_mode       = "no_wall_pass"
 *   - selected_difficulty = "medium"
 */
public class PrefsManager {

    private static final String PREFS_NAME = "snake_prefs";

    private static final String KEY_USER_ID = "current_user_id";
    private static final String KEY_BGM_ENABLED = "bgm_enabled";
    private static final String KEY_BGM_VOLUME = "bgm_volume";
    private static final String KEY_SFX_ENABLED = "sfx_enabled";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_SELECTED_MODE = "selected_mode";
    private static final String KEY_SELECTED_DIFFICULTY = "selected_difficulty";

    // 默认值
    private static final long DEFAULT_USER_ID = -1L;
    private static final boolean DEFAULT_BGM_ENABLED = true;
    private static final float DEFAULT_BGM_VOLUME = 0.7f;
    private static final boolean DEFAULT_SFX_ENABLED = true;
    private static final float DEFAULT_SFX_VOLUME = 0.7f;
    private static final String DEFAULT_MODE = "no_wall_pass";
    private static final String DEFAULT_DIFFICULTY = "medium";

    // 全局 Application 引用（用于无参 getter）
    private static volatile Application sApp;

    private PrefsManager() {
        // 工具类，禁止实例化
    }

    /**
     * 在 Application.onCreate() 中初始化，使无参 getter 可用。
     */
    public static void init(@NonNull Application app) {
        if (sApp == null) {
            synchronized (PrefsManager.class) {
                if (sApp == null) {
                    sApp = app;
                }
            }
        }
    }

    private static SharedPreferences getPrefs(@Nullable Context context) {
        Context ctx = (context != null) ? context.getApplicationContext() : sApp;
        if (ctx == null) {
            throw new IllegalStateException(
                    "PrefsManager not initialized. Call PrefsManager.init(Application) first " +
                            "or pass a non-null Context."
            );
        }
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ------------------------------------------------------------------
    // 用户登录态
    // ------------------------------------------------------------------

    public static long getUserId() {
        return getPrefs(null).getLong(KEY_USER_ID, DEFAULT_USER_ID);
    }

    public static void setUserId(@NonNull Context context, long userId) {
        getPrefs(context).edit().putLong(KEY_USER_ID, userId).apply();
    }

    public static void clearUserId(@NonNull Context context) {
        getPrefs(context).edit().remove(KEY_USER_ID).apply();
    }

    public static boolean isLoggedIn() {
        return getUserId() != DEFAULT_USER_ID;
    }

    // ------------------------------------------------------------------
    // 背景音乐
    // ------------------------------------------------------------------

    public static boolean isBgmEnabled() {
        return getPrefs(null).getBoolean(KEY_BGM_ENABLED, DEFAULT_BGM_ENABLED);
    }

    public static void setBgmEnabled(@NonNull Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_BGM_ENABLED, enabled).apply();
    }

    public static float getBgmVolume() {
        return getPrefs(null).getFloat(KEY_BGM_VOLUME, DEFAULT_BGM_VOLUME);
    }

    public static void setBgmVolume(@NonNull Context context, float volume) {
        getPrefs(context).edit().putFloat(KEY_BGM_VOLUME, clampVolume(volume)).apply();
    }

    // ------------------------------------------------------------------
    // 游戏音效
    // ------------------------------------------------------------------

    public static boolean isSfxEnabled() {
        return getPrefs(null).getBoolean(KEY_SFX_ENABLED, DEFAULT_SFX_ENABLED);
    }

    public static void setSfxEnabled(@NonNull Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_SFX_ENABLED, enabled).apply();
    }

    public static float getSfxVolume() {
        return getPrefs(null).getFloat(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME);
    }

    public static void setSfxVolume(@NonNull Context context, float volume) {
        getPrefs(context).edit().putFloat(KEY_SFX_VOLUME, clampVolume(volume)).apply();
    }

    // ------------------------------------------------------------------
    // 模式 / 难度
    // ------------------------------------------------------------------

    public static String getSelectedMode() {
        return getPrefs(null).getString(KEY_SELECTED_MODE, DEFAULT_MODE);
    }

    public static void setSelectedMode(@NonNull Context context, @NonNull String mode) {
        getPrefs(context).edit().putString(KEY_SELECTED_MODE, mode).apply();
    }

    public static String getSelectedDifficulty() {
        return getPrefs(null).getString(KEY_SELECTED_DIFFICULTY, DEFAULT_DIFFICULTY);
    }

    public static void setSelectedDifficulty(@NonNull Context context, @NonNull String difficulty) {
        getPrefs(context).edit().putString(KEY_SELECTED_DIFFICULTY, difficulty).apply();
    }

    // ------------------------------------------------------------------
    // 辅助
    // ------------------------------------------------------------------

    private static float clampVolume(float v) {
        if (v < 0f) return 0f;
        if (v > 1f) return 1f;
        return v;
    }
}
