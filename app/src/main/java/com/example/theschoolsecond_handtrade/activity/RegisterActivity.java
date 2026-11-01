package com.example.theschoolsecond_handtrade.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.db.DBHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 注册页。
 *
 * 布局：activity_register.xml
 * 控件：etUsername / etPassword / etConfirm / btnRegister / tvGoLogin
 *
 * 校验规则：用户名非空且不少于 3 位、密码不少于 6 位、两次密码一致、用户名不能重复
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirm;
    private Button btnRegister;

    private DBHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = DBHelper.getInstance(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> doRegister());

        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void doRegister() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            toast("请输入用户名");
            return;
        }
        if (username.length() < 3) {
            toast("用户名至少 3 位");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            toast("请输入密码");
            return;
        }
        if (password.length() < 6) {
            toast("密码至少 6 位");
            return;
        }
        if (!password.equals(confirm)) {
            toast("两次输入的密码不一致");
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("注册中…");

        executor.execute(() -> {
            final long result = dbHelper.register(username, password);
            mainHandler.post(() -> {
                btnRegister.setEnabled(true);
                btnRegister.setText(R.string.btn_register);

                if (result == -1) {
                    toast("该用户名已被注册，请换一个");
                    return;
                }
                toast("注册成功，请登录");
                // 注册完带着用户名回登录页，省得再手输一遍
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("username", username);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}