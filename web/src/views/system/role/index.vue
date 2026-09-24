<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>System</span>
        <h1>角色管理</h1>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增角色</el-button>
    </div>

    <section class="content-section">
      <el-table v-loading="loading" :data="roles" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" min-width="160" />
        <el-table-column prop="roleKey" label="角色标识" min-width="160" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="170">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px">
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { menuApi, roleApi } from '../../../api/system'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const roles = ref([])
const menus = ref([])
const treeRef = ref()
const formRef = ref()
const form = reactive(defaultForm())
const menuTree = computed(() => menus.value)
const dialogTitle = computed(() => (form.id ? '编辑角色' : '新增角色'))

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
    const [roleList, menuList] = await Promise.all([roleApi.list(), menuApi.tree()])
    roles.value = roleList || []
    menus.value = menuList || []
  } finally {
    loading.value = false
  }
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

onMounted(loadData)
</script>
