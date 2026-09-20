<template>
  <div class="login-page">

    <!-- =====================================================
         动态视频背景
    ====================================================== -->
    <video class="video-background" autoplay loop muted playsinline>
      <source src="/login-bg-waves.mp4" type="video/mp4" />
    </video>

    <!-- 全局极轻视频柔化层 -->
    <div class="video-overlay"></div>

    <!-- =====================================================
         主体内容
    ====================================================== -->
    <main class="login-container">

      <!-- ===================================================
           左侧品牌 / 介绍区域
      ==================================================== -->
      <section class="login-intro">

        <!-- 左侧液态毛玻璃保护层 -->
        <div class="intro-glass"></div>

        <!-- 左侧内容 -->
        <div class="intro-inner">

          <div class="brand">
            <div class="logo-wrapper">
              <div class="logo-halo"></div>

              <img
                class="logo"
                src="https://api.iconify.design/logos:vue.svg"
                alt="Logo"
              />
            </div>

            <span class="brand-name">
              [Project Name]
            </span>
          </div>

          <div class="intro-content">

            <div class="eyebrow">
              ENTERPRISE PLATFORM
            </div>

            <h1 class="slogan">
              构建新一代
              <br />

              <span>
                企业级基础底座
              </span>
            </h1>

            <p class="desc">
              提供开箱即用的权限管控、动态路由与全栈规范。
              <br />
              告别繁琐配置，专注核心业务逻辑开发。
            </p>

            <div class="feature-badges">

              <span class="badge">
                <i></i>
                RBAC 权限
              </span>

              <span class="badge">
                <i></i>
                Vue 3 + Element Plus
              </span>

              <span class="badge">
                <i></i>
                Spring Boot 3
              </span>

            </div>

          </div>
        </div>
      </section>

      <!-- ===================================================
           登录卡片
      ==================================================== -->
      <section class="login-card">

        <div class="card-top-light"></div>

        <div class="card-color-glow"></div>

        <div class="card-header">

          <div class="header-icon">
            <Lock />
          </div>

          <div>
            <h2>欢迎回来</h2>
            <p>请登录您的企业账号</p>
          </div>

        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          size="large"
          class="login-form"
          @keyup.enter="handleLogin"
        >

          <!-- 用户名 -->
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
          <el-form-item
            prop="captcha"
            class="captcha-item"
          >

            <div class="captcha-box">

              <el-input
                v-model.trim="loginForm.captcha"
                placeholder="验证码"
                :prefix-icon="Key"
                maxlength="4"
              />

              <div
                class="captcha-img"
                @click="refreshCaptcha"
                title="点击刷新验证码"
              >

                <img
                  v-if="captchaUrl"
                  :src="captchaUrl"
                  alt="验证码"
                />

                <span v-else>
                  加载中
                </span>

              </div>

            </div>

          </el-form-item>

          <!-- 选项 -->
          <div class="form-options">

            <el-checkbox v-model="loginForm.rememberMe">
              记住账号
            </el-checkbox>

            <el-link
              type="primary"
              :underline="false"
            >
              忘记密码？
            </el-link>

          </div>

          <!-- 登录 -->
          <el-button
            class="submit-btn"
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >

            <span class="button-text">
              {{ loading ? '验证中...' : '登 录' }}
            </span>

            <span class="button-light"></span>

          </el-button>

        </el-form>

        <div class="card-footer">

          <span class="security-dot"></span>

          <span>
            Secure Enterprise Authentication
          </span>

        </div>

      </section>

    </main>

    <!-- =====================================================
         Footer
    ====================================================== -->
    <footer class="global-footer">
      Copyright © 2024 - 2026 [Company Name] All Rights Reserved.
    </footer>

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


/* =========================================================
   登录表单数据
========================================================= */
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false,
  uuid: ''
})


const loginRules = {

  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: 'blur'
    }
  ],

  password: [
    {
      required: true,
      message: '请输入登录密码',
      trigger: 'blur'
    }
  ],

  captcha: [
    {
      required: true,
      message: '请输入验证码',
      trigger: 'blur'
    }
  ]

}


/* =========================================================
   初始化
========================================================= */
onMounted(() => {
  initRememberMe()
  refreshCaptcha()
})


function initRememberMe() {

  const rememberedUsername =
    localStorage.getItem('login_username')

  if (rememberedUsername) {

    loginForm.username =
      rememberedUsername

    loginForm.rememberMe =
      true
  }
}


/* =========================================================
   验证码
========================================================= */
async function refreshCaptcha() {

  captchaUrl.value =
    `https://dummyimage.com/120x40/e2e8f0/64748b&text=${Math.floor(
      1000 + Math.random() * 9000
    )}`

  captchaUuid.value =
    'mock-uuid-' + Date.now()
}


/* =========================================================
   登录逻辑
========================================================= */
async function handleLogin() {

  if (!loginFormRef.value) return

  try {

    await loginFormRef.value.validate()

    loading.value = true

    loginForm.uuid =
      captchaUuid.value

    await userStore.loginByPassword(
      loginForm
    )

    if (loginForm.rememberMe) {

      localStorage.setItem(
        'login_username',
        loginForm.username
      )

    } else {

      localStorage.removeItem(
        'login_username'
      )
    }

    ElMessage.success(
      '登录成功'
    )

    router.replace(
      route.query.redirect ||
      '/dashboard'
    )

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

/* =========================================================
   页面
========================================================= */
.login-page {

  position: relative;

  width: 100vw;
  height: 100vh;

  overflow: hidden;

  display: flex;

  align-items: center;
  justify-content: center;

  background: #f4f7ff;
}


/* =========================================================
   视频背景
========================================================= */
.video-background {

  position: absolute;

  top: 50%;
  left: 50%;

  min-width: 100%;
  min-height: 100%;

  width: auto;
  height: auto;

  transform:
    translateX(-50%)
    translateY(-50%);

  object-fit: cover;

  z-index: 0;
}


/* =========================================================
   全局视频柔化层
========================================================= */
.video-overlay {

  position: absolute;

  inset: 0;

  /*
    保持视频原本的颜色，
    只进行非常轻微的柔化。
  */
  background:
    rgba(255, 255, 255, 0.10);

  z-index: 1;

  pointer-events: none;
}


/* =========================================================
   主体
========================================================= */
.login-container {

  position: relative;

  z-index: 10;

  width: 100%;
  max-width: 1120px;

  display: flex;

  align-items: center;
  justify-content: space-between;

  gap: 90px;

  padding: 0 40px;
}


/* =========================================================
   左侧区域
========================================================= */
.login-intro {

  position: relative;

  width: 520px;
  max-width: 520px;

  animation:
    introEnter
    0.9s
    cubic-bezier(0.16, 1, 0.3, 1)
    both;
}


/* =========================================================
   左侧液态毛玻璃保护层
========================================================= */
.intro-glass {

  position: absolute;

  top: -60px;
  left: -60px;

  width: 620px;
  height: 620px;

  border-radius: 50%;

  /*
    保持之前那种很轻的空气感玻璃。
    这里不再继续增强，
    避免左侧变成一块灰白色区域。
  */
  background:
    radial-gradient(
      circle at 40% 42%,

      rgba(255, 255, 255, 0.34) 0%,

      rgba(255, 255, 255, 0.22) 28%,

      rgba(255, 255, 255, 0.10) 55%,

      rgba(255, 255, 255, 0.00) 76%
    );

  backdrop-filter:
    blur(10px)
    saturate(115%);

  -webkit-backdrop-filter:
    blur(10px)
    saturate(115%);

  box-shadow:

    0 30px 80px
    rgba(80, 110, 160, 0.045),

    inset 0 1px 0
    rgba(255, 255, 255, 0.15);

  /*
    保持轻盈，
    不再增加白色覆盖。
  */
  opacity: 0.82;

  pointer-events: none;

  z-index: 0;

  /*
    边缘自然消失。
  */
  mask-image:
    radial-gradient(
      circle,

      #000 35%,

      rgba(0, 0, 0, 0.85) 48%,

      rgba(0, 0, 0, 0.45) 65%,

      transparent 78%
    );

  -webkit-mask-image:
    radial-gradient(
      circle,

      #000 35%,

      rgba(0, 0, 0, 0.85) 48%,

      rgba(0, 0, 0, 0.45) 65%,

      transparent 78%
    );
}


/* =========================================================
   左侧内容
========================================================= */
.intro-inner {

  position: relative;

  z-index: 2;
}


/* =========================================================
   入场动画
========================================================= */
@keyframes introEnter {

  from {

    opacity: 0;

    transform:
      translateX(-50px);
  }

  to {

    opacity: 1;

    transform:
      translateX(0);
  }
}


/* =========================================================
   Brand
========================================================= */
.brand {

  display: flex;

  align-items: center;

  gap: 14px;

  margin-bottom: 38px;
}


.logo-wrapper {

  position: relative;

  width: 50px;
  height: 50px;

  display: flex;

  align-items: center;
  justify-content: center;

  border-radius: 16px;

  background:
    rgba(255, 255, 255, 0.48);

  border:
    1px solid
    rgba(255, 255, 255, 0.78);

  backdrop-filter:
    blur(15px);

  -webkit-backdrop-filter:
    blur(15px);

  box-shadow:

    0 12px 32px
    rgba(50, 85, 150, 0.08),

    inset 0 1px 0
    rgba(255, 255, 255, 0.90);

  animation:
    logoFloat
    4.5s
    ease-in-out
    infinite;
}


.logo-halo {

  position: absolute;

  inset: -6px;

  border-radius: 20px;

  border:
    1px solid
    rgba(90, 143, 245, 0.12);

  animation:
    logoPulse
    3.5s
    ease-in-out
    infinite;
}


.logo {

  position: relative;

  width: 34px;
  height: 34px;
}


@keyframes logoFloat {

  0%,
  100% {

    transform:
      translateY(0);
  }

  50% {

    transform:
      translateY(-4px);
  }
}


@keyframes logoPulse {

  0%,
  100% {

    opacity: 0.35;

    transform:
      scale(0.96);
  }

  50% {

    opacity: 0.8;

    transform:
      scale(1.04);
  }
}


.brand-name {

  font-size: 26px;

  font-weight: 700;

  color: #172033;

  letter-spacing: -0.6px;

  text-shadow:
    0 1px 8px
    rgba(255, 255, 255, 0.34);
}


/* =========================================================
   Eyebrow
========================================================= */
.eyebrow {

  margin-bottom: 14px;

  font-size: 11px;

  font-weight: 700;

  letter-spacing: 2px;

  color:
    rgba(45, 105, 220, 0.63);

  text-shadow:
    0 1px 10px
    rgba(255, 255, 255, 0.48);
}


/* =========================================================
   主标题
========================================================= */
.slogan {

  margin:
    0 0 24px;

  font-size: 44px;

  line-height: 1.25;

  font-weight: 800;

  letter-spacing: -1.8px;

  color: #182133;

  /*
    第一行保持原来的深色。
    轻微白色阴影让边缘更柔和，
    不承担主要对比度。
  */
  text-shadow:
    0 1px 10px
    rgba(255, 255, 255, 0.28);
}


/* =========================================================
   企业级基础底座
   核心优化
========================================================= */
.slogan span {

  /*
    这里是这次真正的重点。

    不再使用偏浅的蓝紫。
    改成更深、更饱和的蓝紫渐变，
    适合灰白色页面底色。

    最亮的区域也刻意压住，
    避免视频经过时文字被吃掉。
  */
  background:
    linear-gradient(
      90deg,

      #185fc9 0%,

      #315fd2 28%,

      #6557cf 58%,

      #7a58d0 76%,

      #247edc 100%
    );

  background-size:
    300% 100%;

  -webkit-background-clip: text;
  background-clip: text;

  color: transparent;

  animation:
    textGradient
    8s
    linear
    infinite;

  /*
    不使用白色描边。

    改成非常轻的深色阴影，
    让文字和灰白视频之间
    形成更清晰的视觉分离。
  */
  text-shadow:

    0 1px 2px
    rgba(30, 60, 120, 0.18),

    0 3px 12px
    rgba(45, 65, 120, 0.12);

  /*
    保持厚实感。
  */
  font-weight: 800;
}


@keyframes textGradient {

  0% {

    background-position:
      0% 50%;
  }

  100% {

    background-position:
      300% 50%;
  }
}


/* =========================================================
   描述文字
========================================================= */
.desc {

  margin:
    0 0 30px;

  font-size: 15px;

  line-height: 1.8;

  color: #667085;

  text-shadow:
    0 1px 9px
    rgba(255, 255, 255, 0.36);
}


/* =========================================================
   特性标签
========================================================= */
.feature-badges {

  display: flex;

  flex-wrap: wrap;

  gap: 10px;
}


.badge {

  display: inline-flex;

  align-items: center;

  gap: 7px;

  padding:
    8px 14px;

  border-radius: 999px;

  background:
    rgba(255, 255, 255, 0.46);

  border:
    1px solid
    rgba(255, 255, 255, 0.70);

  backdrop-filter:
    blur(12px);

  -webkit-backdrop-filter:
    blur(12px);

  color: #606b7d;

  font-size: 12px;

  box-shadow:
    0 8px 22px
    rgba(60, 90, 140, 0.04);

  transition:
    all 0.25s ease;
}


.badge i {

  width: 5px;
  height: 5px;

  border-radius: 50%;

  background: #6398ed;

  box-shadow:
    0 0 8px
    rgba(99, 152, 237, 0.45);
}


.badge:hover {

  transform:
    translateY(-3px);

  background:
    rgba(255, 255, 255, 0.72);

  box-shadow:
    0 12px 28px
    rgba(60, 90, 140, 0.08);
}


/* =========================================================
   右侧登录卡片
========================================================= */
.login-card {

  position: relative;

  width: 420px;

  padding:
    42px 40px 27px;

  border-radius: 28px;

  background:
    rgba(255, 255, 255, 0.52);

  border:
    1px solid
    rgba(255, 255, 255, 0.78);

  backdrop-filter:
    blur(34px)
    saturate(160%);

  -webkit-backdrop-filter:
    blur(34px)
    saturate(160%);

  box-shadow:

    0 35px 90px
    rgba(40, 62, 105, 0.12),

    0 12px 35px
    rgba(80, 110, 180, 0.06),

    inset 0 1px 0
    rgba(255, 255, 255, 0.94),

    inset 0 -1px 0
    rgba(255, 255, 255, 0.20);

  overflow: hidden;

  animation:
    cardEnter
    0.9s
    cubic-bezier(0.16, 1, 0.3, 1)
    both;

  transition:
    transform 0.4s ease,
    box-shadow 0.4s ease;
}


.login-card:hover {

  transform:
    translateY(-4px);

  box-shadow:

    0 42px 100px
    rgba(40, 62, 105, 0.15),

    0 15px 40px
    rgba(80, 110, 180, 0.08),

    inset 0 1px 0
    rgba(255, 255, 255, 0.96);
}


@keyframes cardEnter {

  from {

    opacity: 0;

    transform:
      translateY(38px)
      scale(0.965);
  }

  to {

    opacity: 1;

    transform:
      translateY(0)
      scale(1);
  }
}


/* =========================================================
   卡片顶部光线
========================================================= */
.card-top-light {

  position: absolute;

  left: 0;
  top: 0;

  width: 100%;
  height: 1px;

  background:
    linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.95),
      transparent
    );
}


.card-color-glow {

  position: absolute;

  width: 300px;
  height: 160px;

  top: -100px;
  left: 50%;

  transform:
    translateX(-50%);

  border-radius: 50%;

  background:
    rgba(100, 145, 255, 0.16);

  filter:
    blur(45px);

  pointer-events: none;
}


/* =========================================================
   卡片 Header
========================================================= */
.card-header {

  position: relative;

  display: flex;

  align-items: center;

  gap: 13px;

  margin-bottom: 30px;
}


.header-icon {

  width: 42px;
  height: 42px;

  flex:
    0 0 42px;

  display: flex;

  align-items: center;
  justify-content: center;

  border-radius: 13px;

  background:
    rgba(255, 255, 255, 0.62);

  border:
    1px solid
    rgba(255, 255, 255, 0.84);

  color: #4a81df;

  box-shadow:
    0 8px 20px
    rgba(60, 95, 155, 0.07);
}


.header-icon :deep(svg) {

  width: 19px;
  height: 19px;
}


.card-header h2 {

  margin:
    0 0 5px;

  font-size: 24px;

  font-weight: 700;

  line-height: 1.2;

  color: #182033;
}


.card-header p {

  margin: 0;

  color: #7b8493;

  font-size: 13px;
}


/* =========================================================
   表单
========================================================= */
.login-form :deep(.el-form-item) {

  margin-bottom: 20px;
}


.login-form :deep(.el-input__wrapper) {

  min-height: 48px;

  border-radius: 12px;

  background:
    rgba(248, 250, 254, 0.63);

  box-shadow:
    0 0 0 1px
    rgba(214, 222, 235, 0.82)
    inset;

  transition:
    all 0.25s ease;
}


.login-form :deep(.el-input__wrapper:hover) {

  background:
    rgba(255, 255, 255, 0.74);

  box-shadow:
    0 0 0 1px
    rgba(167, 181, 205, 0.72)
    inset;
}


.login-form :deep(.el-input__wrapper.is-focus) {

  background:
    rgba(255, 255, 255, 0.88);

  box-shadow:

    0 0 0 1px
    rgba(67, 124, 228, 0.90)
    inset,

    0 0 0 4px
    rgba(67, 124, 228, 0.08);

  transform:
    translateY(-1px);
}


.login-form :deep(.el-input__inner) {

  height: 48px;
}


.login-form :deep(.el-input__prefix-inner) {

  color: #94a2b8;
}


/* =========================================================
   验证码
========================================================= */
.captcha-box {

  width: 100%;

  display: flex;

  gap: 12px;
}


.captcha-box .el-input {

  flex: 1;
}


.captcha-img {

  flex:
    0 0 110px;

  height: 48px;

  display: flex;

  align-items: center;
  justify-content: center;

  overflow: hidden;

  border-radius: 12px;

  border:
    1px solid
    rgba(215, 223, 236, 0.90);

  background:
    rgba(249, 250, 253, 0.68);

  cursor: pointer;

  transition:
    all 0.25s ease;
}


.captcha-img:hover {

  transform:
    translateY(-2px);

  background:
    rgba(255, 255, 255, 0.84);

  box-shadow:
    0 8px 20px
    rgba(60, 90, 140, 0.07);
}


.captcha-img img {

  width: 100%;
  height: 100%;

  object-fit: cover;

  transition:
    transform 0.3s ease;
}


.captcha-img:hover img {

  transform:
    scale(1.05);
}


.captcha-img span {

  color: #9aa4b3;

  font-size: 12px;
}


/* =========================================================
   表单选项
========================================================= */
.form-options {

  display: flex;

  align-items: center;

  justify-content: space-between;

  margin:
    -1px 0 23px;
}


.form-options :deep(.el-checkbox__label) {

  color: #697486;

  font-size: 13px;
}


.form-options :deep(.el-link) {

  font-size: 13px;
}


/* =========================================================
   登录按钮
========================================================= */
.submit-btn {

  position: relative;

  width: 100%;

  height: 50px;

  overflow: hidden;

  border: none;

  border-radius: 12px;

  font-size: 15px;

  font-weight: 600;

  letter-spacing: 2px;

  background:
    linear-gradient(
      135deg,
      #417ff0,
      #596fe7,
      #8064df
    );

  box-shadow:
    0 12px 28px
    rgba(70, 105, 225, 0.24);

  transition:
    all 0.25s ease;
}


.submit-btn:hover {

  transform:
    translateY(-2px);

  box-shadow:
    0 16px 35px
    rgba(70, 105, 225, 0.30);
}


.submit-btn:active {

  transform:
    translateY(0);
}


.button-text {

  position: relative;

  z-index: 2;
}


.button-light {

  position: absolute;

  top: 0;
  left: -120px;

  width: 90px;
  height: 100%;

  transform:
    skewX(-22deg);

  background:
    linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.35),
      transparent
    );

  animation:
    buttonLight
    4.5s
    ease-in-out
    infinite;
}


@keyframes buttonLight {

  0% {

    left: -120px;
  }

  25% {

    left: 120%;
  }

  100% {

    left: 120%;
  }
}


/* =========================================================
   卡片 Footer
========================================================= */
.card-footer {

  margin-top: 22px;

  display: flex;

  align-items: center;
  justify-content: center;

  gap: 7px;

  color: #b1bac7;

  font-size: 10px;

  letter-spacing: 0.5px;
}


.security-dot {

  width: 5px;
  height: 5px;

  border-radius: 50%;

  background: #74c99d;

  box-shadow:
    0 0 8px
    rgba(116, 201, 157, 0.5);

  animation:
    securityPulse
    2.5s
    ease-in-out
    infinite;
}


@keyframes securityPulse {

  0%,
  100% {

    transform:
      scale(0.85);

    opacity: 0.45;
  }

  50% {

    transform:
      scale(1.12);

    opacity: 1;
  }
}


/* =========================================================
   Footer
========================================================= */
.global-footer {

  position: absolute;

  left: 0;
  bottom: 20px;

  width: 100%;

  z-index: 10;

  text-align: center;

  color:
    rgba(100, 116, 139, 0.70);

  font-size: 11px;
}


/* =========================================================
   响应式
========================================================= */
@media (max-width: 992px) {

  .login-container {

    flex-direction: column;

    justify-content: center;

    gap: 35px;
  }


  .login-intro {

    width: 100%;

    max-width: 100%;

    text-align: center;
  }


  .intro-glass {

    left: 50%;

    transform:
      translateX(-50%);
  }


  .brand {

    justify-content: center;
  }


  .feature-badges {

    justify-content: center;
  }


  .desc br {

    display: none;
  }
}


@media (max-width: 600px) {

  .login-page {

    padding: 20px;
  }


  .login-intro {

    display: none;
  }


  .login-container {

    width: 100%;

    padding: 0;
  }


  .login-card {

    width: 100%;

    max-width: 420px;

    padding:
      36px 24px 25px;
  }


  .global-footer {

    padding:
      0 20px;
  }
}


/* =========================================================
   减少动态效果
========================================================= */
@media (prefers-reduced-motion: reduce) {

  *,
  *::before,
  *::after {

    animation-duration:
      0.01ms !important;

    animation-iteration-count:
      1 !important;

    transition-duration:
      0.01ms !important;
  }
}

</style>
