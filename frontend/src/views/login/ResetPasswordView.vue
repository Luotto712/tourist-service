<template>
  <div class="reset-password-container">
    <div class="login-right-bg" />
    <div class="reset-password-card">
      <div class="card-header">
        <el-icon class="header-icon"><Lock /></el-icon>
        <h1>重置密码</h1>
      </div>

      <p class="card-desc">
        为账号 <strong>{{ username }}</strong> 设置新密码
      </p>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" class="reset-form">
        <el-form-item prop="newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="back-link">
        <router-link to="/login">
          <el-icon><ArrowLeft /></el-icon>
          返回登录
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { resetPassword } from '@/api/auth'
import { Lock, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const username = route.query.username || ''

const form = reactive({ newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

async function handleSubmit() {
  if (!formRef.value) return
  if (!username) {
    ElMessage.error('无效的请求，请返回重新验证身份')
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await resetPassword({ username, newPassword: form.newPassword })
    ElMessage.success('密码更改成功，请重新登录')
    router.push('/login')
  } catch {
    /* handled by interceptor */
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.reset-password-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #57181b 0%, #701e22 30%, #96282c 70%, #bb4a4e 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;

  .login-right-bg {
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 50%;
    background: url('/left_img.jpg') center/cover no-repeat;
    opacity: 0.38;
    pointer-events: none;
    mix-blend-mode: overlay;
    mask-image: linear-gradient(to left, rgba(0, 0, 0, 1) 40%, rgba(0, 0, 0, 0) 100%);
    -webkit-mask-image: linear-gradient(to left, rgba(0, 0, 0, 1) 40%, rgba(0, 0, 0, 0) 100%);
  }
}
.reset-password-card {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  transform: translateX(-260px);
  .card-header {
    text-align: center;
    margin-bottom: 20px;
    .header-icon {
      font-size: 48px;
      color: #409eff;
    }
    h1 {
      font-size: 22px;
      font-weight: 700;
      color: #1e3c72;
      margin-top: 12px;
    }
  }
  .card-desc {
    color: #909399;
    font-size: 14px;
    text-align: center;
    margin-bottom: 28px;
    strong {
      color: #409eff;
    }
  }
  .reset-form .submit-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
  }
  .back-link {
    text-align: center;
    margin-top: 20px;
    a {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      color: #606266;
      font-size: 14px;
      text-decoration: none;
      &:hover {
        color: #409eff;
      }
    }
  }
}
</style>
