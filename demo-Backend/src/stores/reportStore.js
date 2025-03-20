import { defineStore } from 'pinia'

export const useReportStore = defineStore('report', {
  state: () => ({
    availableFields: ['sales', 'inventory', 'customer'],
    selectedFields: [],
    layoutType: 'grid',
    reportData: []
  }),
  actions: {
    addField(field) {
      if (!this.selectedFields.includes(field)) {
        this.selectedFields.push(field)
      }
    },
    removeField(index) {
      this.selectedFields.splice(index, 1)
    },
    updateLayout(newLayout) {
      this.layoutType = newLayout
    }
  },
  getters: {
    formattedData: (state) => {
      return state.reportData.map(item => ({
        ...item,
        date: new Date(item.date).toLocaleDateString()
      }))
    }
  }
})
