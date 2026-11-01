package com.example.theschoolsecond_handtrade.activity;

import com.example.theschoolsecond_handtrade.db.GoodsBean;

import java.util.List;

/**
 * 我卖出的商品。
 *
 * 数据来源：订单表里 seller_id = 当前用户的商品
 * 不显示下架按钮（已经卖掉了）
 */
public class MySellActivity extends BaseMyListActivity {

    @Override
    protected String getTitleText() {
        return "我卖出的商品";
    }

    @Override
    protected List<GoodsBean> loadData(int userId) {
        return dbHelper.getMySoldGoods(userId);
    }

    @Override
    protected boolean showOffShelf() {
        return false;
    }
}
