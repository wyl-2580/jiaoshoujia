<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="welcome-row">
      <el-col :span="24">
        <el-card class="welcome-card" shadow="hover">
          <div class="welcome-content">
            <div>
              <h2 class="welcome-title">欢迎回来, {{ username }}</h2>
              <p class="welcome-desc">{{ greeting }}，祝您工作顺利！</p>
            </div>
            <div class="welcome-date">
              <p class="date-text">{{ currentDate }}</p>
              <p class="week-text">{{ currentWeek }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :lg="6" v-for="item in statCards" :key="item.title">
        <el-card class="stat-card" shadow="hover" :style="{ borderLeft: `4px solid ${item.color}` }">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-value">{{ item.value }}</p>
              <p class="stat-title">{{ item.title }}</p>
            </div>
            <div class="stat-icon" :style="{ backgroundColor: item.color + '15', color: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :lg="16">
        <el-card class="section-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>近期操作</span>
            </div>
          </template>
          <el-table :data="recentOperations" style="width: 100%">
            <el-table-column prop="module" label="操作模块" width="140" />
            <el-table-column prop="action" label="操作类型" width="120">
              <template #default="{ row }">
                <el-tag :type="row.tagType" size="small">{{ row.action }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operator" label="操作人" width="120" />
            <el-table-column prop="time" label="操作时间" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="section-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div class="shortcut-grid">
            <div
              v-for="item in shortcuts"
              :key="item.title"
              class="shortcut-item"
              @click="router.push(item.path)"
            >
              <el-icon :size="24" :style="{ color: item.color }">
                <component :is="item.icon" />
              </el-icon>
              <span>{{ item.title }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { UserFilled, Avatar, Menu, OfficeBuilding, Setting, Document, Timer, Key } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const username = computed(() => userStore.name || '管理员')

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
})

const currentWeek = computed(() => {
  const weeks = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weeks[new Date().getDay()]
})

const statCards = [
  { title: '用户数量', value: 256, icon: UserFilled, color: '#409EFF' },
  { title: '角色数量', value: 12, icon: Key, color: '#67C23A' },
  { title: '菜单数量', value: 48, icon: Menu, color: '#E6A23C' },
  { title: '部门数量', value: 8, icon: OfficeBuilding, color: '#F56C6C' }
]

const recentOperations = [
  { module: '用户管理', action: '新增', tagType: 'success', operator: 'admin', time: '2025-01-15 10:30:00', status: '成功' },
  { module: '角色管理', action: '修改', tagType: 'warning', operator: 'admin', time: '2025-01-15 09:20:00', status: '成功' },
  { module: '菜单管理', action: '删除', tagType: 'danger', operator: 'admin', time: '2025-01-14 16:45:00', status: '成功' },
  { module: '部门管理', action: '新增', tagType: 'success', operator: 'admin', time: '2025-01-14 14:10:00', status: '成功' },
  { module: '字典管理', action: '修改', tagType: 'warning', operator: 'admin', time: '2025-01-14 11:00:00', status: '失败' }
]

const shortcuts = [
  { title: '用户管理', icon: UserFilled, color: '#409EFF', path: '/system/user' },
  { title: '角色管理', icon: Key, color: '#67C23A', path: '/system/role' },
  { title: '菜单管理', icon: Menu, color: '#E6A23C', path: '/system/menu' },
  { title: '部门管理', icon: OfficeBuilding, color: '#F56C6C', path: '/system/dept' },
  { title: '字典管理', icon: Document, color: '#909399', path: '/system/dict' },
  { title: '系统设置', icon: Setting, color: '#9B59B6', path: '/system/config' },
  { title: '定时任务', icon: Timer, color: '#1ABC9C', path: '/monitor/job' },
  { title: '操作日志', icon: Avatar, color: '#E74C3C', path: '/monitor/operlog' }
]
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
}

.welcome-row {
  margin-bottom: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;

  :deep(.el-card__body) {
    padding: 30px;
  }
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  opacity: 0.85;
}

.welcome-date {
  text-align: right;
}

.date-text {
  margin: 0 0 4px;
  font-size: 14px;
  opacity: 0.9;
}

.week-text {
  margin: 0;
  font-size: 13px;
  opacity: 0.75;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 10px;
  margin-bottom: 10px;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 4px;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-card {
  border-radius: 10px;
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.shortcut-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #f5f7fa;
  font-size: 13px;
  color: #606266;

  &:hover {
    background: #ecf5ff;
    transform: translateY(-2px);
  }
}
</style>
