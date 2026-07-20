package com.example.snake.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import android.widget.Button;
import android.widget.Toast;
import com.example.snake.data.db.entity.User;
import androidx.appcompat.app.AppCompatActivity;

import com.example.snake.R;
import com.example.snake.data.prefs.PrefsManager;
import com.example.snake.data.repository.UserRepository;
import com.example.snake.ui.home.HomeActivity;
import com.example.snake.util.MD5Util;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查登录状态，若已登录直接跳转主页
        if (PrefsManager.getUserId() != -1L) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        userRepository = new UserRepository(getApplication());

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        // 清除错误提示
        tilUsername.setError(null);
        tilPassword.setError(null);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // 校验非空
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("请输入账号");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("请输入密码");
            return;
        }

        // 禁用按钮防止重复点击
        btnLogin.setEnabled(false);

        // 查询用户
        userRepository.findByUsername(username, new UserRepository.UserQueryCallback() {
            @Override
            public void onResult(User user) {
                // 恢复按钮状态
                btnLogin.setEnabled(true);

                if (user == null) {
                    tilUsername.setError("账号不存在");
                    Toast.makeText(LoginActivity.this, "登录失败：账号不存在", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 比对MD5密码
                String inputPasswordHash = MD5Util.md5(password);
                if (inputPasswordHash == null || !inputPasswordHash.equals(user.getPasswordHash())) {
                    tilPassword.setError("密码错误");
                    Toast.makeText(LoginActivity.this, "登录失败：密码错误", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 登录成功，保存登录状态
                PrefsManager.setUserId(LoginActivity.this, user.getId());

                // 跳转主页
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }
        });
    }
}
