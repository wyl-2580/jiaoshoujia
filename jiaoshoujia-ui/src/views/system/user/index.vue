<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- Department Tree -->
      <el-col :xs="24" :sm="8" :md="6" :lg="5">
        <el-card shadow="never" class="dept-card">
          <template #header>
            <div class="card-header">
              <span>部门列表</span>
              <el-input
                v-model="deptFilterText"
                placeholder="输入部门名称搜索"
                size="small"
                :prefix-icon="Search"
                clearable
              />
            </div>
          </template>
          <el-tree
            ref="deptTreeRef"
            :data="deptTree"
            :props="{ label: 'label', children: 'children' }"
            node-key="id"
            default-expand-all
            highlight-current
            :filter-node-method="filterDeptNode"
            @node-click="handleDeptNodeClick"
          />
        </el-card>
      </el-col>

      <!-- User Table -->
      <el-col :xs="24" :sm="16" :md="18" :lg="19">
        <!-- Search -->
        <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
          <el-form-item label="用户名称" prop="username">
            <el-input v-model="queryParams.username" placeholder="请输入用户名称" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phone">
            <el-input v-model="queryParams.phone" placeholder="请输入手机号码" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="用户状态" clearable>
              <el-option label="正常" value="0" />
              <el-option label="停用" value="1" />
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
            <el-button type="primary" plain :icon="Plus" @click="handleAdd" v-hasPermi="['system:user:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain :icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:user:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:user:remove']">删除</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain :icon="Download" @click="handleExport" v-hasPermi="['system:user:export']">导出</el-button>
          </el-col>
        </el-row>

        <!-- Table -->
        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange" class="data-table">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="序号" type="index" width="60" align="center" :index="(index: number) => (queryParams.pageNum - 1) * queryParams.pageSize + index + 1" />
          <el-table-column label="用户名称" prop="username" show-overflow-tooltip />
          <el-table-column label="用户昵称" prop="nickname" show-overflow-tooltip />
          <el-table-column label="部门" prop="deptName" show-overflow-tooltip />
          <el-table-column label="角色" show-overflow-tooltip min-width="140">
            <template #default="{ row }">
              <template v-if="row.roles && row.roles.length">
                <el-tag v-for="role in row.roles" :key="role.id" size="small" style="margin-right: 4px">
                  {{ role.roleName }}
                </el-tag>
              </template>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column label="手机号码" prop="phone" width="120" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-switch
                v-if="hasEditPermi"
                v-model="row.status"
                :active-value="0"
                :inactive-value="1"
                @change="handleStatusChange(row)"
              />
              <el-tag v-else :type="row.status === 0 ? 'success' : 'danger'" size="small">
                {{ row.status === 0 ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
          <el-table-column v-if="hasAnyOperPermi" label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:user:edit']">修改</el-button>
              <el-button link type="primary" :icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:user:remove']">删除</el-button>
              <el-dropdown v-if="hasResetPwdPermi" @command="(cmd: string) => handleCommand(cmd, row)">
                <el-button link type="primary" :icon="DArrowRight">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="resetPwd" :icon="Key">重置密码</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
      </el-col>
    </el-row>

    <!-- Add/Edit Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="680px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入用户昵称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="({ label: 'label', value: 'id', children: 'children' } as any)"
                placeholder="请选择归属部门"
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号码" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名称" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名称" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!form.id">
            <el-form-item label="用户密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入用户密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户性别" prop="sex">
              <el-radio-group v-model="form.sex">
                <el-radio :value="0">男</el-radio>
                <el-radio :value="1">女</el-radio>
                <el-radio :value="2">未知</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="0">正常</el-radio>
                <el-radio :value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="roleIds">
              <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
                <el-option
                  v-for="item in roleOptions"
                  :key="item.id"
                  :label="item.roleName"
                  :value="item.id"
                  :disabled="item.status === 1"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>

    <!-- Reset Password Dialog -->
    <el-dialog title="重置密码" v-model="resetPwdVisible" width="420px" append-to-body destroy-on-close>
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="90px">
        <el-form-item label="用户名称">
          <span>{{ resetPwdForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetPwdForm.password" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitResetPwd">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Search, Refresh, Plus, Edit, Delete, Download, DArrowRight, Key } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { listUser, getUser, addUser, updateUser, deleteUser, resetUserPwd, changeUserStatus, exportUser } from '@/api/system/user'
import { listDept } from '@/api/system/dept'
import { optionselect as roleOptionselect } from '@/api/system/role'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()
function checkPermi(permi: string): boolean {
  return userStore.permissions.includes(permi)
}
const hasEditPermi = computed(() => checkPermi('system:user:edit'))
const hasRemovePermi = computed(() => checkPermi('system:user:remove'))
const hasResetPwdPermi = computed(() => checkPermi('system:user:resetPwd'))
const hasAnyOperPermi = computed(() => hasEditPermi.value || hasRemovePermi.value || hasResetPwdPermi.value)

const loading = ref(false)
const submitLoading = ref(false)
const userList = ref<any[]>([])
const total = ref(0)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)
const dateRange = ref<string[]>([])
const deptFilterText = ref('')
const deptTreeRef = ref<any>(null)
const deptTree = ref<any[]>([])
const roleOptions = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const resetPwdVisible = ref(false)

const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const resetPwdFormRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  phone: '',
  status: '',
  deptId: undefined as number | undefined,
  beginTime: '',
  endTime: ''
})

const form = reactive<Record<string, any>>({
  id: undefined,
  username: '',
  nickname: '',
  deptId: undefined,
  phone: '',
  email: '',
  password: '',
  sex: 0,
  status: 0,
  roleIds: [],
  remark: ''
})

const resetPwdForm = reactive({
  id: undefined as number | undefined,
  username: '',
  password: ''
})

const formRules: FormRules = {
  username: [{ required: true, message: '用户名称不能为空', trigger: 'blur' }],
  nickname: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
  password: [
    { required: true, message: '用户密码不能为空', trigger: 'blur' },
    { min: 5, max: 20, message: '用户密码长度必须介于 5 和 20 之间', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
}

const resetPwdRules: FormRules = {
  password: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 5, max: 20, message: '密码长度必须介于 5 和 20 之间', trigger: 'blur' }
  ]
}

watch(deptFilterText, (val) => {
  deptTreeRef.value?.filter(val)
})

function filterDeptNode(value: string, data: any) {
  if (!value) return true
  return data.label.includes(value)
}

async function getDeptTree() {
  const res = await listDept()
  deptTree.value = buildTree(res.data)
}

function buildTree(data: any[]): any[] {
  const map: Record<number, any> = {}
  const roots: any[] = []
  data.forEach((item: any) => {
    map[item.id] = { id: item.id, label: item.deptName, children: [] }
  })
  data.forEach((item: any) => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  return roots
}

function handleDeptNodeClick(data: any) {
  queryParams.deptId = data.id
  handleQuery()
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
    const res = await listUser(queryParams)
    userList.value = res.rows
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
  queryParams.deptId = undefined
  handleQuery()
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item) => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function resetForm() {
  form.id = undefined
  form.username = ''
  form.nickname = ''
  form.deptId = undefined
  form.phone = ''
  form.email = ''
  form.password = ''
  form.sex = 0
  form.status = 0
  form.roleIds = []
  form.remark = ''
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '添加用户'
  dialogVisible.value = true
}

async function handleUpdate(row?: any) {
  resetForm()
  const userId = row?.id || ids.value[0]
  const res = await getUser(userId)
  Object.assign(form, res.data)
  form.roleIds = res.data.roleIds || []
  roleOptions.value = roleOptions.value.length ? roleOptions.value : []
  dialogTitle.value = '修改用户'
  dialogVisible.value = true
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (form.id) {
        await updateUser(form)
        ElMessage.success('修改成功')
      } else {
        await addUser(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      getList()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDelete(row?: any) {
  const userIds = row?.id ? [row.id] : ids.value
  ElMessageBox.confirm(`是否确认删除用户编号为"${userIds}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteUser(userIds.join(','))
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

async function handleStatusChange(row: any) {
  const text = row.status === 0 ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认要${text}"${row.username}"用户吗？`, '警告', { type: 'warning' })
    await changeUserStatus({ id: row.id, status: row.status })
    ElMessage.success(`${text}成功`)
  } catch {
    row.status = row.status === 0 ? 1 : 0
  }
}

function handleCommand(command: string, row: any) {
  if (command === 'resetPwd') {
    resetPwdForm.id = row.id
    resetPwdForm.username = row.username
    resetPwdForm.password = ''
    resetPwdVisible.value = true
  }
}

function submitResetPwd() {
  resetPwdFormRef.value?.validate(async (valid) => {
    if (!valid) return
    try {
      await resetUserPwd({ id: resetPwdForm.id!, password: resetPwdForm.password })
      ElMessage.success('重置密码成功')
      resetPwdVisible.value = false
    } catch {
      // 后端异常由全局拦截器处理提示
    }
  })
}

async function handleExport() {
  try {
    await exportUser(queryParams)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

async function getRoleList() {
  const res = await roleOptionselect()
  roleOptions.value = res.data || []
}

onMounted(() => {
  getList()
  getDeptTree()
  getRoleList()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}

.dept-card {
  .card-header {
    display: flex;
    flex-direction: column;
    gap: 10px;
    font-weight: 600;
  }
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
