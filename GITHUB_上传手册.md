# 上传 GitHub 操作手册（TheSchoolSecondHandTrade）

> 适用场景：Windows、无 git 环境。按顺序做完即可，全程约 10 分钟。
> 仓库内容已准备在：`C:\Users\15586\Documents\deepseek-harness\default-workspace\TheSchoolSecondHandTrade`

---

## 第 1 步：安装 Git for Windows

1. 打开 https://git-scm.com/download/win ，下载 **64-bit Git for Windows Setup**
2. 双击安装，**一路点 Next 保持默认**即可（默认配置就是对的）
3. 安装完成后**关掉所有已打开的终端窗口**（PowerShell / CMD），重新打开一个，执行：

```bat
git --version
```

看到类似 `git version 2.x.x.windows.1` 就说明装好了。

---

## 第 2 步：配置你的身份

在终端里执行以下两条（把名字和邮箱换成你自己的）：

```bat
git config --global user.name "陈萧瑞"
git config --global user.email "你的GitHub邮箱@example.com"
```

> 这里的邮箱建议和 GitHub 账号的邮箱一致，否则 GitHub 上提交记录不会关联到你的头像。

---

## 第 3 步：在 GitHub 上创建空仓库

1. 登录 GitHub，右上角 `+` → **New repository**
2. **Repository name** 填：`TheSchoolSecondHandTrade`
3. **Description** 填：`校园二手交易平台（Android + Java + SQLite）`
4. 可见性选 **Public**（作业项目建议公开，方便老师查看）
5. ⚠️ **不要勾选** "Add a README file"、"Add .gitignore"、"Choose a license"
   （因为本地已经有这些文件了，勾选会导致推送时冲突）
6. 点 **Create repository**

创建成功后页面会显示仓库地址，形如：

```
https://github.com/你的用户名/TheSchoolSecondHandTrade.git
```

**把这个地址记下来，第 5 步要用。**

---

## 第 4 步：生成 Personal Access Token（PAT）

GitHub 从 2021 年起**不再接受账号密码推送**，必须用 Token 当密码。

1. 点击右上角头像 → **Settings**
2. 左侧最下方 → **Developer settings**
3. 展开 **Personal access tokens** → 选 **Tokens (classic)**
4. 右上角 **Generate new token** → **Generate new token (classic)**
5. 填写：
   - **Note**：`校园二手交易上传`（随便写，方便识别）
   - **Expiration**：选 `90 days`（够用了）
   - **Select scopes**：**勾选 `repo`**（整个 repo 大项，它下面的子项会自动勾上）
6. 拉到最下面点 **Generate token**
7. **立刻复制那串以 `ghp_` 开头的字符串**

> ⚠️ Token 只显示这一次，关掉页面就再也看不到了。先粘贴到记事本里存好。
>
> 如果仓库是 Public，理论上 `public_repo` 子项就够了，但勾选整个 `repo` 最省事，
> 以后要改私有仓库也不用重新生成。

---

## 第 5 步：初始化仓库并推送

打开终端，**逐段执行**（遇到需要输入的地方见下方说明）。

### 5.1 进入项目目录

```bat
cd /d "C:\Users\15586\Documents\deepseek-harness\default-workspace\TheSchoolSecondHandTrade"
```

### 5.2 初始化并提交

```bat
git init
git add .
git commit -m "feat: 校园二手交易平台初始版本（登录注册/首页/发布/详情/个人中心）"
```

### 5.3 关联远程仓库

把下面命令里的 `你的用户名` 换成你的 GitHub 用户名：

```bat
git remote add origin https://github.com/你的用户名/TheSchoolSecondHandTrade.git
```

### 5.4 推送（这一步要输入账号和 Token）

```bat
git branch -M main
git push -u origin main
```

此时终端会提示：

```
Username for 'https://github.com': 你的GitHub用户名
Password for 'https://你的用户名@github.com': 粘贴刚才的PAT
```

**关键点：**

- 用户名输入你的 GitHub 用户名，回车
- 密码处**粘贴 PAT**（`ghp_` 开头那串），**粘贴时屏幕不会显示任何字符，这是正常的**，直接回车即可
- 如果粘贴不进去，用鼠标右键点终端窗口 → 粘贴（Ctrl+V 在某些终端里不生效）

推送成功后终端会显示 `Branch 'main' set up to track remote branch 'main'`。
刷新 GitHub 页面就能看到代码了。

---

## 第 6 步：让 Windows 记住凭据（推荐）

第一次推送后执行一次，以后就不用反复输入 Token：

```bat
git config --global credential.helper manager
```

如果机器上没有 Git Credential Manager，可以改用：

```bat
git config --global credential.helper store
```

> `store` 会把凭据以**明文**保存在 `~/.git-credentials`，安全性较低但最省事。
> 作业项目可以接受；如果在意安全就用 `manager`。

---

## 第 7 步：验证推送结果

在 GitHub 仓库页面确认：

- [ ] 能看到 `app/`、`gradle/`、`build.gradle.kts`、`README.md`、`.gitignore`
- [ ] README 正常渲染（有表格、代码块、中文不乱码）
- [ ] **没有** `build/`、`.gradle/`、`local.properties`、`.idea/` 等本机文件
- [ ] 提交记录里有一条 `feat: 校园二手交易平台初始版本...`

可以在本地终端用这条命令确认暂存区没有漏掉的垃圾文件：

```bat
git ls-files | findstr /i "build/ .gradle/ local.properties"
```

没有输出就说明干净了。

---

## 后续修改代码后怎么再上传

每次改完代码，只需三步：

```bat
cd /d "C:\Users\15586\Documents\deepseek-harness\default-workspace\TheSchoolSecondHandTrade"
git add .
git commit -m "fix: 修复某某问题"
git push
```

**建议的提交信息格式**（让提交记录看起来专业）：

| 前缀 | 用于 |
|---|---|
| `feat:` | 新增功能，如 `feat: 增加商品分类筛选` |
| `fix:` | 修复问题，如 `fix: 修复退出登录后弹回首页` |
| `docs:` | 文档改动，如 `docs: 补充 README 运行说明` |
| `refactor:` | 重构，如 `refactor: 抽取列表页基类` |
| `style:` | 格式调整，如 `style: 统一代码缩进` |

---

## 想分多次提交，让提交记录更好看（可选）

如果希望 GitHub 上显示多条有意义的提交（答辩时更显得有过程），可以先只加部分文件：

```bat
git init

git add README.md .gitignore gradle/ gradlew gradlew.bat build.gradle.kts settings.gradle.kts gradle.properties
git commit -m "chore: 初始化 Android 项目与 Gradle 构建配置"

git add app/src/main/res
git commit -m "feat: 完成全部页面布局与主题资源"

git add app/src/main/java/com/example/theschoolsecond_handtrade/db
git commit -m "feat: 实现 SQLite 数据层（三张表 + 事务下单 + 版本迁移）"

git add app/src/main/java/com/example/theschoolsecond_handtrade/adapter app/src/main/java/com/example/theschoolsecond_handtrade/util
git commit -m "feat: 实现列表适配器与图片加载、分类标签工具"

git add app/src/main/java/com/example/theschoolsecond_handtrade/activity
git commit -m "feat: 实现登录注册、首页、发布、详情、个人中心等页面"

git add .
git commit -m "chore: 补充 AndroidManifest 与测试资源"

git remote add origin https://github.com/你的用户名/TheSchoolSecondHandTrade.git
git branch -M main
git push -u origin main
```

---

## 常见问题

| 现象 | 原因 | 解决 |
|---|---|---|
| `remote: Support for password authentication was removed` | 用了账号密码而非 PAT | 重新执行 `git push`，密码处粘贴 PAT |
| `Authentication failed` | PAT 过期或没勾 `repo` 权限 | 重新生成 Token，务必勾选 `repo` |
| `remote origin already exists` | 已经加过 origin | `git remote set-url origin https://github.com/你的用户名/TheSchoolSecondHandTrade.git` |
| `failed to push some refs` | 远程仓库已有 README 等文件 | 建仓库时不要勾选初始化文件；已勾选则先 `git pull --rebase origin main` |
| 推上去发现带了 `build/` | `.gitignore` 没生效（文件已被 add） | `git rm -r --cached app/build` 后重新 commit |
| 中文文件名显示乱码 | Windows 下 git 默认转义 | `git config --global core.quotepath false` |
| `LF will be replaced by CRLF` 警告 | Windows 换行符提示 | 可忽略；或执行 `git config --global core.autocrlf true` |

---

## 仓库内容说明

仓库里**只有源码和构建配置**，不包含：

- `build/`（编译产物，几十 MB）
- `.gradle/`（Gradle 缓存）
- `local.properties`（本机 SDK 路径，换电脑会失效）
- `.idea/`（IDE 本机配置，部分保留共享配置）
- `.gitignore` 里已明确排除签名文件与密钥

克隆者用 Android Studio 打开后，Gradle 会自动下载依赖并生成缺失的 `local.properties`。
