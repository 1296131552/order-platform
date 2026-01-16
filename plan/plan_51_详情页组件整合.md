---
level: 3
file_id: plan_51
parent: plan_50
status: pending
created: 2025-01-16
estimated_days: 3.0
---

# 任务：详情页组件整合

## 任务概述

### 任务描述
实现订单详情页面的组件整合，包括布局、数据聚合、状态同步。

### 任务目的
提供信息完整、交互流畅的订单详情页面。

---

## 依赖关系

### 前置条件
- **前置任务**：plan_50（订单详情页整合）

---

## 可视化辅助

### 组件结构图
```
OrderDetailPage
├── OrderBasicInfo      # 基础信息卡片
│   ├── 订单号、客户、金额、状态
│   └── 操作按钮
├── OrderLinesTable     # 订单行表格
│   ├── 产品信息
│   └── 金额明细
├── ShipmentList        # 发运批次列表
│   ├── 批次信息
│   ├── 物流跟踪
│   └── 展开详情
├── ReceiptList         # 签收记录
│   ├── 签收信息
│   └── 差异记录
├── AttachmentList      # 关联附件
│   └── 文件列表
└── OrderTimeline       # 状态时间线
    ├── 时间节点
    └── 操作记录
```

### 风险监控表
| 风险项 | 等级 | 触发信号 | 应对策略 | 责任人 |
|--------|------|----------|----------|--------|
| 数据加载慢 | 中 | 页面卡顿 | 分模块加载 | 前端开发者 |
| 数据不一致 | 高 | 信息冲突 | 统一数据源 | 后端开发者 |

---

## 执行步骤

### 步骤1：创建详情页布局

### 步骤2：实现基础信息卡片
- 订单信息展示
- 状态标签
- 操作按钮

### 步骤3：实现订单行表格

### 步骤4：实现发运批次列表
- 折叠展开
- 物流跟踪
- 签收状态

### 步骤5：实现关联附件
- 附件列表
- 文件预览
- 下载功能

### 步骤6：实现时间线组件
- 时间节点
- 状态变更
- 操作记录

### 步骤7：实现数据聚合接口

---

## 核心接口定义

### 主要类/接口
```typescript
// 订单详情聚合接口
export const orderDetailApi = {
  // 详情页聚合数据
  getDetail: (id: number) => request.get<OrderDetailAggregateVO>(`/api/order/${id}/detail`)
};

interface OrderDetailAggregateVO {
  basic: OrderBasicVO;        // 基础信息
  lines: OrderLineVO[];        // 订单行
  shipments: ShipmentVO[];     // 发运批次
  receipts: ReceiptVO[];       // 签收记录
  attachments: AttachmentVO[]; // 附件
  timeline: TimelineItemVO[];  // 时间线
}
```

---

## 验收标准

### 功能验收
1. [ ] 详情页数据完整
2. [ ] 各模块数据一致
3. [ ] 时间线顺序正确
4. [ ] 操作功能正常

### 性能验收
- [ ] 首屏加载 < 2秒
- [ ] 操作响应 < 500ms

---

## 注意事项

- 各模块数据并行加载
- 缓存策略优化
- 实时状态更新
