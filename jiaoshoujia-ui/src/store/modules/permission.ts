import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import type { RouteMenu } from '@/api/system/auth'
import Layout from '@/layout/index.vue'

interface PermissionState {
  routes: RouteRecordRaw[]
  addRoutes: RouteRecordRaw[]
}

const modules = import.meta.glob('@/views/**/*.vue')

function loadView(component: string) {
  const key = `/src/views/${component}.vue`
  if (modules[key]) {
    return modules[key]
  }
  const keyWithIndex = `/src/views/${component}/index.vue`
  if (modules[keyWithIndex]) {
    return modules[keyWithIndex]
  }
  return modules['/src/views/error/404.vue']
}

function filterAsyncRoutes(menus: RouteMenu[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []

  menus.forEach((menu) => {
    if (menu.hidden) return

    const route: Partial<RouteRecordRaw> & { path: string; meta: Record<string, any> } = {
      path: menu.path,
      name: menu.name,
      meta: {
        title: menu.meta?.title,
        icon: menu.meta?.icon,
        noCache: menu.meta?.noCache ?? false,
        link: menu.meta?.link,
      },
    }

    if (menu.component === 'Layout') {
      route.component = Layout
    } else if (menu.component === 'ParentView') {
      route.component = () => import('@/layout/components/AppMain.vue')
    } else if (menu.component) {
      route.component = loadView(menu.component)
    }

    if (menu.children && menu.children.length > 0) {
      route.redirect = 'noRedirect'
      route.children = filterAsyncRoutes(menu.children)
    }

    routes.push(route as RouteRecordRaw)
  })

  return routes
}

export const usePermissionStore = defineStore('permission', {
  state: (): PermissionState => ({
    routes: [],
    addRoutes: [],
  }),

  actions: {
    generateRoutes(menus: RouteMenu[]) {
      const accessedRoutes = filterAsyncRoutes(menus)
      this.addRoutes = accessedRoutes
      this.routes = accessedRoutes
      return accessedRoutes
    },

    setRoutes(routes: RouteRecordRaw[]) {
      this.addRoutes = routes
      this.routes = routes
    },
  },
})
