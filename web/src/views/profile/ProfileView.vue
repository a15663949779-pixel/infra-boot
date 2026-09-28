<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人资料</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="profile-form"
      >
        <el-form-item label="头像">
          <div class="avatar-area">
            <el-avatar :size="80" :src="form.avatarUrl || undefined">
              {{ form.nickname?.slice(0, 1)?.toUpperCase() || 'U' }}
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="handleAvatarUpload"
              accept="image/png,image/jpeg,image/gif"
            >
              <el-button size="small" type="primary" :loading="avatarLoading">
                {{ form.avatarUrl ? '更换头像' : '上传头像' }}
              </el-button>
            </el-upload>
          </div>
        </el-form-item>

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
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>

      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-width="100px"
        class="profile-form"
      >
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
          <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword">修改密码</el-button>
          <el-button @click="resetPwdForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
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
.profile-page {
  max-width: 600px;
  margin: 0 auto;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
}

.avatar-area {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  display: inline-block;
}
</style>
