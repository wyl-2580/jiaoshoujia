<template>
  <div class="app-container">
    <!-- Search -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
      <el-form-item label="字典类型" prop="dictType">
        <el-input v-model="queryParams.dictType" disabled />
      </el-form-item>
      <el-form-item label="字典标签" prop="dictLabel">
        <el-input v-model="queryParams.dictLabel" placeholder="请输入字典标签" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="数据状态" clearable>
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
        <el-button type="primary" plain :icon="Plus" @click="handleAdd" v-hasPermi="['system:dict:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain :icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:dict:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:dict:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain :icon="Close" @click="handleClose">关闭</el-button>
      </el-col>
    </el-row>

    <!-- Table -->
    <el-table v-loading="loading" :data="dictDataList" @selection-change="handleSelectionChange" class="data-table">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="字典编码" prop="id" width="100" align="center" />
      <el-table-column label="字典排序" prop="dictSort" width="100" align="center" />
      <el-table-column label="字典标签" prop="dictLabel" show-overflow-tooltip />
      <el-table-column label="字典键值" prop="dictValue" show-overflow-tooltip />
      <el-table-column label="字典类型" prop="dictType" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="handleUpdate(row)" v-hasPermi="['system:dict:edit']">修改</el-button>
          <el-button link type="primary" :icon="Delete" @click="handleDelete(row)" v-hasPermi="['system:dict:remove']">删除</el-button>
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="520px" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="字典类型">
          <el-input v-model="form.dictType" disabled />
        </el-form-item>
        <el-form-item label="数据标签" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="请输入数据标签" />
        </el-form-item>
        <el-form-item label="数据键值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="请输入数据键值" />
        </el-form-item>
        <el-form-item label="显示排序" prop="dictSort">
          <el-input-number v-model="form.dictSort" :min="0" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">正常</el-radio>
            <el-radio :value="1">停用</el-radio>
          </el-radio-group>
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Refresh, Plus, Edit, Delete, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  listData as listDictData,
  getData as getDictData,
  addData as addDictData,
  updateData as updateDictData,
  deleteData as deleteDictData,
} from '@/api/system/dict'

const route = useRoute()
const router = useRouter()
const dictType = ref(route.params.dictType as string || '')

const loading = ref(false)
const submitLoading = ref(false)
const dictDataList = ref<any[]>([])
const total = ref(0)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryFormRef = ref<FormInstance>()
const formRef = ref<FormInstance>()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  dictType: dictType.value,
  dictLabel: '',
  status: ''
})

const form = reactive<Record<string, any>>({
  id: undefined,
  dictType: dictType.value,
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  status: 0,
  remark: ''
})

const formRules: FormRules = {
  dictLabel: [{ required: true, message: '数据标签不能为空', trigger: 'blur' }],
  dictValue: [{ required: true, message: '数据键值不能为空', trigger: 'blur' }]
}

async function getList() {
  loading.value = true
  try {
    const res = await listDictData(queryParams)
    dictDataList.value = res.rows
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
  queryParams.dictType = dictType.value
  handleQuery()
}

function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item) => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function resetForm() {
  form.id = undefined
  form.dictType = dictType.value
  form.dictLabel = ''
  form.dictValue = ''
  form.dictSort = 0
  form.status = 0
  form.remark = ''
}

function handleAdd() {
  resetForm()
  dialogTitle.value = '添加字典数据'
  dialogVisible.value = true
}

async function handleUpdate(row?: any) {
  resetForm()
  const dictCode = row?.id || ids.value[0]
  const res = await getDictData(dictCode)
  Object.assign(form, res.data)
  dialogTitle.value = '修改字典数据'
  dialogVisible.value = true
}

function submitForm() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (form.id) {
        await updateDictData(form)
        ElMessage.success('修改成功')
      } else {
        await addDictData(form)
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
  const dictCodes = row?.id ? [row.id] : ids.value
  ElMessageBox.confirm(`是否确认删除字典编码为"${dictCodes}"的数据项？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDictData(dictCodes.join(','))
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

function handleClose() {
  router.push('/system/dict')
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
