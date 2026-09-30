<template>
  <el-tag v-if="matched" :type="tagType(matched.elTagType)">{{ matched.label }}</el-tag>
  <span v-else>{{ value }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  options: { type: Array, default: () => [] },
  value: { type: [String, Number], required: true }
})

const tagTypeMap = {
  default: undefined,
  primary: undefined,
  success: 'success',
  info: 'info',
  warning: 'warning',
  danger: 'danger'
}

function tagType(listClass) {
  return tagTypeMap[listClass] ?? undefined
}

const matched = computed(() =>
  props.options.find((item) => String(item.value) === String(props.value))
)
</script>
