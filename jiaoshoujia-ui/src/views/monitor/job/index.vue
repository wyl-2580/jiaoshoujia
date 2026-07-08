<template>
  <div class="app-container">
    <!-- Search -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="任务名称" prop="jobName">
        <el-input v-model="queryParams.jobName" placeholder="请输入任务名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="任务组名" prop="jobGroup">
        <el-select v-model="queryParams.jobGroup" placeholder="请选择" clearable>
          <el-option label="默认" value="DEFAULT" />
          <el-option label="系统" value="SYSTEM" />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="暂停" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
        <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- Toolbar -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain :icon="Plus" @click="handleAdd" v-hasPermi="['monitor:job:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['monitor:job:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- Table -->
    <el-table v-loading="loading" :data="jobList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="center" prop="id" width="80" />
      <el-table-column label="任务名称" align="center" prop="jobName" :show-overflow-tooltip="true" />
      <el-table-column label="任务组名" align="center" prop="jobGroup" />
      <el-table-column label="调用目标" align="center" prop="invokeTarget" :show-overflow-tooltip="true" />
      <el-table-column label="cron表达式" align="center" prop="cronExpression" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-switch v-model="scope.row.status" :active-value="0" :inactive-value="1"
            @change="handleStatusChange(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" :icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['monitor:job:edit']">修改</el-button>
          <el-button link type="primary" :icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['monitor:job:remove']">删除</el-button>
          <el-button link type="primary" :icon="CaretRight" @click="handleRun(scope.row)" v-hasPermi="['monitor:job:run']">执行</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-show="total > 0" :total="total" v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
      @size-change="getList" @current-change="getList" class="mt12" />

    <!-- Add/Edit Dialog -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="jobRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="任务名称" prop="jobName">
          <el-input v-model="form.jobName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务分组" prop="jobGroup">
          <el-select v-model="form.jobGroup" placeholder="请选择">
            <el-option label="默认" value="DEFAULT" />
            <el-option label="系统" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="调用方法" prop="invokeTarget">
          <el-input v-model="form.invokeTarget" placeholder="请输入调用目标字符串" />
        </el-form-item>
        <el-form-item label="cron表达式" prop="cronExpression">
          <el-input v-model="form.cronExpression" placeholder="请输入cron执行表达式" />
        </el-form-item>
        <el-form-item label="执行策略" prop="misfirePolicy">
          <el-radio-group v-model="form.misfirePolicy">
            <el-radio :value="0">默认策略</el-radio>
            <el-radio :value="1">立即执行</el-radio>
            <el-radio :value="2">执行一次</el-radio>
            <el-radio :value="3">放弃执行</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否并发" prop="concurrent">
          <el-radio-group v-model="form.concurrent">
            <el-radio :value="0">允许</el-radio>
            <el-radio :value="1">禁止</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">暂停</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listJob, getJob, addJob, updateJob, deleteJob, changeJobStatus, runJob } from '@/api/system/job'
import { Search, Refresh, Plus, Edit, Delete, CaretRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const showSearch = ref(true)
const multiple = ref(true)
const total = ref(0)
const jobList = ref<any[]>([])
const open = ref(false)
const title = ref('')
const ids = ref<number[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  jobName: undefined as string | undefined,
  jobGroup: undefined as string | undefined,
  status: undefined as number | undefined,
})

const form = reactive<any>({
  id: undefined,
  jobName: '',
  jobGroup: 'DEFAULT',
  invokeTarget: '',
  cronExpression: '',
  misfirePolicy: 0,
  concurrent: 1,
  status: 0,
})

const rules = {
  jobName: [{ required: true, message: '任务名称不能为空', trigger: 'blur' }],
  invokeTarget: [{ required: true, message: '调用目标不能为空', trigger: 'blur' }],
  cronExpression: [{ required: true, message: 'cron表达式不能为空', trigger: 'blur' }],
}

const queryRef = ref()
const jobRef = ref()

function getList() {
  loading.value = true
  listJob(queryParams).then((res: any) => {
    jobList.value = res.data?.rows || []
    total.value = res.data?.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

function reset() {
  Object.assign(form, {
    id: undefined, jobName: '', jobGroup: 'DEFAULT', invokeTarget: '',
    cronExpression: '', misfirePolicy: 0, concurrent: 1, status: 0,
  })
  jobRef.value?.resetFields()
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '添加任务'
}

function handleUpdate(row?: any) {
  reset()
  const jobId = row?.id || ids.value[0]
  getJob(jobId).then((res: any) => {
    Object.assign(form, res.data)
    open.value = true
    title.value = '修改任务'
  })
}

function submitForm() {
  jobRef.value?.validate((valid: boolean) => {
    if (valid) {
      if (form.id) {
        updateJob(form).then(() => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        addJob(form).then(() => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function cancel() {
  open.value = false
  reset()
}

function handleDelete(row?: any) {
  const jobIds = row?.id ? [row.id] : ids.value
  ElMessageBox.confirm('是否确认删除选中的定时任务?', '警告', { type: 'warning' }).then(() => {
    deleteJob(jobIds.join(',')).then(() => {
      getList()
      ElMessage.success('删除成功')
    })
  }).catch(() => {})
}

function handleStatusChange(row: any) {
  const text = row.status === 0 ? '启用' : '停用'
  ElMessageBox.confirm(`确认要${text}「${row.jobName}」任务吗?`, '警告', { type: 'warning' }).then(() => {
    changeJobStatus({ id: row.id, status: row.status }).then(() => {
      ElMessage.success(`${text}成功`)
    })
  }).catch(() => {
    row.status = row.status === 0 ? 1 : 0
  })
}

function handleRun(row: any) {
  ElMessageBox.confirm(`确认要立即执行一次「${row.jobName}」任务吗?`, '警告', { type: 'warning' }).then(() => {
    runJob({ id: row.id, jobGroup: row.jobGroup }).then(() => {
      ElMessage.success('执行成功')
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
</style>
