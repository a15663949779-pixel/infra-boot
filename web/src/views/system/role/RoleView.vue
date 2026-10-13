<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>System</span>
        <h1>角色管理</h1>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate" round>新增角色</el-button>
    </div>

    <section class="content-section">
      <el-form :model="queryParams" inline>
        <el-form-item label="角色名称">
          <el-input v-model.trim="queryParams.roleName" placeholder="请输入角色名称" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="角色标识">
          <el-input v-model.trim="queryParams.roleKey" placeholder="请输入角色标识" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in dicts.sys_normal_disable" :key="item.value" :label="item.label" :value="Number(item.value)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery" round>搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery" round>重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="roles">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" min-width="160">
          <template #default="{ row }">
            <div class="role-cell">
              <span class="role-badge">{{ row.roleName?.charAt(0) }}</span>
              <span class="role-name">{{ row.roleName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roleKey" label="角色标识" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <dict-tag :options="dicts.sys_normal_disable" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" @click="remove(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-show="total > 0"
        v-model:pageNum="queryParams.pageNum"
        v-model:pageSize="queryParams.pageSize"
        :total="total"
        @pagination="loadData"
      />
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model.trim="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色标识" prop="roleKey">
          <el-input v-model.trim="form.roleKey" placeholder="请输入角色标识" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="菜单权限">
          <div class="tree-box">
            <el-tree
              ref="treeRef"
              :data="menuTree"
              show-checkbox
              node-key="id"
              default-expand-all
              :props="{ label: 'menuName', children: 'children' }"
            />
          </div>
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
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { menuApi, roleApi } from '../../../api/system'
import { buildTreeFromFlat } from '../../../utils/menu'
import { useDict } from '../../../hooks/useDict'
import DictTag from '../../../components/DictTag/index.vue'
import Pagination from '../../../components/Pagination/index.vue'

const dicts = useDict('sys_normal_disable')

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const roles = ref([])
const total = ref(0)
const menus = ref([])
const treeRef = ref()
const formRef = ref()
const form = reactive(defaultForm())
const menuTree = computed(() => buildTreeFromFlat(menus.value))
const dialogTitle = computed(() => (form.id ? '编辑角色' : '新增角色'))

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  roleName: '',
  roleKey: '',
  status: undefined
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入角色标识', trigger: 'blur' }]
}

function defaultForm() {
  return {
    id: null,
    roleName: '',
    roleKey: '',
    sort: 0,
    status: 1,
    menuIds: []
  }
}

function assignForm(data) {
  Object.assign(form, defaultForm(), data)
}

async function loadData() {
  loading.value = true
  try {
    const res = await roleApi.list(queryParams)
    roles.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function loadMenus() {
  menus.value = await menuApi.list() || []
}

function handleQuery() {
  queryParams.pageNum = 1
  loadData()
}

function resetQuery() {
  queryParams.roleName = ''
  queryParams.roleKey = ''
  queryParams.status = undefined
  queryParams.pageNum = 1
  loadData()
}

function openCreate() {
  assignForm(defaultForm())
  dialogVisible.value = true
  nextTick(() => treeRef.value?.setCheckedKeys([]))
}

async function openEdit(row) {
  const [detail, menuIds] = await Promise.all([roleApi.detail(row.id), roleApi.menus(row.id)])
  assignForm({ ...detail, menuIds: menuIds || [] })
  dialogVisible.value = true
  nextTick(() => treeRef.value?.setCheckedKeys(form.menuIds))
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const checked = treeRef.value.getCheckedKeys()
    const halfChecked = treeRef.value.getHalfCheckedKeys()
    const payload = { ...form, menuIds: [...new Set([...checked, ...halfChecked])] }
    if (payload.id) {
      await roleApi.update(payload)
    } else {
      await roleApi.create(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除角色「${row.roleName}」？`, '删除确认', { type: 'warning' })
  await roleApi.remove(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

onMounted(() => {
  loadMenus()
  loadData()
})
</script>

<style scoped>
.role-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.role-badge {
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

.role-name {
  font-weight: 500;
}
</style>
