package com.example.snake.ui.login;

import android.os.Bundle;
import android.text.TextUtils;

import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.snake.R;
import com.example.snake.data.db.entity.User;
import com.example.snake.data.repository.UserRepository;
import com.example.snake.util.MD5Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword, tilConfirmPassword, tilNickname;
    private TextInputEditText etUsername, etPassword, etConfirmPassword, etNickname;
    private Button btnRegister;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(getApplication());

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        tilNickname = findViewById(R.id.til_nickname);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etNickname = findViewById(R.id.et_nickname);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        // 清除错误提示
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilNickname.setError(null);
        //获取输入框的值
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();

        // 校验所有字段非空
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("请输入账号");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("请输入密码");
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError("请确认密码");
            return;
        }

        if (TextUtils.isEmpty(nickname)) {
            tilNickname.setError("请输入昵称");
            return;
        }

        // 校验密码长度 ≥ 6位
        if (password.length() < 6) {
            tilPassword.setError("密码长度至少6位");
            return;
        }

        // 校验两次密码一致
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("两次密码输入不一致");
            return;
        }

        // 禁用按钮防止重复点击
        btnRegister.setEnabled(false);

        // 检查账号是否已存在
        userRepository.findByUsername(username, new UserRepository.UserQueryCallback() {
            @Override
            public void onResult(User existingUser) {
                if (existingUser != null) {
                    // 恢复按钮状态
                    btnRegister.setEnabled(true);
                    tilUsername.setError("账号已存在");
                    Toast.makeText(RegisterActivity.this, "注册失败：账号已存在", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 创建新用户
                String passwordHash = MD5Util.md5(password);
                long createdAt = System.currentTimeMillis();
                User newUser = new User(username, passwordHash, nickname, createdAt);

                // 插入数据库
                userRepository.insert(newUser, new UserRepository.InsertCallback() {
                    @Override
                    public void onInserted(long userId) {
                        Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        finish(); // 返回登录页
                    }

                    @Override
                    public void onError(Throwable t) {
                        btnRegister.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "注册失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
