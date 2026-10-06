<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>System</span>
        <h1>用户管理</h1>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate" round>
        新增用户
      </el-button>
    </div>

    <section class="content-section">
      <el-table v-loading="loading" :data="users">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="140">
          <template #default="{ row }">
            <div class="user-cell">
              <span class="user-avatar">{{ row.username?.charAt(0).toUpperCase() }}</span>
              <span class="user-name">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="190" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <dict-tag :options="dicts.sys_normal_disable" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="danger" @click="remove(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="form.id ? '不修改请留空' : '默认 123456'" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model.trim="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model.trim="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model.trim="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="form.roleIds">
            <el-checkbox v-for="role in roles" :key="role.id" :label="role.id">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit" round>保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { roleApi, userApi } from '../../../api/system'
import { useDict } from '../../../hooks/useDict'
import DictTag from '../../../components/DictTag/index.vue'

const dicts = useDict('sys_normal_disable')

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const users = ref([])
const roles = ref([])
const formRef = ref()
const form = reactive(defaultForm())
const dialogTitle = computed(() => (form.id ? '编辑用户' : '新增用户'))

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

function defaultForm() {
  return {
    id: null,
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    status: 1,
    roleIds: []
  }
}

function assignForm(data) {
  Object.assign(form, defaultForm(), data)
}

async function loadData() {
  loading.value = true
  try {
    const [userList, roleList] = await Promise.all([userApi.list(), roleApi.list()])
    users.value = userList || []
    roles.value = roleList || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  assignForm(defaultForm())
  dialogVisible.value = true
}

async function openEdit(row) {
  const [detail, roleIds] = await Promise.all([userApi.detail(row.id), userApi.roles(row.id)])
  assignForm({ ...detail, password: '', roleIds: roleIds || [] })
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form }
    if (!payload.password) {
      delete payload.password
    }
    if (payload.id) {
      await userApi.update(payload)
    } else {
      await userApi.create(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '删除确认', { type: 'warning' })
  await userApi.remove(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(74, 121, 240, 0.12), rgba(128, 100, 223, 0.08));
  color: var(--brand);
  font-size: 13px;
  font-weight: 600;
}

.user-name {
  font-weight: 500;
}
</style>
