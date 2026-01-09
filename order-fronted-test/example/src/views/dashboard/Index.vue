<template>
  <div class="dashboard-container">
    <h1>数据看板</h1>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="24">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
      </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card class="quick-actions" shadow="hover">
      <template #header>
        <span>快捷入口</span>
      </template>
      <el-space :size="20" wrap>
        <el-button type="primary" :icon="Plus" @click="handleQuickAction('create')">
          新建订单
        </el-button>
        <el-button type="success" :icon="Van" @click="handleQuickAction('shipment')">
          创建发运
        </el-button>
        <el-button type="warning" :icon="Warning" @click="handleQuickAction('exception')">
          异常上报
        </el-button>
        <el-button :icon="Upload" @click="handleQuickAction('upload')">
          上传附件
        </el-button>
      </el-space>
    </el-card>

    <!-- 最近订单 -->
    <el-card class="recent-orders" shadow="hover">
      <template #header>
        <span>最近订单</span>
      </template>
      <el-table :data="recentOrders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单编号" width="150" />
        <el-table-column prop="customerName" label="客户名称" />
        <el-table-column prop="amount" label="订单金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount?.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document,
  Van,
  Box,
  Warning,
  Checked,
  Plus,
  Upload
} from '@element-plus/icons-vue'

const router = useRouter()

// 统计数据
const stats = ref([
  {
    title: '总订单数',
    value: '1,234',
    icon: Document,
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '在途订单',
    value: '56',
    icon: Van,
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    title: '本月签收',
    value: '234',
    icon: Box,
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    title: '准时率',
    value: '96.5%',
    icon: Checked,
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  }
])

// 最近订单（模拟数据）
const recentOrders = ref([
  {
    id: 1,
    orderNo: 'ORD20260105001',
    customerName: '北京科技有限公司',
    amount: 125000,
    status: 'processing',
    createTime: '2026-01-05 10:30:00'
  },
  {
    id: 2,
    orderNo: 'ORD20260104002',
    customerName: '上海贸易公司',
    amount: 89500,
    status: 'shipped',
    createTime: '2026-01-04 14:20:00'
  },
  {
    id: 3,
    orderNo: 'ORD20260104001',
    customerName: '深圳电子厂',
    amount: 210000,
    status: 'completed',
    createTime: '2026-01-04 09:15:00'
  }
])

function handleQuickAction(action: string) {
  switch (action) {
    case 'create':
      ElMessage.info('新建订单功能开发中')
      break
    case 'shipment':
      ElMessage.info('创建发运功能开发中')
      break
    case 'exception':
      router.push('/exception')
      break
    case 'upload':
      router.push('/attachment')
      break
  }
}

function viewDetail(id: number) {
  router.push(`/order/${id}`)
}

function getStatusType(status: string) {
  const map: Record<string, any> = {
    draft: 'info',
    processing: 'warning',
    shipped: 'primary',
    completed: 'success'
  }
  return map[status] || 'info'
}

function getStatusText(status: string) {
  const map: Record<string, string> = {
    draft: '草稿',
    processing: '执行中',
    shipped: '已发运',
    completed: '已完成'
  }
  return map[status] || '未知'
}
</script>

<style scoped lang="scss">
.dashboard-container {
  h1 {
    font-size: 24px;
    font-weight: 500;
    margin-bottom: 20px;
    color: var(--el-text-color-primary);
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    margin-bottom: 20px;

    :deep(.el-card__body) {
      padding: 20px;
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 15px;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: var(--el-text-color-primary);
          margin-bottom: 4px;
        }

        .stat-title {
          font-size: 14px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }

  .quick-actions {
    margin-bottom: 20px;
  }

  .recent-orders {
    margin-bottom: 20px;
  }
}
</style>
