package com.example.snake.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snake.R;
import com.example.snake.data.db.entity.GameRecord;
import com.example.snake.data.prefs.PrefsManager;
import com.example.snake.data.repository.GameRecordRepository;

import java.util.List;

/**
 * 历史记录页面
 * 显示当前用户的所有游戏对局记录，按时间倒序排列。
 */
public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TextView tvEmpty;
    private HistoryAdapter adapter;
    private GameRecordRepository repository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 初始化视图
        rvHistory = findViewById(R.id.rv_history);
        tvEmpty = findViewById(R.id.tv_empty_history);

        // 返回按钮点击事件
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 初始化 RecyclerView
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter();
        rvHistory.setAdapter(adapter);

        // 初始化 Repository
        repository = new GameRecordRepository(getApplication());

        // 获取当前用户 ID
        long userId = PrefsManager.getUserId();

        if (userId != -1L) {
            // 观察数据变化
            LiveData<List<GameRecord>> liveData = repository.getByUserId(userId);
            liveData.observe(this, records -> {
                if (records == null || records.isEmpty()) {
                    rvHistory.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvHistory.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                    adapter.submitList(records);
                }
            });
        } else {
            // 用户未登录（理论上不应发生，作为容错处理）
            rvHistory.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }
}
