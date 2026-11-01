package com.example.theschoolsecond_handtrade.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.db.DBHelper;

/**
 * 个人中心。
 *
 * 布局：activity_mine.xml
 * 控件：tvBack / tvUsername / layoutMyPublish / layoutMyBuy / layoutMySell / btnLogout
 *
 * 知识点：
 *   1) <include> 进来的布局，控件要先定位 include 容器再 findViewById
 *   2) 读 SharedPreferences 显示当前登录用户名
 *   3) 退出登录：清空登录状态 + 清空返回栈 + 回到登录页
 */
public class MineActivity extends AppCompatActivity {

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 未登录不允许进个人中心（游客只能浏览首页）
        userId = LoginActivity.getUserId(this);
        if (userId <= 0) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_mine);

        // ========== 显示当前登录用户名 ==========
        TextView tvUsername = findViewById(R.id.tvUsername);
        String username = LoginActivity.getUserName(this);
        tvUsername.setText(username == null ? "未登录" : username);

        // ========== 三个菜单行 ==========
        // ★ 这三行是 <include> 引的 item_mine_menu.xml，
        //   里面的 tvMenuTitle 有三个副本，必须先找到各自的容器再查找
        View rowPublish = findViewById(R.id.layoutMyPublish);
        View rowBuy = findViewById(R.id.layoutMyBuy);
        View rowSell = findViewById(R.id.layoutMySell);

        ((TextView) rowPublish.findViewById(R.id.tvMenuTitle)).setText(R.string.mine_publish);
        ((TextView) rowBuy.findViewById(R.id.tvMenuTitle)).setText(R.string.mine_buy);
        ((TextView) rowSell.findViewById(R.id.tvMenuTitle)).setText(R.string.mine_sell);

        // ========== 用数据库统计的数量补在标题后面，报告里更好看 ==========
        DBHelper dbHelper = DBHelper.getInstance(this);
        int publishCount = dbHelper.getPublishCount(userId);
        int buyCount = dbHelper.getBuyCount(userId);
        int sellCount = dbHelper.getSellCount(userId);

        ((TextView) rowPublish.findViewById(R.id.tvMenuTitle))
                .setText(getString(R.string.mine_publish) + "（" + publishCount + "）");
        ((TextView) rowBuy.findViewById(R.id.tvMenuTitle))
                .setText(getString(R.string.mine_buy) + "（" + buyCount + "）");
        ((TextView) rowSell.findViewById(R.id.tvMenuTitle))
                .setText(getString(R.string.mine_sell) + "（" + sellCount + "）");

        // ========== 跳转 ==========
        rowPublish.setOnClickListener(v ->
                startActivity(new Intent(this, MyPublishActivity.class)));
        rowBuy.setOnClickListener(v ->
                startActivity(new Intent(this, MyBuyActivity.class)));
        rowSell.setOnClickListener(v ->
                startActivity(new Intent(this, MySellActivity.class)));

        // 返回
        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        // ========== 退出登录 ==========
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    /** 退出登录前先弹确认框，避免误触 */
    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_logout)
                .setMessage(R.string.logout_confirm)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> doLogout())
                .show();
    }

    /**
     * 执行退出登录。
     *
     * 三步：
     *   1) 清空 SharedPreferences 里的登录状态
     *   2) finishAffinity() 关掉栈内所有页面（首页/个人中心等）
     *   3) 启动登录页 —— 这样按返回键也回不到已登录的页面
     * 顺序不能颠倒：先清状态再跳，否则登录页的"已登录直接进首页"判断会把你又弹回首页。
     */
    private void doLogout() {
        LoginActivity.clearLoginState(this);
        Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        // 带上标记，让登录页知道是"主动退出"而不是"首次启动"
        intent.putExtra("from_logout", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finishAffinity();
    }
}
