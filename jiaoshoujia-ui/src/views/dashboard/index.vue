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
      <el-col :xs="24" :sm="12" :lg="6" v-for="item in visibleStatCards" :key="item.title">
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
      <el-col :xs="24" :lg="16" v-if="showOperlog">
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
import { computed, ref, onMounted } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { usePermissionStore } from '@/store/modules/permission'
import { UserFilled, Avatar, Menu, OfficeBuilding, Document, Timer, Key } from '@element-plus/icons-vue'
import { listOperlog } from '@/api/system/operlog'
import { get } from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

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

const statCards = ref([
  { title: '用户数量', value: 0, icon: UserFilled, color: '#409EFF' },
  { title: '角色数量', value: 0, icon: Key, color: '#67C23A' },
  { title: '菜单数量', value: 0, icon: Menu, color: '#E6A23C' },
  { title: '部门数量', value: 0, icon: OfficeBuilding, color: '#F56C6C' },
])

const recentOperations = ref<any[]>([])

const businessTypeMap: Record<number, { label: string; tagType: string }> = {
  1: { label: '新增', tagType: 'success' },
  2: { label: '修改', tagType: 'warning' },
  3: { label: '删除', tagType: 'danger' },
  4: { label: '授权', tagType: '' },
  5: { label: '导出', tagType: 'info' },
  6: { label: '导入', tagType: 'info' },
  7: { label: '强退', tagType: 'danger' },
  8: { label: '清空', tagType: 'danger' },
}

function formatTime(time: string) {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 19)
}

function hasPerms(perm: string) {
  const perms = userStore.permissions
  return perms.includes('*:*:*') || perms.includes(perm)
}

const showOperlog = computed(() => hasPerms('monitor:operlog:list'))

const statPermMap = ['system:user:list', 'system:role:list', 'system:menu:list', 'system:dept:list']
const visibleStatCards = computed(() =>
  statCards.value.filter((_, i) => hasPerms(statPermMap[i])),
)

async function loadRecentOperations() {
  if (!hasPerms('monitor:operlog:list')) return
  try {
    const res = await listOperlog({ pageNum: 1, pageSize: 8 })
    recentOperations.value = (res.rows || []).map((item: any) => {
      const bt = businessTypeMap[item.businessType] || { label: '其他', tagType: 'info' }
      return {
        module: item.title,
        action: bt.label,
        tagType: bt.tagType,
        operator: item.operName,
        time: formatTime(item.operTime),
        status: item.status === 0 ? '成功' : '失败',
      }
    })
  } catch {
    recentOperations.value = []
  }
}

async function loadStats() {
  const requests: Promise<any>[] = []
  const indices: number[] = []

  if (hasPerms('system:user:list')) {
    indices.push(0)
    requests.push(get('/api/system/user/list', { pageNum: 1, pageSize: 1 }))
  }
  if (hasPerms('system:role:list')) {
    indices.push(1)
    requests.push(get('/api/system/role/list', { pageNum: 1, pageSize: 1 }))
  }
  if (hasPerms('system:menu:list')) {
    indices.push(2)
    requests.push(get('/api/system/menu/list'))
  }
  if (hasPerms('system:dept:list')) {
    indices.push(3)
    requests.push(get('/api/system/dept/list'))
  }

  if (requests.length === 0) return

  try {
    const results = await Promise.all(requests)
    results.forEach((res, i) => {
      const idx = indices[i]
      if (idx === 0 || idx === 1) {
        statCards.value[idx].value = res.total || 0
      } else {
        statCards.value[idx].value = Array.isArray(res.data) ? res.data.length : 0
      }
    })
  } catch {
    // keep default 0
  }
}

onMounted(() => {
  loadRecentOperations()
  loadStats()
})

const shortcutOptions = [
  { title: '用户管理', icon: UserFilled, color: '#409EFF', path: '/system/user' },
  { title: '角色管理', icon: Key, color: '#67C23A', path: '/system/role' },
  { title: '菜单管理', icon: Menu, color: '#E6A23C', path: '/system/menu' },
  { title: '部门管理', icon: OfficeBuilding, color: '#F56C6C', path: '/system/dept' },
  { title: '字典管理', icon: Document, color: '#909399', path: '/system/dict' },
  { title: '定时任务', icon: Timer, color: '#1ABC9C', path: '/monitor/job' },
  { title: '登录日志', icon: UserFilled, color: '#9B59B6', path: '/monitor/logininfor' },
  { title: '操作日志', icon: Avatar, color: '#E74C3C', path: '/monitor/operlog' },
]

function joinRoutePath(parentPath: string, childPath: string) {
  if (childPath.startsWith('/')) return childPath
  const parent = parentPath.replace(/\/$/, '')
  return `${parent}/${childPath}`.replace(/\/+/g, '/')
}

function collectRoutePaths(routes: RouteRecordRaw[], parentPath = ''): string[] {
  return routes.flatMap((route) => {
    const path = joinRoutePath(parentPath, route.path)
    const childPaths = route.children ? collectRoutePaths(route.children, path) : []
    return [path, ...childPaths]
  })
}

const availableRoutePaths = computed(() => new Set(collectRoutePaths(permissionStore.routes)))

const shortcuts = computed(() => shortcutOptions.filter((item) => availableRoutePaths.value.has(item.path)))
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
