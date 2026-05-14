<template>
  <a v-if="isExternal" :href="to" target="_blank" rel="noopener">
    <slot />
  </a>
  <router-link v-else :to="to">
    <slot />
  </router-link>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  to: string
}>()

const isExternal = computed(() => /^(https?:|mailto:|tel:)/.test(props.to))
</script>

<style lang="scss" scoped>
a {
  text-decoration: none;
}
</style>
