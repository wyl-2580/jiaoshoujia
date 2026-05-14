<template>
  <div class="login-container">
    <div class="login-bg-decoration">
      <div class="circle circle-1" />
      <div class="circle circle-2" />
      <div class="circle circle-3" />
    </div>
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <h2 class="login-title">脚手架管理系统</h2>
        <p class="login-subtitle">Enterprise Admin System</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            size="large"
            :prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon class="password-toggle" @click="showPassword = !showPassword">
                <View v-if="showPassword" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import Cookies from 'js-cookie'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const showPassword = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 5, message: '密码长度不能少于5个字符', trigger: 'blur' }
  ]
}

function getCookieData() {
  const username = Cookies.get('username')
  const password = Cookies.get('password')
  const rememberMe = Cookies.get('rememberMe')
  if (rememberMe === 'true' && username && password) {
    loginForm.username = username
    loginForm.password = decodeURIComponent(password)
    loginForm.rememberMe = true
  }
}

function handleLogin() {
  loginFormRef.value?.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      if (loginForm.rememberMe) {
        Cookies.set('username', loginForm.username, { expires: 30 })
        Cookies.set('password', encodeURIComponent(loginForm.password), { expires: 30 })
        Cookies.set('rememberMe', 'true', { expires: 30 })
      } else {
        Cookies.remove('username')
        Cookies.remove('password')
        Cookies.remove('rememberMe')
      }
      await userStore.login({
        username: loginForm.username,
        password: loginForm.password
      })
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
      ElMessage.success('登录成功')
    } catch (error: any) {
      ElMessage.error(error?.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

getCookieData()
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    background: #fff;
  }

  .circle-1 {
    width: 400px;
    height: 400px;
    top: -100px;
    right: -100px;
    animation: float 6s ease-in-out infinite;
  }

  .circle-2 {
    width: 300px;
    height: 300px;
    bottom: -80px;
    left: -60px;
    animation: float 8s ease-in-out infinite reverse;
  }

  .circle-3 {
    width: 200px;
    height: 200px;
    top: 50%;
    left: 50%;
    animation: float 10s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.login-card {
  width: 420px;
  padding: 20px 10px;
  border-radius: 16px;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px;
  letter-spacing: 2px;
}

.login-subtitle {
  font-size: 13px;
  color: #909399;
  margin: 0;
  letter-spacing: 1px;
}

.login-form {
  padding: 0 10px;
}

.password-toggle {
  cursor: pointer;
  color: #909399;
  transition: color 0.2s;

  &:hover {
    color: #409eff;
  }
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  border-radius: 8px;
}
</style>
