<template>
  <div class="profile-page">
    <el-card class="card-shadow">
      <template #header><h2>个人信息</h2></template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="profile-form" v-loading="loading">
        <el-form-item label="用户名">
          <el-input :model-value="userStore.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input :model-value="userStore.realName" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag>{{ roleLabel }}</el-tag>
        </el-form-item>
        <el-divider />

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getProfile, updateProfile } from '@/api/user'
import { ElMessage } from 'element-plus'
import { roleLabel as roleLabelMap } from '@/constants/roles'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const roleLabel = computed(() => roleLabelMap[userStore.role] || userStore.role || '用户')

const form = reactive({
  phone: '',
  email: '',
  studentNo: '',
  teacherNo: '',
  gpa: null,
  gradeScore: null
})

const rules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

async function fetchProfile() {
  loading.value = true
  try {
    const res = await getProfile()
    const data = res.data || res
    form.phone = data.phone || ''
    form.email = data.email || ''
    form.studentNo = data.studentNo || ''
    form.teacherNo = data.teacherNo || ''
    form.gpa = data.gpa || null
    form.gradeScore = data.gradeScore || null
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updateProfile({ ...form })
    ElMessage.success('个人信息更新成功')
    await userStore.fetchProfile()
  } catch { /* handled */ } finally {
    submitting.value = false
  }
}

onMounted(() => fetchProfile())
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 600px;
  .profile-form { max-width: 480px; }
}
</style>
