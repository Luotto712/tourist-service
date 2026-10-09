<template>
  <div class="forgot-password-container">
    <div class="login-right-bg" />
    <div class="forgot-password-card">
      <div class="card-header">
        <el-icon class="header-icon"><Lock /></el-icon>
        <h1>忘记密码</h1>
      </div>

      <p class="card-desc">请输入您的用户名和注册邮箱以验证身份</p>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" class="forgot-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入注册邮箱"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
            验证身份
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
import { useRouter } from 'vue-router'
import { forgotPassword } from '@/api/auth'
import { User, Lock, Message, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', email: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

async function handleSubmit() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await forgotPassword({ username: form.username, email: form.email })
    router.push({ path: '/reset-password', query: { username: form.username } })
  } catch {
    ElMessage.error('请输入正确的邮箱或账号')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.forgot-password-container {
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
.forgot-password-card {
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
    line-height: 1.6;
  }
  .forgot-form .submit-btn {
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
