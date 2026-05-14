import { defineStore } from 'pinia'

type DeviceType = 'desktop' | 'mobile'

interface AppState {
  sidebar: {
    opened: boolean
    withoutAnimation: boolean
  }
  device: DeviceType
}

export const useAppStore = defineStore('app', {
  state: (): AppState => ({
    sidebar: {
      opened: localStorage.getItem('sidebarStatus') !== '0',
      withoutAnimation: false,
    },
    device: 'desktop',
  }),

  actions: {
    toggleSidebar() {
      this.sidebar.opened = !this.sidebar.opened
      this.sidebar.withoutAnimation = false
      localStorage.setItem('sidebarStatus', this.sidebar.opened ? '1' : '0')
    },

    closeSidebar(withoutAnimation: boolean) {
      this.sidebar.opened = false
      this.sidebar.withoutAnimation = withoutAnimation
      localStorage.setItem('sidebarStatus', '0')
    },

    toggleDevice(device: DeviceType) {
      this.device = device
    },
  },
})
