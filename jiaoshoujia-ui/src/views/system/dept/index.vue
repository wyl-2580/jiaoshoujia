<template>
  <div class="app-container">
    <!-- Search -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="queryParams.deptName" placeholder="请输入部门名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="部门状态" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="停用" :value="1" />
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
        <el-button type="primary" plain :icon="Plus" @click="handleAdd()" v-hasPermi="['system:dept:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain :icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
    </el-row>

    <!-- Table -->
    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="deptList"
      row-key="id"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      class="data-table"
    >
      <el-table-column prop="deptName" label="部门名称" min-width="200" />
      <el-table-column prop="orderNum" label="排序" width="100" align="center" />
      <el-table-column prop="leader" label="负责人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:dept:edit']">修改</el-button>
          <el-button link type="primary" :icon="Plus" @click="handleAdd(row)" v-hasPermi="['system:dept:add']">新增</el-button>
          <el-button link type="primary" :icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:dept:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add/Edit Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="deptTreeOptions"
            :props="({ label: 'label', value: 'id', children: 'children' } as any)"
            placeholder="选择上级部门"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="显示排序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Search, Refresh, Plus, Edit, Delete, Sort } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { listDept, getDept, addDept, updateDept, deleteDept } from '@/api/system/dept'

const loading = ref(false)
const submitLoading = ref(false)
const deptList = ref<any[]>([])
const deptTreeOptions = ref<any[]>([])
const isExpandAll = ref(true)
const refreshTable = ref(true)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()

const queryParams = reactive({
  deptName: '',
  status: ''
})

const form = reactive<Record<string, any>>({
  id: undefined,
  parentId: undefined,
  deptName: '',
  orderNum: 0,
  leader: '',
  phone: '',
  email: '',
  status: 0
})

const formRules: FormRules = {
  parentId: [{ required: true, message: '上级部门不能为空', trigger: 'blur' }],
  deptName: [{ required: true, message: '部门名称不能为空', trigger: 'blur' }],
  orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
}

function buildTree(data: any[]): any[] {
  const map: Record<number, any> = {}
  const roots: any[] = []
  data.forEach((item) => {
    map[item.id] = { ...item, children: [] }
  })
  data.forEach((item) => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

function buildTreeSelect(data: any[]): any[] {
  const map: Record<number, any> = {}
  const roots: any[] = []
  data.forEach((item) => {
    map[item.id] = { id: item.id, label: item.deptName, children: [] }
  })
  data.forEach((item) => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

async function getList() {
  loading.value = true
  try {
    const res = await listDept(queryParams)
    deptList.value = buildTree(res.data)
  } finally {
    loading.value = false
  }
}

async function getDeptTreeOptions() {
  const res = await listDept()
  deptTreeOptions.value = buildTreeSelect(res.data)
}

function handleQuery() {
  getList()
}

function resetQuery() {
  queryFormRef.value?.resetFields()
  handleQuery()
}

function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

function resetForm() {
  form.id = undefined
  form.parentId = undefined
  form.deptName = ''
  form.orderNum = 0
  form.leader = ''
  form.phone = ''
  form.email = ''
  form.status = 0
}

function handleAdd(row?: any) {
  resetForm()
  getDeptTreeOptions()
  if (row) {
    form.parentId = row.id
  }
  dialogTitle.value = '添加部门'
  dialogVisible.value = true
}

async function handleUpdate(row: any) {
  resetForm()
  await getDeptTreeOptions()
  const res = await getDept(row.id)
  Object.assign(form, res.data)
  dialogTitle.value = '修改部门'
  dialogVisible.value = true
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (form.id) {
        await updateDept(form)
        ElMessage.success('修改成功')
      } else {
        await addDept(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      getList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDelete(row: any) {
  ElMessageBox.confirm(`是否确认删除名称为"${row.deptName}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDept(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
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
</style>
