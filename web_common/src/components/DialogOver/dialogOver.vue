<script setup lang="ts">
  import {computed} from "vue";

  const props = defineProps({
    openOrCloseFlag: {type: Boolean, require: true, default: false}
  })
  const flag = computed(() => props.openOrCloseFlag)
</script>

<template>
  <div v-if="flag" class="dialog-container">
      <slot/>
  </div>
</template>

<style scoped lang="scss">
.dialog-container {
  padding: 12px;
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  z-index: 10;
}
</style>
