<template>
  <div class="process-timeline">
    <el-timeline>
      <el-timeline-item
        v-for="(item, index) in timelineData"
        :key="index"
        :timestamp="item.time"
        placement="top"
        :type="getStatusType(item.status)"
        :icon="getStatusIcon(item.status)"
        :size="index === 0 ? 'large' : 'normal'"
      >
        <div class="timeline-item">
          <div class="timeline-header">
            <span class="timeline-title">{{ item.title }}</span>
            <el-tag v-if="item.status" :type="getStatusType(item.status)" size="small">
              {{ getStatusLabel(item.status) }}
            </el-tag>
          </div>
          <div class="timeline-content">
            <div class="timeline-description" v-if="item.description">
              {{ item.description }}
            </div>
            <div class="timeline-operator" v-if="item.operator">
              <span class="operator-label">操作人：</span>
              <span>{{ item.operator }}</span>
            </div>
            <div class="timeline-attachments" v-if="item.attachments?.length">
              <span class="attachment-label">附件：</span>
              <el-link
                v-for="(file, idx) in item.attachments"
                :key="idx"
                :href="file.url"
                target="_blank"
                type="primary"
                style="margin-right: 8px"
              >
                {{ file.name }}
              </el-link>
            </div>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>

    <!-- 空状态 -->
    <el-empty v-if="!timelineData.length" description="暂无流程记录" :image-size="80" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Check, Circle, Loading, Clock } from '@element-plus/icons-vue'

/** 时间线节点 */
export interface TimelineNode {
  title: string           // 节点标题
  time?: string           // 时间
  status?: string         // 状态: completed, active, pending, cancelled
  description?: string    // 描述
  operator?: string       // 操作人
  attachments?: Array<{   // 附件
    name: string
    url: string
  }>
}

interface Props {
  data: TimelineNode[]
}

const props = defineProps<Props>()

// 时间线数据（添加默认初始节点）
const timelineData = computed(() => {
  if (!props.data.length) return []

  // 确保第一个节点是"来单"
  const items = [...props.data]
  if (items[0].title !== '来单登记') {
    items.unshift({
      title: '来单登记',
      status: 'completed',
      description: '客户下达订单需求'
    })
  }

  // 确保最后一个节点根据状态显示
  const lastItem = items[items.length - 1]
  if (lastItem.status !== 'active' && lastItem.title !== '归档完成') {
    items.push({
      title: '等待处理',
      status: 'pending',
      description: '等待下一步操作'
    })
  }

  return items
})

// 获取状态类型
const getStatusType = (status?: string) => {
  const map: Record<string, any> = {
    completed: 'success',
    active: 'primary',
    pending: 'info',
    cancelled: 'danger'
  }
  return map[status || ''] || ''
}

// 获取状态标签
const getStatusLabel = (status?: string) => {
  const map: Record<string, string> = {
    completed: '已完成',
    active: '进行中',
    pending: '待处理',
    cancelled: '已取消'
  }
  return map[status || ''] || ''
}

// 获取状态图标
const getStatusIcon = (status?: string) => {
  const map: Record<string, any> = {
    completed: Check,
    active: Loading,
    pending: Clock,
    cancelled: Circle
  }
  return map[status || ''] || null
}
</script>

<style scoped lang="scss">
.process-timeline {
  padding: 20px 0;

  .timeline-item {
    .timeline-header {
      display: flex;
      align-items: center;
      margin-bottom: 8px;

      .timeline-title {
        font-size: 16px;
        font-weight: 500;
        margin-right: 8px;
      }
    }

    .timeline-content {
      font-size: 14px;
      color: var(--el-text-color-secondary);

      .timeline-description {
        margin-bottom: 8px;
      }

      .timeline-operator {
        margin-bottom: 8px;

        .operator-label {
          color: var(--el-text-color-regular);
          margin-right: 4px;
        }
      }

      .timeline-attachments {
        .attachment-label {
          color: var(--el-text-color-regular);
          margin-right: 4px;
        }
      }
    }
  }

  :deep(.el-timeline-item__timestamp) {
    font-weight: 500;
  }

  :deep(.el-timeline-item__wrapper) {
    padding-left: 0;
  }
}
</style>
