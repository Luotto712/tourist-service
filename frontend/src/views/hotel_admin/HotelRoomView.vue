<template>
  <el-card>
    <template #header><span>房态录入</span></template>

    <el-form :inline="true" :model="filter" class="mb-16">
      <el-form-item label="酒店类型">
        <el-select v-model="filter.hotelType" @change="onTypeChange" style="width: 160px">
          <el-option label="星级酒店" value="STAR" />
          <el-option label="非星级/乡村" value="NONSTAR" />
        </el-select>
      </el-form-item>
      <el-form-item label="酒店">
        <el-select
          v-model="filter.hotelId"
          filterable
          placeholder="选择酒店"
          style="width: 200px"
          @change="fetchData"
        >
          <el-option v-for="h in hotelOptions" :key="h.id" :label="h.name" :value="h.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker
          v-model="filter.date"
          type="date"
          value-format="YYYY-MM-DD"
          :clearable="false"
          @change="fetchData"
        />
      </el-form-item>
      <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="编号" width="70">
        <template #default="{ $index }">{{ $index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="roomType" label="房型" />
      <el-table-column prop="total" label="总房量" width="100" />
      <el-table-column label="当日已预定" width="120">
        <template #default="{ row }">{{ row.booked }}</template>
      </el-table-column>
      <el-table-column label="剩余" width="90">
        <template #default="{ row }">{{ row.remaining }}</template>
      </el-table-column>
      <el-table-column label="价格" width="110">
        <template #default="{ row }">￥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="info" link @click="showBookings(row)">预订明细</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 16px">
      <el-button type="primary" plain @click="openAdd" :disabled="!filter.hotelId"
        >新增房型</el-button
      >
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑房型' : '新增房型'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="房型">
          <el-input v-model="form.roomType" :disabled="!!editing" />
        </el-form-item>
        <el-form-item label="总房量"
          ><el-input-number v-model="form.total" :min="0"
        /></el-form-item>
        <el-form-item label="已预定"
          ><el-input-number v-model="form.baseBooked" :min="0" :max="form.total"
        /></el-form-item>
        <el-form-item label="价格"
          ><el-input-number v-model="form.price" :min="0" :precision="2"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bookingsVisible" :title="`预订明细 — ${bookingsRoomType}`" width="640px">
      <el-table :data="bookings" v-loading="bookingsLoading" border size="small">
        <el-table-column label="编号" width="70">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column prop="userName" label="预定人" width="120" />
        <el-table-column prop="roomType" label="房型" width="140" />
        <el-table-column label="日期" width="220">
          <template #default="{ row }">{{ row.checkIn }} — {{ row.checkOut }}</template>
        </el-table-column>
        <el-table-column label="人数" width="80">
          <template #default="{ row }">{{ row.guests }}</template>
        </el-table-column>
        <el-table-column label="总价" width="110">
          <template #default="{ row }">￥{{ row.totalPrice }}</template>
        </el-table-column>
      </el-table>
      <el-empty
        v-if="!bookingsLoading && !bookings.length"
        description="该房型暂无预订"
        :image-size="60"
      />
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRoomTypes, saveRoomType, deleteRoomType } from '@/api/roomType'
import { getRoomTypeBookings } from '@/api/booking'
import { listCatalog } from '@/api/catalog'

const today = new Date().toISOString().slice(0, 10)
const filter = reactive({ hotelType: 'STAR', hotelId: null, date: today })
const hotels = ref([])
const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const editing = ref(null)
const form = reactive({ roomType: '', total: 0, baseBooked: 0, price: 0 })

const bookingsVisible = ref(false)
const bookingsLoading = ref(false)
const bookingsRoomType = ref('')
const bookings = ref([])

const hotelOptions = computed(() =>
  hotels.value.filter(h => (filter.hotelType === 'STAR' ? h._isStar : !h._isStar))
)

onMounted(fetchHotels)

async function fetchHotels() {
  const [star, nonstar] = await Promise.all([
    listCatalog('hotels/star', { page: 1, pageSize: 100 }),
    listCatalog('hotels/nonstar', { page: 1, pageSize: 100 })
  ])
  hotels.value = [
    ...((star.data && star.data.list) || []).map(h => ({ ...h, _isStar: true })),
    ...((nonstar.data && nonstar.data.list) || []).map(h => ({ ...h, _isStar: false }))
  ]
}

function onTypeChange() {
  filter.hotelId = null
  list.value = []
}

async function fetchData() {
  if (!filter.hotelId) {
    ElMessage.warning('请选择酒店')
    return
  }
  loading.value = true
  try {
    const res = await listRoomTypes({
      hotelType: filter.hotelType,
      hotelId: filter.hotelId,
      date: filter.date
    })
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openAdd() {
  editing.value = null
  form.roomType = ''
  form.total = 0
  form.baseBooked = 0
  form.price = 0
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = row
  form.roomType = row.roomType
  form.total = Number(row.total) || 0
  form.baseBooked = Number(row.baseBooked) || 0
  form.price = Number(row.price) || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!filter.hotelId) {
    ElMessage.warning('请选择酒店')
    return
  }
  if (!form.roomType) {
    ElMessage.warning('请填写房型')
    return
  }
  submitting.value = true
  try {
    await saveRoomType({
      hotelType: filter.hotelType,
      hotelId: filter.hotelId,
      roomType: form.roomType,
      total: form.total,
      baseBooked: form.baseBooked,
      price: form.price
    })
    ElMessage.success('已保存')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(
    `确定彻底删除房型「${row.roomType}」吗？相关预订也会一并删除。`,
    '提示',
    { type: 'warning' }
  )
  await deleteRoomType(row.roomTypeId)
  ElMessage.success('已删除')
  fetchData()
}

async function showBookings(row) {
  bookingsRoomType.value = row.roomType
  bookingsVisible.value = true
  bookingsLoading.value = true
  bookings.value = []
  try {
    const res = await getRoomTypeBookings({
      hotelType: filter.hotelType,
      hotelId: filter.hotelId,
      roomType: row.roomType,
      date: filter.date
    })
    bookings.value = res.data || []
  } finally {
    bookingsLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.mb-16 {
  margin-bottom: 16px;
}
</style>
