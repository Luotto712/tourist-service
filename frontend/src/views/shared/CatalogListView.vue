<template>
  <el-card :key="config.resource">
    <template #header>
      <div class="header-bar">
        <span>{{ config.title }}</span>
        <el-input
          v-model="keyword"
          placeholder="按名称搜索"
          clearable
          style="width: 220px"
          @keyup.enter="fetchList"
          @clear="fetchList"
        />
      </div>
    </template>

    <el-table
      :data="list"
      v-loading="loading"
      border
      @row-click="showDetail"
      style="cursor: pointer"
    >
      <el-table-column
        v-for="c in config.columns"
        :key="c.prop"
        :label="c.label"
        show-overflow-tooltip
        min-width="120"
      >
        <template #default="{ row }">{{ display(c, row) }}</template>
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

    <el-dialog v-model="detailVisible" :title="config.title" width="640px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item v-for="c in config.columns" :key="c.prop" :label="c.label">{{
          display(c, current) || '—'
        }}</el-descriptions-item>
      </el-descriptions>

      <template v-if="isHotel">
        <h4 style="margin: 16px 0 10px">房型 / 预订</h4>
        <el-table :data="rooms" v-loading="roomLoading" border size="small">
          <el-table-column prop="roomType" label="房型" />
          <el-table-column prop="total" label="总房量" width="90" />
          <el-table-column prop="booked" label="已预定" width="90" />
          <el-table-column label="剩余" width="90">
            <template #default="{ row }">{{
              row.remaining ?? (row.total || 0) - (row.booked || 0)
            }}</template>
          </el-table-column>
          <el-table-column prop="price" label="价格" width="110" />
          <el-table-column label="操作" width="110">
            <template #default="{ row }">
              <el-button
                size="small"
                type="primary"
                :disabled="(row.remaining ?? 0) <= 0"
                @click="openBooking(row)"
                >预订</el-button
              >
            </template>
          </el-table-column>
        </el-table>
        <el-empty
          v-if="!roomLoading && !rooms.length"
          description="暂无房型信息"
          :image-size="60"
        />
      </template>
    </el-dialog>

    <el-dialog
      v-model="bookingVisible"
      title="预订详情"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form :model="booking" label-width="90px">
        <el-form-item label="房型"
          ><el-input :model-value="booking.roomType" disabled
        /></el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="booking.checkIn"
            type="date"
            value-format="YYYY-MM-DD"
            :clearable="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="离开日期">
          <el-date-picker
            v-model="booking.checkOut"
            type="date"
            value-format="YYYY-MM-DD"
            :clearable="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="入住人数">
          <el-input-number v-model="booking.guests" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="总价">
          <span style="font-size: 18px; font-weight: 700; color: #f56c6c"
            >￥{{ bookingTotal }}</span
          >
          <span style="color: #909399; margin-left: 8px"
            >（{{ unitPrice }} × {{ bookingNights }} 天）</span
          >
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingVisible = false">取消</el-button>
        <el-button type="primary" :loading="bookingSubmitting" @click="confirmBooking"
          >确认</el-button
        >
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { listCatalog } from '@/api/catalog'
import { getAvailability, createBooking } from '@/api/booking'
import { catalogs } from '@/constants/catalogs'
import { ElMessage } from 'element-plus'

const props = defineProps({ resource: { type: String, default: '' } })
const route = useRoute()
const config = computed(
  () =>
    catalogs[props.resource || route.params.resource] || { title: '', resource: '', columns: [] }
)

const list = ref([])
const loading = ref(false)
const keyword = ref('')
const pagination = reactive({ page: 1, size: 10, total: 0 })
const detailVisible = ref(false)
const current = ref(null)
const refMaps = ref({})
const rooms = ref([])
const roomLoading = ref(false)
const isHotel = computed(() => (config.value.resource || '').startsWith('hotels/'))

onMounted(fetchList)
watch(
  () => config.value.resource,
  (nv, ov) => {
    if (nv && nv !== ov) {
      list.value = []
      pagination.page = 1
      keyword.value = ''
      detailVisible.value = false
      current.value = null
      fetchList()
    }
  }
)

async function fetchList() {
  loading.value = true
  await loadRefMaps()
  try {
    const res = await listCatalog(config.value.resource, {
      page: pagination.page,
      pageSize: pagination.size,
      keyword: keyword.value || undefined
    })
    const data = res.data || {}
    const type =
      config.value.resource === 'hotels/star'
        ? 'STAR'
        : config.value.resource === 'hotels/nonstar'
          ? 'NONSTAR'
          : null
    list.value = (data.list || []).map(it => (type ? { ...it, _hotelType: type } : it))
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

const bookingVisible = ref(false)
const bookingSubmitting = ref(false)
const booking = reactive({ roomType: '', price: 0, checkIn: '', checkOut: '', guests: 1 })

const bookingNights = computed(() => {
  if (!booking.checkIn || !booking.checkOut) return 0
  const inD = new Date(booking.checkIn)
  const outD = new Date(booking.checkOut)
  const days = Math.floor((outD - inD) / 86400000) + 1
  return days > 0 ? days : 0
})
const unitPrice = computed(() => Number(booking.price) || 0)
const bookingTotal = computed(() => (unitPrice.value * bookingNights.value).toFixed(2))

function fmtDate(d) {
  return d.toISOString().slice(0, 10)
}

function display(col, row) {
  const raw = row ? row[col.prop] : ''
  if (!raw) return ''
  if (col.ref && refMaps.value[col.ref]) {
    const map = refMaps.value[col.ref]
    return String(raw)
      .split(',')
      .map(id => map[String(id).trim()] || id)
      .join('、')
  }
  return raw
}

async function loadRefMaps() {
  const refs = [...new Set((config.value.columns || []).filter(c => c.ref).map(c => c.ref))]
  for (const r of refs) {
    const res = await listCatalog(r, { page: 1, pageSize: 200 })
    const map = {}
    ;((res.data && res.data.list) || []).forEach(it => {
      map[String(it.id)] = it.name
    })
    refMaps.value = { ...refMaps.value, [r]: map }
  }
}

function showDetail(row) {
  current.value = row
  detailVisible.value = true
  rooms.value = []
  if (isHotel.value) fetchRooms()
}

async function fetchRooms() {
  roomLoading.value = true
  try {
    const res = await getAvailability({
      hotelType: current.value._hotelType,
      hotelId: current.value.id
    })
    rooms.value = res.data || []
  } finally {
    roomLoading.value = false
  }
}

function openBooking(row) {
  booking.roomType = row.roomType
  booking.price = row.price
  const today = new Date()
  const tomorrow = new Date(today.getTime() + 86400000)
  booking.checkIn = fmtDate(today)
  booking.checkOut = fmtDate(tomorrow)
  booking.guests = 1
  bookingVisible.value = true
}

async function confirmBooking() {
  if (!booking.checkIn || !booking.checkOut) {
    ElMessage.warning('请选择入住/离开日期')
    return
  }
  bookingSubmitting.value = true
  try {
    await createBooking({
      hotelType: current.value._hotelType,
      hotelId: current.value.id,
      roomType: booking.roomType,
      checkIn: booking.checkIn,
      checkOut: booking.checkOut,
      guests: booking.guests
    })
    ElMessage.success('预订成功')
    bookingVisible.value = false
    fetchRooms()
  } finally {
    bookingSubmitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
