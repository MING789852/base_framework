<script setup lang="ts">
import common from "@/utils/common";
import {isNullOrUnDef} from "@pureadmin/utils";
import {computed} from "vue";

interface Props {
  itemList: any,
  disabled:boolean,
  isJson:boolean,
}
const props=withDefaults(defineProps<Props>(),{
  itemList:()=>({}),
  disabled:()=>false,
  isJson:()=>true
})
const modelValue = defineModel<any>('modelValue', {required: true,
  get(v) {
    if (props.isJson){
      if (common.isStrBlank(v)){
        v='[]'
        modelValue.value = v
      }
    }else {
      if (isNullOrUnDef(v)) {
        v=[]
        modelValue.value = v
      }
    }
    return v;
  }})
const checkList = computed<any[]>({
  get(){
    let arrFile: any[]
    if (props.isJson){
      try {
        arrFile = JSON.parse(modelValue.value)
      } catch (e) {
        arrFile = []
      }
    }else {
      arrFile = modelValue.value
    }
    return arrFile
  },
  set(newValue): void {
    if (props.isJson){
      modelValue.value = JSON.stringify(newValue)
    }else {
      modelValue.value = newValue
    }
  }
})
</script>

<template>
  <el-checkbox-group v-model="checkList">
    <el-checkbox v-for="(label,value) in itemList"
                 :key="value"
                 :disabled="disabled"
                 :label="label"
                 :value="value" />
  </el-checkbox-group>
</template>

<style scoped lang="scss">

</style>