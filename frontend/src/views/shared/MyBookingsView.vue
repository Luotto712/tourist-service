<template>
  <el-card>
    <template #header><span>订单详情</span></template>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="编号" width="80">
        <template #default="{ $index }">{{
          (pagination.page - 1) * pagination.size + $index + 1
        }}</template>
      </el-table-column>
      <el-table-column prop="hotelName" label="酒店名称" min-width="160" />
      <el-table-column prop="roomType" label="房型" width="140" />
      <el-table-column label="日期" width="220">
        <template #default="{ row }">{{ row.checkIn }} — {{ row.checkOut }}</template>
      </el-table-column>
      <el-table-column label="总价" width="120">
        <template #default="{ row }">￥{{ row.totalPrice }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'BOOKED' ? 'success' : 'info'">{{
            row.status === 'BOOKED' ? '已预订' : '已取消'
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'BOOKED'"
            size="small"
            type="danger"
            link
            @click="handleCancel(row)"
            >取消</el-button
          >
          <span v-else style="color: #c0c4cc">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      background
      layout="total, prev, pager, next"
      v-model:current-page="pagination.page"
      :page-size="pagination.size"
      :total="pagination.total"
      @current-change="fetchList"
    />
  </el-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyBookings, cancelBooking } from '@/api/booking'

const list = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getMyBookings({ page: pagination.page, pageSize: pagination.size })
    const data = res.data || {}
    list.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消该预订吗？', '提示', { type: 'warning' })
  await cancelBooking(row.id)
  ElMessage.success('已取消')
  fetchList()
}
</script>
