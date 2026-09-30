import { defineStore } from 'pinia'
import { dictDataApi } from '../api/system'

export const useDictStore = defineStore('dict', {
  state: () => ({
    cache: {}
  }),
  actions: {
    async getDict(dictType) {
      if (this.cache[dictType]) {
        return this.cache[dictType]
      }
      const data = await dictDataApi.getByType(dictType)
      this.cache[dictType] = data
      return data
    },
    removeDict(dictType) {
      delete this.cache[dictType]
    },
    reset() {
      this.cache = {}
    }
  }
})
