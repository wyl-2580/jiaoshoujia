<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
        <span
          v-if="index === breadcrumbs.length - 1 || !item.redirect"
          class="no-redirect"
        >
          {{ item.meta?.title }}
        </span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta?.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter, type RouteLocationMatched } from 'vue-router'

const route = useRoute()
const router = useRouter()

const breadcrumbs = ref<RouteLocationMatched[]>([])

function getBreadcrumb() {
  const matched = route.matched.filter((item) => item.meta?.title)
  breadcrumbs.value = matched
}

function handleLink(item: RouteLocationMatched) {
  const { redirect, path } = item
  if (redirect) {
    router.push(redirect as string)
  } else {
    router.push(path)
  }
}

watch(() => route.path, getBreadcrumb, { immediate: true })
</script>

<style lang="scss" scoped>
.app-breadcrumb {
  font-size: 14px;
  line-height: 50px;

  .no-redirect {
    color: #97a8be;
    cursor: text;
  }
}
</style>
