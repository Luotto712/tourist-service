<template>
  <div class="approval-timeline">
    <div v-if="records.length === 0" class="empty-state">
      <el-empty description="暂无审批记录" :image-size="100" />
    </div>
    <el-timeline v-else>
      <el-timeline-item
        v-for="record in records"
        :key="record.id"
        :color="getNodeColor(record.action)"
        :hollow="false"
        :timestamp="formatTime(record.createTime)"
        placement="top"
      >
        <div class="timeline-card">
          <div class="card-header">
            <span class="node-name">{{ record.nodeName || '审批节点' }}</span>
            <el-tag :type="getActionType(record.action)" size="small" effect="dark">
              {{ getActionText(record.action) }}
            </el-tag>
          </div>
          <div class="card-body">
            <div class="card-row">
              <span class="label">审批人：</span>
              <span class="value">{{ record.approverName || '--' }}</span>
            </div>
            <div v-if="record.comment" class="card-row">
              <span class="label">审批意见：</span>
              <span class="value comment">{{ record.comment }}</span>
            </div>
          </div>
        </div>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>
defineProps({
  records: {
    type: Array,
    default: () => []
  }
})

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  const Y = date.getFullYear()
  const M = String(date.getMonth() + 1).padStart(2, '0')
  const D = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  const s = String(date.getSeconds()).padStart(2, '0')
  return `${Y}-${M}-${D} ${h}:${m}:${s}`
}

function getNodeColor(action) {
  if (!action) return '#909399'
  const upper = action.toUpperCase()
  if (upper === 'APPROVE' || upper === 'APPROVED' || upper === 'PASS') return '#67c23a'
  if (upper === 'REJECT' || upper === 'REJECTED' || upper === 'RETURN') return '#f56c6c'
  if (upper === 'PENDING' || upper === 'SUBMITTED') return '#409eff'
  return '#909399'
}

function getActionType(action) {
  if (!action) return 'info'
  const upper = action.toUpperCase()
  if (upper === 'APPROVE' || upper === 'APPROVED' || upper === 'PASS') return 'success'
  if (upper === 'REJECT' || upper === 'REJECTED' || upper === 'RETURN') return 'danger'
  if (upper === 'PENDING' || upper === 'SUBMITTED') return 'primary'
  return 'info'
}

function getActionText(action) {
  if (!action) return '未知'
  const upper = action.toUpperCase()
  const map = {
    APPROVE: '通过',
    APPROVED: '通过',
    PASS: '通过',
    REJECT: '驳回',
    REJECTED: '驳回',
    RETURN: '退回',
    PENDING: '待审批',
    SUBMITTED: '已提交'
  }
  return map[upper] || action
}
</script>

<style lang="scss" scoped>
.approval-timeline {
  padding: 4px 0;

  .empty-state {
    padding: 20px 0;
  }

  .timeline-card {
    background: #f9fafc;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 12px 16px;
    transition: box-shadow 0.2s;

    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 10px;

      .node-name {
        font-size: 15px;
        font-weight: 600;
        color: #303133;
      }
    }

    .card-body {
      display: flex;
      flex-direction: column;
      gap: 6px;

      .card-row {
        font-size: 13px;
        line-height: 1.6;

        .label {
          color: #909399;
        }

        .value {
          color: #303133;

          &.comment {
            color: #606266;
            word-break: break-word;
            white-space: pre-wrap;
          }
        }
      }
    }
  }
}
</style>
