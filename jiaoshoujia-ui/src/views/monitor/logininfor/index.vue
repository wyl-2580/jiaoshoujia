<template>
  <div class="app-container">
    <!-- Search -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
      <el-form-item label="用户账号" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户账号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="登录地址" prop="ipaddr">
        <el-input v-model="queryParams.ipaddr" placeholder="请输入登录IP" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="登录状态" clearable>
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- Toolbar -->
    <el-row :gutter="10" class="toolbar">
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['monitor:logininfor:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" @click="handleClean" v-hasPermi="['monitor:logininfor:clear']">清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :icon="Download" @click="handleExport" v-hasPermi="['monitor:logininfor:export']">导出</el-button>
      </el-col>
    </el-row>

    <!-- Table -->
    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" :index="(index: number) => (queryParams.pageNum - 1) * queryParams.pageSize + index + 1" />
      <el-table-column label="用户账号" prop="userName" width="120" align="center" />
      <el-table-column label="登录IP" prop="ipaddr" width="140" show-overflow-tooltip />
      <el-table-column label="登录地点" prop="loginLocation" show-overflow-tooltip />
      <el-table-column label="浏览器" prop="browser" width="140" show-overflow-tooltip />
      <el-table-column label="操作系统" prop="os" width="140" show-overflow-tooltip />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" prop="msg" width="140" show-overflow-tooltip />
      <el-table-column label="登录时间" prop="loginTime" width="160" align="center" sortable />
    </el-table>

    <!-- Pagination -->
    <el-pagination
      class="pagination"
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="getList"
      @current-change="getList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Delete, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { listLogininfor, deleteLogininfor, cleanLogininfor, exportLogininfor } from '@/api/system/logininfor'

const loading = ref(false)
const logList = ref<any[]>([])
const total = ref(0)
const ids = ref<number[]>([])
const multiple = ref(true)

const queryFormRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userName: '',
  ipaddr: '',
  status: ''
})

async function getList() {
  loading.value = true
  try {
    const res: any = await listLogininfor(queryParams)
    logList.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item) => item.id)
  multiple.value = !selection.length
}

function handleDelete(row?: any) {
  const infoIds = row?.id ? [row.id] : ids.value
  ElMessageBox.confirm(`是否确认删除访问编号为"${infoIds}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteLogininfor(infoIds.join(','))
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

function handleClean() {
  ElMessageBox.confirm('是否确认清空所有登录日志数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await cleanLogininfor()
    ElMessage.success('清空成功')
    getList()
  }).catch(() => {})
}

async function handleExport() {
  try {
    await exportLogininfor(queryParams)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}

.search-form {
  padding: 18px 18px 0;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.data-table {
  border-radius: 8px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
