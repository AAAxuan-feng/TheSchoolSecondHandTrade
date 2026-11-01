package com.example.theschoolsecond_handtrade.activity;

import com.example.theschoolsecond_handtrade.db.GoodsBean;

import java.util.List;

/**
 * 我买到的商品。
 *
 * 数据来源：订单表里 buyer_id = 当前用户的商品
 * 不显示下架按钮（买来的东西不能由买家下架）
 */
public class MyBuyActivity extends BaseMyListActivity {

    @Override
    protected String getTitleText() {
        return "我买到的商品";
    }

    @Override
    protected List<GoodsBean> loadData(int userId) {
        return dbHelper.getMyBoughtGoods(userId);
    }

    @Override
    protected boolean showOffShelf() {
        return false;
    }
}
