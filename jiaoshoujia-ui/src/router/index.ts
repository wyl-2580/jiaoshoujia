import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'
import { getRouters } from '@/api/system/auth'
import Layout from '@/layout/index.vue'

NProgress.configure({ showSpinner: false })

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/dashboard/index.vue'),
        name: 'Index',
        meta: { title: '首页', icon: 'HomeFilled', affix: true },
      },
    ],
  },
  {
    path: '/user',
    component: Layout,
    meta: { hidden: true },
    children: [
      {
        path: 'profile',
        component: () => import('@/views/user/profile/index.vue'),
        name: 'Profile',
        meta: { title: '个人中心' },
      },
    ],
  },
  {
    path: '/redirect',
    component: Layout,
    meta: { hidden: true },
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue'),
      },
    ],
  },
  {
    path: '/system/dict-data',
    component: Layout,
    meta: { hidden: true },
    children: [
      {
        path: ':dictType',
        component: () => import('@/views/system/dict/data.vue'),
        name: 'DictData',
        meta: { title: '字典数据', activeMenu: '/system/dict' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/error/404.vue'),
    meta: { hidden: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 }),
})

const whiteList = ['/login']

router.beforeEach(async (to, _from, next) => {
  NProgress.start()

  const token = getToken()

  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
      return
    }

    const userStore = useUserStore()
    if (userStore.roles.length === 0) {
      try {
        await userStore.getInfo()
        const res = await getRouters()
        const permissionStore = usePermissionStore()
        const accessRoutes = permissionStore.generateRoutes(res.data)
        accessRoutes.forEach((route) => {
          router.addRoute(route)
        })
        next({ ...to, replace: true })
      } catch (error) {
        userStore.resetState()
        next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
        NProgress.done()
      }
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
