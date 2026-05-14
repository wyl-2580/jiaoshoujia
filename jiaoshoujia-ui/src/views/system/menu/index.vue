<template>
  <div class="app-container">
    <!-- Search -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
      <el-form-item label="菜单名称" prop="menuName">
        <el-input v-model="queryParams.menuName" placeholder="请输入菜单名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="菜单状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
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
        <el-button type="primary" plain :icon="Plus" @click="handleAdd()" v-hasPermi="['system:menu:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain :icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
    </el-row>

    <!-- Table -->
    <el-table
      v-if="refreshTable"
      v-loading="loading"
      :data="menuList"
      row-key="menuId"
      :default-expand-all="isExpandAll"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      class="data-table"
    >
      <el-table-column prop="menuName" label="菜单名称" show-overflow-tooltip min-width="160" />
      <el-table-column prop="icon" label="图标" width="80" align="center">
        <template #default="{ row }">
          <el-icon v-if="row.icon && row.icon !== '#'"><component :is="row.icon" /></el-icon>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" label="排序" width="80" align="center" />
      <el-table-column prop="perms" label="权限标识" show-overflow-tooltip />
      <el-table-column prop="component" label="组件路径" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
            {{ row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" align="center" />
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:menu:edit']">修改</el-button>
          <el-button link type="primary" :icon="Plus" @click="handleAdd(row)" v-hasPermi="['system:menu:add']">新增</el-button>
          <el-button link type="primary" :icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:menu:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add/Edit Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="上级菜单" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="menuTree"
                :props="{ label: 'label', value: 'id', children: 'children' }"
                placeholder="选择上级菜单"
                check-strictly
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="菜单类型" prop="menuType">
              <el-radio-group v-model="form.menuType">
                <el-radio value="M">目录</el-radio>
                <el-radio value="C">菜单</el-radio>
                <el-radio value="F">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.menuType !== 'F'">
          <el-col :span="24">
            <el-form-item label="菜单图标" prop="icon">
              <el-input v-model="form.icon" placeholder="请输入图标名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.menuType !== 'F'">
          <el-col :span="12">
            <el-form-item label="是否外链" prop="isFrame">
              <el-radio-group v-model="form.isFrame">
                <el-radio value="0">是</el-radio>
                <el-radio value="1">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="form.path" placeholder="请输入路由地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.menuType === 'C'">
          <el-col :span="12">
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="form.component" placeholder="请输入组件路径" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="路由参数" prop="query">
              <el-input v-model="form.query" placeholder="请输入路由参数" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.menuType !== 'M'">
          <el-col :span="12">
            <el-form-item label="权限标识" prop="perms">
              <el-input v-model="form.perms" placeholder="请输入权限标识" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.menuType !== 'F'">
          <el-col :span="12">
            <el-form-item label="显示状态" prop="visible">
              <el-radio-group v-model="form.visible">
                <el-radio value="0">显示</el-radio>
                <el-radio value="1">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">正常</el-radio>
                <el-radio value="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import { listMenu, getMenu, addMenu, updateMenu, deleteMenu, treeselect } from '@/api/system/menu'

const loading = ref(false)
const submitLoading = ref(false)
const menuList = ref<any[]>([])
const menuTree = ref<any[]>([])
const isExpandAll = ref(false)
const refreshTable = ref(true)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()

const queryParams = reactive({
  menuName: '',
  status: ''
})

const form = reactive<Record<string, any>>({
  menuId: undefined,
  parentId: 0,
  menuType: 'M',
  icon: '',
  menuName: '',
  orderNum: 0,
  isFrame: '1',
  path: '',
  component: '',
  query: '',
  perms: '',
  visible: '0',
  status: '0'
})

const formRules: FormRules = {
  menuName: [{ required: true, message: '菜单名称不能为空', trigger: 'blur' }],
  orderNum: [{ required: true, message: '显示排序不能为空', trigger: 'blur' }],
  path: [{ required: true, message: '路由地址不能为空', trigger: 'blur' }]
}

function buildMenuTree(data: any[]): any[] {
  const map: Record<number, any> = {}
  const roots: any[] = []
  data.forEach((item) => {
    map[item.menuId] = { ...item, children: [] }
  })
  data.forEach((item) => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.menuId])
    } else {
      roots.push(map[item.menuId])
    }
  })
  return roots
}

async function getList() {
  loading.value = true
  try {
    const res = await listMenu(queryParams)
    menuList.value = buildMenuTree(res.data)
  } finally {
    loading.value = false
  }
}

async function getMenuTreeselect() {
  const res = await treeselect()
  menuTree.value = [{ id: 0, label: '主类目', children: res.data }]
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
  form.menuId = undefined
  form.parentId = 0
  form.menuType = 'M'
  form.icon = ''
  form.menuName = ''
  form.orderNum = 0
  form.isFrame = '1'
  form.path = ''
  form.component = ''
  form.query = ''
  form.perms = ''
  form.visible = '0'
  form.status = '0'
}

function handleAdd(row?: any) {
  resetForm()
  getMenuTreeselect()
  if (row) {
    form.parentId = row.menuId
  }
  dialogTitle.value = '添加菜单'
  dialogVisible.value = true
}

async function handleUpdate(row: any) {
  resetForm()
  await getMenuTreeselect()
  const res = await getMenu(row.menuId)
  Object.assign(form, res.data)
  dialogTitle.value = '修改菜单'
  dialogVisible.value = true
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (form.menuId) {
        await updateMenu(form)
        ElMessage.success('修改成功')
      } else {
        await addMenu(form)
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
  ElMessageBox.confirm(`是否确认删除名称为"${row.menuName}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteMenu(row.menuId)
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
