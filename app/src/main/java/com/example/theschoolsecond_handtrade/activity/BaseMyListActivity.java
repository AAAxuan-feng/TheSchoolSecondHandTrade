package com.example.theschoolsecond_handtrade.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.adapter.MyGoodsAdapter;
import com.example.theschoolsecond_handtrade.db.DBHelper;
import com.example.theschoolsecond_handtrade.db.GoodsBean;

import java.util.List;

/**
 * "我的商品"三个列表页的公共基类。
 *
 * 子类只需实现两件事：
 *   1) getTitleText()      —— 标题
 *   2) loadData(userId)    —— 查哪一批数据
 *
 * 布局：activity_my_list.xml（三页共用）
 * 列表项：item_my_goods.xml
 *
 * 这样 MyPublishActivity / MyBuyActivity / MySellActivity 各只有十来行代码，
 * 是"继承 + 模板方法"的典型用法。
 */
public abstract class BaseMyListActivity extends AppCompatActivity {

    protected DBHelper dbHelper;
    private MyGoodsAdapter adapter;
    private RecyclerView rvMyGoods;
    private TextView tvEmpty;

    /** 子类返回页面标题 */
    protected abstract String getTitleText();

    /** 子类返回要展示的商品列表 */
    protected abstract List<GoodsBean> loadData(int userId);

    /** 子类指定是否显示"下架"按钮（只有"我发布的"需要） */
    protected abstract boolean showOffShelf();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_list);

        dbHelper = DBHelper.getInstance(this);

        // 未登录守卫
        if (LoginActivity.getUserId(this) <= 0) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(getTitleText());

        tvEmpty = findViewById(R.id.tvEmpty);
        rvMyGoods = findViewById(R.id.rvMyGoods);

        rvMyGoods.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyGoodsAdapter(this, showOffShelf(), this::openDetail, this::confirmOffShelf);
        rvMyGoods.setAdapter(adapter);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        loadAndRender();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从详情页返回时刷新（商品可能被买走/下架）
        loadAndRender();
    }

    private void loadAndRender() {
        int userId = LoginActivity.getUserId(this);
        List<GoodsBean> list = loadData(userId);
        adapter.setData(list);

        boolean empty = list == null || list.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvMyGoods.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    /** 点卡片进详情 */
    private void openDetail(GoodsBean goods) {
        Intent intent = new Intent(this, GoodsDetailActivity.class);
        intent.putExtra("goods_id", goods.getId());
        startActivity(intent);
    }

    /** 下架前二次确认 */
    private void confirmOffShelf(GoodsBean goods) {
        new AlertDialog.Builder(this)
                .setTitle("下架商品")
                .setMessage("确定要下架「" + goods.getName() + "」吗？\n下架后他人将无法再购买。")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定下架", (dialog, which) -> {
                    int rows = dbHelper.offShelfGoods(goods.getId(),
                            LoginActivity.getUserId(this));
                    if (rows > 0) {
                        Toast.makeText(this, "已下架", Toast.LENGTH_SHORT).show();
                        loadAndRender();
                    } else {
                        Toast.makeText(this, "下架失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
