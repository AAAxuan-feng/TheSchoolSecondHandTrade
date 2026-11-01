package com.example.theschoolsecond_handtrade.db;

/**
 * 商品实体类，对应数据库 goods 表。
 *
 * 表结构：
 *   id          INTEGER PRIMARY KEY AUTOINCREMENT
 *   name        TEXT     商品名称
 *   price       REAL     价格
 *   desc        TEXT     商品描述
 *   img_path    TEXT     图片本地路径（Uri 字符串或 "res:demo_xxx"）
 *   user_id     INTEGER  发布者用户 id
 *   status      INTEGER  0=在售 1=已售出
 *   create_time INTEGER  发布时间（毫秒时间戳）
 *   category    TEXT     商品分类（书籍/数码/生活用品/其他）
 *
 * ★ sellerName 不是数据库字段，是查询时 JOIN user 表带出来的卖家昵称，
 *   方便列表直接显示，不用再查一次数据库。
 */
public class GoodsBean {

    /** 在售 */
    public static final int STATUS_ON_SALE = 0;
    /** 已售出 */
    public static final int STATUS_SOLD = 1;

    /** 分类：全部（只用于筛选，不存数据库） */
    public static final String CATEGORY_ALL = "全部";
    /** 分类：书籍 */
    public static final String CATEGORY_BOOK = "书籍";
    /** 分类：数码 */
    public static final String CATEGORY_DIGITAL = "数码";
    /** 分类：生活用品 */
    public static final String CATEGORY_LIFE = "生活用品";
    /** 分类：运动 */
    public static final String CATEGORY_SPORT = "运动";
    /** 分类：其他 */
    public static final String CATEGORY_OTHER = "其他";

    /** 发布页可选的分类数组（下拉框、分类栏都用它） */
    public static final String[] CATEGORIES = {
            CATEGORY_BOOK, CATEGORY_DIGITAL, CATEGORY_LIFE, CATEGORY_SPORT, CATEGORY_OTHER
    };

    private int id;
    private String name;
    private double price;
    private String desc;
    private String imgPath;
    private int userId;
    private int status;
    private long createTime;
    private String category;

    /** 非数据库字段：卖家昵称，查询时联表带出 */
    private String sellerName;

    public GoodsBean() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    // ==================== 给界面用的辅助方法 ====================

    /** 价格显示成 ¥ 25.00 */
    public String getPriceText() {
        return "¥ " + String.format(java.util.Locale.CHINA, "%.2f", price);
    }

    /** 状态文案：在售 / 已售出 */
    public String getStatusText() {
        return status == STATUS_ON_SALE ? "在售" : "已售出";
    }

    /** 是否还在售 */
    public boolean isOnSale() {
        return status == STATUS_ON_SALE;
    }

    /** 分类兜底：老数据没有分类时显示"其他" */
    public String getCategorySafe() {
        return (category == null || category.trim().isEmpty()) ? CATEGORY_OTHER : category;
    }
}
