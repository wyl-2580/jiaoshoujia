<template>
  <div class="app-container">
    <!-- Search -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
      <el-form-item label="系统模块" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入系统模块" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作人员" prop="operName">
        <el-input v-model="queryParams.operName" placeholder="请输入操作人员" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="业务类型" prop="businessType">
        <el-select v-model="queryParams.businessType" placeholder="业务类型" clearable>
          <el-option v-for="item in businessTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="操作状态" clearable>
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- Toolbar -->
    <el-row :gutter="10" class="toolbar">
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['monitor:operlog:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" @click="handleClean" v-hasPermi="['monitor:operlog:clear']">清空</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :icon="Download" @click="handleExport" v-hasPermi="['monitor:operlog:export']">导出</el-button>
      </el-col>
    </el-row>

    <!-- Table -->
    <el-table v-loading="loading" :data="logList" @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="日志编号" prop="id" width="90" align="center" />
      <el-table-column label="系统模块" prop="title" show-overflow-tooltip />
      <el-table-column label="业务类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ getBusinessTypeLabel(row.businessType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作人员" prop="operName" width="120" align="center" />
      <el-table-column label="主机地址" prop="operIp" width="140" show-overflow-tooltip />
      <el-table-column label="请求地址" prop="operUrl" show-overflow-tooltip />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作时间" prop="operTime" width="160" align="center" sortable />
      <el-table-column label="操作" width="80" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="View" @click="handleView(row)">详细</el-button>
        </template>
      </el-table-column>
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

    <!-- Detail Dialog -->
    <el-dialog title="操作日志详细" v-model="detailVisible" width="750px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="操作模块">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ getBusinessTypeLabel(detailData.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detailData.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detailData.operName }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ detailData.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="操作地址" :span="2">{{ detailData.operIp }} / {{ detailData.operLocation }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="detail-text">{{ detailData.operParam }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回参数" :span="2">
          <div class="detail-text">{{ detailData.jsonResult }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="detailData.status === 0 ? 'success' : 'danger'" size="small">
            {{ detailData.status === 0 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detailData.operTime }}</el-descriptions-item>
        <el-descriptions-item label="异常信息" :span="2" v-if="detailData.errorMsg">
          <div class="detail-text error-text">{{ detailData.errorMsg }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Delete, Download, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { listOperlog, deleteOperlog, cleanOperlog, exportOperlog } from '@/api/system/operlog'

const businessTypeOptions = [
  { label: '其它', value: '0' },
  { label: '新增', value: '1' },
  { label: '修改', value: '2' },
  { label: '删除', value: '3' },
  { label: '授权', value: '4' },
  { label: '导出', value: '5' },
  { label: '导入', value: '6' },
  { label: '强退', value: '7' },
  { label: '生成代码', value: '8' },
  { label: '清空数据', value: '9' }
]

const loading = ref(false)
const logList = ref<any[]>([])
const total = ref(0)
const ids = ref<number[]>([])
const multiple = ref(true)
const dateRange = ref<string[]>([])
const detailVisible = ref(false)
const detailData = ref<Record<string, any>>({})

const queryFormRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  operName: '',
  businessType: '',
  status: '',
  beginTime: '',
  endTime: ''
})

function getBusinessTypeLabel(value: number | string) {
  const item = businessTypeOptions.find((opt) => opt.value === String(value))
  return item?.label || '其它'
}

async function getList() {
  loading.value = true
  try {
    if (dateRange.value?.length === 2) {
      queryParams.beginTime = dateRange.value[0]
      queryParams.endTime = dateRange.value[1]
    } else {
      queryParams.beginTime = ''
      queryParams.endTime = ''
    }
    const res: any = await listOperlog(queryParams)
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
  dateRange.value = []
  handleQuery()
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item) => item.id)
  multiple.value = !selection.length
}

function handleDelete(row?: any) {
  const operIds = row?.id ? [row.id] : ids.value
  ElMessageBox.confirm(`是否确认删除日志编号为"${operIds}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteOperlog(operIds.join(','))
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

function handleClean() {
  ElMessageBox.confirm('是否确认清空所有操作日志数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await cleanOperlog()
    ElMessage.success('清空成功')
    getList()
  }).catch(() => {})
}

function handleView(row: any) {
  detailData.value = row
  detailVisible.value = true
}

async function handleExport() {
  try {
    await exportOperlog(queryParams)
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

.detail-text {
  max-height: 200px;
  overflow-y: auto;
  word-break: break-all;
  font-size: 13px;
  line-height: 1.6;
}

.error-text {
  color: #f56c6c;
}
</style>
