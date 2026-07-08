<template>
  <div class="app-container">
    <!-- Search -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="权限字符" prop="roleKey">
        <el-input v-model="queryParams.roleKey" placeholder="请输入权限字符" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="角色状态" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="dateRange">
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
        <el-button type="primary" plain :icon="Plus" @click="handleAdd" v-hasPermi="['system:role:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain :icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:role:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:role:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :icon="Download" @click="handleExport" v-hasPermi="['system:role:export']">导出</el-button>
      </el-col>
    </el-row>

    <!-- Table -->
    <el-table v-loading="loading" :data="roleList" @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="角色编号" prop="id" width="100" align="center" />
      <el-table-column label="角色名称" prop="roleName" show-overflow-tooltip />
      <el-table-column label="权限字符" prop="roleKey" show-overflow-tooltip />
      <el-table-column label="显示顺序" prop="roleSort" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="0"
            :inactive-value="1"
            @change="handleStatusChange(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:role:edit']">修改</el-button>
          <el-button link type="primary" :icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:role:remove']">删除</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <template #label>
            <span>
              <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize('hasRole(admin)')" placement="top">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
              权限字符
            </span>
          </template>
          <el-input v-model="form.roleKey" placeholder="请输入权限字符" />
        </el-form-item>
        <el-form-item label="角色顺序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数据权限" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="请选择数据权限" style="width: 100%">
            <el-option label="全部数据权限" value="1" />
            <el-option label="自定数据权限" value="2" />
            <el-option label="本部门数据权限" value="3" />
            <el-option label="本部门及以下数据权限" value="4" />
            <el-option label="仅本人数据权限" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据权限" v-show="form.dataScope === '2'">
          <div class="menu-tree-header">
            <el-checkbox v-model="deptExpand" @change="handleDeptExpand">展开/折叠</el-checkbox>
            <el-checkbox v-model="deptNodeAll" @change="handleDeptNodeAll">全选/全不选</el-checkbox>
            <el-checkbox v-model="deptCheckStrictly">父子联动</el-checkbox>
          </div>
          <el-tree
            ref="deptTreeRef"
            class="tree-border"
            :data="deptOptions"
            show-checkbox
            node-key="id"
            :check-strictly="!deptCheckStrictly"
            :props="{ label: 'label', children: 'children' }"
            empty-text="加载中，请稍候"
          />
        </el-form-item>
        <el-form-item label="菜单权限">
          <div class="menu-tree-header">
            <el-checkbox v-model="menuExpand" @change="handleMenuExpand">展开/折叠</el-checkbox>
            <el-checkbox v-model="menuNodeAll" @change="handleMenuNodeAll">全选/全不选</el-checkbox>
            <el-checkbox v-model="menuCheckStrictly">父子联动</el-checkbox>
          </div>
          <el-tree
            ref="menuTreeRef"
            class="tree-border"
            :data="menuOptions"
            show-checkbox
            node-key="id"
            :check-strictly="!menuCheckStrictly"
            :props="{ label: 'label', children: 'children' }"
            empty-text="加载中，请稍候"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { Search, Refresh, Plus, Edit, Delete, Download, QuestionFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { listRole, getRole, addRole, updateRole, deleteRole, changeRoleStatus, exportRole } from '@/api/system/role'
import { treeselect as menuTreeselect } from '@/api/system/menu'
import { deptTreeselect } from '@/api/system/dept'

const loading = ref(false)
const submitLoading = ref(false)
const roleList = ref<any[]>([])
const total = ref(0)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)
const dateRange = ref<string[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const menuOptions = ref<any[]>([])
const menuExpand = ref(false)
const menuNodeAll = ref(false)
const menuCheckStrictly = ref(true)
const menuTreeRef = ref<any>(null)
const deptOptions = ref<any[]>([])
const deptExpand = ref(true)
const deptNodeAll = ref(false)
const deptCheckStrictly = ref(true)
const deptTreeRef = ref<any>(null)

const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  roleName: '',
  roleKey: '',
  status: '',
  beginTime: '',
  endTime: ''
})

const form = reactive<Record<string, any>>({
  id: undefined,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  status: 0,
  dataScope: '1',
  menuIds: [],
  deptIds: [],
  remark: ''
})

const formRules: FormRules = {
  roleName: [{ required: true, message: '角色名称不能为空', trigger: 'blur' }],
  roleKey: [{ required: true, message: '权限字符不能为空', trigger: 'blur' }],
  roleSort: [{ required: true, message: '角色顺序不能为空', trigger: 'blur' }]
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
    const res = await listRole(queryParams)
    roleList.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function getMenuTree() {
  const res = await menuTreeselect()
  menuOptions.value = toTreeSelect(res.data)
}

async function getDeptTree() {
  const res = await deptTreeselect()
  deptOptions.value = toDeptTreeSelect(res.data)
}

function toDeptTreeSelect(data: any[]): any[] {
  return data.map((item) => ({
    id: item.id,
    label: item.deptName,
    children: item.children?.length ? toDeptTreeSelect(item.children) : [],
  }))
}

function toTreeSelect(data: any[]): any[] {
  return data.map((item) => ({
    id: item.id,
    label: item.menuName,
    children: item.children?.length ? toTreeSelect(item.children) : [],
  }))
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
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function resetForm() {
  form.id = undefined
  form.roleName = ''
  form.roleKey = ''
  form.roleSort = 0
  form.status = 0
  form.dataScope = '1'
  form.menuIds = []
  form.deptIds = []
  form.remark = ''
}

function handleAdd() {
  resetForm()
  getMenuTree()
  getDeptTree()
  dialogTitle.value = '添加角色'
  dialogVisible.value = true
  nextTick(() => {
    menuTreeRef.value?.setCheckedKeys([])
    deptTreeRef.value?.setCheckedKeys([])
  })
}

async function handleUpdate(row?: any) {
  resetForm()
  await Promise.all([getMenuTree(), getDeptTree()])
  const roleId = row?.id || ids.value[0]
  const res = await getRole(roleId)
  Object.assign(form, res.data)
  dialogTitle.value = '修改角色'
  dialogVisible.value = true
  nextTick(() => {
    const checkedMenuIds = res.data.menuIds || []
    menuTreeRef.value?.setCheckedKeys(checkedMenuIds)
    const checkedDeptIds = res.data.deptIds || []
    deptTreeRef.value?.setCheckedKeys(checkedDeptIds)
  })
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      form.menuIds = getMenuAllCheckedKeys()
      form.deptIds = getDeptAllCheckedKeys()
      if (form.id) {
        await updateRole(form)
        ElMessage.success('修改成功')
      } else {
        await addRole(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      getList()
    } finally {
      submitLoading.value = false
    }
  })
}

function getMenuAllCheckedKeys() {
  const checked = menuTreeRef.value?.getCheckedKeys() || []
  const half = menuTreeRef.value?.getHalfCheckedKeys() || []
  return [...checked, ...half]
}

function handleDelete(row?: any) {
  const roleIds = row?.id ? [row.id] : ids.value
  ElMessageBox.confirm(`是否确认删除角色编号为"${roleIds}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteRole(roleIds.join(','))
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

async function handleStatusChange(row: any) {
  const text = row.status === 0 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认要${text}"${row.roleName}"角色吗？`, '警告', { type: 'warning' })
    await changeRoleStatus({ id: row.id, status: row.status })
    ElMessage.success(`${text}成功`)
  } catch {
    row.status = row.status === 0 ? 1 : 0
  }
}

function getDeptAllCheckedKeys() {
  const checked = deptTreeRef.value?.getCheckedKeys() || []
  const half = deptTreeRef.value?.getHalfCheckedKeys() || []
  return [...checked, ...half]
}

function handleDeptExpand(val: any) {
  const nodes = deptTreeRef.value?.store.nodesMap
  for (const node in nodes) {
    nodes[node].expanded = val
  }
}

function handleDeptNodeAll(val: any) {
  deptTreeRef.value?.setCheckedNodes(val ? deptOptions.value : [])
}

function handleMenuExpand(val: any) {
  const nodes = menuTreeRef.value?.store.nodesMap
  for (const node in nodes) {
    nodes[node].expanded = val
  }
}

function handleMenuNodeAll(val: any) {
  menuTreeRef.value?.setCheckedNodes(val ? menuOptions.value : [])
}

async function handleExport() {
  try {
    await exportRole(queryParams)
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

.menu-tree-header {
  margin-bottom: 8px;
}

.tree-border {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
  max-height: 300px;
  overflow-y: auto;
  width: 100%;
}
</style>
