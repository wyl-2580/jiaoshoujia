<template>
  <section class="app-main-inner">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-transform" mode="out-in">
        <keep-alive :include="cachedViews">
          <component :is="Component" :key="route.path" />
        </keep-alive>
      </transition>
    </router-view>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const cachedViews = computed(() => {
  const views: string[] = []
  router.getRoutes().forEach((route) => {
    if (route.meta?.noCache !== true && route.name) {
      views.push(route.name as string)
    }
  })
  return views
})
</script>

<style lang="scss" scoped>
.app-main-inner {
  min-height: calc(100vh - 50px - 32px);
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
