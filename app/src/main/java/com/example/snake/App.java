package com.example.snake;

import android.app.Application;

import com.example.snake.data.prefs.PrefsManager;

/**
 * Application 入口类
 * 用于在应用启动时初始化全局组件（如 PrefsManager）
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化 PrefsManager，使其内部持有全局 Application 引用
        PrefsManager.init(this);
    }
}
