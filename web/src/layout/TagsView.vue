<template>
  <div class="tags-bar">
    <div ref="scrollRef" class="tags-scroll" role="tablist" aria-label="已打开的页面" @wheel.prevent="onWheel">
      <div
        v-for="tag in tagsStore.visitedViews"
        :key="tag.path"
        class="tag-item"
        :class="{ 'is-active': tag.path === route.path }"
        role="tab"
        :aria-selected="tag.path === route.path"
        tabindex="0"
        @click="goTo(tag)"
        @keydown.enter.prevent="goTo(tag)"
        @keydown.space.prevent="goTo(tag)"
        @contextmenu.prevent="openContext($event, tag)"
      >
        <span class="tag-title">{{ tag.title }}</span>
        <button
          v-if="tagsStore.visitedViews.length > 1"
          class="tag-close"
          :aria-label="`关闭${tag.title}`"
          @click.stop="closeTag(tag)"
          @keydown.stop
        >&times;</button>
      </div>
    </div>
    <div class="tags-search">
      <el-input
        v-model="searchKey"
        placeholder="搜索菜单…"
        clearable
        size="small"
        class="menu-search-input"
        @input="onSearch"
        @focus="onSearch"
        @blur="searchResults = []"
        @keydown.esc="clearSearch"
        @keydown.enter.prevent="searchResults[0] && selectMenu(searchResults[0])"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div v-if="searchResults.length" class="search-dropdown">
        <div
          v-for="item in searchResults"
          :key="item.fullPath"
          class="search-option"
          :class="{ 'is-active': item.fullPath === route.path }"
          @mousedown.prevent="selectMenu(item)"
        >
          <span class="search-option-title">{{ item.menuName }}</span>
          <span class="search-option-path">{{ item.fullPath }}</span>
        </div>
      </div>
    </div>
    <div v-if="contextVisible" ref="contextRef" class="tags-context" :style="contextStyle" @click="contextVisible = false">
      <button @click="closeOthers">关闭其他</button>
      <button @click="closeAll">关闭全部</button>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useTagsStore } from '../stores/tags'

const route = useRoute()
const router = useRouter()
const tagsStore = useTagsStore()
const scrollRef = ref()
const contextRef = ref()
const contextVisible = ref(false)
const contextStyle = ref({})
const contextTag = ref(null)
const searchKey = ref('')
const searchResults = ref([])
let resizeObserver

function dismissContext(event) {
  if (contextRef.value && !contextRef.value.contains(event.target)) contextVisible.value = false
}

onMounted(() => {
  resizeObserver = new ResizeObserver(scrollToActive)
  if (scrollRef.value) resizeObserver.observe(scrollRef.value)
  document.addEventListener('mousedown', dismissContext)
})
onUnmounted(() => {
  resizeObserver?.disconnect()
  document.removeEventListener('mousedown', dismissContext)
})

const searchableRoutes = computed(() =>
  router.getRoutes()
    .filter((r) => r.meta?.title && r.path !== '/' && !r.path.includes(':'))
    .map((r) => ({ menuName: r.meta.title, fullPath: r.path }))
)

watch(
  () => route.path,
  () => {
    tagsStore.addView(route)
    scrollToActive()
  },
  { immediate: true }
)

function goTo(tag) {
  if (tag.path !== route.path) {
    router.push(tag.fullPath || tag.path)
  }
}

function closeTag(tag) {
  const removed = tagsStore.removeView(tag.path)
  if (!removed) return
  if (tag.path === route.path) {
    const views = tagsStore.visitedViews
    const next = views[views.length - 1]
    if (next) {
      router.push(next.fullPath || next.path)
    }
  }
}

function closeOthers() {
  if (contextTag.value) {
    tagsStore.removeOthers(contextTag.value.path)
    if (contextTag.value.path !== route.path) {
      router.push(contextTag.value.fullPath || contextTag.value.path)
    }
  }
  contextVisible.value = false
}

function closeAll() {
  tagsStore.removeAll()
  contextVisible.value = false
}

function openContext(e, tag) {
  contextTag.value = tag
  contextStyle.value = { left: e.clientX + 'px', top: e.clientY + 'px' }
  contextVisible.value = true
}

function onWheel(e) {
  scrollRef.value.scrollLeft += e.deltaY
}

function scrollToActive() {
  nextTick(() => {
    const el = scrollRef.value
    if (!el) return
    const active = el.querySelector('.tag-item.is-active')
    if (active) {
      active.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' })
    }
  })
}

function onSearch() {
  const key = searchKey.value.trim().toLowerCase()
  if (!key) {
    searchResults.value = []
    return
  }
  searchResults.value = searchableRoutes.value.filter((r) =>
    r.menuName.toLowerCase().includes(key)
  )
}

function selectMenu(item) {
  searchKey.value = ''
  searchResults.value = []
  tagsStore.addView({ path: item.fullPath, fullPath: item.fullPath, meta: { title: item.menuName } })
  router.push(item.fullPath)
}

function clearSearch() {
  searchKey.value = ''
  searchResults.value = []
}
</script>
