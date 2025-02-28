<script setup lang="ts">
import {computed, ref} from "vue";
import {isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";

interface ObjectInputProp {
  propKey:string,
  placeholder?:string,
  disabled?:boolean
}

const prop=withDefaults(defineProps<ObjectInputProp>(),{
  disabled: false,
  placeholder: ''
})
const dataProp= defineModel('dataProp',{required: false, type: Object})

const modelData = computed({
  get(oldValue: string): string {
    if (isNullOrUnDef(dataProp.value)){
      dataProp.value = {}
    }
    return common.getNestedProperty(dataProp.value,prop.propKey,'')
  },
  set(newValue: string): void {
    if (isNullOrUnDef(dataProp.value)){
      dataProp.value = {}
    }
    common.setNestedProperty(dataProp.value,prop.propKey,newValue)
  }
})
</script>

<template>
  <el-input
      v-model="modelData"
      :placeholder="placeholder"
      :disabled="isNullOrUnDef(disabled)?false:disabled"
  />
</template>

<style scoped lang="scss">

</style>