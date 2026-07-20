package com.example.snake.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.example.snake.R;
import com.example.snake.data.prefs.PrefsManager;

import java.io.IOException;

/**
 * 游戏音频统一管理（单例）。
 *
 * - BGM：MediaPlayer 加载 res/raw/bgm_game.mp3，循环播放
 * - 音效：SoundPool 加载 res/raw/sfx_eat.mp3 和 sfx_death.mp3，短促低延迟
 * - 音量 / 开关读取自 {@link PrefsManager}
 */
public class GameAudioManager {

    private static final String TAG = "GameAudioManager";

    private static volatile GameAudioManager instance;

    private final Object lock = new Object();

    // BGM
    private MediaPlayer bgmPlayer;

    // SFX
    private SoundPool soundPool;
    private int eatSoundId = -1;
    private boolean eatSoundLoaded = false;

    private int deathSoundId = -1;        // 新增：死亡音效 ID
    private boolean deathSoundLoaded = false; // 新增：死亡音效加载标志

    // 缓存的设置
    private boolean bgmEnabled = true;
    private float bgmVolume = 0.7f;
    private boolean sfxEnabled = true;
    private float sfxVolume = 0.7f;

    private boolean initialized = false;

    private GameAudioManager() {
        // 单例
    }

    public static GameAudioManager getInstance() {
        if (instance == null) {
            synchronized (GameAudioManager.class) {
                if (instance == null) {
                    instance = new GameAudioManager();
                }
            }
        }
        return instance;
    }

    // ------------------------------------------------------------------
    // 初始化
    // ------------------------------------------------------------------

    public void init(Context context) {
        if (context == null) {
            Log.w(TAG, "init: context is null, ignored");
            return;
        }
        final Context app = context.getApplicationContext();

        synchronized (lock) {
            if (initialized && bgmPlayer != null && soundPool != null) {
                Log.w(TAG, "init: already initialized, ignored");
                return;
            }
            releaseInternal();

            // 1) 读取 Prefs
            bgmEnabled = PrefsManager.isBgmEnabled();
            bgmVolume = PrefsManager.getBgmVolume();
            sfxEnabled = PrefsManager.isSfxEnabled();
            sfxVolume = PrefsManager.getSfxVolume();

            // 2) 创建 BGM MediaPlayer
            try {
                AssetFileDescriptor afd = app.getResources().openRawResourceFd(R.raw.bgm_game);
                if (afd != null) {
                    bgmPlayer = new MediaPlayer();
                    bgmPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_GAME)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                    );
                    bgmPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();

                    bgmPlayer.setLooping(true);

                    bgmPlayer.setOnCompletionListener(mp -> {
                        try {
                            if (bgmEnabled) {
                                mp.seekTo(0);
                                mp.start();
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "BGM loop fallback error", ex);
                        }
                    });

                    bgmPlayer.setOnErrorListener((mp, what, extra) -> {
                        Log.e(TAG, "BGM MediaPlayer Error: what=" + what + ", extra=" + extra);
                        try {
                            mp.reset();
                            AssetFileDescriptor afd2 = app.getResources().openRawResourceFd(R.raw.bgm_game);
                            if (afd2 != null) {
                                mp.setDataSource(afd2.getFileDescriptor(), afd2.getStartOffset(), afd2.getLength());
                                afd2.close();
                                mp.prepare();
                                if (bgmEnabled) mp.start();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "BGM error recovery failed", e);
                        }
                        return true;
                    });

                    bgmPlayer.setVolume(bgmVolume, bgmVolume);
                    bgmPlayer.prepare();
                } else {
                    Log.e(TAG, "init: openRawResourceFd returned null (R.raw.bgm_game missing?)");
                }
            } catch (Throwable t) {
                Log.e(TAG, "init: failed to create BGM MediaPlayer", t);
                if (bgmPlayer != null) {
                    bgmPlayer.release();
                    bgmPlayer = null;
                }
            }

            // 3) 创建 SoundPool
            try {
                AudioAttributes attrs = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                soundPool = new SoundPool.Builder()
                        .setMaxStreams(4) // 最多同时播放4条音效流
                        .setAudioAttributes(attrs)
                        .build();

                // 修改：统一处理多个音效的加载回调
                soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
                    if (status == 0) {
                        if (sampleId == eatSoundId) {
                            eatSoundLoaded = true;
                        } else if (sampleId == deathSoundId) {
                            deathSoundLoaded = true;
                        }
                    } else {
                        Log.e(TAG, "SoundPool load failed, sampleId=" + sampleId + ", status=" + status);
                    }
                });

                eatSoundId = soundPool.load(app, R.raw.sfx_eat, 1);
                deathSoundId = soundPool.load(app, R.raw.sfx_death, 1); // 新增：加载死亡音效
            } catch (Throwable t) {
                Log.e(TAG, "init: failed to create SoundPool / load sfx", t);
                soundPool = null;
                eatSoundId = -1;
                eatSoundLoaded = false;
                deathSoundId = -1;  // 新增
                deathSoundLoaded = false; // 新增
            }

            initialized = true;
        }
    }

    // ------------------------------------------------------------------
    // BGM 控制
    // ------------------------------------------------------------------

    public void startBgm() {
        synchronized (lock) {
            if (!initialized || bgmPlayer == null) return;
            if (!bgmEnabled) return;
            try {
                if (!bgmPlayer.isPlaying()) {
                    bgmPlayer.start();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "startBgm error", e);
            }
        }
    }

    public void pauseBgm() {
        synchronized (lock) {
            if (!initialized || bgmPlayer == null) return;
            try {
                if (bgmPlayer.isPlaying()) {
                    bgmPlayer.pause();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "pauseBgm error", e);
            }
        }
    }

    public void resumeBgm() {
        synchronized (lock) {
            if (!initialized || bgmPlayer == null) return;
            if (!bgmEnabled) return;
            try {
                if (!bgmPlayer.isPlaying()) {
                    bgmPlayer.start();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "resumeBgm error", e);
            }
        }
    }

    public void stopBgm() {
        synchronized (lock) {
            if (!initialized || bgmPlayer == null) return;
            try {
                if (bgmPlayer.isPlaying()) {
                    bgmPlayer.stop();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "stopBgm error", e);
            }
        }
    }

    // ------------------------------------------------------------------
    // 音效
    // ------------------------------------------------------------------

    public void playEatSound() {
        synchronized (lock) {
            if (!initialized || soundPool == null) return;
            if (!sfxEnabled) return;
            if (!eatSoundLoaded || eatSoundId < 0) return;
            try {
                soundPool.play(eatSoundId, sfxVolume, sfxVolume, 1, 0, 1.0f);
            } catch (Throwable t) {
                Log.e(TAG, "playEatSound error", t);
            }
        }
    }

    // 新增：播放死亡音效
    public void playDeathSound() {
        synchronized (lock) {
            if (!initialized || soundPool == null) return;
            if (!sfxEnabled) return;
            if (!deathSoundLoaded || deathSoundId < 0) return;
            try {
                // 死亡音效通常比较长，优先级可以设高一点(2)，不循环
                soundPool.play(deathSoundId, sfxVolume, sfxVolume, 2, 0, 1.0f);
            } catch (Throwable t) {
                Log.e(TAG, "playDeathSound error", t);
            }
        }
    }

    // ------------------------------------------------------------------
    // 设置同步
    // ------------------------------------------------------------------

    public void reloadSettings(Context context) {
        synchronized (lock) {
            boolean newBgmEnabled = PrefsManager.isBgmEnabled();
            float newBgmVolume = PrefsManager.getBgmVolume();
            boolean newSfxEnabled = PrefsManager.isSfxEnabled();
            float newSfxVolume = PrefsManager.getSfxVolume();

            boolean bgmToggleChanged = (newBgmEnabled != bgmEnabled);
            boolean bgmVolumeChanged = (newBgmVolume != bgmVolume);

            bgmEnabled = newBgmEnabled;
            bgmVolume = newBgmVolume;
            sfxEnabled = newSfxEnabled;
            sfxVolume = newSfxVolume;

            if (bgmPlayer != null) {
                try {
                    bgmPlayer.setVolume(bgmVolume, bgmVolume);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "reloadSettings setVolume error", e);
                }
            }

            if (bgmToggleChanged || bgmVolumeChanged) {
                if (bgmEnabled) {
                    startBgm();
                } else {
                    pauseBgm();
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // 释放
    // ------------------------------------------------------------------

    public void release() {
        synchronized (lock) {
            releaseInternal();
            initialized = false;
        }
    }

    private void releaseInternal() {
        if (bgmPlayer != null) {
            try {
                bgmPlayer.setOnCompletionListener(null);
                bgmPlayer.setOnErrorListener(null);
                if (bgmPlayer.isPlaying()) bgmPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            try {
                bgmPlayer.release();
            } catch (Throwable ignored) {
            }
            bgmPlayer = null;
        }
        if (soundPool != null) {
            try {
                soundPool.release();
            } catch (Throwable ignored) {
            }
            soundPool = null;
        }
        eatSoundId = -1;
        eatSoundLoaded = false;

        deathSoundId = -1;      // 新增
        deathSoundLoaded = false; // 新增
    }
}
