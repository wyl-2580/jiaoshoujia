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

function routeName(menu: RouteMenu) {
  return menu.name || menu.path.replace(/^\//, '').replace(/[-/](\w)/g, (_, c) => c.toUpperCase())
}

function normalizePath(path: string, isChild: boolean) {
  const cleanPath = path.replace(/^\/+/, '')
  return isChild ? cleanPath : `/${cleanPath}`
}

function filterAsyncRoutes(menus: RouteMenu[], isChild = false): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []

  menus.forEach((menu) => {
    if (menu.menuType === 'F') return

    const route: Partial<RouteRecordRaw> & { path: string; meta: Record<string, any> } = {
      path: normalizePath(menu.path, isChild),
      name: routeName(menu),
      meta: {
        title: menu.meta?.title || menu.menuName,
        icon: menu.meta?.icon || menu.icon,
        noCache: menu.meta?.noCache ?? false,
        link: menu.meta?.link,
        hidden: menu.hidden || menu.visible === 1,
      },
    }

    if (menu.component === 'Layout' || (!menu.component && menu.children?.length)) {
      route.component = Layout
    } else if (menu.component === 'ParentView') {
      route.component = () => import('@/layout/components/AppMain.vue')
    } else if (menu.component) {
      route.component = loadView(menu.component)
    }

    if (menu.children && menu.children.length > 0) {
      const firstChild = menu.children.find((child) => child.visible !== 1 && child.menuType !== 'F')
      if (firstChild) {
        route.redirect = `${route.path}/${normalizePath(firstChild.path, true)}`
      }
      route.children = filterAsyncRoutes(menu.children, true)
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
