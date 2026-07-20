package com.example.snake.ui.leaderboard;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snake.R;
import com.example.snake.data.db.entity.UserScoreDTO;
import com.example.snake.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private LeaderboardAdapter adapter;
    private TextView emptyView;
    private RecyclerView recyclerView;
    private ImageView ivBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // 初始化视图
        recyclerView = findViewById(R.id.rv_leaderboard);
        emptyView = findViewById(R.id.tv_empty_leaderboard);
        ivBack = findViewById(R.id.iv_back);

        // 设置 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 获取 UserRepository 实例
        userRepository = new UserRepository(getApplication());

        // 观察排行榜数据
        userRepository.getLeaderboard().observe(this, this::updateUI);

        // 返回按钮
        ivBack.setOnClickListener(v -> finish());
    }

    private void updateUI(List<UserScoreDTO> leaderboard) {
        adapter.updateData(leaderboard);
        if (leaderboard == null || leaderboard.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
