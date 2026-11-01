package com.example.theschoolsecond_handtrade.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.db.DBHelper;
import com.example.theschoolsecond_handtrade.db.UserBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 登录页。
 *
 * 布局：activity_login.xml
 * 控件：etUsername / etPassword / btnLogin / tvGoRegister
 *
 * 知识点：
 *   1) SQLiteOpenHelper 查库校验账号密码
 *   2) SharedPreferences 保存登录状态（"记住登录"）
 *   3) 后台线程查数据库 + 主线程刷新 UI
 */
public class LoginActivity extends AppCompatActivity {

    /** SharedPreferences 文件名 */
    public static final String SP_NAME = "user_prefs";
    /** 保存的键：当前登录用户 id */
    public static final String KEY_USER_ID = "login_user_id";
    /** 保存的键：当前登录用户名 */
    public static final String KEY_USER_NAME = "login_user_name";

    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;

    private DBHelper dbHelper;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ========== 已登录就直接进首页 ==========
        if (getUserId() > 0) {
            goMain();
            return;
        }

        setContentView(R.layout.activity_login);

        dbHelper = DBHelper.getInstance(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> doLogin());

        // 如果是刚点了"退出登录"过来的，给个明确反馈
        if (getIntent().getBooleanExtra("from_logout", false)) {
            Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();
        }

        // 从注册页跳回来时，把用户名自动填好
        String preUsername = getIntent().getStringExtra("username");
        if (!TextUtils.isEmpty(preUsername)) {
            etUsername.setText(preUsername);
            etPassword.requestFocus();
        }

        // 去注册页
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // 底部退出程序
        TextView btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> exitApp());
    }

    /** 校验输入并查库登录 */
    private void doLogin() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            toast("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            toast("请输入密码");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("登录中…");

        executor.execute(() -> {
            final UserBean user = dbHelper.login(username, password);
            mainHandler.post(() -> {
                btnLogin.setEnabled(true);
                btnLogin.setText(R.string.btn_login);

                if (user == null) {
                    toast("用户名或密码错误，请重试");
                    return;
                }
                // 保存登录状态
                saveLoginState(user.getId(), user.getUsername());
                toast("欢迎回来，" + user.getUsername());
                goMain();
            });
        });
    }

    /** 保存登录状态到 SharedPreferences */
    private void saveLoginState(int userId, String username) {
        getSharedPreferences(SP_NAME, MODE_PRIVATE)
                .edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, username)
                .apply();
    }

    /** 读取当前登录用户 id，未登录返回 -1 */
    public static int getUserId(android.content.Context context) {
        return context.getSharedPreferences(SP_NAME, MODE_PRIVATE)
                .getInt(KEY_USER_ID, -1);
    }

    /** 读取当前登录用户名，未登录返回 null */
    public static String getUserName(android.content.Context context) {
        return context.getSharedPreferences(SP_NAME, MODE_PRIVATE)
                .getString(KEY_USER_NAME, null);
    }

    /** 退出登录：清空登录状态 */
    public static void clearLoginState(android.content.Context context) {
        context.getSharedPreferences(SP_NAME, MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    private int getUserId() {
        return getUserId(this);
    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * 退出程序。
     *
     * 用 finishAffinity() 而不是 finish()：
     *   finish() 只关掉当前页；如果返回栈里还有别的 Activity，会退到那一页。
     *   finishAffinity() 会把本任务（task）栈内所有 Activity 一起关掉，直接回到桌面。
     *
     * 说明：Android 不推荐用 System.exit(0) 强杀进程，交给系统回收更安全，
     *      所以这里只做"关掉所有页面"，进程由系统在合适时机回收。
     */
    private void exitApp() {
        finishAffinity();
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
