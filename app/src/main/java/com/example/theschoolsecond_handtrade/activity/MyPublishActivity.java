package com.example.theschoolsecond_handtrade.activity;

import com.example.theschoolsecond_handtrade.db.GoodsBean;

import java.util.List;

/**
 * 我发布的商品。
 *
 * 布局与列表逻辑都在父类 BaseMyListActivity 里，
 * 这里只描述"这一页是什么"：
 *   - 标题：我发布的商品
 *   - 数据：当前用户发布的全部商品（含已售出/已下架）
 *   - 显示下架按钮
 */
public class MyPublishActivity extends BaseMyListActivity {

    @Override
    protected String getTitleText() {
        return "我发布的商品";
    }

    @Override
    protected List<GoodsBean> loadData(int userId) {
        return dbHelper.getMyPublishGoods(userId);
    }

    @Override
    protected boolean showOffShelf() {
        return true;
    }
}
