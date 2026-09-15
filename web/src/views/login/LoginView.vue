<template>
  <div class="login-wrapper">
    <!-- 全局氛围背景：利用 CSS 径向渐变打造现代光晕效果 -->
    <div class="ambient-bg shape-1"></div>
    <div class="ambient-bg shape-2"></div>
    <div class="ambient-bg shape-3"></div>

    <div class="login-container">
      <!-- 左侧：悬浮的品牌文案区（无背景框，直接与全局背景融合） -->
      <div class="login-intro">
        <div class="brand">
          <img class="logo" src="https://api.iconify.design/logos:vue.svg" alt="Logo" />
          <span class="brand-name">[Project Name]</span>
        </div>
        <h1 class="slogan">构建新一代<br/>企业级基础底座</h1>
        <p class="desc">
          提供开箱即用的权限管控、动态路由与全栈规范。<br/>
          告别繁琐配置，专注核心业务逻辑开发。
        </p>
        
        <div class="feature-badges">
          <span class="badge">RBAC 权限</span>
          <span class="badge">Vue 3 + Element Plus</span>
          <span class="badge">Spring Boot 3</span>
        </div>
      </div>

      <!-- 右侧：悬浮登录卡片 -->
      <div class="login-card">
        <div class="card-header">
          <h2>欢迎回来</h2>
          <p>请登录您的企业账号</p>
        </div>

        <el-form 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules" 
          size="large" 
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <!-- 账号 -->
          <el-form-item prop="username">
            <el-input 
              v-model.trim="loginForm.username" 
              placeholder="用户名 / 工号" 
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="登录密码" 
              :prefix-icon="Lock" 
              show-password
            />
          </el-form-item>

          <!-- 验证码 -->
          <el-form-item prop="captcha" class="captcha-item">
            <div class="captcha-box">
              <el-input 
                v-model.trim="loginForm.captcha" 
                placeholder="验证码" 
                :prefix-icon="Key"
                maxlength="4"
              />
              <div class="captcha-img" @click="refreshCaptcha" title="点击刷新">
                <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" />
                <span v-else>加载中</span>
              </div>
            </div>
          </el-form-item>

          <!-- 辅助功能 -->
          <div class="form-options">
            <el-checkbox v-model="loginForm.rememberMe">记住账号</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>

          <!-- 登录按钮 -->
          <el-button 
            class="submit-btn" 
            type="primary" 
            size="large" 
            :loading="loading" 
            @click="handleLogin"
          >
            {{ loading ? '验证中...' : '登 录' }}
          </el-button>
        </el-form>
      </div>
    </div>
    
    <!-- 底部全局版权 -->
    <div class="global-footer">
      <p>Copyright © 2024 - 2026 [Company Name] All Rights Reserved.</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, User, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const captchaUrl = ref('')
const captchaUuid = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false,
  uuid: ''
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入登录密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

onMounted(() => {
  initRememberMe()
  refreshCaptcha()
})

function initRememberMe() {
  const rememberedUsername = localStorage.getItem('login_username')
  if (rememberedUsername) {
    loginForm.username = rememberedUsername
    loginForm.rememberMe = true
  }
}

async function refreshCaptcha() {
  // 占位演示，实际替换为后端接口
  captchaUrl.value = `https://dummyimage.com/120x40/e2e8f0/64748b&text=${Math.floor(1000 + Math.random() * 9000)}`
  captchaUuid.value = 'mock-uuid-' + Date.now()
}

async function handleLogin() {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    loginForm.uuid = captchaUuid.value
    await userStore.loginByPassword(loginForm)
    
    if (loginForm.rememberMe) {
      localStorage.setItem('login_username', loginForm.username)
    } else {
      localStorage.removeItem('login_username')
    }

    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
    
  } catch (error) {
    if (error?.message) {
      loginForm.captcha = ''
      refreshCaptcha()
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 根级布局：全屏，相对定位用于容纳背景 */
.login-wrapper {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8fafc; /* 极简的浅灰蓝底色 */
  overflow: hidden;
}

/* ================= 现代 SaaS 风格光晕背景 ================= */
.ambient-bg {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  z-index: 0;
  opacity: 0.6;
}
.shape-1 {
  width: 600px;
  height: 600px;
  background: rgba(59, 130, 246, 0.25); /* 科技蓝 */
  top: -150px;
  left: -100px;
}
.shape-2 {
  width: 500px;
  height: 500px;
  background: rgba(139, 92, 246, 0.2); /* 柔和紫 */
  bottom: -100px;
  right: -50px;
}
.shape-3 {
  width: 400px;
  height: 400px;
  background: rgba(16, 185, 129, 0.15); /* 点缀绿 */
  top: 40%;
  left: 40%;
}

/* ================= 主体容器 ================= */
.login-container {
  position: relative;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 1100px;
  padding: 0 40px;
}

/* ================= 左侧：悬浮文案区 ================= */
.login-intro {
  max-width: 500px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}
.logo {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.brand-name {
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.slogan {
  font-size: 42px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.3;
  margin-bottom: 24px;
  letter-spacing: -1px;
}

.desc {
  font-size: 16px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 32px;
}

.feature-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.badge {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 20px;
  font-size: 13px;
  color: #475569;
  backdrop-filter: blur(4px);
}

/* ================= 右侧：悬浮白底卡片 ================= */
.login-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px); /* 毛玻璃质感 */
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.08);
  border-radius: 20px;
  padding: 48px 40px;
}

.card-header {
  margin-bottom: 32px;
}
.card-header h2 {
  font-size: 24px;
  color: #0f172a;
  font-weight: 600;
  margin: 0 0 8px 0;
}
.card-header p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

/* 覆盖 Element Plus 输入框的默认样式，使其更清爽 */
.login-form :deep(.el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  border-radius: 8px;
  transition: all 0.2s;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2) inset, 0 0 0 1px #3b82f6 inset;
}
.login-form :deep(.el-input__inner) {
  height: 46px;
}

.captcha-box {
  display: flex;
  gap: 12px;
  width: 100%;
}
.captcha-img {
  width: 110px;
  height: 46px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
}
.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: -8px 0 24px 0;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* ================= 底部版权 ================= */
.global-footer {
  position: absolute;
  bottom: 24px;
  width: 100%;
  text-align: center;
  z-index: 10;
}
.global-footer p {
  color: #94a3b8;
  font-size: 13px;
  margin: 0;
}

/* ================= 响应式适配 ================= */
@media (max-width: 992px) {
  .login-container {
    flex-direction: column;
    justify-content: center;
    gap: 40px;
  }
  .login-intro {
    text-align: center;
    max-width: 100%;
  }
  .brand {
    justify-content: center;
  }
  .feature-badges {
    justify-content: center;
  }
}
@media (max-width: 480px) {
  .login-card {
    width: 100%;
    padding: 32px 24px;
  }
  .slogan {
    font-size: 32px;
  }
}
</style>