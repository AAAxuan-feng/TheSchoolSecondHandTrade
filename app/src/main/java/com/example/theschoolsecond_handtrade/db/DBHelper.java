package com.example.theschoolsecond_handtrade.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库帮助类：建表 + 全部增删改查。
 *
 * 数据库名：campus_secondhand.db      版本：1
 *
 * 三张表：
 *   user        —— 用户（id / username 唯一 / password）
 *   goods       —— 商品（id / name / price / desc / img_path / user_id / status / create_time）
 *   order_table —— 订单（id / goods_id / buyer_id / seller_id / order_time）
 *
 * ★ 预置数据（App 首次安装、数据库首次创建时插入）：
 *   测试账号 test / 123456、演示卖家 xiaoming / 123456
 *   6 件中文商品：其中 3 件属于 test（登录后"我发布的"能看到），3 件属于 xiaoming
 *   这样即使模拟器无法输入中文，也能完整演示列表、详情、购买、个人中心
 *   想重新触发：卸载 App 重装，或改 DB_VERSION 让 onUpgrade 重建表
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "campus_secondhand.db";
    /** 版本历史：v1 无分类字段；v2 增加 goods.category（商品分类） */
    private static final int DB_VERSION = 2;

    // ==================== 表名 ====================
    public static final String TABLE_USER = "user";
    public static final String TABLE_GOODS = "goods";
    public static final String TABLE_ORDER = "order_table";

    /** 预置测试账号 */
    public static final String TEST_USERNAME = "test";
    public static final String TEST_PASSWORD = "123456";
    /** 预置演示卖家账号 */
    public static final String DEMO_SELLER_USERNAME = "xiaoming";
    public static final String DEMO_SELLER_PASSWORD = "123456";

    // ==================== 建表语句 ====================
    private static final String SQL_CREATE_USER =
            "CREATE TABLE " + TABLE_USER + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT UNIQUE NOT NULL, "
                    + "password TEXT NOT NULL)";

    private static final String SQL_CREATE_GOODS =
            "CREATE TABLE " + TABLE_GOODS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL, "
                    + "price REAL NOT NULL DEFAULT 0, "
                    + "[desc] TEXT, "
                    + "img_path TEXT, "
                    + "user_id INTEGER NOT NULL, "
                    + "status INTEGER NOT NULL DEFAULT 0, "
                    + "create_time INTEGER NOT NULL DEFAULT 0, "
                    + "category TEXT NOT NULL DEFAULT '其他')";

    // 表名用 order_table，避开 SQL 关键字 ORDER
    private static final String SQL_CREATE_ORDER =
            "CREATE TABLE " + TABLE_ORDER + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "goods_id INTEGER NOT NULL, "
                    + "buyer_id INTEGER NOT NULL, "
                    + "seller_id INTEGER NOT NULL, "
                    + "order_time TEXT NOT NULL)";

    private static DBHelper instance;

    /** 单例：整个 App 共用一个 Helper，避免多实例导致数据库锁冲突 */
    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER);
        db.execSQL(SQL_CREATE_GOODS);
        db.execSQL(SQL_CREATE_ORDER);

        insertDemoData(db);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        // ========== v1 -> v2：只加字段，保留原有数据 ==========
        if (oldVersion < 2) {
            // 老库的 goods 表没有 category 列，用 ALTER TABLE 补上，
            // 已有商品统一归到"其他"，不会丢数据。
            db.execSQL("ALTER TABLE " + TABLE_GOODS
                    + " ADD COLUMN category TEXT NOT NULL DEFAULT '" + GoodsBean.CATEGORY_OTHER + "'");
        }
        // 注意：不再用 DROP TABLE 重建，避免升级时把用户数据清空。
        // 如果以后表结构变化很大（比如改字段类型），才考虑重建 + 数据迁移。
    }

    // ====================================================================
    //                        预置演示数据
    // ====================================================================

    /**
     * 插入 2 个账号 + 6 件中文商品。
     * 商品图片用项目内置 drawable：img_path 存 "res:demo_xxx"，
     * 由 util/Images 负责把 "res:xxx" 解析成资源 id 交给 Glide。
     */
    private void insertDemoData(SQLiteDatabase db) {
        // ---------- 账号 1：test（也是登录测试账号） ----------
        db.execSQL("INSERT INTO " + TABLE_USER + " (username, password) VALUES (?, ?)",
                new Object[]{TEST_USERNAME, TEST_PASSWORD});
        long testId = 1L;

        // ---------- 账号 2：xiaoming（演示"别人卖的东西"） ----------
        db.execSQL("INSERT INTO " + TABLE_USER + " (username, password) VALUES (?, ?)",
                new Object[]{DEMO_SELLER_USERNAME, DEMO_SELLER_PASSWORD});
        long mingId = 2L;

        long now = System.currentTimeMillis();
        long oneHour = 60L * 60L * 1000L;

        // ---------- test 发布的 3 件 ----------
        insertDemoGoods(db, "九成新高等数学教材 同济第七版", 25.0,
                "无笔记无划线，封面轻微磨损，可校内自提。", "res:demo_book", testId,
                GoodsBean.CATEGORY_BOOK, now - oneHour * 5);
        insertDemoGoods(db, "二手 iPad Air 4 64G 深空灰", 1899.0,
                "自用一年，屏幕无划痕，无拆修，配件齐全。", "res:demo_tablet", testId,
                GoodsBean.CATEGORY_DIGITAL, now - oneHour * 8);
        insertDemoGoods(db, "宿舍护眼小台灯 三档亮度", 15.0,
                "三档亮度可调，用了半年，功能正常。", "res:demo_lamp", testId,
                GoodsBean.CATEGORY_LIFE, now - oneHour * 12);

        // ---------- xiaoming 发布的 3 件 ----------
        insertDemoGoods(db, "闲置篮球 斯伯丁 七成新", 45.0,
                "打了两次就不爱打了，气足，可约操场看球。", "res:demo_ball", mingId,
                GoodsBean.CATEGORY_SPORT, now - oneHour * 2);
        insertDemoGoods(db, "头戴式耳机 音质很好", 120.0,
                "购于去年双十一，闲置吃灰，耳罩干净。", "res:demo_headphone", mingId,
                GoodsBean.CATEGORY_DIGITAL, now - oneHour * 3);
        insertDemoGoods(db, "保温水杯 500ml 全新未拆", 35.0,
                "买重了，全新未拆封，绿色。", "res:demo_bottle", mingId,
                GoodsBean.CATEGORY_LIFE, now - oneHour * 6);
    }

    private void insertDemoGoods(SQLiteDatabase db, String name, double price, String desc,
                                 String imgPath, long userId, String category, long createTime) {
        db.execSQL("INSERT INTO " + TABLE_GOODS
                        + " (name, price, [desc], img_path, user_id, status, create_time, category) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                new Object[]{name, price, desc, imgPath, userId, GoodsBean.STATUS_ON_SALE,
                        createTime, category});
    }

    // ====================================================================
    //                            用户相关
    // ====================================================================

    /** 用户名是否已存在 */
    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{"id"},
                "username = ?", new String[]{username},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * 注册。
     *
     * @return 注册成功返回新用户 id；用户名已存在返回 -1
     */
    public long register(String username, String password) {
        if (isUsernameExists(username)) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_USER, null, values);
    }

    /**
     * 登录校验。
     *
     * @return 校验通过返回该用户；用户名或密码错误返回 null
     */
    public UserBean login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{"id", "username", "password"},
                "username = ? AND password = ?", new String[]{username, password},
                null, null, null);
        UserBean user = null;
        if (cursor.moveToFirst()) {
            user = new UserBean(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")));
        }
        cursor.close();
        return user;
    }

    /** 按 id 查用户名（详情页显示卖家昵称用） */
    public String getUsernameById(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{"username"},
                "id = ?", new String[]{String.valueOf(userId)},
                null, null, null);
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow("username"));
        }
        cursor.close();
        return name;
    }

    /**
     * 生成卖家联系方式（模拟，不是真实数据）。
     * 课程作业没有服务器，用固定的"校园手机号 + 用户 id"模拟，
     * 纯本地生成不涉及隐私，演示"联系卖家"功能足够。
     */
    public String getContactOfSeller(int sellerId) {
        return "138****" + String.format(java.util.Locale.CHINA, "%04d", 1000 + sellerId);
    }

    // ====================================================================
    //                            商品相关
    // ====================================================================

    /**
     * 发布商品。
     *
     * @return 成功返回新商品 id，失败返回 -1
     */
    public long addGoods(String name, double price, String desc,
                         String imgPath, int userId, String category) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("price", price);
        values.put("[desc]", desc);
        values.put("img_path", imgPath);
        values.put("user_id", userId);
        values.put("status", GoodsBean.STATUS_ON_SALE);
        values.put("create_time", System.currentTimeMillis());
        values.put("category", category == null ? GoodsBean.CATEGORY_OTHER : category);
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_GOODS, null, values);
    }

    /** 首页商品列表：只在售，最新发布在前 */
    public List<GoodsBean> getOnSaleGoods() {
        return queryGoods("g.status = ?", new String[]{String.valueOf(GoodsBean.STATUS_ON_SALE)});
    }

    /** 首页按商品名模糊搜索（同样只在售） */
    public List<GoodsBean> searchOnSaleGoods(String keyword) {
        return queryGoods("g.status = ? AND g.name LIKE ?",
                new String[]{String.valueOf(GoodsBean.STATUS_ON_SALE), "%" + keyword + "%"});
    }

    /**
     * 首页：分类 + 关键字组合筛选。
     *
     * @param category 传 GoodsBean.CATEGORY_ALL 或 null/空 表示不限分类
     * @param keyword  传 null/空 表示不按名称过滤
     */
    public List<GoodsBean> filterOnSaleGoods(String category, String keyword) {
        StringBuilder where = new StringBuilder("g.status = ?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(GoodsBean.STATUS_ON_SALE));

        boolean hasCategory = category != null && !category.isEmpty()
                && !GoodsBean.CATEGORY_ALL.equals(category);
        if (hasCategory) {
            where.append(" AND g.category = ?");
            args.add(category);
        }

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        if (hasKeyword) {
            where.append(" AND g.name LIKE ?");
            args.add("%" + keyword.trim() + "%");
        }

        return queryGoods(where.toString(), args.toArray(new String[0]));
    }

    /** 我发布的商品（全部状态，含已下架/已售出） */
    public List<GoodsBean> getMyPublishGoods(int userId) {
        return queryGoods("g.user_id = ?", new String[]{String.valueOf(userId)});
    }

    /**
     * 我买到的商品：通过订单表关联。
     * 一个商品可能被下单多次，这里用 IN 子查询避免出现重复行。
     */
    public List<GoodsBean> getMyBoughtGoods(int buyerId) {
        return queryGoods("g.id IN (SELECT goods_id FROM " + TABLE_ORDER + " WHERE buyer_id = ?)",
                new String[]{String.valueOf(buyerId)});
    }

    /** 我卖出的商品：别人买了我的东西 */
    public List<GoodsBean> getMySoldGoods(int sellerId) {
        return queryGoods("g.id IN (SELECT goods_id FROM " + TABLE_ORDER + " WHERE seller_id = ?)",
                new String[]{String.valueOf(sellerId)});
    }

    /** 按 id 查单个商品（详情页用） */
    public GoodsBean getGoodsById(int goodsId) {
        List<GoodsBean> list = queryGoods("g.id = ?", new String[]{String.valueOf(goodsId)});
        return list.isEmpty() ? null : list.get(0);
    }

    // ★ 每列都写显式别名（AS xxx）：
    //   desc 是 SQL 保留字，所以建表和查询都用 [desc] 括起来；
    //   再用 AS desc 把结果列名固定成 "desc"，这样
    //   cursor.getColumnIndexOrThrow("desc") 一定能取到，不受实现差异影响。
    private static final String SQL_QUERY_GOODS =
            "SELECT g.id AS id, g.name AS name, g.price AS price, g.[desc] AS desc, "
                    + "g.img_path AS img_path, g.user_id AS user_id, "
                    + "g.status AS status, g.create_time AS create_time, "
                    + "g.category AS category, "
                    + "u.username AS seller_name "
                    + "FROM " + TABLE_GOODS + " g "
                    + "LEFT JOIN " + TABLE_USER + " u ON g.user_id = u.id ";

    /** 统一的商品查询：联表带出卖家昵称，按发布时间倒序 */
    private List<GoodsBean> queryGoods(String where, String[] args) {
        List<GoodsBean> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_QUERY_GOODS
                + (where == null ? "" : "WHERE " + where + " ")
                + "ORDER BY g.create_time DESC", args);
        while (cursor.moveToNext()) {
            GoodsBean bean = new GoodsBean();
            bean.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            bean.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            bean.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            bean.setDesc(cursor.getString(cursor.getColumnIndexOrThrow("desc")));
            bean.setImgPath(cursor.getString(cursor.getColumnIndexOrThrow("img_path")));
            bean.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
            bean.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow("status")));
            bean.setCreateTime(cursor.getLong(cursor.getColumnIndexOrThrow("create_time")));
            bean.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            bean.setSellerName(cursor.getString(cursor.getColumnIndexOrThrow("seller_name")));
            list.add(bean);
        }
        cursor.close();
        return list;
    }

    /** 修改商品状态（只有发布者能操作，所以带 userId 条件更严谨） */
    public int updateGoodsStatus(int goodsId, int userId, int status) {
        ContentValues values = new ContentValues();
        values.put("status", status);
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_GOODS, values, "id = ? AND user_id = ?",
                new String[]{String.valueOf(goodsId), String.valueOf(userId)});
    }

    /** 下架商品（标记为已售出/已下架） */
    public int offShelfGoods(int goodsId, int userId) {
        return updateGoodsStatus(goodsId, userId, GoodsBean.STATUS_SOLD);
    }

    // ====================================================================
    //                            订单相关
    // ====================================================================

    /**
     * 下单：插入订单 + 把商品标记为已售出。
     *
     * 两步操作放在同一个事务里，避免"订单插进去了但商品还是待售"的脏数据。
     *
     * @return 成功返回订单 id，失败返回 -1
     */
    public long buyGoods(int goodsId, int buyerId, int sellerId) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues order = new ContentValues();
            order.put("goods_id", goodsId);
            order.put("buyer_id", buyerId);
            order.put("seller_id", sellerId);
            order.put("order_time", OrderBean.now());
            long orderId = db.insert(TABLE_ORDER, null, order);
            if (orderId == -1) {
                return -1;
            }

            ContentValues goods = new ContentValues();
            goods.put("status", GoodsBean.STATUS_SOLD);
            db.update(TABLE_GOODS, goods, "id = ?", new String[]{String.valueOf(goodsId)});

            db.setTransactionSuccessful();
            return orderId;
        } finally {
            db.endTransaction();
        }
    }

    /** 我买到的订单条数（个人中心显示统计用） */
    public int getBuyCount(int buyerId) {
        return countBy(TABLE_ORDER, "buyer_id = ?", new String[]{String.valueOf(buyerId)});
    }

    /** 我卖出的订单条数 */
    public int getSellCount(int sellerId) {
        return countBy(TABLE_ORDER, "seller_id = ?", new String[]{String.valueOf(sellerId)});
    }

    /** 我发布的商品条数 */
    public int getPublishCount(int userId) {
        return countBy(TABLE_GOODS, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    private int countBy(String table, String where, String[] args) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table + " WHERE " + where, args);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
