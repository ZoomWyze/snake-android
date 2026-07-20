package com.example.snake.ui.leaderboard;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snake.R;
import com.example.snake.data.db.entity.UserScoreDTO;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<UserScoreDTO> dataList;
    private static final int[] RANK_COLORS = {
            Color.parseColor("#FFD700"), // 金色
            Color.parseColor("#C0C0C0"), // 银色
            Color.parseColor("#CD7F32")  // 铜色
    };

    public LeaderboardAdapter(List<UserScoreDTO> dataList) {
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserScoreDTO item = dataList.get(position);
        int rank = position + 1;

        // 设置排名
        holder.tvRank.setText(String.valueOf(rank));
        if (rank <= 3) {
            holder.tvRank.setTextColor(RANK_COLORS[rank - 1]);
            holder.tvRank.setTextSize(28);
        } else {
            holder.tvRank.setTextColor(Color.parseColor("#1A1A1A"));
            holder.tvRank.setTextSize(20);
        }

        // 设置昵称
        holder.tvNickname.setText(item.getNickname());

        // 设置分数（空则显示0）
        int score = item.getMaxScore();
        holder.tvScore.setText(score == 0 ? "0" : String.valueOf(score));

        // 设置难度（空则显示"-"）
        String difficulty = item.getDifficulty();
        if (difficulty == null) {
            holder.tvDifficulty.setText("-");
        } else {
            holder.tvDifficulty.setText(getDifficultyChinese(difficulty));
        }

        // 为难度标签设置背景色（根据难度不同）
        setDifficultyTagBackground(holder.tvDifficulty, difficulty);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void updateData(List<UserScoreDTO> newData) {
        this.dataList.clear();
        this.dataList.addAll(newData);
        notifyDataSetChanged();
    }

    private String getDifficultyChinese(String difficulty) {
        if (difficulty == null) return "-";
        switch (difficulty) {
            case "easy":
                return "简单";
            case "medium":
                return "中等";
            case "hard":
                return "困难";
            default:
                return "-";
        }
    }

    private void setDifficultyTagBackground(TextView textView, String difficulty) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(8f);

        int bgColor;
        if (difficulty == null) {
            bgColor = Color.parseColor("#9E9E9E"); // 默认灰色
        } else {
            switch (difficulty) {
                case "easy":
                    bgColor = Color.parseColor("#4CAF50"); // 绿色
                    break;
                case "medium":
                    bgColor = Color.parseColor("#FF9800"); // 橙色
                    break;
                case "hard":
                    bgColor = Color.parseColor("#F44336"); // 红色
                    break;
                default:
                    bgColor = Color.parseColor("#9E9E9E"); // 默认灰色
                    break;
            }
        }

        drawable.setColor(bgColor);
        textView.setBackground(drawable);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;
        TextView tvNickname;
        TextView tvScore;
        TextView tvDifficulty;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvScore = itemView.findViewById(R.id.tv_score);
            tvDifficulty = itemView.findViewById(R.id.tv_difficulty);
        }
    }
}
