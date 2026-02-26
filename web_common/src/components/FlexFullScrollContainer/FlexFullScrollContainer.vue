<script setup lang="ts">
import {computed} from "vue";

//fit 自适应高度
//grow 撑满高度
interface Props {
  type?:'fit'|'grow'
}
const props = withDefaults(defineProps<Props>(), {
  type:'grow'
})

const containerClass = computed(()=>{
  return props.type === 'fit' ? 'container-fit' : 'grow-[1] container-grow'
})
</script>

<template>
  <div class="relative w-full h-full">
    <div class="flex flex-col h-full">
      <div :class="containerClass">
        <slot/>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container-fit{
  height: fit-content;
}
.container-grow{
  height: 0;
}
</style>