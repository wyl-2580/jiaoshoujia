<template>
  <template v-if="!item.meta?.hidden">
    <!-- Single child or no children -->
    <template v-if="hasOneShowingChild(item) && (!onlyOneChild?.children || onlyOneChild.noShowingChildren) && !item.alwaysShow">
      <app-link v-if="onlyOneChild" :to="resolvePath(onlyOneChild.path)">
        <el-menu-item :index="resolvePath(onlyOneChild.path)">
          <el-icon v-if="onlyOneChild.meta?.icon || item.meta?.icon">
            <component :is="onlyOneChild.meta?.icon || item.meta?.icon" />
          </el-icon>
          <template #title>
            <span>{{ onlyOneChild.meta?.title }}</span>
          </template>
        </el-menu-item>
      </app-link>
    </template>

    <!-- Multiple children -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </template>
      <SidebarItem
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(child.path)"
      />
    </el-sub-menu>
  </template>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import AppLink from './AppLink.vue'

interface Props {
  item: RouteRecordRaw & { alwaysShow?: boolean }
  basePath?: string
}

const props = withDefaults(defineProps<Props>(), {
  basePath: '',
})

const onlyOneChild = ref<(RouteRecordRaw & { noShowingChildren?: boolean }) | null>(null)

function hasOneShowingChild(parent: RouteRecordRaw): boolean {
  const showingChildren = (parent.children || []).filter((child) => {
    if (child.meta?.hidden) return false
    onlyOneChild.value = child
    return true
  })

  if (showingChildren.length === 1) return true

  if (showingChildren.length === 0) {
    onlyOneChild.value = { ...parent, path: '', noShowingChildren: true } as any
    return true
  }
  return false
}

function resolvePath(childPath: string): string {
  if (childPath.startsWith('/')) return childPath
  if (props.basePath.startsWith('/')) {
    return props.basePath.endsWith('/')
      ? `${props.basePath}${childPath}`
      : `${props.basePath}/${childPath}`
  }
  return `${props.basePath}/${childPath}`
}
</script>
