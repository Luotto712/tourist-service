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

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-links">
        <router-link to="/forgot-password">忘记密码</router-link>
        <router-link to="/register">注册账号</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    const msg = error?.response?.data?.message || error?.message || '登录失败，请检查用户名和密码'
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
  mask-image: linear-gradient(to left, rgba(0, 0, 0, 1) 40%, rgba(0, 0, 0, 0) 100%);
  -webkit-mask-image: linear-gradient(to left, rgba(0, 0, 0, 1) 40%, rgba(0, 0, 0, 0) 100%);
}

.login-card {
  width: 420px;
  max-width: 100%;
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  transform: translateX(-260px);

  .login-header {
    text-align: center;
    margin-bottom: 36px;

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
      letter-spacing: 4px;
    }
  }

  .login-links {
    display: flex;
    justify-content: space-between;
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
