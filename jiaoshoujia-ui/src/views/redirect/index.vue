<template>
  <div></div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  const { params, query } = route
  const path = params.path
  const target = Array.isArray(path) ? path.join('/') : path
  router.replace({ path: '/' + (target || ''), query })
})
</script>
