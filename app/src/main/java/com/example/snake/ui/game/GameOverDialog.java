package com.example.snake.ui.game;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.snake.R;

/**
 * 游戏结算弹窗。
 * <p>
 * 显示本次得分、新纪录标识（霓虹闪烁），提供"重新开始"和"返回主页"两个操作。
 * 弹窗不可点击外部关闭（setCancelable(false)）。
 */
public class GameOverDialog extends DialogFragment {

    private static final String ARG_SCORE = "score";
    private static final String ARG_IS_NEW_RECORD = "is_new_record";

    private OnGameOverListener listener;

    /**
     * 由 GameActivity 实现，处理按钮点击。
     */
    public interface OnGameOverListener {
        void onRestart();
        void onBackHome();
    }

    public static GameOverDialog newInstance(int score, boolean isNewRecord) {
        GameOverDialog dialog = new GameOverDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_SCORE, score);
        args.putBoolean(ARG_IS_NEW_RECORD, isNewRecord);
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnGameOverListener(OnGameOverListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            // 透明背景，让 XML 中的圆角 shape 可见
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        setCancelable(false); // 不可点击外部关闭
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_game_over, container, false);

        TextView tvScore     = view.findViewById(R.id.tv_score);
        TextView tvNewRecord = view.findViewById(R.id.tv_new_record);
        Button   btnRestart  = view.findViewById(R.id.btn_restart);
        Button   btnHome     = view.findViewById(R.id.btn_home);

        Bundle args = getArguments();
        if (args != null) {
            int score = args.getInt(ARG_SCORE, 0);
            boolean isNewRecord = args.getBoolean(ARG_IS_NEW_RECORD, false);

            tvScore.setText(String.valueOf(score));

            if (isNewRecord) {
                tvNewRecord.setVisibility(View.VISIBLE);
                startNeonBlink(tvNewRecord);
            } else {
                tvNewRecord.setVisibility(View.GONE);
            }
        }

        btnRestart.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onRestart();
        });

        btnHome.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onBackHome();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // 弹窗宽度 = 屏幕宽度 × 85%
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * 霓虹闪烁动画：透明度在 1.0 ↔ 0.15 之间循环，模拟霓虹灯闪烁效果。
     */
    private void startNeonBlink(TextView tv) {
        AlphaAnimation blink = new AlphaAnimation(1.0f, 0.15f);
        blink.setDuration(600);
        blink.setRepeatCount(Animation.INFINITE);
        blink.setRepeatMode(Animation.REVERSE);
        tv.startAnimation(blink);
    }
}
