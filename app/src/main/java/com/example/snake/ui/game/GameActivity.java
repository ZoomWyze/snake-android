package com.example.snake.ui.game;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.snake.R;
import com.example.snake.data.prefs.PrefsManager;
import com.example.snake.engine.GameConfig;
import com.example.snake.engine.GameEngine;
import com.example.snake.engine.GameEventListener;
import com.example.snake.util.Constants;
import com.example.snake.util.DpPxUtil;
import com.example.snake.widget.GameSurfaceView;
import com.example.snake.data.repository.GameRecordRepository;
import com.example.snake.data.db.entity.GameRecord;
import com.example.snake.audio.GameAudioManager;


/**
 * 游戏主界面。
 * 负责初始化配置、引擎、渲染视图及UI交互。
 */
public class GameActivity extends AppCompatActivity {

    private GameEngine engine;
    private GameSurfaceView surfaceView;
    private TextView tvScore;
    private Button btnPause;
    private View topBar;
    private PauseOverlayView pauseOverlay;
    private GameRecordRepository recordRepository;
    private boolean isGameOverDialogShowing = false; // 防止重复弹窗
    private boolean wasAutoPaused = false; // 标记是否因切后台而自动暂停

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 设置全屏沉浸式
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            // 隐藏系统栏
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                controller.hide(WindowInsets.Type.systemBars());
            }
            // 设置系统栏行为
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            // 降级处理
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_game);

        // 初始化音频
        GameAudioManager.getInstance().init(this);
        GameAudioManager.getInstance().startBgm();

        // 初始化数据仓库
        recordRepository = new GameRecordRepository(getApplication());

        // 2. 初始化视图
        tvScore = findViewById(R.id.tv_score);
        btnPause = findViewById(R.id.btn_pause);
        topBar = findViewById(R.id.top_bar);
        FrameLayout gameContainer = findViewById(R.id.game_container);

        // 初始化暂停遮罩层并添加到容器中，确保覆盖在 SurfaceView 之上
        pauseOverlay = new PauseOverlayView(this);
        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        gameContainer.addView(pauseOverlay, overlayParams);

        // 设置遮罩层监听
        pauseOverlay.setOnPauseActionListener(new PauseOverlayView.OnPauseActionListener() {
            @Override
            public void onResume() {
                resumeGame();
            }

            @Override
            public void onQuit() {
                exitGame();
            }
        });

        // 3. 等待布局完成，获取准确的容器尺寸
        gameContainer.post(() -> {
            int containerWidth = gameContainer.getWidth();
            int containerHeight = gameContainer.getHeight();

            int paddingLeft = gameContainer.getPaddingLeft();
            int paddingRight = gameContainer.getPaddingRight();
            int paddingTop = gameContainer.getPaddingTop();
            int paddingBottom = gameContainer.getPaddingBottom();

            int areaWidth = containerWidth - paddingLeft - paddingRight;
            int areaHeight = containerHeight - paddingTop - paddingBottom;

            if (areaWidth <= 0 || areaHeight <= 0) {
                areaWidth = containerWidth;
                areaHeight = containerHeight;
            }

            initGame(areaWidth, areaHeight);
        });

        // 7. UI 交互：点击暂停按钮
        btnPause.setOnClickListener(v -> {
            if (engine != null) {
                // 如果当前正在运行，则手动暂停
                // 如果已经暂停（比如已经显示了遮罩），则不处理，或者可以逻辑为点击遮罩上的继续
                // 这里按照需求：点击暂停按钮 -> 暂停并显示遮罩
                if (!engine.isPaused() && !engine.isGameOver()) {
                    manualPause();
                }
            }
        });

        // 顶部信息栏拦截触摸事件，防止误触游戏控制
        topBar.setOnTouchListener((v, event) -> true);
    }

    /**
     * 初始化游戏引擎和视图
     */
    private void initGame(int areaWidth, int areaHeight) {
        String modeStr = PrefsManager.getSelectedMode();
        String diffStr = PrefsManager.getSelectedDifficulty();
        boolean isWallPass = Constants.MODE_WALL_PASS.equals(modeStr);

        int[] speedParams = getSpeedParams(diffStr);

        // 把 dp 单位转成像素（适配不同屏幕密度）
        float initialSpeed = DpPxUtil.dp2px(this, speedParams[0]);
        float speedIncrement = DpPxUtil.dp2px(this, speedParams[1]);
        float maxSpeed = DpPxUtil.dp2px(this, speedParams[2]);

        float nodeRadius = DpPxUtil.dp2px(this, Constants.NODE_DIAMETER_DP) / 2f;
        //  创建游戏配置对象（把所有参数打包）
        GameConfig config = new GameConfig(
                areaWidth, areaHeight,
                initialSpeed, speedIncrement, maxSpeed,
                isWallPass,
                nodeRadius
        );

        engine = new GameEngine(config);
        // 设置引擎回调
        engine.setListener(new GameEventListener() {
            @Override
            public void onFoodEaten(int newScore) {
                GameAudioManager.getInstance().playEatSound();
            }

            @Override
            public void onGameOver(int finalScore) {
                runOnUiThread(() -> {
                    GameAudioManager.getInstance().playDeathSound();
                    GameAudioManager.getInstance().pauseBgm();
                    // 游戏结束隐藏暂停遮罩（防止叠加）
                    if (pauseOverlay != null) pauseOverlay.setVisibility(View.GONE);

                    if (isGameOverDialogShowing) return;
                    isGameOverDialogShowing = true;

                    long userId = PrefsManager.getUserId();
                    String currentModeStr = PrefsManager.getSelectedMode();
                    String currentDiffStr = PrefsManager.getSelectedDifficulty();
                    // 查询历史最高分，判断是否新纪录
                    recordRepository.getMaxScoreForCompare(userId, currentMax -> {
                        int prevMax = (currentMax == null) ? 0 : currentMax;
                        boolean isNewRecord = finalScore > prevMax;

                        GameRecord record = new GameRecord();
                        record.setUserId(userId);
                        record.setScore(finalScore);
                        record.setMode(currentModeStr);
                        record.setDifficulty(currentDiffStr);
                        record.setPlayedAt(System.currentTimeMillis());

                        recordRepository.insert(record, new GameRecordRepository.InsertCallback() {
                            @Override
                            public void onInserted() {
                                showGameOverDialog(finalScore, isNewRecord);
                            }

                            @Override
                            public void onError(Throwable t) {
                                showGameOverDialog(finalScore, isNewRecord);
                            }
                        });
                    });
                });
            }

            @Override
            public void onScoreChanged(int score) {
                runOnUiThread(() -> tvScore.setText("分数：" + score));
            }
        });
        // 创建渲染视图，把它添加到容器最底层
        surfaceView = new GameSurfaceView(this, engine);
        FrameLayout gameContainer = findViewById(R.id.game_container);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        surfaceView.setLayoutParams(params);
        // 将 SurfaceView 添加到 PauseOverlay 下方，或者直接 addView (ViewGroup 会按添加顺序绘制)
        // 我们先添加了 pauseOverlay，所以这里应该把 surfaceView 添加到底部，或者把 pauseOverlay 移到顶部
        // 简单做法：将 SurfaceView 添加到 index 0，确保它在遮罩下面
        gameContainer.addView(surfaceView, 0, params);
    }

    /**
     * 手动暂停逻辑
     */
    private void manualPause() {
        if (engine != null) engine.pause();
        if (surfaceView != null) surfaceView.pauseRendering();
        if (pauseOverlay != null) pauseOverlay.setVisibility(View.VISIBLE);
        btnPause.setText("▶"); // 可选：视觉反馈
        GameAudioManager.getInstance().pauseBgm();
    }

    /**
     * 恢复游戏逻辑
     */
    private void resumeGame() {
        if (engine != null) engine.resume();
        if (surfaceView != null) surfaceView.resumeRendering();
        if (pauseOverlay != null) pauseOverlay.setVisibility(View.GONE);
        btnPause.setText("⏸");
        wasAutoPaused = false; // 恢复后重置自动暂停标记，防止冲突
        GameAudioManager.getInstance().resumeBgm();
    }

    /**
     * 退出游戏逻辑
     */
    private void exitGame() {
        finish();
    }

    private void showGameOverDialog(int score, boolean isNewRecord) {
        GameOverDialog dialog = GameOverDialog.newInstance(score, isNewRecord);
        dialog.setOnGameOverListener(new GameOverDialog.OnGameOverListener() {
            @Override
            public void onRestart() {
                isGameOverDialogShowing = false;
                restartGame();
            }

            @Override
            public void onBackHome() {
                isGameOverDialogShowing = false;
                finish();
            }
        });
        dialog.show(getSupportFragmentManager(), "game_over");
    }

    private void restartGame() {
        if (engine == null) return;

        engine.reset();
        tvScore.setText("分数：0");

        if (surfaceView != null) {
            surfaceView.resumeRendering();
        }

        engine.resume();
        GameAudioManager.getInstance().resumeBgm();

        btnPause.setText("⏸");
        // 确保遮罩隐藏
        if (pauseOverlay != null) pauseOverlay.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        GameAudioManager.getInstance().pauseBgm();
        // 如果游戏正在进行中（未结束且未暂停），则自动暂停
        if (engine != null && !engine.isGameOver() && !engine.isPaused()) {
            engine.pause();
            if (surfaceView != null) surfaceView.pauseRendering();
            wasAutoPaused = true; // 标记为自动暂停
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (surfaceView != null) {
            surfaceView.resumeRendering();
        }

        if (wasAutoPaused) {
            // 切后台回来：显示遮罩让用户选择，BGM 不自动恢复
            if (pauseOverlay != null) pauseOverlay.setVisibility(View.VISIBLE);
            wasAutoPaused = false;
        } else {
            // 非自动暂停场景：若遮罩未显示（即非手动暂停状态），恢复 BGM
            if (pauseOverlay == null || pauseOverlay.getVisibility() != View.VISIBLE) {
                GameAudioManager.getInstance().resumeBgm();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        GameAudioManager.getInstance().stopBgm();
        GameAudioManager.getInstance().release();
    }



    private int[] getSpeedParams(String difficulty) {
        switch (difficulty) {
            case Constants.DIFFICULTY_EASY:
                return Constants.DIFFICULTY_SPEED_PARAMS[0];
            case Constants.DIFFICULTY_HARD:
                return Constants.DIFFICULTY_SPEED_PARAMS[2];
            case Constants.DIFFICULTY_MEDIUM:
            default:
                return Constants.DIFFICULTY_SPEED_PARAMS[1];
        }
    }
}
