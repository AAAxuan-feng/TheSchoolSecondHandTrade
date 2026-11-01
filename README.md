# 校园二手交易平台（TheSchoolSecondHandTrade）

一个面向在校学生的 **原生 Android 校园二手交易 App**。学生可以注册登录、浏览校园二手商品、发布闲置物品（支持从相册选图）、查看商品详情、联系卖家、下单购买，并在个人中心管理自己发布/买到/卖出的商品。

**无任何服务端依赖**，全部数据存储于本地 SQLite，图片采用「系统相册 Uri + 内置演示图」的模拟上传方案，克隆后用 Android Studio 打开即可直接运行。

---

## 技术栈

| 分类 | 内容 |
|---|---|
| 语言 | Java 11 |
| 构建 | Gradle 9.6（Kotlin DSL `build.gradle.kts` + Version Catalog `libs.versions.toml`） |
| SDK | compileSdk 37 / minSdk 24 / targetSdk 37 |
| UI | AppCompat + Material Components（`MaterialCardView`、`MaterialButton`、`TextInputLayout`） |
| 列表 | RecyclerView + 自定义 Adapter（ViewHolder 模式） |
| 刷新 | SwipeRefreshLayout |
| 图片 | Glide 4.16.0 |
| 存储 | SQLite（`SQLiteOpenHelper`）、SharedPreferences |
| 架构 | Activity + 分层（activity / db / adapter / util） |

---

## 功能清单

### 核心功能

- **注册 / 登录**：用户名密码存入 SQLite，用户名唯一约束；登录状态用 SharedPreferences 保持，支持自动登录
- **首页商品列表**：RecyclerView 卡片展示商品图片、名称、价格、分类、卖家、状态；下拉刷新
- **发布商品**：从系统相册选图、填写名称/价格/描述、选择分类后入库，并关联当前登录用户
- **商品详情**：大图 + 完整信息展示；「联系卖家」弹出模拟联系方式；「立即购买」事务写入订单并标记商品已售
- **个人中心**：显示当前用户名与三类数量统计，进入「我发布的 / 我买到的 / 我卖出的」三个列表

### 扩展功能

- **商品分类**：书籍 / 数码 / 生活用品 / 运动 / 其他，首页可横向筛选，可与关键字叠加
- **商品搜索**：输入即过滤（TextWatcher + SQL `LIKE` 模糊匹配）
- **商品下架**：发布者可将自己的商品下架，SQL 层带 `user_id` 条件防越权
- **退出登录**：清空登录状态 + 清空返回栈，退出后按返回键回不到已登录页面
- **游客模式**：未登录可浏览首页，但发布商品与进入个人中心会被拦截并引导登录
- **预置演示数据**：首次启动自动写入 2 个账号与 6 件中文商品，无需手动录入即可演示

---

## 测试账号

| 用户名 | 密码 | 说明 |
|---|---|---|
| `test` | `123456` | 主测试账号，预置发布了 3 件商品 |
| `xiaoming` | `123456` | 演示卖家账号，预置发布了 3 件商品 |

> 预置数据只在**数据库首次创建**时写入。若要重置数据：卸载 App 重装，或在 Device Explorer 中删除
> `/data/data/com.example.theschoolsecond_handtrade/databases/campus_secondhand.db`。

---

## 数据库设计

数据库：`campus_secondhand.db`，版本 **2**

### user 用户表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER PK AUTOINCREMENT | 用户 id |
| username | TEXT UNIQUE NOT NULL | 用户名（唯一） |
| password | TEXT NOT NULL | 密码 |

### goods 商品表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER PK AUTOINCREMENT | 商品 id |
| name | TEXT NOT NULL | 商品名称 |
| price | REAL | 价格 |
| [desc] | TEXT | 商品描述（`desc` 是 SQL 保留字，用方括号转义） |
| img_path | TEXT | 图片：`res:demo_xxx` 表示内置演示图，`content://...` 表示相册 Uri |
| user_id | INTEGER | 发布者用户 id |
| status | INTEGER | 0 = 在售，1 = 已售出 |
| create_time | INTEGER | 发布时间（毫秒时间戳） |
| category | TEXT | 分类，默认「其他」（v2 新增） |

### order_table 订单表

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER PK AUTOINCREMENT | 订单 id |
| goods_id | INTEGER | 商品 id |
| buyer_id | INTEGER | 买家 id |
| seller_id | INTEGER | 卖家 id |
| order_time | TEXT | 下单时间 `yyyy-MM-dd HH:mm:ss` |

> 表名使用 `order_table` 而非 `order`，因为 `ORDER` 是 SQL 保留字。
>
> 版本迁移：v1 → v2 通过 `ALTER TABLE goods ADD COLUMN category ...` 新增字段，
> **不使用 DROP TABLE 重建**，保证升级时用户已有数据不丢失。

---

## 项目结构

```
app/src/main/
├── java/com/example/theschoolsecond_handtrade/
│   ├── activity/                     # 页面层
│   │   ├── LoginActivity.java        # 登录 + 登录状态保持 + 退出程序
│   │   ├── RegisterActivity.java     # 注册 + 表单校验 + 用户名查重
│   │   ├── MainActivity.java         # 首页：列表 / 搜索 / 分类 / 下拉刷新
│   │   ├── PublishActivity.java      # 发布：相册选图 + 分类 + 入库
│   │   ├── GoodsDetailActivity.java  # 详情：联系卖家 + 下单
│   │   ├── MineActivity.java         # 个人中心 + 退出登录
│   │   ├── BaseMyListActivity.java   # 三个列表页的公共基类（模板方法）
│   │   ├── MyPublishActivity.java    # 我发布的（可下架）
│   │   ├── MyBuyActivity.java        # 我买到的
│   │   └── MySellActivity.java       # 我卖出的
│   ├── adapter/                      # 列表适配器
│   │   ├── GoodsAdapter.java         # 首页商品卡片
│   │   └── MyGoodsAdapter.java       # 我的商品（带下架按钮）
│   ├── db/                           # 数据层
│   │   ├── DBHelper.java             # 建表 + 全部增删改查 + 事务
│   │   ├── UserBean.java
│   │   ├── GoodsBean.java
│   │   └── OrderBean.java
│   └── util/                         # 工具层
│       ├── Images.java               # 图片加载统一入口（Glide）
│       └── CategoryBar.java          # 分类标签动态生成
└── res/
    ├── layout/                       # 10 个布局
    ├── drawable/                     # 界面背景 + 8 张演示商品图
    └── values/                       # colors / strings / dimens / themes
```

---

## 运行方式

1. 用 **Android Studio** 打开本项目根目录（等待 Gradle Sync 完成）
2. 连接模拟器或真机（Android 7.0 / API 24 及以上）
3. 点击 Run 运行
4. 首次启动会自动创建数据库并写入演示数据，直接用 `test / 123456` 登录即可

> 命令行构建需要 JDK 17+，并设置 `JAVA_HOME` 指向该 JDK。

---

## 关键实现说明

| 主题 | 处理方式 |
|---|---|
| SQL 保留字 | 商品描述列写作 `[desc]`；订单表名写作 `order_table` |
| 数据一致性 | 下单同时写订单表与改商品状态，使用 `beginTransaction` / `setTransactionSuccessful` / `endTransaction` 放在 `try`-`finally` 中保证原子性 |
| 线程模型 | 数据库写操作（登录/注册）在 `ExecutorService` 后台线程执行，通过 `Handler(Looper.getMainLooper())` 切回主线程更新 UI |
| 登录状态 | SharedPreferences 只存 `userId` 与 `username`，未登录时 `getInt` 返回 `-1` 作为约定值 |
| 图片加载 | `Images.toModel()` 统一把 `res:xxx` 解析为资源 id、把 `content://` 解析为 `Uri`，再交给 Glide |
| Drawable 染色 | 状态标签染色前调用 `getBackground().mutate()`，避免污染同资源的所有控件 |
| include 布局 | `item_mine_menu.xml` 被 include 三次，控件 id 重复，必须先定位容器再 `findViewById` |
| 退出登录顺序 | 必须先清 SharedPreferences 再跳转，否则登录页会读到旧状态又跳回首页 |
| 相册选图 | 使用 `ActivityResultContracts.GetContent`，走系统文件选择器属按需授权，无需申请 `READ_MEDIA_IMAGES` |

---

## 已知限制

- 密码以明文存储于本地数据库，仅用于课程演示；生产环境应改为加盐哈希
- 相册选图使用 `GetContent` 的临时读权限，卸载重装后旧图片 Uri 可能失效；如需长期有效可改用 `OpenDocument` 并保存持久化权限
- 「联系卖家」的联系方式为本地生成的模拟数据，不涉及真实隐私
- 按分类/关键字筛选时，读操作在主线程直接查库（数据量小）；若数据量增大应改为异步加载

---

## 文档

| 文件 | 说明 |
|---|---|
| `docs/校园二手交易平台_实验报告.docx` | 课程实验报告（设计、实现、关键代码、测试、心得） |
| `docs/make_report.py` | 报告生成脚本（python-docx），可修改内容后重新生成 |
| `GITHUB_上传手册.md` | 本仓库上传 GitHub 的完整操作步骤（含 PAT 认证） |

---

## 附：数据库建表 SQL

```sql
CREATE TABLE user (
    id       INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL
);

CREATE TABLE goods (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    price       REAL NOT NULL DEFAULT 0,
    [desc]      TEXT,
    img_path    TEXT,
    user_id     INTEGER NOT NULL,
    status      INTEGER NOT NULL DEFAULT 0,
    create_time INTEGER NOT NULL DEFAULT 0,
    category    TEXT NOT NULL DEFAULT '其他'
);

CREATE TABLE order_table (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    goods_id   INTEGER NOT NULL,
    buyer_id   INTEGER NOT NULL,
    seller_id  INTEGER NOT NULL,
    order_time TEXT NOT NULL
);
```
