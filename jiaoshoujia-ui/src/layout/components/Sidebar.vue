<template>
  <div class="sidebar-wrap">
    <div class="logo-container" :class="{ collapse: !appStore.sidebar.opened }">
      <span v-if="appStore.sidebar.opened" class="logo-title">脚手架管理系统</span>
      <span v-else class="logo-title-mini">管</span>
    </div>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="!appStore.sidebar.opened"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        unique-opened
        mode="vertical"
      >
        <SidebarItem
          v-for="route in permissionRoutes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import { usePermissionStore } from '@/store/modules/permission'
import { constantRoutes } from '@/router'
import SidebarItem from './SidebarItem.vue'

const route = useRoute()
const appStore = useAppStore()
const permissionStore = usePermissionStore()

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta?.activeMenu) return meta.activeMenu as string
  return path
})

const permissionRoutes = computed(() => [
  ...constantRoutes,
  ...permissionStore.routes,
])
</script>

<style lang="scss" scoped>
.sidebar-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo-container {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b2f3a;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  white-space: nowrap;

  &.collapse .logo-title-mini {
    display: block;
  }
}

.logo-title {
  font-size: 14px;
  letter-spacing: 2px;
}

.logo-title-mini {
  font-size: 18px;
  font-weight: bold;
}
</style>
