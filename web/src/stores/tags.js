import { defineStore } from 'pinia'

export const useTagsStore = defineStore('tags', {
  state: () => ({
    visitedViews: []
  }),
  actions: {
    addView(route) {
      if (!route.meta?.title) return
      const exists = this.visitedViews.find((v) => v.path === route.path)
      if (exists) {
        exists.title = route.meta.title
        return
      }
      this.visitedViews.push({
        path: route.path,
        fullPath: route.fullPath,
        title: route.meta.title
      })
    },
    removeView(path) {
      const index = this.visitedViews.findIndex((v) => v.path === path)
      if (index === -1) return null
      const removed = this.visitedViews.splice(index, 1)[0]
      return removed
    },
    removeOthers(path) {
      this.visitedViews = this.visitedViews.filter((v) => v.path === path)
    },
    removeAll() {
      this.visitedViews = []
    }
  }
})
