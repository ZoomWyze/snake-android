package com.example.snake.ui.history;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snake.R;
import com.example.snake.data.db.entity.GameRecord;
import com.example.snake.util.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 历史记录列表适配器
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<GameRecord> mList = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(List<GameRecord> list) {
        this.mList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRecord record = mList.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvScore;
        TextView tvTime;
        TextView tvMode;
        TextView tvDifficulty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMode = itemView.findViewById(R.id.tv_mode);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
        }

        public void bind(GameRecord record) {
            // 1. 设置得分（大号霓虹色）
            tvScore.setText(String.valueOf(record.getScore()));
            tvScore.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.neon_green));

            // 2. 设置时间 (yyyy-MM-dd HH:mm)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String timeStr = sdf.format(new Date(record.getPlayedAt()));
            tvTime.setText(timeStr);

            // 3. 设置模式
            String modeKey = record.getMode();
            String modeDisplay;
            if (Constants.MODE_WALL_PASS.equals(modeKey)) {
                modeDisplay = "穿墙模式";
            } else if (Constants.MODE_NO_WALL.equals(modeKey)) {
                modeDisplay = "不穿墙模式";
            } else {
                modeDisplay = "未知模式";
            }
            tvMode.setText(modeDisplay);

            // 4. 设置难度
            String diffKey = record.getDifficulty();
            String diffDisplay;
            int bgColorRes; // 根据难度改变标签背景色（可选优化）

            if (Constants.DIFFICULTY_EASY.equals(diffKey)) {
                diffDisplay = "简单";
                bgColorRes = R.color.colorSecondary; // 灰色系
            } else if (Constants.DIFFICULTY_MEDIUM.equals(diffKey)) {
                diffDisplay = "中等";
                bgColorRes = R.color.colorPrimary; // 蓝色系
            } else if (Constants.DIFFICULTY_HARD.equals(diffKey)) {
                diffDisplay = "困难";
                bgColorRes = R.color.neon_pink; // 霓虹粉
            } else {
                diffDisplay = "未知";
                bgColorRes = R.color.colorSecondary;
            }
            tvDifficulty.setText(diffDisplay);
            tvDifficulty.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), bgColorRes));
        }
    }
}
