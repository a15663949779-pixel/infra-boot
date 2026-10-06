<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>Account</span>
        <h1>个人资料</h1>
      </div>
    </div>

    <div class="profile-layout">
      <section class="content-section profile-hero">
        <div class="hero-avatar">
          <div class="avatar-ring">
            <el-avatar :size="88" :src="form.avatarUrl || undefined">
              {{ form.nickname?.slice(0, 1)?.toUpperCase() || 'U' }}
            </el-avatar>
          </div>
          <el-upload
            class="avatar-upload"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
            accept="image/png,image/jpeg,image/gif"
          >
            <el-button size="small" type="primary" :loading="avatarLoading" round>
              {{ form.avatarUrl ? '更换头像' : '上传头像' }}
            </el-button>
          </el-upload>
        </div>
        <div class="hero-info">
          <h2 class="hero-name">{{ form.nickname || userStore.profile?.username }}</h2>
          <p class="hero-username">@{{ userStore.profile?.username }}</p>
          <div class="hero-meta">
            <span v-if="form.email">
              <el-icon><Message /></el-icon>{{ form.email }}
            </span>
            <span v-if="form.phone">
              <el-icon><Phone /></el-icon>{{ form.phone }}
            </span>
          </div>
        </div>
      </section>

      <div class="profile-columns">
        <section class="content-section profile-form-section">
          <div class="section-head">
            <h2>
              <el-icon><User /></el-icon>基本信息
            </h2>
          </div>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="profile-form">
            <el-form-item label="用户名">
              <el-input :model-value="userStore.profile?.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave" round>保存修改</el-button>
              <el-button @click="resetForm" round>重置</el-button>
            </el-form-item>
          </el-form>
        </section>

        <section class="content-section profile-form-section">
          <div class="section-head">
            <h2>
              <el-icon><Lock /></el-icon>修改密码
            </h2>
          </div>
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" class="profile-form">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码（6-20位）" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword" round>修改密码</el-button>
              <el-button @click="resetPwdForm" round>重置</el-button>
            </el-form-item>
          </el-form>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Phone } from '@element-plus/icons-vue'
import request from '../../api/request'
import { updateProfile, changePassword } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const formRef = ref(null)
const pwdFormRef = ref(null)
const saving = ref(false)
const pwdSaving = ref(false)
const avatarLoading = ref(false)

const form = reactive({
  nickname: '',
  email: '',
  phone: '',
  avatarId: null,
  avatarUrl: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

function loadFormFromStore() {
  const p = userStore.profile
  if (p) {
    form.nickname = p.nickname || ''
    form.email = p.email || ''
    form.phone = p.phone || ''
    form.avatarUrl = p.avatar || ''
    form.avatarId = null
  }
}

onMounted(() => {
  loadFormFromStore()
})

function resetForm() {
  loadFormFromStore()
  formRef.value?.clearValidate()
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateProfile({
      nickname: form.nickname,
      email: form.email,
      phone: form.phone,
      avatar: form.avatarId
    })
    await userStore.loadUserContext()
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = ['image/png', 'image/jpeg', 'image/gif'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG/GIF 格式的图片')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

async function handleAvatarUpload({ file }) {
  avatarLoading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('businessType', 'avatar')
    const data = await request.post('/system/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.avatarId = data.fileId
    form.avatarUrl = data.fileUrl
    ElMessage.success('头像已上传，点击保存生效')
  } finally {
    avatarLoading.value = false
  }
}

function resetPwdForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.clearValidate()
}

async function handleChangePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  pwdSaving.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    resetPwdForm()
  } finally {
    pwdSaving.value = false
  }
}
</script>

<style scoped>
.profile-layout {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.profile-hero {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 28px 32px;
}

.hero-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-ring {
  padding: 4px;
  border-radius: 50%;
  background: var(--brand-grad);
  box-shadow: 0 8px 24px rgba(74, 121, 240, 0.2);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.avatar-ring:hover {
  transform: scale(1.05);
}

.avatar-ring :deep(.el-avatar) {
  background: var(--brand-grad);
  font-size: 32px;
  font-weight: 700;
  border: 3px solid var(--surface);
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.hero-name {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.3px;
  background: var(--grad-text);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-username {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 14px;
}

.hero-meta {
  display: flex;
  gap: 20px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.hero-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--muted);
  font-size: 13px;
}

.hero-meta .el-icon {
  color: var(--brand);
}

.profile-columns {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.profile-form-section {
  padding: 24px;
}

.profile-form-section .section-head {
  margin-bottom: 20px;
}

.profile-form-section .section-head h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.profile-form-section .section-head .el-icon {
  color: var(--brand);
  font-size: 18px;
}

.profile-form {
  max-width: 400px;
}

@media (max-width: 960px) {
  .profile-columns {
    grid-template-columns: 1fr;
  }

  .profile-hero {
    flex-direction: column;
    text-align: center;
  }

  .hero-meta {
    justify-content: center;
  }
}
</style>
