package com.example.theschoolsecond_handtrade.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 订单实体类，对应数据库 order_table 表。
 *
 * 表结构：
 *   id         INTEGER PRIMARY KEY AUTOINCREMENT
 *   goods_id   INTEGER  商品 id
 *   buyer_id   INTEGER  买家 id
 *   seller_id  INTEGER  卖家 id
 *   order_time TEXT     下单时间（yyyy-MM-dd HH:mm:ss）
 *
 * ★ 表名为什么叫 order_table 而不是 order？
 *   "ORDER" 是 SQL 关键字，表名叫 order 时
 *   create table order(...) 和 select * from order 都可能报语法错误，
 *   加 _table 后缀是最省事的规避方式。
 */
public class OrderBean {

    private int id;
    private int goodsId;
    private int buyerId;
    private int sellerId;
    private String orderTime;

    /** 非数据库字段：联表带出的商品名，方便订单列表显示 */
    private String goodsName;

    public OrderBean() {
    }

    public OrderBean(int goodsId, int buyerId, int sellerId, String orderTime) {
        this.goodsId = goodsId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.orderTime = orderTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(int goodsId) {
        this.goodsId = goodsId;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    /** 生成"当前时间"字符串，下单时调用 */
    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date());
    }
}
