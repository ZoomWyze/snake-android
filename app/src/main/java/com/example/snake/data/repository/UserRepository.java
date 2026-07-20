package com.example.snake.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.example.snake.data.db.AppDatabase;
import com.example.snake.data.db.dao.UserDao;
import com.example.snake.data.db.entity.User;
import com.example.snake.data.db.entity.UserScoreDTO;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用户数据仓库。
 *   - 所有写操作（insert / 单条同步查询）在单线程后台执行
 *   - LiveData 查询直接暴露给 UI 层观察
 *   - 同步查询（如登录校验）通过 Callback 在主线程回调
 */
public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public UserRepository(@NonNull Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.userDao = db.userDao();
    }

    // ------------------------------------------------------------------
    // 写操作（异步 + 主线程回调）
    // ------------------------------------------------------------------

    public interface InsertCallback {
        void onInserted(long userId);

        void onError(Throwable t);
    }

    public interface UserQueryCallback {
        void onResult(User user);
    }

    /**
     * 异步插入用户，成功后在主线程回调返回新用户 id。
     */
    public void insert(@NonNull User user, @Nullable InsertCallback callback) {
        executor.execute(() -> {
            try {
                long id = userDao.insert(user);
                if (callback != null) {
                    mainHandler.post(() -> callback.onInserted(id));
                }
            } catch (Throwable t) {
                if (callback != null) {
                    mainHandler.post(() -> callback.onError(t));
                }
            }
        });
    }

    /**
     * 异步按账号查询用户（注册/登录校验用），主线程回调。
     */
    public void findByUsername(@NonNull String username, @NonNull UserQueryCallback callback) {
        executor.execute(() -> {
            User u = userDao.findByUsername(username);
            mainHandler.post(() -> callback.onResult(u));
        });
    }

    /**
     * 异步按 id 查询用户（自动登录后获取昵称等），主线程回调。
     */
    public void findById(long userId, @NonNull UserQueryCallback callback) {
        executor.execute(() -> {
            User u = userDao.findById(userId);
            mainHandler.post(() -> callback.onResult(u));
        });
    }

    // ------------------------------------------------------------------
    // LiveData 查询（直接暴露，Room 自动后台执行 + 主线程通知）
    // ------------------------------------------------------------------

    /**
     * 本机全用户排行榜。
     */
    public LiveData<List<UserScoreDTO>> getLeaderboard() {
        return userDao.getLeaderboard();
    }
}
