<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>System</span>
        <h1>菜单管理</h1>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增菜单</el-button>
    </div>

    <section class="content-section">
      <el-table v-loading="loading" :data="menuTree" row-key="id" border default-expand-all>
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column prop="path" label="路由路径" min-width="150" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="perms" label="权限标识" min-width="210" show-overflow-tooltip />
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="menuTypeTag(row.menuType)" effect="plain">{{ menuTypeLabel(row.menuType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visible" label="显示" width="90">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" effect="plain">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="操作" fixed="right" width="170">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="parentOptions"
            node-key="id"
            check-strictly
            default-expand-all
            :props="{ label: 'menuName', children: 'children' }"
          />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType">
            <el-radio-button :label="0">目录</el-radio-button>
            <el-radio-button :label="1">菜单</el-radio-button>
            <el-radio-button :label="2">按钮</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model.trim="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径">
          <el-input v-model.trim="form.path" placeholder="如 /system 或 user" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model.trim="form.component" placeholder="如 system/user/index" />
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model.trim="form.perms" placeholder="如 system:user:list" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model.trim="form.icon" placeholder="如 setting、user" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="显示">
          <el-switch v-model="form.visible" :active-value="1" :inactive-value="0" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { menuApi } from '../../../api/system'
import { buildTreeFromFlat } from '../../../utils/menu'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const menus = ref([])
const formRef = ref()
const form = reactive(defaultForm())
const menuTree = computed(() => buildTreeFromFlat(menus.value))
const dialogTitle = computed(() => (form.id ? '编辑菜单' : '新增菜单'))
const parentOptions = computed(() => [{ id: 0, menuName: '根目录', children: menuTree.value }])

const rules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
}

function defaultForm() {
  return {
    id: null,
    parentId: 0,
    menuName: '',
    path: '',
    component: '',
    perms: '',
    icon: '',
    menuType: 0,
    visible: 1,
    sort: 0,
    status: 1
  }
}

function assignForm(data) {
  Object.assign(form, defaultForm(), data)
}

async function loadData() {
  loading.value = true
  try {
    menus.value = await menuApi.list()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  assignForm(defaultForm())
  dialogVisible.value = true
}

async function openEdit(row) {
  const detail = await menuApi.detail(row.id)
  assignForm(detail)
  dialogVisible.value = true
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form }
    if (payload.id) {
      await menuApi.update(payload)
    } else {
      await menuApi.create(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？`, '删除确认', { type: 'warning' })
  await menuApi.remove(row.id)
  ElMessage.success('删除成功')
  await loadData()
}

function menuTypeLabel(type) {
  return ['目录', '菜单', '按钮'][type] || '未知'
}

function menuTypeTag(type) {
  return type === 0 ? 'primary' : type === 1 ? 'success' : 'warning'
}

onMounted(loadData)
</script>
