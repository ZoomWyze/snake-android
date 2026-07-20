package com.example.snake.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.snake.R;
import com.example.snake.data.prefs.PrefsManager;
import com.example.snake.data.repository.GameRecordRepository;
import com.example.snake.data.repository.UserRepository;
import com.example.snake.ui.game.GameActivity;
//import com.example.snake.ui.history.HistoryActivity;
//import com.example.snake.ui.leaderboard.LeaderboardActivity;
import com.example.snake.ui.history.HistoryActivity;
import com.example.snake.ui.leaderboard.LeaderboardActivity;
import com.example.snake.ui.login.LoginActivity;
import com.example.snake.ui.settings.SettingsActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvHighScore;
    private Button btnStartGame, btnSettings, btnHistory, btnLeaderboard, btnLogout;
    private UserRepository userRepository;
    private GameRecordRepository gameRecordRepository;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 初始化仓库
        userRepository = new UserRepository(getApplication());
        gameRecordRepository = new GameRecordRepository(getApplication());

        // 获取当前用户ID
        currentUserId = PrefsManager.getUserId();
        if (currentUserId == -1L) {
            // 未登录，跳转登录页
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadUserData();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tv_welcome);
        tvHighScore = findViewById(R.id.tv_high_score);
        btnStartGame = findViewById(R.id.btn_start_game);
        btnSettings = findViewById(R.id.btn_settings);
        btnHistory = findViewById(R.id.btn_history);
        btnLeaderboard = findViewById(R.id.btn_leaderboard);
        btnLogout = findViewById(R.id.btn_logout);
    }

    private void setupListeners() {
        btnStartGame.setOnClickListener(v -> {
            // 跳转游戏页
            startActivity(new Intent(HomeActivity.this, GameActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            // 跳转设置页
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });

        btnHistory.setOnClickListener(v -> {
            // 跳转历史记录页
            startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
        });

        btnLeaderboard.setOnClickListener(v -> {
            // 跳转排行榜页
            startActivity(new Intent(HomeActivity.this, LeaderboardActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            // 清除登录状态
            PrefsManager.setUserId(HomeActivity.this, -1L);
            // 跳转登录页
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        // 加载用户昵称
        userRepository.findById(currentUserId, new UserRepository.UserQueryCallback() {
            @Override
            public void onResult(com.example.snake.data.db.entity.User user) {
                if (user != null) {
                    tvWelcome.setText(user.getNickname() + "，欢迎回来");
                } else {
                    tvWelcome.setText("欢迎回来");
                }
            }
        });

        // 加载最高分
        gameRecordRepository.getMaxScore(currentUserId).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer maxScore) {
                if (maxScore != null && maxScore > 0) {
                    tvHighScore.setText("个人最高分：" + maxScore);
                } else {
                    tvHighScore.setText("个人最高分：0");
                }
            }
        });
    }
}
