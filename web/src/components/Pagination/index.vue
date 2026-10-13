<template>
  <div :class="{ hidden: hidden }" class="pagination-wrapper">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="currentPageSize"
      :background="background"
      :layout="layout"
      :page-sizes="pageSizes"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  total: { required: true, type: Number },
  pageNum: { type: Number, default: 1 },
  pageSize: { type: Number, default: 20 },
  pageSizes: { type: Array, default: () => [10, 20, 50, 100] },
  layout: { type: String, default: 'total, sizes, prev, pager, next, jumper' },
  background: { type: Boolean, default: true },
  hidden: { type: Boolean, default: false }
})

const emit = defineEmits(['update:pageNum', 'update:pageSize', 'pagination'])

const currentPage = computed({
  get: () => props.pageNum,
  set: (val) => emit('update:pageNum', val)
})

const currentPageSize = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val)
})

function handleSizeChange(val) {
  emit('pagination', { pageNum: currentPage.value, pageSize: val })
}

function handleCurrentChange(val) {
  emit('pagination', { pageNum: val, pageSize: currentPageSize.value })
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}

.pagination-wrapper.hidden {
  display: none;
}
</style>
