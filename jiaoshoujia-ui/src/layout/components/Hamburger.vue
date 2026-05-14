<template>
  <div class="hamburger" @click="$emit('toggle')">
    <el-icon :size="20">
      <Fold v-if="isActive" />
      <Expand v-else />
    </el-icon>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  isActive: boolean
}>()

defineEmits<{
  toggle: []
}>()
</script>

<style lang="scss" scoped>
.hamburger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #333;
}
</style>
