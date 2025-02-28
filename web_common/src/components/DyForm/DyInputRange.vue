<script setup lang="ts">
import {computed} from "vue";
import {isNullOrUnDef} from "@pureadmin/utils";

interface DyInputRangeProp {
  size?:'large' | 'default' | 'small',

}
const rangeProp = defineModel<Array<any>>('rangeProp',{required: false})
const props = withDefaults(defineProps<DyInputRangeProp>(),{
  size(prop){
    return 'default'
  }
})

const rangeValue = computed({
  get():Array<any>{
    if (isNullOrUnDef(rangeProp.value)){
      let temp = ['','']
      rangeProp.value = temp
      return temp
    }else {
      return rangeProp.value
    }
  },
  set(newVal:Array<any>){
    rangeProp.value = newVal
  }
})
</script>

<template>
<div class="flex w-full h-full">
  <el-input  class="basis-[49%]" :size="size" v-model="rangeValue[0]"/>
  <div class="basis-[2%] flex justify-center">-</div>
  <el-input  class="basis-[49%]" :size="size" v-model="rangeValue[1]"/>
</div>
</template>

<style scoped lang="scss">

</style>