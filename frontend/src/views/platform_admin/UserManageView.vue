<template>
  <div class="user-manage-page">
    <el-card class="card-shadow">
      <template #header>
        <h2>用户管理</h2>
      </template>

      <el-form :model="searchForm" inline class="mb-20">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名/姓名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable style="width: 160px">
            <el-option v-for="o in roleOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="users" border stripe empty-text="暂无用户数据">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column label="角色" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="roleType[row.role] || 'info'">{{ roleLabel[row.role] || row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="(val) => handleToggleStatus(row, val)" />
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" class="pagination-wrapper mt-20">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchUsers"
          @size-change="fetchUsers"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { searchUsers } from '@/api/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { roleLabel, roleType, roleOptions } from '@/constants/roles'

const loading = ref(false)
const users = ref([])
const total = ref(0)

const searchForm = reactive({ keyword: '', role: '', college: '' })
const pagination = reactive({ page: 1, size: 10 })

async function fetchUsers() {
  loading.value = true
  try {
    const res = await searchUsers({
      keyword: searchForm.keyword || undefined,
      role: searchForm.role || undefined,
      page: pagination.page,
      pageSize: pagination.size
    })
    const data = res.data || res
    users.value = data.records || data.content || data.list || data || []
    total.value = data.total || data.totalElements || users.value.length
  } catch {
    users.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function handleToggleStatus(row, val) {
  try {
    await request.put(`/users/${row.id}/status`, { status: val ? 1 : 0 })
    ElMessage.success(val ? '已启用' : '已禁用')
    row.status = val ? 1 : 0
  } catch { /* handled */ }
}

function handleSearch() { pagination.page = 1; fetchUsers() }
function handleReset() {
  searchForm.keyword = ''
  searchForm.role = ''
  pagination.page = 1
  fetchUsers()
}

onMounted(() => fetchUsers())
</script>

<style lang="scss" scoped>
.user-manage-page {
  .pagination-wrapper { display: flex; justify-content: center; }
}
</style>
