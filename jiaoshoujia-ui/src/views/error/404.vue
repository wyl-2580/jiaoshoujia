<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <p class="error-desc">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: #f0f2f5;
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  color: #409eff;
  margin: 0;
  line-height: 1;
}

.error-desc {
  font-size: 20px;
  color: #666;
  margin: 24px 0 32px;
}
</style>
