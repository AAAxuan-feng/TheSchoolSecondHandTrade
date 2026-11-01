package com.example.theschoolsecond_handtrade.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.theschoolsecond_handtrade.R;
import com.example.theschoolsecond_handtrade.adapter.GoodsAdapter;
import com.example.theschoolsecond_handtrade.db.DBHelper;
import com.example.theschoolsecond_handtrade.db.GoodsBean;
import com.example.theschoolsecond_handtrade.util.CategoryBar;

import java.util.List;

/**
 * 首页：商品列表。
 *
 * 布局：activity_main.xml
 * 控件：etSearch / btnSearch / tvMine / srlRefresh / rvGoods / tvEmpty / btnPublish
 *
 * 知识点：
 *   1) RecyclerView + LinearLayoutManager + 自定义 Adapter
 *   2) SwipeRefreshLayout 下拉刷新
 *   3) SQLite 查询数据填充列表
 *   4) 空数据状态切换（tvEmpty 显隐）
 *   5) 游客模式：未登录可浏览，但发布/个人中心要先登录
 */
public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private GoodsAdapter adapter;
    private RecyclerView rvGoods;
    private SwipeRefreshLayout srlRefresh;
    private TextView tvEmpty;
    private EditText etSearch;

    private String currentKeyword = "";
    /** 当前选中的分类，"全部"表示不筛选 */
    private String currentCategory = GoodsBean.CATEGORY_ALL;
    private LinearLayout layoutCategoryBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DBHelper.getInstance(this);

        rvGoods = findViewById(R.id.rvGoods);
        srlRefresh = findViewById(R.id.srlRefresh);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        layoutCategoryBar = findViewById(R.id.layoutCategoryBar);
        TextView tvMine = findViewById(R.id.tvMine);

        // ========== 分类筛选栏（动态生成"全部/书籍/数码/…"） ==========
        CategoryBar.build(layoutCategoryBar, CategoryBar.withAll(), currentCategory,
                category -> {
                    currentCategory = category;
                    loadGoods();
                });

        // ========== RecyclerView 初始化 ==========
        rvGoods.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GoodsAdapter(this, this::openDetail);
        rvGoods.setAdapter(adapter);

        // ========== 下拉刷新 ==========
        srlRefresh.setColorSchemeResources(R.color.primary);
        srlRefresh.setOnRefreshListener(this::loadGoods);

        // ========== 搜索：输入即过滤 ==========
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s == null ? "" : s.toString().trim();
                loadGoods();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 搜索按钮：收起键盘并刷新
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            currentKeyword = etSearch.getText().toString().trim();
            loadGoods();
        });

        // ========== 个人中心：未登录先去登录 ==========
        tvMine.setOnClickListener(v -> {
            if (isNotLoggedIn()) {
                toastAndGoLogin("请先登录后再进入个人中心");
                return;
            }
            startActivity(new Intent(this, MineActivity.class));
        });

        // ========== 发布闲置：未登录先去登录 ==========
        findViewById(R.id.btnPublish).setOnClickListener(v -> {
            if (isNotLoggedIn()) {
                toastAndGoLogin("请先登录后再发布闲置");
                return;
            }
            startActivity(new Intent(this, PublishActivity.class));
        });

        loadGoods();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从发布页 / 详情页返回时刷新（新发布的商品、被买走的商品状态变化）
        loadGoods();
    }

    /** 查询数据并刷新列表：分类 + 关键字组合筛选 */
    private void loadGoods() {
        List<GoodsBean> list = dbHelper.filterOnSaleGoods(currentCategory, currentKeyword);

        adapter.setData(list);

        // 空状态切换
        boolean empty = list == null || list.isEmpty();
        if (empty) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvGoods.setVisibility(View.GONE);

            // 提示文案按"当前筛选条件"变化，用户能知道是筛掉了不是没数据
            boolean filtering = !TextUtils.isEmpty(currentKeyword)
                    || !GoodsBean.CATEGORY_ALL.equals(currentCategory);
            if (!filtering) {
                tvEmpty.setText(getString(R.string.empty_goods));
            } else if (!TextUtils.isEmpty(currentKeyword)) {
                tvEmpty.setText("没有找到「" + currentKeyword + "」相关商品");
            } else {
                tvEmpty.setText("「" + currentCategory + "」分类下暂无在售商品");
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvGoods.setVisibility(View.VISIBLE);
        }

        // 结束下拉刷新动画
        if (srlRefresh.isRefreshing()) {
            srlRefresh.setRefreshing(false);
        }
    }

    /** 点商品卡片进详情页，传商品 id */
    private void openDetail(GoodsBean goods) {
        Intent intent = new Intent(this, GoodsDetailActivity.class);
        intent.putExtra("goods_id", goods.getId());
        startActivity(intent);
    }

    private boolean isNotLoggedIn() {
        return LoginActivity.getUserId(this) <= 0;
    }

    private void toastAndGoLogin(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
    }
}
