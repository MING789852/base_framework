<script setup lang="ts">
import {defineProps, defineOptions, nextTick, ref, computed} from "vue";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";
defineOptions({
  name: "editOption"
});
interface Props {
  dict: Record<any, any>;
  disabled?: boolean;
  dictReverse?: boolean;
  multiple?: boolean;
  multipleDataType?: 'splitStr'|'list';
  separator?: string;
  onlyShowEdit?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  dict: ()=>{return {} as Record<any, any>},
  dictReverse: ()=>false,
  multiple: ()=>false,
  multipleDataType:()=> 'splitStr',
  separator: ()=>',',
  disabled: ()=>false,
  onlyShowEdit: ()=>false
})

const data = defineModel<string | number | Array<string> | Array<number>| undefined | null>('data', {required: true})


const getMultipleList = () => {
  let list:any[] = []
  if (props.multipleDataType==='splitStr'){
    let multipleValue:string = data.value as string
    if (common.isStrBlank(multipleValue)){
      list  = []
    }else {
      list = multipleValue.split(props.separator)
    }
  }
  if (props.multipleDataType==='list'){
    let dataList = data.value as Array<string | number>
    if (common.isEmptyArr(dataList)){
      list = []
    }else {
      list = cloneDeep(data.value)
    }
  }
  return list
}

const inputValue = computed({
  // getter
  get() {
    if (props.multiple){
      return getMultipleList()
    }else {
      return data.value
    }
  },
  // setter
  set(newValue) {
    if (props.multiple){
      if (props.multipleDataType==='splitStr'){
        data.value = (newValue as Array<string | number>).join(props.separator)
      }
      if (props.multipleDataType==='list'){
        data.value = newValue
      }
    }else {
      data.value = newValue
    }
  }
})
const isEdit = ref(false);
const inputRef = ref(null);

const handleOnClick = ()=> {
  if (props.disabled){
     return
  }
  isEdit.value = true;
  nextTick(() => {
    inputRef.value.focus();
  });
}

const handleChange = () => {
  isEdit.value = false;
}

const showCommon = ()=>{
  if (inputValue.value==null){
    return ''
  }
  if (props.multiple){
    let list:any[] = getMultipleList()
    let dictValueList:any[] = []
    list.forEach((value)=>{
      let dictValue = props.dict[value]
      if (!isNullOrUnDef(dictValue)){
        dictValueList.push(props.dict[value])
      }
    })
    return dictValueList.join(props.separator)
  }else {
    let keyValue:any = inputValue.value
    return props.dict[keyValue]
  }
}
</script>

<template>
  <div style="display: flex;min-height: 23px">
    <el-select v-if="onlyShowEdit||isEdit"  ref="inputRef" size="small" v-model="inputValue" :multiple="multiple" filterable @blur="handleChange">
      <el-option
        v-for="(value,key) in dict"
        :key="key"
        :label="value"
        :value="key"
      />
    </el-select>
    <div v-else style="flex: 1" @click="handleOnClick">{{ showCommon() }}</div>
  </div>
</template>

<style scoped lang="scss"></style>

