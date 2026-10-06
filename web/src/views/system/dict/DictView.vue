<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>System</span>
        <h1>字典管理</h1>
      </div>
      <div>
        <el-button type="danger" plain @click="handleClearCache" round>刷新缓存</el-button>
        <el-button type="primary" :icon="Plus" @click="openTypeDialog()" round>新增类型</el-button>
      </div>
    </div>

    <div class="dict-layout">
      <!-- Left: Dict Types -->
      <section class="content-section dict-type-panel">
        <div class="panel-header">
          <h3 class="panel-title">字典类型</h3>
        </div>
        <el-form :model="typeQuery" inline>
          <el-form-item>
            <el-input v-model.trim="typeQuery.dictName" placeholder="字典名称" clearable style="width: 140px" @keyup.enter="searchTypes" />
          </el-form-item>
          <el-form-item>
            <el-input v-model.trim="typeQuery.dictType" placeholder="字典类型" clearable style="width: 140px" @keyup.enter="searchTypes" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="searchTypes" round>搜索</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="typeLoading" :data="typeList" highlight-current-row @current-change="handleTypeSelect">
          <el-table-column prop="dictName" label="名称" min-width="100" />
          <el-table-column prop="dictType" label="类型编码" min-width="130" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="70">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
                {{ row.status === 1 ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openTypeDialog(row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button link type="danger" @click.stop="removeType(row)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="typeQuery.pageNum"
            v-model:page-size="typeQuery.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="typeTotal"
            layout="total, prev, pager"
            small
            @size-change="fetchTypes"
            @current-change="fetchTypes"
          />
        </div>
      </section>

      <!-- Right: Dict Data -->
      <section class="content-section dict-data-panel">
        <div class="panel-header">
          <h3 v-if="selectedType" class="panel-title">
            字典数据 — {{ selectedType.dictName }}
            <el-tag size="small" type="info" effect="plain" style="margin-left: 8px">{{ selectedType.dictType }}</el-tag>
          </h3>
          <h3 v-else class="panel-title placeholder">请选择左侧字典类型</h3>
          <el-button v-if="selectedType" type="primary" :icon="Plus" size="small" @click="openDataDialog()" round>新增数据</el-button>
        </div>

        <template v-if="selectedType">
          <el-form :model="dataQuery" inline>
            <el-form-item>
              <el-input v-model.trim="dataQuery.dictLabel" placeholder="字典标签" clearable style="width: 140px" @keyup.enter="searchData" />
            </el-form-item>
            <el-form-item>
              <el-select v-model="dataQuery.status" placeholder="状态" clearable style="width: 100px">
                <el-option label="正常" :value="1" />
                <el-option label="停用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="searchData" round>搜索</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="dataLoading" :data="dataList">
            <el-table-column prop="dictLabel" label="标签" min-width="100" />
            <el-table-column prop="dictValue" label="键值" width="100" />
            <el-table-column prop="dictSort" label="排序" width="70" />
            <el-table-column prop="listClass" label="样式" width="100">
              <template #default="{ row }">
                <el-tag :type="listClassToTag(row.listClass)" effect="plain" size="small">{{ row.listClass || 'default' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
                  {{ row.status === 1 ? '正常' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDataDialog(row)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button link type="danger" @click="removeData(row)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrapper">
            <el-pagination
              v-model:current-page="dataQuery.pageNum"
              v-model:page-size="dataQuery.pageSize"
              :page-sizes="[10, 20, 50]"
              :total="dataTotal"
              layout="total, sizes, prev, pager, next"
              small
              @size-change="fetchData"
              @current-change="fetchData"
            />
          </div>
        </template>
        <el-empty v-else description="请在左侧选择一个字典类型" />
      </section>
    </div>

    <!-- Dict Type Dialog -->
    <el-dialog v-model="typeDialogVisible" :title="typeForm.dictId ? '编辑字典类型' : '新增字典类型'" width="500px" destroy-on-close>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="96px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model.trim="typeForm.dictName" placeholder="如：用户性别" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model.trim="typeForm.dictType" placeholder="如：sys_user_sex" :disabled="!!typeForm.dictId" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="typeForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="typeSaving" @click="submitType" round>保存</el-button>
      </template>
    </el-dialog>

    <!-- Dict Data Dialog -->
    <el-dialog v-model="dataDialogVisible" :title="dataForm.dictCode ? '编辑字典数据' : '新增字典数据'" width="550px" destroy-on-close>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="96px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model.trim="dataForm.dictLabel" placeholder="如：男" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model.trim="dataForm.dictValue" placeholder="如：0" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="回显样式">
          <el-select v-model="dataForm.listClass" placeholder="选择样式" style="width: 200px">
            <el-option label="default" value="default" />
            <el-option label="primary" value="primary" />
            <el-option label="success" value="success" />
            <el-option label="info" value="info" />
            <el-option label="warning" value="warning" />
            <el-option label="danger" value="danger" />
          </el-select>
          <el-tag v-if="dataForm.listClass" :type="listClassToTag(dataForm.listClass)" effect="plain" size="small" style="margin-left: 12px">预览</el-tag>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dataForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="dataSaving" @click="submitData" round>保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { dictTypeApi, dictDataApi } from '../../../api/system'
import { useDictStore } from '../../../stores/dict'

const dictStore = useDictStore()

const typeLoading = ref(false)
const typeList = ref([])
const typeTotal = ref(0)
const typeQuery = reactive({ dictName: '', dictType: '', status: 1, pageNum: 1, pageSize: 20 })
const typeDialogVisible = ref(false)
const typeSaving = ref(false)
const typeFormRef = ref()
const typeForm = reactive(defaultTypeForm())

const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const selectedType = ref(null)
const dataLoading = ref(false)
const dataList = ref([])
const dataTotal = ref(0)
const dataQuery = reactive({ dictType: '', dictLabel: '', status: undefined, pageNum: 1, pageSize: 20 })
const dataDialogVisible = ref(false)
const dataSaving = ref(false)
const dataFormRef = ref()
const dataForm = reactive(defaultDataForm())

const dataRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }]
}

function defaultTypeForm() {
  return { dictId: null, dictName: '', dictType: '', status: 1, remark: '' }
}

function defaultDataForm() {
  return { dictCode: null, dictSort: 0, dictLabel: '', dictValue: '', dictType: '', listClass: 'default', status: 1, remark: '' }
}

function listClassToTag(cls) {
  const map = { default: 'info', primary: '', success: 'success', info: 'info', warning: 'warning', danger: 'danger' }
  return map[cls] ?? 'info'
}

onMounted(fetchTypes)

async function fetchTypes() {
  typeLoading.value = true
  try {
    const res = await dictTypeApi.list(typeQuery)
    typeList.value = res.records
    typeTotal.value = Number(res.total)
  } finally {
    typeLoading.value = false
  }
}

function searchTypes() {
  typeQuery.pageNum = 1
  fetchTypes()
}

function handleTypeSelect(row) {
  if (!row) return
  selectedType.value = row
  dataQuery.dictType = row.dictType
  dataQuery.pageNum = 1
  fetchData()
}

async function fetchData() {
  if (!dataQuery.dictType) return
  dataLoading.value = true
  try {
    const res = await dictDataApi.list(dataQuery)
    dataList.value = res.records
    dataTotal.value = Number(res.total)
  } finally {
    dataLoading.value = false
  }
}

function searchData() {
  dataQuery.pageNum = 1
  fetchData()
}

async function openTypeDialog(row) {
  if (row) {
    const detail = await dictTypeApi.detail(row.dictId)
    Object.assign(typeForm, defaultTypeForm(), detail)
  } else {
    Object.assign(typeForm, defaultTypeForm())
  }
  typeDialogVisible.value = true
}

async function submitType() {
  await typeFormRef.value.validate()
  typeSaving.value = true
  try {
    if (typeForm.dictId) {
      await dictTypeApi.update(typeForm)
    } else {
      await dictTypeApi.create(typeForm)
    }
    ElMessage.success('保存成功')
    typeDialogVisible.value = false
    await fetchTypes()
  } finally {
    typeSaving.value = false
  }
}

async function removeType(row) {
  await ElMessageBox.confirm(`确认删除字典类型「${row.dictName}」？`, '提示', { type: 'warning' })
  await dictTypeApi.remove(row.dictId)
  ElMessage.success('删除成功')
  if (selectedType.value?.dictId === row.dictId) {
    selectedType.value = null
    dataList.value = []
  }
  await fetchTypes()
}

function openDataDialog(row) {
  if (row) {
    Object.assign(dataForm, defaultDataForm(), row)
  } else {
    Object.assign(dataForm, defaultDataForm())
    dataForm.dictType = selectedType.value.dictType
  }
  dataDialogVisible.value = true
}

async function submitData() {
  await dataFormRef.value.validate()
  dataSaving.value = true
  try {
    if (dataForm.dictCode) {
      await dictDataApi.update(dataForm)
    } else {
      await dictDataApi.create(dataForm)
    }
    ElMessage.success('保存成功')
    dataDialogVisible.value = false
    dictStore.removeDict(dataForm.dictType)
    await fetchData()
  } finally {
    dataSaving.value = false
  }
}

async function removeData(row) {
  await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」？`, '提示', { type: 'warning' })
  await dictDataApi.remove(row.dictCode)
  ElMessage.success('删除成功')
  dictStore.removeDict(row.dictType)
  await fetchData()
}

async function handleClearCache() {
  await dictTypeApi.clearCache()
  dictStore.reset()
  ElMessage.success('缓存已刷新')
}
</script>

<style scoped>
.dict-layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.dict-type-panel {
  flex: 0 0 420px;
  min-width: 0;
}
.dict-data-panel {
  flex: 1;
  min-width: 0;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.panel-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
}
.panel-title.placeholder {
  color: var(--el-text-color-placeholder);
  font-weight: 400;
  font-size: 14px;
}
</style>
