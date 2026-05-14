<template>
  <el-container class="app-wrapper" :class="classObj">
    <el-aside
      v-if="!sidebar.hide"
      class="sidebar-container"
      :width="sidebar.opened ? sidebarWidth : collapsedWidth"
    >
      <Sidebar />
    </el-aside>

    <el-container class="main-container" :class="{ 'has-sidebar': !sidebar.hide }">
      <el-header class="fixed-header" :height="navbarHeight">
        <Navbar />
      </el-header>
      <el-main class="app-main">
        <AppMain />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/store/modules/app'
import Sidebar from './components/Sidebar.vue'
import Navbar from './components/Navbar.vue'
import AppMain from './components/AppMain.vue'

const appStore = useAppStore()

const sidebarWidth = '210px'
const collapsedWidth = '64px'
const navbarHeight = '50px'

const sidebar = computed(() => appStore.sidebar)

const classObj = computed(() => ({
  'sidebar-opened': sidebar.value.opened,
  'sidebar-collapsed': !sidebar.value.opened,
  'mobile': appStore.device === 'mobile',
  'without-animation': sidebar.value.withoutAnimation,
}))
</script>

<style lang="scss" scoped>
.app-wrapper {
  position: relative;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  transition: width 0.28s;
  background-color: #304156;
  overflow: hidden;
  flex-shrink: 0;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.fixed-header {
  padding: 0;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 9;
}

.app-main {
  padding: 16px;
  overflow-y: auto;
  background-color: #f0f2f5;
  flex: 1;
}

.without-animation .sidebar-container {
  transition: none;
}
</style>
