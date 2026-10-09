<template>
  <div class="change-password-page">
    <el-card class="password-card card-shadow">
      <template #header>
        <h2 class="card-title">修改密码</h2>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        size="large"
        class="password-form"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="form.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit"> 确认修改 </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { changePassword } from '@/api/auth'
import { ElMessage } from 'element-plus'

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码修改成功')
    formRef.value.resetFields()
  } catch {
    // Error handled by request interceptor
  } finally {
    loading.value = false
  }
}

function handleReset() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style lang="scss" scoped>
.change-password-page {
  display: flex;
  justify-content: center;
  padding: 40px 0;

  .password-card {
    width: 560px;
    max-width: 100%;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }

    .password-form {
      max-width: 440px;
    }
  }
}
</style>
