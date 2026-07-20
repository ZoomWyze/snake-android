package com.example.snake.ui.settings;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.snake.R;
import com.example.snake.data.prefs.PrefsManager;
import com.example.snake.util.Constants;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * 游戏设置Activity
 * 提供游戏模式、难度和音频设置
 * 所有设置即时保存到SharedPreferences
 */
public class SettingsActivity extends AppCompatActivity {

    // UI 组件
    private RadioGroup rgMode;
    private RadioGroup rgDifficulty;
    private SwitchMaterial switchBgm;
    private SwitchMaterial switchSfx;
    private SeekBar seekbarBgmVolume;
    private SeekBar seekbarSfxVolume;
    private TextView tvBgmVolumeValue;
    private TextView tvSfxVolumeValue;
    private TextView tvModeDescription;
    private TextView tvDifficultyParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        setupListeners();
        loadSettings();
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 游戏模式
        rgMode = findViewById(R.id.rg_mode);
        tvModeDescription = findViewById(R.id.tv_mode_description);

        // 游戏难度
        rgDifficulty = findViewById(R.id.rg_difficulty);
        tvDifficultyParams = findViewById(R.id.tv_difficulty_params);

        // 音频设置
        switchBgm = findViewById(R.id.switch_bgm);
        switchSfx = findViewById(R.id.switch_sfx);
        seekbarBgmVolume = findViewById(R.id.seekbar_bgm_volume);
        seekbarSfxVolume = findViewById(R.id.seekbar_sfx_volume);
        tvBgmVolumeValue = findViewById(R.id.tv_bgm_volume_value);
        tvSfxVolumeValue = findViewById(R.id.tv_sfx_volume_value);
    }

    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 游戏模式选择监听
        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedMode;
            if (checkedId == R.id.rb_wall_pass) {
                selectedMode = Constants.MODE_WALL_PASS;
                tvModeDescription.setText("蛇头触碰边界后从对侧出现，游戏继续");
            } else {
                selectedMode = Constants.MODE_NO_WALL;
                tvModeDescription.setText("蛇头触碰边界即游戏结束");
            }
            PrefsManager.setSelectedMode(this, selectedMode);
        });

        // 游戏难度选择监听
        rgDifficulty.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedDifficulty;
            int[] speedParams;

            if (checkedId == R.id.rb_easy) {
                selectedDifficulty = Constants.DIFFICULTY_EASY;
                speedParams = Constants.DIFFICULTY_SPEED_PARAMS[0];
                tvDifficultyParams.setText(String.format(
                        "初始速度: %d dp/s | 加速量: +%d dp/s | 最大速度: %d dp/s",
                        speedParams[0], speedParams[1], speedParams[2]));
            } else if (checkedId == R.id.rb_hard) {
                selectedDifficulty = Constants.DIFFICULTY_HARD;
                speedParams = Constants.DIFFICULTY_SPEED_PARAMS[2];
                tvDifficultyParams.setText(String.format(
                        "初始速度: %d dp/s | 加速量: +%d dp/s | 最大速度: %d dp/s",
                        speedParams[0], speedParams[1], speedParams[2]));
            } else {
                selectedDifficulty = Constants.DIFFICULTY_MEDIUM;
                speedParams = Constants.DIFFICULTY_SPEED_PARAMS[1];
                tvDifficultyParams.setText(String.format(
                        "初始速度: %d dp/s | 加速量: +%d dp/s | 最大速度: %d dp/s",
                        speedParams[0], speedParams[1], speedParams[2]));
            }

            PrefsManager.setSelectedDifficulty(this, selectedDifficulty);
        });

        // 背景音乐开关监听
        switchBgm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PrefsManager.setBgmEnabled(this, isChecked);
        });

        // 游戏音效开关监听
        switchSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PrefsManager.setSfxEnabled(this, isChecked);
        });

        // 背景音乐音量监听
        seekbarBgmVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBgmVolumeValue.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 开始拖动时不处理
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                float volume = progress / 100f;
                PrefsManager.setBgmVolume(SettingsActivity.this, volume);
            }
        });

        // 游戏音效音量监听
        seekbarSfxVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSfxVolumeValue.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 开始拖动时不处理
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                float volume = progress / 100f;
                PrefsManager.setSfxVolume(SettingsActivity.this, volume);
            }
        });
    }

    /**
     * 从SharedPreferences加载设置并更新UI
     */
    private void loadSettings() {
        // 加载游戏模式设置
        String selectedMode = PrefsManager.getSelectedMode();
        if (selectedMode.equals(Constants.MODE_WALL_PASS)) {
            rgMode.check(R.id.rb_wall_pass);
        } else {
            rgMode.check(R.id.rb_no_wall);
        }

        // 加载游戏难度设置
        String selectedDifficulty = PrefsManager.getSelectedDifficulty();
        if (selectedDifficulty.equals(Constants.DIFFICULTY_EASY)) {
            rgDifficulty.check(R.id.rb_easy);
        } else if (selectedDifficulty.equals(Constants.DIFFICULTY_HARD)) {
            rgDifficulty.check(R.id.rb_hard);
        } else {
            rgDifficulty.check(R.id.rb_medium);
        }

        // 加载音频设置
        boolean bgmEnabled = PrefsManager.isBgmEnabled();
        switchBgm.setChecked(bgmEnabled);

        boolean sfxEnabled = PrefsManager.isSfxEnabled();
        switchSfx.setChecked(sfxEnabled);

        float bgmVolume = PrefsManager.getBgmVolume();
        int bgmProgress = (int) (bgmVolume * 100);
        seekbarBgmVolume.setProgress(bgmProgress);
        tvBgmVolumeValue.setText(bgmProgress + "%");

        float sfxVolume = PrefsManager.getSfxVolume();
        int sfxProgress = (int) (sfxVolume * 100);
        seekbarSfxVolume.setProgress(sfxProgress);
        tvSfxVolumeValue.setText(sfxProgress + "%");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
