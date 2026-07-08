<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：用户信息卡片 -->
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>个人信息</span>
          </template>
          <div class="user-info-card">
            <div class="avatar-section">
              <el-avatar :size="100" :src="userStore.avatar || undefined">
                {{ profile.nickname?.charAt(0) || profile.username?.charAt(0) }}
              </el-avatar>
            </div>
            <ul class="info-list">
              <li>
                <el-icon><User /></el-icon>
                <span>用户名称</span>
                <span class="info-value">{{ profile.username }}</span>
              </li>
              <li>
                <el-icon><Phone /></el-icon>
                <span>手机号码</span>
                <span class="info-value">{{ profile.phone || '未设置' }}</span>
              </li>
              <li>
                <el-icon><Message /></el-icon>
                <span>用户邮箱</span>
                <span class="info-value">{{ profile.email || '未设置' }}</span>
              </li>
              <li>
                <el-icon><OfficeBuilding /></el-icon>
                <span>所属部门</span>
                <span class="info-value">{{ profile.deptName || '-' }}</span>
              </li>
              <li>
                <el-icon><UserFilled /></el-icon>
                <span>所属角色</span>
                <span class="info-value">{{ roleNames }}</span>
              </li>
              <li>
                <el-icon><Calendar /></el-icon>
                <span>创建日期</span>
                <span class="info-value">{{ profile.createTime || '-' }}</span>
              </li>
            </ul>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：修改信息 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span>基本资料</span>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本资料" name="info">
              <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="80px" style="max-width: 480px;">
                <el-form-item label="用户昵称" prop="nickname">
                  <el-input v-model="infoForm.nickname" placeholder="请输入用户昵称" maxlength="30" />
                </el-form-item>
                <el-form-item label="手机号码" prop="phone">
                  <el-input v-model="infoForm.phone" placeholder="请输入手机号码" maxlength="11" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="infoForm.email" placeholder="请输入邮箱" maxlength="50" />
                </el-form-item>
                <el-form-item label="性别" prop="sex">
                  <el-radio-group v-model="infoForm.sex">
                    <el-radio :value="0">男</el-radio>
                    <el-radio :value="1">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitInfo" :loading="infoLoading">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px" style="max-width: 480px;">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="submitPassword" :loading="pwdLoading">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { get, put } from '@/utils/request'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

interface ProfileData {
  username: string
  nickname: string
  email: string
  phone: string
  sex: number
  avatar: string
  deptName: string
  createTime: string
  roles: { roleName: string }[]
}

const profile = reactive<Partial<ProfileData>>({})
const activeTab = ref('info')

const roleNames = computed(() => {
  return profile.roles?.map((r) => r.roleName).join('、') || '-'
})

const infoFormRef = ref<FormInstance>()
const infoForm = reactive({ nickname: '', phone: '', email: '', sex: 0 as number })
const infoLoading = ref(false)

const infoRules: FormRules = {
  nickname: [{ required: true, message: '用户昵称不能为空', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }],
}

const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdLoading = ref(false)

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '新密码不能为空', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '确认密码不能为空', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function loadProfile() {
  const res = await get<ProfileData>('/api/auth/profile')
  Object.assign(profile, res.data)
  infoForm.nickname = res.data.nickname || ''
  infoForm.phone = res.data.phone || ''
  infoForm.email = res.data.email || ''
  infoForm.sex = res.data.sex ?? 0
}

async function submitInfo() {
  await infoFormRef.value?.validate()
  infoLoading.value = true
  try {
    await put('/api/auth/profile', { ...infoForm })
    ElMessage.success('修改成功')
    await loadProfile()
    await userStore.getInfo()
  } finally {
    infoLoading.value = false
  }
}

async function submitPassword() {
  await pwdFormRef.value?.validate()
  pwdLoading.value = true
  try {
    await put('/api/auth/profile/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } finally {
    pwdLoading.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style lang="scss" scoped>
.user-info-card {
  .avatar-section {
    text-align: center;
    padding-bottom: 20px;
    border-bottom: 1px solid #f0f0f0;
    margin-bottom: 16px;
  }

  .info-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      display: flex;
      align-items: center;
      padding: 10px 0;
      font-size: 14px;
      color: #606266;
      border-bottom: 1px solid #f5f5f5;

      .el-icon {
        margin-right: 8px;
        color: #409eff;
      }

      .info-value {
        margin-left: auto;
        color: #333;
      }
    }
  }
}
</style>
