<template>
  <div class="navbar">
    <Hamburger
      :is-active="appStore.sidebar.opened"
      class="hamburger-container"
      @toggle="toggleSidebar"
    />
    <Breadcrumb class="breadcrumb-container" />

    <div class="right-menu">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="avatar-wrapper">
          <el-avatar :size="30" :src="userStore.avatar || defaultAvatar">
            {{ userStore.name?.charAt(0) }}
          </el-avatar>
          <span class="user-name">{{ userStore.name }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import Hamburger from './Hamburger.vue'
import Breadcrumb from './Breadcrumb.vue'

const appStore = useAppStore()
const userStore = useUserStore()
const router = useRouter()

const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIzMiIgaGVpZ2h0PSIzMiIgdmlld0JveD0iMCAwIDMyIDMyIj48Y2lyY2xlIGN4PSIxNiIgY3k9IjE2IiByPSIxNiIgZmlsbD0iIzQwOWVmZiIvPjwvc3ZnPg=='

function toggleSidebar() {
  appStore.toggleSidebar()
}

async function handleCommand(command: string) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await userStore.logout()
      router.push('/login')
    } catch {
      // cancelled
    }
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  display: flex;
  align-items: center;
  background: #fff;
  padding: 0;
  position: relative;
}

.hamburger-container {
  padding: 0 15px;
  cursor: pointer;
  line-height: 50px;
  height: 100%;
  display: flex;
  align-items: center;

  &:hover {
    background: rgba(0, 0, 0, 0.025);
  }
}

.breadcrumb-container {
  flex: 1;
}

.right-menu {
  display: flex;
  align-items: center;
  padding-right: 16px;

  .avatar-wrapper {
    display: flex;
    align-items: center;
    cursor: pointer;
    gap: 8px;

    .user-name {
      font-size: 14px;
      color: #333;
    }
  }
}
</style>
