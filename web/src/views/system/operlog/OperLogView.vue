<template>
  <div class="page">
    <div class="page-title">
      <div>
        <span>System</span>
        <h1>操作日志</h1>
      </div>
      <div>
        <el-button type="danger" :icon="Delete" :disabled="!selectedIds.length" @click="handleBatchDelete">删除选中</el-button>
        <el-button type="danger" plain :icon="Delete" @click="handleClean">清空日志</el-button>
      </div>
    </div>

    <section class="content-section">
      <el-form :model="queryParams" inline>
        <el-form-item label="模块">
          <el-input v-model.trim="queryParams.title" placeholder="操作模块" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model.trim="queryParams.operName" placeholder="操作人员" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryParams.businessType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in dicts.sys_oper_business_type" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in dicts.sys_success_fail" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="logList" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="操作模块" min-width="120" show-overflow-tooltip />
        <el-table-column prop="businessType" label="业务类型" width="100">
          <template #default="{ row }">
            <dict-tag :options="dicts.sys_oper_business_type" :value="row.businessType" />
          </template>
        </el-table-column>
        <el-table-column prop="operName" label="操作人" width="120" />
        <el-table-column prop="operIp" label="IP" width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <dict-tag :options="dicts.sys_success_fail" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" />
        <el-table-column prop="operTime" label="操作时间" width="170" />
        <el-table-column label="操作" fixed="right" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作模块">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">
          <dict-tag :options="dicts.sys_oper_business_type" :value="detail.businessType" />
        </el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detail.operIp }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ detail.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :options="dicts.sys_success_fail" :value="detail.status" />
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="操作时间" :span="2">{{ detail.operTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">请求参数</el-divider>
      <div class="detail-text-block">{{ detail.operParam || '无' }}</div>
      <el-divider content-position="left">返回结果</el-divider>
      <div class="detail-text-block">{{ detail.jsonResult || '无' }}</div>
      <template v-if="detail.errorMsg">
        <el-divider content-position="left">错误信息</el-divider>
        <div class="detail-text-block error">{{ detail.errorMsg }}</div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Delete } from '@element-plus/icons-vue'
import { operLogApi } from '../../../api/system'
import { useDict } from '../../../hooks/useDict'
import DictTag from '../../../components/DictTag/index.vue'

const dicts = useDict('sys_oper_business_type', 'sys_success_fail')

const loading = ref(false)
const logList = ref([])
const total = ref(0)
const selectedIds = ref([])
const dateRange = ref([])
const detailVisible = ref(false)
const detail = ref({})

const queryParams = reactive({
  title: '',
  operName: '',
  businessType: '',
  status: undefined,
  beginTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 20
})

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    } else {
      params.beginTime = ''
      params.endTime = ''
    }
    const res = await operLogApi.list(params)
    logList.value = res.records
    total.value = Number(res.total)
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  fetchList()
}

function resetQuery() {
  queryParams.title = ''
  queryParams.operName = ''
  queryParams.businessType = ''
  queryParams.status = undefined
  dateRange.value = []
  handleQuery()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleBatchDelete() {
  if (!selectedIds.value.length) return
  await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条日志？`, '提示', { type: 'warning' })
  await operLogApi.remove(selectedIds.value.join(','))
  ElMessage.success('删除成功')
  fetchList()
}

async function handleClean() {
  await ElMessageBox.confirm('确认清空所有操作日志？此操作不可恢复！', '警告', { type: 'warning' })
  await operLogApi.clean()
  ElMessage.success('清空成功')
  fetchList()
}

function handleDetail(row) {
  detail.value = row
  detailVisible.value = true
}
</script>

<style scoped>
.detail-text-block {
  background: var(--el-fill-color-light);
  border-radius: 4px;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}
.detail-text-block.error {
  color: var(--el-color-danger);
}
</style>
