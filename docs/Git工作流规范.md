# Git工作流规范教程

> **订单可视化平台 - Git使用规范**
>
> **版本**：v1.0
> **创建时间**：2026-01-09
> **作者**：开发组

---

## 📋 目录

- [1. Git基础概念](#1-git基础概念)
- [2. 分支管理策略](#2-分支管理策略) 
- [3. 提交规范](#3-提交规范)
- [4. 常用命令速查](#4-常用命令速查)
- [5. 常见问题解决](#5-常见问题解决)
- [6. 团队协作流程](#6-团队协作流程)

---

## 1. Git基础概念

### 1.1 什么是Git？

**Git**：分布式版本控制系统
- 📝 记录代码的每一次修改
- 🔄 支持多人协作开发
- ⏪ 可以随时回退到历史版本
- 🌿 支持多分支并行开发

### 1.2 核心概念

**工作区、暂存区、本地仓库、远程仓库**：

```
┌─────────────┐
│  工作区      │  ← 你编辑代码的地方
│  (Workspace)│
└──────┬──────┘
       │ git add
       ▼
┌─────────────┐
│  暂存区      │  ← 准备提交的文件
│  (Staging)  │
└──────┬──────┘
       │ git commit
       ▼
┌─────────────┐
│  本地仓库    │  ← 本地的提交历史
│ (Local Repo)│
└──────┬──────┘
       │ git push
       ▼
┌─────────────┐
│  远程仓库    │  ← GitHub/GitLab上的仓库
│ (Remote)    │
└─────────────┘
```

### 1.3 Git文件状态

| 状态 | 说明 | 操作 |
|------|------|------|
| **Untracked** | 新文件，Git未追踪 | `git add` |
| **Modified** | 已修改，未暂存 | `git add` |
| **Staged** | 已暂存，准备提交 | `git commit` |
| **Unmodified** | 未修改 | 无需操作 |

---

## 2. 分支管理策略

### 2.1 分支命名规范

**主分支**：
```
main        ← 生产环境代码（永远保持稳定）
develop     ← 开发环境代码（最新开发进度）
```

**功能分支**：
```
feature/功能名称          ← 新功能开发
├─ feature/user-registration
├─ feature/order-management
└─ feature/dashboard

fix/问题描述              ← Bug修复
├─ fix/login-error
└─ fix/database-connection

hotfix/紧急问题            ← 生产环境紧急修复
├─ hotfix/security-patch
└─ hotfix/data-loss

release/版本号            ← 发布准备
├─ release/v1.0.0
└─ release/v1.1.0
```

**分支命名规则**：
- ✅ 使用小写字母
- ✅ 使用连字符 `-` 分隔单词
- ✅ 见名知意，描述清晰
- ❌ 不要使用中文
- ❌ 不要使用特殊字符

### 2.2 Git Flow工作流（推荐）

```
main分支（生产环境）
  ↑
  │ 合并发布版本
  │
develop分支（开发环境）
  ↑
  │ 合并功能分支
  │
feature/xxx（功能分支）
  ↑
  │ 开发完成后合并到develop
  │
开发者A的本地分支
```

**完整流程**：

```bash
# 1. 从develop创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/user-registration

# 2. 在功能分支开发
git add .
git commit -m "feat: 添加用户注册功能"

# 3. 推送功能分支到远程
git push origin feature/user-registration

# 4. 在GitHub上创建Pull Request
# feature/user-registration → develop

# 5. 代码审核通过后合并

# 6. 删除本地功能分支
git branch -d feature/user-registration

# 7. 删除远程功能分支
git push origin --delete feature/user-registration
```

### 2.3 分支保护策略

**受保护分支**：
- `main` - 只允许通过PR合并，需要审核
- `develop` - 只允许通过PR合并，需要审核

**不受保护分支**：
- `feature/*` - 开发者可以直接推送
- `fix/*` - 开发者可以直接推送

---

## 3. 提交规范

### 3.1 提交信息格式（Conventional Commits）

**格式**：
```
<类型>(<范围>): <简短描述>

<详细描述>

<页脚>
```

**类型（type）**：

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat: 添加用户注册功能` |
| `fix` | Bug修复 | `fix: 修复登录错误` |
| `docs` | 文档更新 | `docs: 更新API文档` |
| `style` | 代码格式调整 | `style: 调整缩进` |
| `refactor` | 重构 | `refactor: 重构用户服务` |
| `perf` | 性能优化 | `perf: 优化数据库查询` |
| `test` | 测试相关 | `test: 添加单元测试` |
| `chore` | 构建/工具相关 | `chore: 更新依赖` |
| `revert` | 回退提交 | `revert: 回退提交abc123` |

**范围（scope）**：
```
user, order, shipment, auth, database, api, ui, config
```

**示例**：

```bash
# 简单提交
git commit -m "feat: 添加用户注册功能"

# 带范围的提交
git commit -m "feat(user): 添加用户注册功能"

# 带详细描述的提交
git commit -m "fix(auth): 修复Token过期问题

- 修正Token过期时间计算逻辑
- 增加Token刷新机制
- 添加过期提示信息

Closes #123"
```

### 3.2 提交信息最佳实践

**✅ 好的提交**：
```bash
"feat: 添加用户注册功能"
"fix: 修复登录时密码错误提示"
"docs: 更新API接口文档"
```

**❌ 不好的提交**：
```bash
"更新"          # 太模糊
"fix bugs"      # 不具体
"final fix"     # 永远不要说"最终修复"
"update code"   # 没说明改了什么
"123"          # 没有任何意义
```

### 3.3 提交粒度

**原则：一个提交只做一件事**

```bash
# ✅ 好的做法
git add UserService.java
git commit -m "feat: 添加用户注册服务"

git add UserServiceTest.java
git commit -m "test: 添加用户注册单元测试"

# ❌ 不好的做法
git add UserService.java UserServiceTest.java OrderService.java
git commit -m "添加多个功能"  # 混在一起了
```

---

## 4. 常用命令速查

### 4.1 基础操作

```bash
# 初始化仓库
git init

# 克隆远程仓库
git clone https://github.com/hjc123abc/order-platform.git

# 查看当前状态
git status

# 查看分支
git branch

# 查看所有分支（包括远程）
git branch -a

# 切换分支
git checkout main
git switch main  # Git 2.23+ 新命令

# 创建并切换到新分支
git checkout -b feature/new-feature
git switch -c feature/new-feature  # Git 2.23+
```

### 4.2 添加和提交

```bash
# 添加所有修改
git add .

# 添加指定文件
git add UserService.java

# 查看暂存区状态
git diff --cached

# 提交暂存区的修改
git commit -m "feat: 添加用户功能"

# 添加并提交（一步完成）
git commit -am "fix: 修复Bug"

# 修改最后一次提交信息
git commit --amend -m "新的提交信息"

# 撤销最后一次提交（保留修改）
git reset --soft HEAD~1

# 撤销最后一次提交（丢弃修改）
git reset --hard HEAD~1
```

### 4.3 远程操作

```bash
# 查看远程仓库
git remote -v

# 添加远程仓库
git remote add origin https://github.com/user/repo.git

# 推送到远程
git push origin main

# 推送所有分支到远程
git push --all origin

# 拉取远程更新
git pull origin main

# 拉取远程更新（不合并）
git fetch origin

# 拉取远程更新（变基方式）
git pull --rebase origin main

# 删除远程分支
git push origin --delete feature/old-feature
```

### 4.4 分支合并

```bash
# 合并指定分支到当前分支
git merge feature/new-feature

# 变基（整理提交历史）
git rebase main

# 查看分支合并图
git log --graph --oneline --all

# 删除已合并的本地分支
git branch -d feature/old-feature

# 强制删除未合并的分支
git branch -D feature/old-feature
```

### 4.5 查看历史

```bash
# 查看提交历史
git log

# 查看简洁的提交历史
git log --oneline

# 查看最近5次提交
git log --oneline -5

# 查看提交文件的修改
git log -p

# 查看某个文件的修改历史
git log -- UserService.java

# 查看暂存区和工作区的差异
git diff

# 查看某次提交的修改
git show abc1234
```

---

## 5. 常见问题解决

### 5.1 冲突解决

**场景**：合并时出现冲突

```bash
# 第1步：拉取最新代码
git pull origin main

# 第2步：如果出现冲突，先查看冲突文件
git status

# 第3步：手动编辑冲突文件
# 找到 <<<<<<< ======= >>>>>>> 标记
# 保留需要的代码，删除标记

# 第4步：标记冲突已解决
git add <冲突文件>

# 第5步：完成合并
git commit
```

**冲突示例**：

```java
<<<<<<< HEAD
public String getUsername() {
    return name;
}
=======
public String getUsername() {
    return this.name;
}
>>>>>>> feature/new-feature
```

**解决后**：

```java
public String getUsername() {
    return this.name;  // 选择需要的版本
}
```

### 5.2 撤销操作

**场景1：撤销工作区的修改**

```bash
# 撤销单个文件的修改
git restore UserService.java

# 撤销所有修改
git restore .
```

**场景2：撤销暂存区的修改**

```bash
# 从暂存区移除，保留修改
git reset HEAD UserService.java

# 从暂存区移除，并丢弃修改
git reset --hard HEAD UserService.java
```

**场景3：撤销已提交的修改**

```bash
# 撤销最后一次提交（保留修改）
git reset --soft HEAD~1

# 撤销最后一次提交（丢弃修改）
git reset --hard HEAD~1

# 回退到指定提交
git reset --hard abc1234
```

**⚠️ 警告**：
- `--hard` 操作会永久丢失代码
- 使用前请确保已备份

### 5.3 误操作恢复

**场景：误删了分支**

```bash
# 查找已删除的分支
git reflog

# 恢复分支
git checkout -b feature/lost-branch abc1234
```

**场景：误强制推送**

```bash
# 查看操作历史
git reflog

# 回退到之前的状态
git reset --hard HEAD@{5}

# 强制推送恢复
git push origin main --force
```

### 5.4 远程仓库问题

**场景：远程和本地不一致**

```bash
# 方法1：以远程为准（推荐）
git reset --hard origin/main

# 方法2：以本地为准（谨慎使用）
git push origin main --force

# 方法3：手动合并
git pull origin main --rebase
```

**场景：推送被拒绝**

```bash
# 错误：Updates were rejected because the tip of your current branch is behind
# 原因：远程有新提交，本地未拉取

# 解决方法1：先拉取再推送
git pull origin main
git push origin main

# 解决方法2：变基后推送
git pull --rebase origin main
git push origin main
```

---

## 6. 团队协作流程

### 6.1 日常开发流程

```
1. 晨会领取任务
   ↓
2. 从develop创建功能分支
   git checkout develop && git pull
   git checkout -b feature/task-123
   ↓
3. 开发代码
   ↓
4. 本地测试
   ↓
5. 提交代码
   git add .
   git commit -m "feat: 完成任务123"
   ↓
6. 推送到远程
   git push origin feature/task-123
   ↓
7. 创建Pull Request
   ↓
8. 代码审核
   ↓
9. 修改反馈意见
   ↓
10. 合并到develop
    ↓
11. 删除功能分支
```

### 6.2 代码审核检查清单

**审核前检查**：
- [ ] 代码通过编译
- [ ] 没有调试代码（console.log、debugger等）
- [ ] 没有注释掉的代码
- [ ] 没有敏感信息（密码、Token等）
- [ ] 提交信息符合规范
- [ ] 代码格式符合项目规范
- [ ] 必要的注释已添加
- [ ] 单元测试已通过

### 6.3 Pull Request模板

**创建 `.github/pull_request_template.md`**：

```markdown
## 变更类型
- [ ] feat: 新功能
- [ ] fix: Bug修复
- [ ] docs: 文档更新
- [ ] style: 代码格式
- [ ] refactor: 重构
- [ ] test: 测试相关
- [ ] chore: 构建/工具

## 变更说明
<!-- 描述本次PR的主要变更内容 -->

## 相关Issue
Closes #(issue编号)

## 测试
<!-- 描述如何测试这些变更 -->

## 截图
<!-- 如果有UI变更，请提供截图 -->

## 检查清单
- [ ] 代码通过编译
- [ ] 代码符合规范
- [ ] 已添加必要的测试
- [ ] 已更新相关文档
```

### 6.4 版本发布流程

```
1. 从main创建release分支
   git checkout main
   git checkout -b release/v1.0.0

2. 更新版本号
   修改 pom.xml 或 package.json

3. 完成最后测试
   运行所有测试

4. 合并到main
   git checkout main
   git merge release/v1.0.0

5. 打标签
   git tag -a v1.0.0 -m "Release v1.0.0"

6. 推送标签
   git push origin v1.0.0

7. 部署到生产环境

8. 合并回develop
   git checkout develop
   git merge release/v1.0.0
```

---

## 7. Git配置优化

### 7.1 常用配置

```bash
# 配置用户信息
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 配置默认分支名称
git config --global init.defaultBranch main

# 配置拉取策略（变基方式）
git config --global pull.rebase true

# 配置推送策略（只推送当前分支）
git config --global push.default simple

# 配置中文编码
git config --global core.quotepath false
git config --global gui.encoding utf-8
git config --global i18n.commitencoding utf-8
git config --global i18n.logoutputencoding utf-8

# 配置编辑器
git config --global core.editor vim

# 配置差异工具
git config --global diff.tool vscode
git config --global difftool.vscode.cmd 'code --wait --diff $LOCAL $REMOTE'
```

### 7.2 常用别名

```bash
# 简化常用命令
git config --global alias.st status
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.unstage 'reset HEAD --'
git config --global alias.last 'log -1 HEAD'
git config --global alias.visual 'log --pretty=format:%h %s' --graph'

# 查看日志的别名
git config --global alias.logs 'log --pretty=format:"%h - %an, %ar : %s" --graph'

# 撤销暂存的别名
git config --global alias.uncommit 'reset --soft HEAD~1'
```

### 7.3 .gitignore 配置

**项目根目录创建 `.gitignore`**：

```gitignore
# Java
*.class
*.jar
*.war
target/
.mvn/
mvnw
mvnw.cmd

# IDE
.idea/
*.iml
.vscode/
*.swp
*.swo
*~

# Node.js
node_modules/
npm-debug.log
yarn-error.log
dist/
.DS_Store

# 日志
*.log
logs/

# 临时文件
*.tmp
*.temp
nul

# 敏感信息
.env
*.key
*.pem
credentials.json

# 测试覆盖率
coverage/
.nyc_output/

# 构建产物
build/
out/
```

---

## 8. 禁止操作清单

### 8.1 绝对禁止

❌ **不要在主分支（main、develop）直接开发**
```bash
# ❌ 错误
git checkout main
# ... 直接修改代码 ...
git add .
git commit -m "update"
git push origin main  # 危险！
```

❌ **不要强制推送到公共分支**
```bash
# ❌ 危险操作！
git push origin main --force
```

❌ **不要提交敏感信息**
```bash
# ❌ 不要提交：
- 密码、Token
- 数据库连接串
- API密钥
- 私钥文件
```

❌ **不要提交大文件**
```bash
# ❌ 不要提交：
- 二进制文件（>50MB）
- 编译产物
- node_modules/
- 依赖包
```

❌ **不要在深夜合并重要代码**
- 等第二天早上再合并
- 确保有人在场可以回滚

### 8.2 谨慎操作

⚠️ **以下操作需要谨慎**：

```bash
# ⚠️ 谨慎使用
git reset --hard HEAD           # 会丢失未提交的修改
git clean -fd                    # 会删除未跟踪的文件
git rebase                       # 会改写历史
git push --force                 # 会覆盖远程历史
git branch -D                    # 强制删除分支
```

---

## 9. 最佳实践总结

### 9.1 提交规范

1. ✅ 提交前先 `git pull` 确保最新
2. ✅ 提交信息符合 Conventional Commits 规范
3. ✅ 一个提交只做一件事
4. ✅ 提交前运行测试，确保代码质量
5. ✅ 提交前删除调试代码和无用注释

### 9.2 分支管理

1. ✅ 使用功能分支开发
2. ✅ 定期同步主分支更新
3. ✅ 功能完成后及时删除分支
4. ✅ 主分支受保护，只允许PR合并
5. ✅ 长期分支：main、develop
6. ✅ 短期分支：feature/*、fix/*、hotfix/*

### 9.3 团队协作

1. ✅ 代码必须经过审核才能合并
2. ✅ 冲突及时沟通解决
3. ✅ 重要变更提前通知团队
4. ✅ 定期同步代码，避免大量冲突
5. ✅ 尊重他人的代码，谨慎修改

---

## 10. 快速参考

### 常用命令速查表

| 操作 | 命令 |
|------|------|
| **查看状态** | `git status` |
| **查看分支** | `git branch` |
| **创建分支** | `git checkout -b feature/xxx` |
| **切换分支** | `git checkout main` |
| **添加文件** | `git add .` |
| **提交代码** | `git commit -m "feat: xxx"` |
| **推送代码** | `git push origin main` |
| **拉取代码** | `git pull origin main` |
| **合并分支** | `git merge feature/xxx` |
| **查看日志** | `git log --oneline` |
| **查看差异** | `git diff` |
| **撤销修改** | `git restore file.java` |

### 提交类型速查表

| 类型 | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat: 添加用户注册` |
| `fix` | Bug修复 | `fix: 修复登录错误` |
| `docs` | 文档 | `docs: 更新API文档` |
| `style` | 格式 | `style: 调整缩进` |
| `refactor` | 重构 | `refactor: 重构服务层` |
| `perf` | 性能 | `perf: 优化查询` |
| `test` | 测试 | `test: 添加单元测试` |
| `chore` | 构建 | `chore: 更新依赖` |

---

**文档版本**：v1.0
**最后更新**：2026-01-09
**作者**：开发组

**相关文档**：
- [Git仓库安全保护指南](./Git仓库安全保护指南.md)
- [用户注册方案](./用户注册方案.md)
