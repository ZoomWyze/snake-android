package com.example.snake.ui.game;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.example.snake.R;

/**
 * 游戏暂停遮罩层
 */
public class PauseOverlayView extends FrameLayout {

    private Button btnResume;
    private Button btnQuit;
    private OnPauseActionListener listener;

    public interface OnPauseActionListener {
        void onResume();
        void onQuit();
    }

    public PauseOverlayView(Context context) {
        super(context);
        init(context);
    }

    public PauseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // 动态加载布局
        LayoutInflater.from(context).inflate(R.layout.dialog_pause, this, true);

        btnResume = findViewById(R.id.btn_resume);
        btnQuit = findViewById(R.id.btn_quit);

        btnResume.setOnClickListener(v -> {
            if (listener != null) {
                listener.onResume();
            }
        });

        btnQuit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuit();
            }
        });

        // 初始状态隐藏
        setVisibility(GONE);
    }

    public void setOnPauseActionListener(OnPauseActionListener listener) {
        this.listener = listener;
    }
}
