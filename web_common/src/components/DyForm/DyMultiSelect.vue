<script setup lang="ts">
import {computed} from "vue";
import common from "@/utils/common";
const selectStrValue = defineModel('selectStrValue',{required: false, type: String, default: ''})
const selectDict = defineModel('selectDict',{required: false, type: Object, default: {} as Object})
const selectModelValue = computed({
  get(oldValue: Array<any> | undefined): Array<any>{
    let list = []
    if (common.isStrBlank(selectStrValue.value)){
      return list
    }
    let splitList=selectStrValue.value.split(',')
    splitList.forEach((value, index)=>{
      list.push(value)
    })
    return list
  },
  set(newValue: Array<any>): void {
    selectStrValue.value = newValue.join(',')
  }
})

</script>

<template>
  <el-select v-model="selectModelValue"  filterable multiple>
    <el-option
        v-for="(value,key) in selectDict"
        :key="key"
        :label="value"
        :value="key"
    />
  </el-select>
</template>

<style scoped lang="scss">

</style>