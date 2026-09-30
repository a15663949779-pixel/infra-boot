import { reactive } from 'vue'
import { useDictStore } from '../stores/dict'

export function useDict(...types) {
  const dictStore = useDictStore()
  const res = reactive({})

  types.forEach((type) => {
    res[type] = []
    dictStore.getDict(type).then((data) => {
      res[type] = (data || []).map((item) => ({
        label: item.dictLabel,
        value: item.dictValue,
        elTagType: item.listClass || 'default',
        raw: item
      }))
    }).catch(() => {
      res[type] = []
    })
  })

  return res
}
