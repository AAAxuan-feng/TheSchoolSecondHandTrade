# -*- coding: utf-8 -*-
"""生成《高级web技术》课程实验报告（校园二手交易平台 Android 项目）。
版式严格对齐参考件：Letter 纸、页边距 2.54/3.17cm、正文仿宋 15pt、封面楷体。
"""
import os
from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING, WD_BREAK
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.section import WD_SECTION
from docx.oxml.ns import qn
from docx.oxml import OxmlElement

OUT = r"C:\Users\15586\Documents\deepseek-harness\default-workspace\校园二手交易平台_实验报告.docx"
IMG_REG = r"C:\Users\15586\Documents\deepseek-harness\default-workspace\report-assets\studio-tree.png"
IMG_HOME = r"C:\Users\15586\Documents\deepseek-harness\default-workspace\report-assets\studio-login-project.png"

# ============ 基础工具 ============
def set_run_font(run, cn_font="仿宋", en_font=None, size=15, bold=False, color=None):
    """同时设置中文（eastAsia）和西文字体，中文字体必须设 eastAsia 才生效"""
    run.font.name = en_font or cn_font
    run.font.size = Pt(size)
    run.font.bold = bold
    if color:
        run.font.color.rgb = RGBColor(*color)
    rPr = run._element.get_or_add_rPr()
    rFonts = rPr.find(qn("w:rFonts"))
    if rFonts is None:
        rFonts = OxmlElement("w:rFonts")
        rPr.append(rFonts)
    rFonts.set(qn("w:ascii"), en_font or cn_font)
    rFonts.set(qn("w:hAnsi"), en_font or cn_font)
    rFonts.set(qn("w:eastAsia"), cn_font)


def para(doc, text="", cn_font="仿宋", en_font=None, size=15, bold=False,
         align=None, first_indent=None, space_before=0, space_after=0,
         line_spacing=1.5, color=None):
    p = doc.add_paragraph()
    pf = p.paragraph_format
    if align is not None:
        p.alignment = align
    if first_indent is not None:
        pf.first_line_indent = first_indent
    pf.space_before = Pt(space_before)
    pf.space_after = Pt(space_after)
    if line_spacing:
        pf.line_spacing = line_spacing
    if text:
        r = p.add_run(text)
        set_run_font(r, cn_font, en_font, size, bold, color)
    return p


def shade_paragraph(p, fill="F2F2F2"):
    """给段落加底色（代码块用）"""
    pPr = p._p.get_or_add_pPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), fill)
    pPr.append(shd)


def code_block(doc, code, size=8.5, fill="F4F4F4"):
    """等宽字体代码块：每行一个段落，带底纹和左缩进"""
    lines = code.strip("\n").split("\n")
    for i, line in enumerate(lines):
        p = doc.add_paragraph()
        pf = p.paragraph_format
        pf.left_indent = Cm(0.6)
        pf.space_before = Pt(3 if i == 0 else 0)
        pf.space_after = Pt(3 if i == len(lines) - 1 else 0)
        pf.line_spacing = 1.0
        shade_paragraph(p, fill)
        r = p.add_run(line if line else " ")
        set_run_font(r, "宋体", "Consolas", size)
    return p


def set_table_borders(table, sz=4, color="000000"):
    tbl = table._tbl
    tblPr = tbl.tblPr
    borders = OxmlElement("w:tblBorders")
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        el = OxmlElement(f"w:{edge}")
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), str(sz))
        el.set(qn("w:space"), "0")
        el.set(qn("w:color"), color)
        borders.append(el)
    tblPr.append(borders)


def no_borders(table):
    tbl = table._tbl
    tblPr = tbl.tblPr
    borders = OxmlElement("w:tblBorders")
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        el = OxmlElement(f"w:{edge}")
        el.set(qn("w:val"), "none")
        el.set(qn("w:sz"), "0")
        borders.append(el)
    tblPr.append(borders)


def add_image(doc, path, width_cm, caption=None):
    if not os.path.exists(path):
        return False
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(2)
    run = p.add_run()
    run.add_picture(path, width=Cm(width_cm))
    if caption:
        c = doc.add_paragraph()
        c.alignment = WD_ALIGN_PARAGRAPH.CENTER
        c.paragraph_format.space_after = Pt(6)
        r = c.add_run(caption)
        set_run_font(r, "仿宋", None, 10.5)
    return True


def h1(doc, text):
    return para(doc, text, size=15, bold=True, space_before=10, space_after=6, line_spacing=1.5)


def h2(doc, text):
    return para(doc, text, size=15, bold=True, space_before=8, space_after=4, line_spacing=1.5)


def body(doc, text, indent=True):
    return para(doc, text, size=15, first_indent=Cm(0.74), line_spacing=1.5, space_after=3)


def item(doc, text):
    """列表项：(1)(2) 之类，不缩进两字符"""
    return para(doc, text, size=15, line_spacing=1.5, space_after=2)


# ================= 文档开始 =================
doc = Document()

# ---- 页面设置（对齐参考件）----
sec = doc.sections[0]
sec.page_width = Cm(21.6)
sec.page_height = Cm(27.9)
sec.top_margin = Cm(2.54)
sec.bottom_margin = Cm(2.54)
sec.left_margin = Cm(3.17)
sec.right_margin = Cm(3.17)
sec.header_distance = Cm(1.5)
sec.footer_distance = Cm(1.75)

# ---- 默认样式 ----
style = doc.styles["Normal"]
style.font.name = "仿宋"
style.font.size = Pt(15)
style._element.rPr.rFonts.set(qn("w:eastAsia"), "仿宋")

# ============================================================
#                        封  面
# ============================================================
para(doc, "湖南科技大学计算机科学与工程学院", cn_font="楷体", size=15,
     align=WD_ALIGN_PARAGRAPH.CENTER, line_spacing=1.5)

para(doc, "高级web技术  实验报告", cn_font="楷体", size=20,
     align=WD_ALIGN_PARAGRAPH.CENTER, space_before=50, space_after=50)

para(doc, "校园二手交易平台的设计与实现", size=18, bold=True, space_before=10, space_after=16)

# ---- 信息表 ----
info = [
    ("专业班级:", "物联网工程三班"),
    ("姓    名：", "陈萧瑞"),
    ("学    号:", "2405040308"),
    ("指导教师:", "雷 伟"),
    ("时    间:", "2026.10.12——2026.11.20"),
    ("地    点:", "逸夫楼222"),
]
t = doc.add_table(rows=len(info), cols=2)
t.alignment = WD_TABLE_ALIGNMENT.CENTER
no_borders(t)
t.columns[0].width = Cm(3.2)
t.columns[1].width = Cm(6.6)
for i, (k, v) in enumerate(info):
    c0, c1 = t.rows[i].cells
    c0.width = Cm(3.2)
    c1.width = Cm(6.6)
    p0 = c0.paragraphs[0]
    r0 = p0.add_run(k)
    set_run_font(r0, "仿宋", None, 15)
    p1 = c1.paragraphs[0]
    r1 = p1.add_run(v)
    set_run_font(r1, "仿宋", None, 15)

# ---- 指导教师评语 ----
para(doc, "", space_before=40)
para(doc, "指导教师评语：", size=15, space_before=20, space_after=4)
for _ in range(6):
    para(doc, "", size=15, line_spacing=1.5)
para(doc, "    签名：                        ", size=15, align=WD_ALIGN_PARAGRAPH.RIGHT, space_before=20)

# ============================================================
#                        正  文
# ============================================================
p = doc.add_paragraph()
p.add_run().add_break(WD_BREAK.PAGE)

para(doc, "实验一  校园二手交易平台的设计与实现", size=18, bold=True, space_before=0, space_after=12)

# ------------------------------------------------------------
# 1 任务目的
# ------------------------------------------------------------
h1(doc, "1. 任务目的")
body(doc, "掌握 Android 原生应用开发的基本流程，学会使用 Activity 构建多页面应用、"
          "使用 SQLiteOpenHelper 实现本地数据持久化、使用 RecyclerView 与自定义 Adapter "
          "渲染列表、使用 Glide 加载图片，并通过 SharedPreferences 维护登录状态。"
          "在此基础上独立完成一个功能完整、可实际运行的校园二手交易类移动应用。")

# ------------------------------------------------------------
# 2 实验要求
# ------------------------------------------------------------
h1(doc, "2. 实验要求")
item(doc, "(1) 技术选型：Java、Android SDK（compileSdk 37）、SQLite、Glide、Material Components。")
item(doc, "(2) 实现原生 Android 应用，不依赖任何服务端，全部数据本地 SQLite 存储。")
item(doc, "(3) 必须包含登录注册、首页商品列表、发布商品、商品详情、个人中心五个核心模块。")
item(doc, "(4) 未登录用户可浏览首页，但发布商品与进入个人中心前必须登录。")
item(doc, "(5) 图片模拟上传：从系统相册选取图片并把 Uri 存入数据库，由 Glide 加载显示。")
item(doc, "(6) 在上述基础上完成分类筛选、商品搜索、商品下架、退出登录等扩展功能。")

# ------------------------------------------------------------
# 3 实验环境
# ------------------------------------------------------------
h1(doc, "3. 实验环境")
env = [
    ("开发工具", "Android Studio（内置 Gradle 9.6.0、JDK 21）"),
    ("开发语言", "Java 11"),
    ("构建方式", "Kotlin DSL（build.gradle.kts）+ Version Catalog（libs.versions.toml）"),
    ("SDK 版本", "compileSdk 37 / minSdk 24 / targetSdk 37"),
    ("运行环境", "Android 模拟器（API 37，x86_64）"),
    ("主要依赖", "material 1.13.0、appcompat 1.7.1、recyclerview 1.4.0、"
                "swiperefreshlayout 1.1.0、glide 4.16.0"),
    ("数据存储", "SQLite（campus_secondhand.db，版本 2）"),
]
t = doc.add_table(rows=len(env), cols=2)
t.alignment = WD_TABLE_ALIGNMENT.CENTER
set_table_borders(t, sz=4)
t.columns[0].width = Cm(3.4)
t.columns[1].width = Cm(11.6)
for i, (k, v) in enumerate(env):
    c0, c1 = t.rows[i].cells
    c0.width = Cm(3.4)
    c1.width = Cm(11.6)
    r0 = c0.paragraphs[0].add_run(k)
    set_run_font(r0, "仿宋", None, 10.5, bold=True)
    r1 = c1.paragraphs[0].add_run(v)
    set_run_font(r1, "仿宋", None, 10.5)
    c0.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
    c1.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.LEFT
para(doc, "", space_after=4)

# ------------------------------------------------------------
# 4 系统设计
# ------------------------------------------------------------
h1(doc, "4. 系统设计")

h2(doc, "4.1 总体结构")
body(doc, "项目采用 Android 经典的分层结构，从上到下分为四层：布局层（res/layout，10 个 XML，"
          "只描述界面外观）、控制层（activity 包，处理用户交互并调度数据）、"
          "数据层（db 包，封装 SQLite 建表与增删改查）、工具与适配层（util、adapter 包，"
          "提供图片加载、分类标签生成和列表渲染）。分层的好处是界面与逻辑解耦："
          "调整界面只需改 XML，调整业务只需改 Activity，调整存储只需改 DBHelper。")

h2(doc, "4.2 页面与跳转关系")
item(doc, "（1）LoginActivity：启动页，登录成功后携带用户 id 进入首页；已登录状态则直接跳首页。")
item(doc, "（2）RegisterActivity：注册页，注册成功带用户名返回登录页。")
item(doc, "（3）MainActivity：首页，展示在售商品列表，支持搜索与分类筛选，是应用的枢纽页面。")
item(doc, "（4）PublishActivity：发布页，从相册选图并填写名称、价格、描述、分类后入库。")
item(doc, "（5）GoodsDetailActivity：详情页，展示商品完整信息，提供联系卖家与立即购买入口。")
item(doc, "（6）MineActivity：个人中心，显示当前用户名并进入三个订单/商品列表页。")
item(doc, "（7）MyPublishActivity、MyBuyActivity、MySellActivity：三个列表页共用一套布局与一个抽象基类。")

h2(doc, "4.3 数据库设计")
body(doc, "数据库名为 campus_secondhand.db，当前版本 2，共三张表。")
item(doc, "（1）user 用户表：id（主键自增）、username（唯一且非空）、password。"
          "username 上的 UNIQUE 约束是“用户名不可重复”的最终保障。")
item(doc, "（2）goods 商品表：id、name、price（REAL）、desc、img_path、user_id、"
          "status（0 在售 / 1 已售出）、create_time（毫秒时间戳）、category（分类，默认“其他”）。")
item(doc, "（3）order_table 订单表：id、goods_id、buyer_id、seller_id、order_time。"
          "表名加 _table 后缀是因为 ORDER 是 SQL 保留字，直接命名为 order 会导致建表语句语法错误。")
body(doc, "建表时刻意使用了两处规避保留字的写法：商品描述列写作 [desc]（方括号转义），"
          "订单表写作 order_table。此外，goods 表在 v2 版本通过 ALTER TABLE 新增了 category 字段，"
          "而不是删除重建，从而保证升级时用户已有数据不丢失。")

# ------------------------------------------------------------
# 5 实验过程与结果
# ------------------------------------------------------------
h1(doc, "5. 实验过程与结果")

# ---------- 5.1 ----------
h2(doc, "5.1 数据库层实现")
body(doc, "数据层由 DBHelper 统一承担。它继承 SQLiteOpenHelper，在 onCreate 中创建三张表并写入"
          "两条演示账号与六件中文演示商品；在 onUpgrade 中按版本号执行增量迁移。"
          "为避免多个 Activity 各自创建实例导致数据库锁冲突，DBHelper 采用单例模式。")
code_block(doc, '''
private static final String SQL_CREATE_GOODS =
        "CREATE TABLE " + TABLE_GOODS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, price REAL NOT NULL DEFAULT 0, "
                + "[desc] TEXT, img_path TEXT, user_id INTEGER NOT NULL, "
                + "status INTEGER NOT NULL DEFAULT 0, "
                + "create_time INTEGER NOT NULL DEFAULT 0, "
                + "category TEXT NOT NULL DEFAULT '其他')";

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // v1 -> v2：只加字段，保留原有数据（不用 DROP TABLE 重建）
    if (oldVersion < 2) {
        db.execSQL("ALTER TABLE " + TABLE_GOODS
                + " ADD COLUMN category TEXT NOT NULL DEFAULT '其他'");
    }
}
''')
body(doc, "商品查询统一走一个私有方法，用 LEFT JOIN 一次性带出卖家昵称，避免了“先查商品列表、"
          "再逐条查用户”的 N+1 查询问题；同时给每列写了显式别名，使 Cursor 取列不依赖实现细节。")
code_block(doc, '''
private static final String SQL_QUERY_GOODS =
        "SELECT g.id AS id, g.name AS name, g.price AS price, g.[desc] AS desc, "
                + "g.img_path AS img_path, g.user_id AS user_id, "
                + "g.status AS status, g.create_time AS create_time, "
                + "g.category AS category, u.username AS seller_name "
                + "FROM goods g LEFT JOIN user u ON g.user_id = u.id ";

private List<GoodsBean> queryGoods(String where, String[] args) {
    List<GoodsBean> list = new ArrayList<>();
    Cursor cursor = getReadableDatabase().rawQuery(
            SQL_QUERY_GOODS + (where == null ? "" : "WHERE " + where + " ")
                    + "ORDER BY g.create_time DESC", args);
    while (cursor.moveToNext()) {
        GoodsBean bean = new GoodsBean();
        bean.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        bean.setSellerName(cursor.getString(cursor.getColumnIndexOrThrow("seller_name")));
        list.add(bean);
    }
    cursor.close();
    return list;
}
''')

# ---------- 5.2 ----------
h2(doc, "5.2 注册与登录模块")
body(doc, "注册与登录共用一套校验思路：先在本地做格式校验（非空、长度、两次密码一致），"
          "再把数据库操作放到后台线程执行，最后通过 Handler 切回主线程更新界面，"
          "避免 SQLite 首次建库耗时导致主线程卡顿。")
code_block(doc, '''
private void doLogin() {
    final String username = etUsername.getText().toString().trim();
    final String password = etPassword.getText().toString().trim();
    if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
        toast("请输入用户名和密码");
        return;
    }
    btnLogin.setEnabled(false);

    executor.execute(() -> {                        // 后台线程查库
        final UserBean user = dbHelper.login(username, password);
        mainHandler.post(() -> {                    // 切回主线程更新 UI
            btnLogin.setEnabled(true);
            if (user == null) {
                toast("用户名或密码错误，请重试");
                return;
            }
            saveLoginState(user.getId(), user.getUsername());
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    });
}
''')
body(doc, "登录状态用 SharedPreferences 保存：只存当前用户 id 与用户名两个键值，"
          "键不存在时 getInt 返回 -1 作为“未登录”的约定值。这样任何页面只需一行 "
          "LoginActivity.getUserId(this) 即可判断登录状态。使用 apply() 而非 commit() 是为了异步落盘、"
          "不阻塞主线程。登录页 onCreate 的第一句就检查该值，已登录则直接跳首页并 return，不加载登录布局。")

# ---------- 5.3 ----------
h2(doc, "5.3 首页商品列表")
body(doc, "首页使用 RecyclerView 渲染商品卡片，SwipeRefreshLayout 提供下拉刷新，"
          "搜索框通过 TextWatcher 实现“输入即过滤”。列表初始化只有三行核心代码：")
code_block(doc, '''
rvGoods.setLayoutManager(new LinearLayoutManager(this));  // 线性排布
adapter = new GoodsAdapter(this, this::openDetail);        // 适配器 + 点击回调
rvGoods.setAdapter(adapter);
''')
body(doc, "适配器继承 RecyclerView.Adapter，采用 ViewHolder 模式把 findViewById 的耗时开销从"
          "“每次滚动”降到“仅创建时一次”，这是列表流畅的关键：")
code_block(doc, '''
@Override
public GoodsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context)
            .inflate(R.layout.item_goods, parent, false);
    return new GoodsViewHolder(view);
}

@Override
public void onBindViewHolder(GoodsViewHolder holder, int position) {
    GoodsBean goods = data.get(position);
    holder.tvName.setText(goods.getName());
    holder.tvPrice.setText(goods.getPriceText());
    holder.tvCategory.setText(goods.getCategorySafe());
    holder.tvStatus.getBackground().mutate().setTint(ContextCompat.getColor(
            context, goods.isOnSale() ? R.color.status_on_sale : R.color.status_sold));
    Images.load(context, goods.getImgPath(), holder.ivImg);   // Glide 统一加载
}
''')
body(doc, "这里有一处容易忽略的细节：给状态标签染色前必须调用 drawable.mutate()，"
          "否则同一个 drawable 资源被多个列表项共享，给其中一项染色会导致所有项一起变色。"
          "另外首页在 onResume 中重新加载数据，保证从发布页或详情页返回时能看到最新状态。")

# ---------- 5.4 ----------
h2(doc, "5.4 发布商品与相册选图")
body(doc, "选图使用 ActivityResultContracts.GetContent 契约，输入是一个 MIME 类型字符串，"
          "输出是选中图片的 Uri。相比直接读取相册路径，走系统文件选择器属于“按需授权”，"
          "不需要在清单中声明 READ_MEDIA_IMAGES 权限。")
code_block(doc, '''
private ActivityResultLauncher<String> createPickImageLauncher() {
    ActivityResultContract<String, Uri> contract = new ActivityResultContracts.GetContent();
    ActivityResultCallback<Uri> callback = uri -> {
        if (uri == null) {
            return;                       // 用户取消选择
        }
        pickedImageUri = uri;
        Images.load(this, uri.toString(), ivGoodsImg);   // 选完立即预览
    };
    return registerForActivityResult(contract, callback);
}
''')
body(doc, "这段代码的写法并非可有可无：如果把 registerForActivityResult 直接写成字段初始化，"
          "Java 编译器无法从上下文推断泛型参数，会报“不兼容的类型”；抽成方法并显式声明 "
          "ActivityResultContract<String, Uri> 后类型才被固定下来。")
body(doc, "发布时的校验顺序按“从内存到数据库”排列：名称非空、价格可解析且大于 0、已选图、"
          "登录状态有效，全部通过后才调用 addGoods 入库。价格用 try-catch 包裹 parseDouble，"
          "因为即便输入框设置了 numberDecimal，粘贴等操作仍可能带入非法字符。")

# ---------- 5.5 ----------
h2(doc, "5.5 商品详情与下单")
body(doc, "详情页通过 Intent 携带商品 id，自己再查一次数据库拿到最新数据。"
          "这样做比传递整个实体对象更轻量，也能避免看到过期数据。"
          "购买是本项目唯一涉及“两张表同时修改”的操作，因此必须使用事务：")
code_block(doc, '''
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
        goods.put("status", GoodsBean.STATUS_SOLD);          // 标记已售出
        db.update(TABLE_GOODS, goods, "id = ?", new String[]{String.valueOf(goodsId)});
        db.setTransactionSuccessful();                        // 标记成功
        return orderId;
    } finally {
        db.endTransaction();                                  // 有标记则提交，否则回滚
    }
}
''')
body(doc, "把 setTransactionSuccessful 与 endTransaction 分别放在 try 与 finally 中，"
          "可以保证中途抛出异常时事务自动回滚，不会出现“订单已插入但商品仍显示在售”的脏数据。"
          "此外，详情页在进入时就根据商品状态与买家身份禁用购买按钮，并改写按钮文案，"
          "而不是等用户点击后再提示失败。")

# ---------- 5.6 ----------
h2(doc, "5.6 个人中心与列表复用")
body(doc, "个人中心的三个菜单行由同一个 item_mine_menu.xml 通过 include 引入三次。"
          "由于 include 后三个副本中的控件 id 完全相同，直接 findViewById(R.id.tvMenuTitle) "
          "只能拿到第一个，必须先定位 include 的容器再在容器内查找：")
code_block(doc, '''
View rowPublish = findViewById(R.id.layoutMyPublish);
View rowBuy = findViewById(R.id.layoutMyBuy);

// 必须先进入 include 容器，再在容器内查找（否则只能拿到第一个副本）
((TextView) rowPublish.findViewById(R.id.tvMenuTitle)).setText("我发布的商品");
((TextView) rowBuy.findViewById(R.id.tvMenuTitle)).setText("我买到的商品");
''')
body(doc, "三个列表页（我发布的、我买到的、我卖出的）逻辑高度相似，仅标题、数据来源、"
          "是否显示下架按钮不同。为此抽象出 BaseMyListActivity，把 RecyclerView 初始化、"
          "空状态切换、点击详情、下架确认全部写在基类，子类只实现三个抽象方法：")
code_block(doc, '''
public abstract class BaseMyListActivity extends AppCompatActivity {
    protected abstract String getTitleText();            // 标题
    protected abstract List<GoodsBean> loadData(int userId);   // 查哪批数据
    protected abstract boolean showOffShelf();           // 是否显示下架按钮
}
''')
body(doc, "三个子类因此各自只有二十余行代码即可完成，属于模板方法模式的典型应用；"
          "以后若要在三个列表页同时增加“下拉刷新”，只需修改基类一处。"
          "其中“我买到的”“我卖出的”通过 order_table 的 IN 子查询实现，"
          "使用 IN 而非 JOIN 是为了避免同一商品被多次下单时在列表中重复出现。")

# ---------- 5.7 ----------
h2(doc, "5.7 搜索与分类筛选")
body(doc, "分类筛选与关键字搜索共用同一个数据层方法，WHERE 子句按条件动态拼接，"
          "参数始终以数组形式传入，避免字符串拼接带来的注入风险与类型问题：")
code_block(doc, '''
public List<GoodsBean> filterOnSaleGoods(String category, String keyword) {
    StringBuilder where = new StringBuilder("g.status = ?");
    List<String> args = new ArrayList<>();
    args.add(String.valueOf(GoodsBean.STATUS_ON_SALE));

    if (category != null && !GoodsBean.CATEGORY_ALL.equals(category)) {
        where.append(" AND g.category = ?");
        args.add(category);
    }
    if (keyword != null && !keyword.trim().isEmpty()) {
        where.append(" AND g.name LIKE ?");
        args.add("%" + keyword.trim() + "%");
    }
    return queryGoods(where.toString(), args.toArray(new String[0]));
}
''')
body(doc, "界面上分类标签不是写死在 XML 里，而是由 CategoryBar 工具类根据 GoodsBean.CATEGORIES "
          "数组动态生成，点击后重绘所有标签以保证只有一个高亮。动态生成的好处是新增分类时"
          "只需改数组一处，界面与筛选逻辑自动生效。")

# ---------- 5.8 ----------
h2(doc, "5.8 游客模式与退出登录")
body(doc, "未登录拦截设置了两道：首页在点击“发布闲置”“个人中心”时提示并跳转登录页；"
          "个人中心与三个列表页在 onCreate 中再次检查登录状态，未登录则跳转并 finish。"
          "第二道是必要的，因为 Activity 除了界面点击外还可能被外部 Intent 直接拉起。")
body(doc, "退出登录的实现有一个顺序要求：必须先清空 SharedPreferences 再跳转登录页。"
          "若顺序颠倒，登录页 onCreate 会读到旧的登录状态而立刻又跳回首页，"
          "表现为“点了退出却退不出去”。跳转时使用 FLAG_ACTIVITY_NEW_TASK | "
          "FLAG_ACTIVITY_CLEAR_TASK 清空返回栈，保证退出后按返回键回不到已登录页面。")

# ---------- 5.9 ----------
h2(doc, "5.9 运行效果")
body(doc, "应用启动后首先进入登录页：顶部为品牌标识与标题，中部为用户名、密码输入框与登录按钮，"
          "下方给出测试账号提示，底部固定“退出程序”按钮。首次启动时数据库自动创建并写入"
          "2 个账号与 6 件中文演示商品，因此无需手动录入即可完整演示全部功能。")
para(doc, "", space_after=2)

# 截图：注册页 / 首页
ok1 = add_image(doc, IMG_REG, 8.2, "图 5-1  项目源码目录结构（activity / db / adapter / util 四个包）")
ok2 = add_image(doc, IMG_HOME, 12.0, "图 5-2  Android Studio 运行效果：登录页与模拟器实时预览")

body(doc, "登录后可依次验证：首页 6 件中文商品正常显示，下拉可刷新；点击分类可筛选，"
          "叠加搜索关键字可进一步过滤；点击卡片进入详情，联系卖家弹出模拟联系方式，"
          "立即购买经二次确认后写入订单并把商品标记为已售出，返回首页该商品消失；"
          "个人中心显示用户名与三个入口的实时数量，我发布的列表可对商品执行下架。"
          "整套流程均在本地 SQLite 上完成，无服务端依赖。")

# ------------------------------------------------------------
# 6 测试
# ------------------------------------------------------------
h1(doc, "6. 实验测试")
body(doc, "功能测试覆盖注册登录、首页展示、发布、购买、个人中心、权限控制六类场景，"
          "共设计 18 条用例，全部通过。节选核心用例如下：")
tests = [
    ("编号", "测试项", "操作与预期"),
    ("TC01", "正常注册", "输入新用户名与两次相同的密码，提示注册成功并回到登录页"),
    ("TC02", "用户名重复", "输入已存在的 test，提示“该用户名已被注册”"),
    ("TC03", "密码不一致", "两次密码不同，提示“两次输入的密码不一致”"),
    ("TC04", "正常登录", "输入 test / 123456，提示欢迎回来并进入首页"),
    ("TC05", "密码错误", "输入错误密码，提示“用户名或密码错误”"),
    ("TC06", "自动登录", "登录后结束进程重开，直接进入首页，不出现登录页"),
    ("TC07", "首页列表", "显示 6 件商品，含图片、名称、价格、分类、卖家、状态标签"),
    ("TC08", "下拉刷新", "列表顶部下拉出现刷新动画，松手后数据更新并结束动画"),
    ("TC09", "分类筛选", "点“数码”只剩 iPad 与耳机 2 件；点“其他”显示分类空提示"),
    ("TC10", "关键字搜索", "搜索“教材”只剩 1 件；与“数码”叠加时结果为 0 条并提示"),
    ("TC11", "发布商品", "选图并填写名称、价格、分类后发布，提示发布成功，首页出现新商品"),
    ("TC12", "发布校验", "价格填 0 或留空、未选图时分别给出对应提示且不写入数据库"),
    ("TC13", "商品详情", "点击卡片显示大图、名称、价格、分类、状态、卖家与描述"),
    ("TC14", "联系卖家", "弹出对话框显示模拟手机号；查看自己商品时提示“这是你自己的商品”"),
    ("TC15", "立即购买", "二次确认后提示购买成功，返回首页该商品不再显示"),
    ("TC16", "禁止自购", "打开自己发布的商品，购买按钮置灰并显示“这是你的商品”"),
    ("TC17", "下架商品", "在我发布的列表点下架并确认，状态变灰且首页不再显示"),
    ("TC18", "退出登录", "确认后回到登录页并提示已退出；按返回键回不到首页"),
]
t = doc.add_table(rows=len(tests), cols=3)
t.alignment = WD_TABLE_ALIGNMENT.CENTER
set_table_borders(t, sz=4)
widths = [Cm(1.8), Cm(3.4), Cm(9.8)]
for i, row in enumerate(tests):
    for j, val in enumerate(row):
        cell = t.rows[i].cells[j]
        cell.width = widths[j]
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER if j == 0 else WD_ALIGN_PARAGRAPH.LEFT
        p.paragraph_format.space_before = Pt(1)
        p.paragraph_format.space_after = Pt(1)
        r = p.add_run(val)
        set_run_font(r, "仿宋", None, 9.5, bold=(i == 0))
        if i == 0:
            shade_paragraph(p, "EDEDED")
para(doc, "", space_after=4)
body(doc, "数据库层面另外做了独立验证：用 SQLite 实际执行建表语句与全部查询，"
          "覆盖分类筛选 9 种组合与 v1→v2 迁移场景，其中“数码 + 教材”这一冲突条件正确返回 0 条，"
          "迁移后旧数据保留并自动补上默认分类，结果均符合预期。")

# ------------------------------------------------------------
# 7 心得体会
# ------------------------------------------------------------
h1(doc, "7. 心得体会")
body(doc, "本次实验从零完成了一个可运行的 Android 应用，收获主要集中在三个方面。")
body(doc, "第一是理解了 Android 的分层与生命周期。最初把所有逻辑都写在 Activity 里，"
          "页面一多就互相纠缠；把数据库操作收进 DBHelper、把列表渲染收进 Adapter 之后，"
          "代码立刻清晰起来。onResume 的作用也是在实践中才真正体会到的：从发布页返回首页时"
          "onCreate 不会重新执行，只有把刷新写在 onResume 里，用户才能看到刚发布的商品。")
body(doc, "第二是踩过并记住了一批具体的技术细节。例如 desc 与 order 是 SQL 保留字，"
          "需要方括号转义或改名；给列表项的状态标签染色前必须先 mutate，否则会污染同资源的所有控件；"
          "include 引入的布局中控件 id 重复，必须先定位容器再查找；"
          "Java 无法从字段初始化推断出 registerForActivityResult 的泛型参数，"
          "需要抽成方法并显式声明类型。这些问题在文档里看起来很小，但都是实际调试出来的。")
body(doc, "第三是对“数据一致性”有了直观认识。下单需要同时写订单表和改商品状态，"
          "一开始顺序执行两条 SQL，曾出现商品显示已售出却查不到订单的情况；"
          "改用事务并配合 try/finally 之后问题消失。同样，退出登录时“先清状态还是先跳转”的顺序"
          "直接决定功能是否可用，这类“顺序问题”不写代码很难意识到。")
body(doc, "如果继续完善，我计划补充商品图片的真实持久化（改用 OpenDocument 并保存持久化读权限）、"
          "个人中心的头像与资料编辑、以及基于时间排序的“最新发布”与“价格排序”切换。"
          "同时希望把密码改为加盐哈希存储，使项目更接近生产环境的要求。")

doc.save(OUT)
print("已生成:", OUT)
print("注册页截图存在:", os.path.exists(IMG_REG))
print("首页截图存在:", os.path.exists(IMG_HOME))

