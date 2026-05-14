<template>
  <div class="app-container">
    <!-- Toolbar -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="info" plain :icon="Upload" @click="openImportDialog" v-hasPermi="['tool:gen:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Download" :disabled="multiple" @click="handleGenTable" v-hasPermi="['tool:gen:code']">生成</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['tool:gen:remove']">删除</el-button>
      </el-col>
    </el-row>

    <!-- Table List -->
    <el-table v-loading="loading" :data="tableList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" type="index" width="60" align="center" />
      <el-table-column label="表名称" align="center" prop="tableName" :show-overflow-tooltip="true" />
      <el-table-column label="表描述" align="center" prop="tableComment" :show-overflow-tooltip="true" />
      <el-table-column label="实体类" align="center" prop="className" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" width="220" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" :icon="View" @click="handlePreview(scope.row)" v-hasPermi="['tool:gen:preview']">预览</el-button>
          <el-button link type="primary" :icon="Edit" @click="handleEditTable(scope.row)" v-hasPermi="['tool:gen:edit']">编辑</el-button>
          <el-button link type="primary" :icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['tool:gen:remove']">删除</el-button>
          <el-button link type="primary" :icon="Download" @click="handleGenTable(scope.row)" v-hasPermi="['tool:gen:code']">生成</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-show="total > 0" :total="total" v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
      @size-change="getList" @current-change="getList" class="mt12" />

    <!-- Import Dialog -->
    <el-dialog title="导入表" v-model="importOpen" width="800px" append-to-body>
      <el-table v-loading="importLoading" :data="dbTableList" @selection-change="handleImportSelection" height="400px">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="表名称" prop="tableName" :show-overflow-tooltip="true" />
        <el-table-column label="表描述" prop="tableComment" :show-overflow-tooltip="true" />
      </el-table>
      <template #footer>
        <el-button type="primary" @click="handleImportTable">确 定</el-button>
        <el-button @click="importOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- Preview Dialog -->
    <el-dialog title="代码预览" v-model="previewOpen" width="80%" top="5vh" append-to-body>
      <el-tabs v-model="activeTab">
        <el-tab-pane v-for="(code, key) in previewData" :key="key" :label="key" :name="key">
          <pre class="code-preview"><code>{{ code }}</code></pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Upload, Download, Delete, View, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const importLoading = ref(false)
const multiple = ref(true)
const total = ref(0)
const tableList = ref<any[]>([])
const dbTableList = ref<any[]>([])
const importOpen = ref(false)
const previewOpen = ref(false)
const previewData = ref<Record<string, string>>({})
const activeTab = ref('')
const ids = ref<number[]>([])
const importIds = ref<string[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
})

function getList() {
  loading.value = true
  request.get('/api/tool/gen/list', { params: queryParams }).then((res: any) => {
    tableList.value = res.data?.rows || res.rows || []
    total.value = res.data?.total || res.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

function handleImportSelection(selection: any[]) {
  importIds.value = selection.map(item => item.tableName)
}

function openImportDialog() {
  importOpen.value = true
  importLoading.value = true
  request.get('/api/tool/gen/db/list').then((res: any) => {
    dbTableList.value = res.data || []
    importLoading.value = false
  }).catch(() => { importLoading.value = false })
}

function handleImportTable() {
  if (importIds.value.length === 0) {
    ElMessage.warning('请选择要导入的表')
    return
  }
  request.post('/api/tool/gen/importTable', { tableNames: importIds.value }).then(() => {
    ElMessage.success('导入成功')
    importOpen.value = false
    getList()
  })
}

function handlePreview(row: any) {
  request.get(`/api/tool/gen/preview/${row.id}`).then((res: any) => {
    previewData.value = res.data || {}
    const keys = Object.keys(previewData.value)
    if (keys.length > 0) activeTab.value = keys[0]
    previewOpen.value = true
  })
}

function handleEditTable(row: any) {
  ElMessage.info('表编辑功能开发中')
}

function handleGenTable(row?: any) {
  const tableId = row?.id || ids.value[0]
  if (!tableId) {
    ElMessage.warning('请选择要生成的表')
    return
  }
  request.get(`/api/tool/gen/generate/${tableId}`, { responseType: 'blob' }).then((res: any) => {
    const blob = new Blob([res], { type: 'application/zip' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'generated-code.zip'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('生成成功')
  })
}

function handleDelete(row?: any) {
  const tableIds = row?.id ? [row.id] : ids.value
  ElMessageBox.confirm('是否确认删除选中的数据?', '警告', { type: 'warning' }).then(() => {
    request.delete(`/api/tool/gen/${tableIds.join(',')}`).then(() => {
      getList()
      ElMessage.success('删除成功')
    })
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.mt12 { margin-top: 12px; }
.code-preview {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  max-height: 500px;
  overflow: auto;
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
