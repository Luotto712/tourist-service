<template>
  <el-card>
    <template #header>
      <div class="header-bar">
        <span>酒店营销</span>
        <el-button type="primary" @click="openDialog()">录入营销</el-button>
      </div>
    </template>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="编号" width="70">
        <template #default="{ $index }">{{ $index + 1 }}</template>
      </el-table-column>
      <el-table-column label="酒店" width="220">
        <template #default="{ row }">{{ hotelName(row) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="120">
        <template #default="{ row }">{{ row.hotelType === 'STAR' ? '星级' : '非星级' }}</template>
      </el-table-column>
      <el-table-column prop="content" label="营销内容" show-overflow-tooltip min-width="240" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑酒店营销' : '录入酒店营销'" width="520px" :close-on-click-modal="false">
      <el-form :model="form" label-width="90px">
        <el-form-item label="酒店类型">
          <el-select v-model="form.hotelType" @change="onTypeChange">
            <el-option label="星级酒店" value="STAR" />
            <el-option label="非星级/乡村" value="NONSTAR" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择酒店">
          <el-select v-model="form.hotelId" filterable placeholder="选择酒店" style="width: 100%">
            <el-option v-for="h in hotelOptions" :key="h.id" :label="h.name" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="营销内容"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listMarketing, createMarketing, updateMarketing, deleteMarketing } from '@/api/hotelMarketing'
import { listCatalog } from '@/api/catalog'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const editing = ref(null)
const hotels = ref([])
const form = reactive({ hotelType: 'STAR', hotelId: null, content: '' })

const hotelOptions = computed(() => hotels.value.filter(h => (form.hotelType === 'STAR' ? h._isStar : !h._isStar)))

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await listMarketing({})
    list.value = res.data || []
  } finally {
    loading.value = false
  }
  const [star, nonstar] = await Promise.all([
    listCatalog('hotels/star', { page: 1, pageSize: 100 }),
    listCatalog('hotels/nonstar', { page: 1, pageSize: 100 })
  ])
  hotels.value = [
    ...((star.data && star.data.list) || []).map(h => ({ ...h, _isStar: true })),
    ...((nonstar.data && nonstar.data.list) || []).map(h => ({ ...h, _isStar: false }))
  ]
}

function onTypeChange() { form.hotelId = null }
function hotelName(row) {
  const h = hotels.value.find(x => x.id === row.hotelId)
  return h ? h.name : `#${row.hotelId}`
}

function openDialog(row) {
  editing.value = row || null
  form.hotelType = row ? row.hotelType : 'STAR'
  form.hotelId = row ? row.hotelId : null
  form.content = row ? row.content : ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.hotelId) { ElMessage.warning('请选择酒店'); return }
  if (!form.content) { ElMessage.warning('请填写营销内容'); return }
  submitting.value = true
  try {
    const payload = { hotelId: form.hotelId, hotelType: form.hotelType, content: form.content }
    if (editing.value) await updateMarketing(editing.value.id, payload)
    else await createMarketing(payload)
    ElMessage.success('已保存')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定彻底删除该营销记录吗？', '提示', { type: 'warning' })
  await deleteMarketing(row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style lang="scss" scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
