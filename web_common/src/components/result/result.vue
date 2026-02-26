<script setup lang="ts">

interface Prop {
  icon?: 'success' | 'warning' | 'info' | 'error',
  title?: string,
  subTitle?: string
}
withDefaults(defineProps<Prop>(), {
  icon: ()=>'success',
  title: ()=>'操作成功',
  subTitle: ()=>''
})

</script>

<template>
  <el-result
      :icon="icon"
      :title="title"
      :sub-title="subTitle"
  />
</template>

<style scoped lang="scss">

</style>