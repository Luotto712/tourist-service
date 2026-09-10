<template>
  <div class="login-container">
    <div class="login-right-bg" />
    <div class="login-card">
      <div class="login-header">
        <a href="https://www.wuhouci.net.cn" target="_blank" class="login-logo-link">
          <img src="/logo.png" alt="武侯祠" class="login-logo" />
        </a>
        <h1 class="login-title">武侯祠游客服务中心</h1>
      </div>

      <el-form ref="formRef" :model="form" class="login-form" size="large" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" clearable />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Iphone" clearable />
        </el-form-item>

        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" :prefix-icon="Message" clearable />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password clearable />
        </el-form-item>

        <el-form-item prop="confirmPassword" :error="confirmError">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" :prefix-icon="Lock" show-password clearable />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" :disabled="!canSubmit" @click="handleRegister">
            点击注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-links">
        <router-link to="/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Iphone, Message } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 两次密码不一致时在输入栏下提示
const confirmError = computed(() => {
  if (form.confirmPassword && form.confirmPassword !== form.password) {
    return '与密码输入不一致'
  }
  return ''
})

// 仅当全部填写且两次密码一致时，才可点击「点击注册」
const canSubmit = computed(() =>
  !!form.username && !!form.phone && !!form.email && !!form.password &&
  !!form.confirmPassword && form.password === form.confirmPassword
)

async function handleRegister() {
  if (!canSubmit.value) return
  if (!/^1[3-9]\d{9}$/.test(form.phone)) { ElMessage.warning('请输入正确的手机号'); return }
  if (!/^\S+@\S+\.\S+$/.test(form.email)) { ElMessage.warning('请输入正确的邮箱'); return }
  loading.value = true
  try {
    await register({
      username: form.username,
      phone: form.phone,
      email: form.email,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    const msg = error?.response?.data?.message || error?.message || '注册失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6b2a2d 0%, #803438 30%, #a04448 70%, #c4696d 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

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
  mask-image: linear-gradient(to left, rgba(0,0,0,1) 40%, rgba(0,0,0,0) 100%);
  -webkit-mask-image: linear-gradient(to left, rgba(0,0,0,1) 40%, rgba(0,0,0,0) 100%);
}

.login-card {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 40px 40px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  transform: translateX(-260px);

  .login-header {
    text-align: center;
    margin-bottom: 28px;

    .login-logo-link {
      display: inline-flex;
    }

    .login-logo {
      width: 200px;
      height: auto;
      margin-bottom: 16px;
      object-fit: contain;
    }

    .login-title {
      font-size: 22px;
      font-weight: 700;
      color: brown;
      line-height: 1.4;
    }
  }

  .login-form {
    .login-btn {
      width: 100%;
      height: 44px;
      font-size: 16px;
      letter-spacing: 2px;
    }
  }

  .login-links {
    display: flex;
    justify-content: center;
    margin-top: 16px;
    font-size: 14px;

    a {
      color: #606266;
      text-decoration: none;
      transition: color 0.2s;

      &:hover {
        color: #409eff;
      }
    }
  }
}
</style>
