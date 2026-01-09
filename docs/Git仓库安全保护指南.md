# Git仓库安全保护指南

> **订单可视化平台 - Git仓库安全配置**
>
> **版本**：v1.0
> **创建时间**：2026-01-09
> **作者**：开发组

---

## 📋 目录

- [1. GitHub分支保护规则](#1-github分支保护规则)
- [2. 防止强制推送](#2-防止强制推送)
- [3. 权限管理最佳实践](#3-权限管理最佳实践)
- [4. 应急恢复方案](#4-应急恢复方案)

---

## 1. GitHub分支保护规则

### 1.1 为什么需要分支保护？

**风险场景**：
- ❌ 任何人都可以强制推送，覆盖历史记录
- ❌ 误操作导致代码丢失
- ❌ 恶意操作导致仓库被破坏
- ❌ 敏感分支（main、develop）被意外修改

### 1.2 设置main分支保护

#### 步骤1：进入仓库设置

1. 打开GitHub仓库：`https://github.com/hjc123abc/order-platform`
2. 点击 **Settings**（仓库设置）
3. 左侧菜单点击 **Branches**（分支）

#### 步骤2：添加分支保护规则

1. 点击 **Add branch protection rule**（添加分支保护规则）
2. 配置以下选项：

```
Branch name pattern: main

✅ 保护规则配置：
├─ ✅ Require a pull request before merging
│   ├─ ✅ Require approvals
│   │   └─ Number of approvals required: 1
│   └─ ✅ Dismiss stale PR approvals when new commits are pushed
│
├─ ✅ Require status checks to pass before merging
│   ├─ Require branches to be up to date before merging
│   └─ 选中必要的状态检查（如果有CI/CD）
│
├─ ✅ Do not allow bypassing the above settings
│   └─ 只有管理员可以绕过（谨慎勾选）
│
└─ ✅ Restrict who can push to matching branches
    └─ 只允许特定用户推送（推荐）
```

#### 步骤3：保存规则

点击 **Create** 或 **Save changes** 保存保护规则。

### 1.3 保护规则效果

设置后，以下操作将被阻止：

| 操作 | 未保护 | 已保护 |
|------|--------|--------|
| **强制推送** | ✅ 可以 | ❌ 禁止 |
| **删除分支** | ✅ 可以 | ❌ 禁止 |
| **直接推送** | ✅ 可以 | ⚠️ 需要PR |
| **未审核的PR合并** | ✅ 可以 | ❌ 禁止 |

---

## 2. 防止强制推送

### 2.1 GitHub级别保护（推荐）

**方法1：分支保护规则**（最有效）

```
Settings → Branches → Add branch protection rule
└─ 限制谁可以推送到受保护分支
```

**配置示例**：
```
Branch name pattern: main, develop, release/*

Restrict who can push to matching branches:
✅ hjc123abc (仓库所有者)
✅ 核心开发者A
✅ 核心开发者B
```

**方法2：要求Pull Request审核**

```
Require a pull request before merging:
├─ Approvals required: 1
└─ Dismiss stale reviews: ✅
```

### 2.2 团队协作规范

**规范1：禁止强制推送到主分支**

```bash
# ❌ 错误操作（危险）
git push origin main --force

# ✅ 正确操作（安全）
git push origin main
```

**规范2：使用功能分支开发**

```
功能开发流程：
1. 创建功能分支：git checkout -b feature/user-registration
2. 在功能分支开发
3. 推送功能分支：git push origin feature/user-registration
4. 创建Pull Request
5. 代码审核通过后合并到main
```

**规范3：禁用本地强制推送别名**

```bash
# 在 ~/.gitconfig 中添加别名
[alias]
    # 禁用强制推送
    push-f = "!echo '⚠️  禁止强制推送！请使用: git push'"

    # 安全推送（会提示如果远程有新提交）
    push = "push --force-with-lease"
```

---

## 3. 权限管理最佳实践

### 3.1 GitHub权限级别

| 角色 | 权限 | 适用人员 |
|------|------|---------|
| **Admin**（管理员） | 完全控制，包括设置、协作者管理 | 项目负责人 |
| **Maintainer**（维护者） | 可推送、强制推送、管理Issue/PR | 核心开发者 |
| **Developer**（开发者） | 可推送，不能强制推送 | 普通开发者 |
| **Reporter**（报告者） | 只读，可提Issue/PR | 外部贡献者 |

### 3.2 协作者管理

**设置路径**：
```
Settings → Collaborators and teams → Collaborators
```

**推荐配置**：

```
仓库所有者（Admin）：
├─ hjc123abc (Admin) - 仓库所有者
└─ 核心开发者A (Maintainer) - 可合并PR

普通开发者（Write）：
├─ 开发者B (Write) - 可推送到功能分支
└─ 开发者C (Write) - 可推送到功能分支

只读访问（Read）：
└─ 利益相关方 (Read) - 只能查看代码
```

### 3.3 团队权限管理（大型团队推荐）

**创建团队**：
```
Settings → Collaborators and teams → Teams
├─ core-team (Maintainer权限) - 核心开发团队
├─ developers (Write权限) - 普通开发团队
└─ reviewers (Write权限) - 代码审核团队
```

---

## 4. 应急恢复方案

### 4.1 远程仓库被覆盖的恢复步骤

**场景**：远程仓库被错误的强制推送覆盖了

**恢复步骤**：

```bash
# 第1步：确认本地代码是否完整
git status
git log --oneline -5

# 第2步：提交本地未提交的修改（如果有）
git add .
git commit -m "紧急恢复：恢复被覆盖的代码"

# 第3步：强制推送恢复远程仓库
git push origin main --force

# 第4步：通知团队成员
# "远程仓库已恢复，请所有人重新拉取最新代码"
git fetch origin
git pull origin main
```

### 4.2 防止再次被覆盖

**立即执行**：

1. ✅ **设置分支保护规则**（最重要！）
2. ✅ **限制推送权限**（只给核心开发者）
3. ✅ **启用审查要求**（PR必须审核后合并）
4. ✅ **团队培训Git规范**

### 4.3 定期备份

**备份策略**：

```bash
# 方法1：定期推送到多个远程仓库
git remote add backup https://gitee.com/hjc123abc/order-platform-backup.git
git push backup main

# 方法2：创建GitHub镜像
# Settings → Repository → Push to a mirror
```

---

## 5. 快速检查清单

### 5.1 仓库安全检查清单

**必须完成**：
- [ ] main分支已设置保护规则
- [ ] 禁止强制推送到main分支（非管理员）
- [ ] 启用PR审核要求（至少1人审核）
- [ ] 设置协作者权限（移除不需要的Write权限）
- [ ] 添加核心开发者到"允许推送"列表

**推荐完成**：
- [ ] 创建develop分支并设置保护
- [ ] 配置CI/CD状态检查
- [ ] 设置备用远程仓库
- [ ] 定期备份到本地

### 5.2 快速验证

**验证命令**：

```bash
# 检查远程仓库地址
git remote -v

# 检查分支跟踪关系
git branch -vv

# 检查本地和远程差异
git log origin/main --oneline -5
git log main --oneline -5

# 测试强制推送是否被阻止（应该在GitHub上失败）
# git push origin main --force  # ⚠️ 实际不要执行！
```

---

## 6. 常见问题

### Q1：分支保护后如何推送代码？

**A**：使用Pull Request工作流

```bash
# 1. 创建功能分支
git checkout -b feature/new-feature

# 2. 开发并提交
git add .
git commit -m "feat: 新功能"

# 3. 推送功能分支
git push origin feature/new-feature

# 4. 在GitHub上创建Pull Request
# 5. 等待审核通过后合并
```

### Q2：紧急情况需要强制推送怎么办？

**A**：临时解除保护（谨慎使用）

```
GitHub操作：
1. Settings → Branches
2. 编辑main分支保护规则
3. 临时取消"Restrict who can push"
4. 执行强制推送
5. 立即恢复保护规则
```

### Q3：如何查看谁推送了代码？

**A**：查看提交记录

```bash
# 查看最近推送的记录
git log --pretty=format:"%h - %an, %ar : %s" --graph -10

# 在GitHub上：
# 点击 "Insights" → "Network"
# 可以看到所有推送者的活动
```

---

## 7. 推荐配置（可直接复制）

### 7.1 main分支保护规则配置模板

```
分支名称模式：main, develop, release/*

✅ Require a pull request before merging
   ├─ ✅ Require approvals
   │   └─ Number of approvals required: 1
   ├─ ✅ Dismiss stale PR approvals when new commits are pushed
   └─ ✅ Require review from CODEOWNERS file

✅ Require status checks to pass before merging
   └─ ✅ Require branches to be up to date before merging

✅ Do not allow bypassing the above settings
   └─ ✅ 限制只有管理员可以绕过

✅ Restrict who can push to matching branches
   ├─ ✅ hjc123abc (Admin)
   ├─ ✅ 核心开发者A (Maintainer)
   └─ ✅ 核心开发者B (Maintainer)

✅ Allow force pushes
   └─ ❌ 不勾选（禁止强制推送）

✅ Allow deletions
   └─ ❌ 不勾选（禁止删除分支）
```

---

## 8. 总结

### 核心原则

1. **最小权限原则**：只给必要的权限
2. **代码审核原则**：所有合并必须经过审核
3. **可追溯原则**：所有操作有记录
4. **备份原则**：定期备份，防止意外

### 优先级

| 优先级 | 任务 | 重要性 |
|--------|------|--------|
| **P0** | 设置main分支保护 | ⭐⭐⭐⭐⭐ |
| **P0** | 限制推送权限 | ⭐⭐⭐⭐⭐ |
| **P0** | 禁止强制推送 | ⭐⭐⭐⭐⭐ |
| **P1** | 配置PR审核 | ⭐⭐⭐⭐ |
| **P1** | 团队Git规范培训 | ⭐⭐⭐⭐ |
| **P2** | 设置备用仓库 | ⭐⭐⭐ |

---

**文档版本**：v1.0
**最后更新**：2026-01-09
**作者**：开发组

**相关文档**：
- [Git工作流规范](./Git工作流规范.md)
- [Git提交规范](./Git提交规范.md)
