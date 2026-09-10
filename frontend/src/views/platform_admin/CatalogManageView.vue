<template>
  <el-card :key="config.resource">
    <template #header>
      <div class="header-bar">
        <span>{{ config.title }}管理</span>
        <el-button type="primary" @click="openDialog()">新增</el-button>
      </div>
    </template>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column v-for="c in config.columns" :key="c.prop" :prop="c.prop" :label="c.label" show-overflow-tooltip min-width="120" />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editing ? `编辑${config.title}` : `新增${config.title}`" width="560px" :close-on-click-modal="false">
      <el-form :model="form" label-width="100px">
        <el-form-item v-for="c in config.columns" :key="c.prop" :label="c.label">
          <el-input v-if="c.prop === 'intro'" v-model="form[c.prop]" type="textarea" :rows="3" />
          <el-input v-else v-model="form[c.prop]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCatalog, createCatalog, updateCatalog, deleteCatalog } from '@/api/catalog'
import { catalogs } from '@/constants/catalogs'

const props = defineProps({ resource: { type: String, default: '' } })
const route = useRoute()
const config = computed(() => catalogs[props.resource || route.params.resource] || { title: '', resource: '', columns: [] })

const list = ref([])
const loading = ref(false)
const pagination = reactive({ page: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const submitting = ref(false)
const editing = ref(null)
const form = reactive({})

onMounted(fetchList)
watch(() => config.value.resource, (nv, ov) => {
  if (nv && nv !== ov) {
    list.value = []
    pagination.page = 1
    dialogVisible.value = false
    editing.value = null
    fetchList()
  }
})


async function fetchList() {
  loading.value = true
  try {
    const res = await listCatalog(config.value.resource, { page: pagination.page, pageSize: pagination.size })
    const data = res.data || {}
    list.value = data.list || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  editing.value = row || null
  config.value.columns.forEach(c => { form[c.prop] = row ? row[c.prop] ?? '' : '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  submitting.value = true
  try {
    const payload = {}
    config.value.columns.forEach(c => { payload[c.prop] = form[c.prop] })
    if (editing.value) await updateCatalog(config.value.resource, editing.value.id, payload)
    else await createCatalog(config.value.resource, payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除吗？', '提示', { type: 'warning' })
  await deleteCatalog(config.value.resource, row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style lang="scss" scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
