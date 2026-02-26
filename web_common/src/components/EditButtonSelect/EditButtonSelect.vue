<script setup lang="ts">
import {isNullOrUnDef} from "@pureadmin/utils";
import TableFnClass from "@/class/TableFnClass";
import common from "@/utils/common";

interface Prop {
  multiple?: boolean,
  tableFn?: TableFnClass,

  rowIndex?: any,
  prop?: string,

  size?: 'large' | 'default' | 'small'
}

const props = withDefaults(defineProps<Prop>(), {})
const row = defineModel<any>('row',{required: false, default: {}})


//选择按钮操作
const buttonSelectAdd= (row:any, index:number, prop:string)=>{
  props.tableFn?.buttonSelect?.addFn?.(row,index,prop)
}
const buttonSelectCanAdd = (row:any, index:number, prop:string)=>{
  return props.tableFn?.buttonSelect?.canAddFn?.(row,index,prop)??true
}
const buttonSelectShowEdit = (dataIndex:any,dataItem:any,row:any, index:number, prop:string)=>{
  return props.tableFn?.buttonSelect?.showEditFn?.(dataIndex,dataItem,row,index,prop)??false
}
const buttonSelectEdit = (dataIndex:any,dataItem:any,row:any, index:number, prop:string)=>{
  props.tableFn?.buttonSelect?.editFn?.(dataIndex,dataItem,row,index,prop)
}
const buttonSelectRemove= (isMulti:boolean,dataIndex:any,dataItem:any, row:any, index:number, prop:string)=>{
  if (props.tableFn?.buttonSelect?.removeFn){
    props.tableFn.buttonSelect.removeFn(dataIndex,dataItem,row,index,prop)
  }else {
    if (isMulti){
      let isJson = props.tableFn?.buttonSelect?.isJson??false
      if (isJson){
        let list = JSON.parse(row[prop])
        list = list.filter((_, i) => i !== dataIndex)
        row[prop] = JSON.stringify(list)
      }else {
        row[prop] = row[prop].filter((_, i) => i !== dataIndex)
      }
    }else {
      row[prop] = null
    }
  }
}
const buttonSelectCanRemove = (isMulti:boolean,dataIndex:any,dataItem:any, row:any, index:number, prop:string)=>{
  return props.tableFn?.buttonSelect?.canRemoveFn?.(dataIndex,dataItem,row,index,prop)??true
}
const buttonSelectShow = (isMulti:boolean,dataIndex:any,dataItem:any,row:any, index:number, prop:string)=>{
  if (props.tableFn?.buttonSelect?.showFn){
    return props.tableFn.buttonSelect.showFn(dataIndex,dataItem,row,index,prop)
  }else {
    if (isMulti){
      return dataItem
    }else {
      return row[prop]
    }
  }
}
const buttonSelectAddName= (isMulti:boolean,row:any, index:number, prop:string)=>{
  if (props.tableFn?.buttonSelect?.addButtonName){
    return props.tableFn.buttonSelect.addButtonName(row,index,prop)
  }else {
    if (isMulti){
      return '+新增'
    }else {
      return '选择'
    }
  }
}
const buttonMultiSelectDataList = (row:any,index:number,prop:string)=>{
  let isJson = props.tableFn?.buttonSelect?.isJson??false
  if (isJson){
    if (common.isStrBlank(row[prop])){
      return []
    }else {
      return JSON.parse(row[prop])
    }
  }else {
    if (common.isEmptyArr(row[prop])){
      return []
    }else {
      return row[prop]
    }
  }
}
</script>

<template>
  <template v-if="multiple">
    <div class="w-full h-full flex flex-wrap gap-2">
      <el-button v-if="buttonSelectCanAdd(row,rowIndex,prop)"  :size="size" @click="buttonSelectAdd(row,rowIndex,prop)">
        {{ buttonSelectAddName(true,row, rowIndex, prop) }}
      </el-button>
      <el-tag
          v-for="(dataItem,dataIndex) in buttonMultiSelectDataList(row,rowIndex,prop)"
          :key="dataIndex"
          :closable="buttonSelectCanRemove(true,dataIndex,dataItem,row,rowIndex,prop)"
          :disable-transitions="true"
          @close="buttonSelectRemove(true,dataIndex,dataItem,row,rowIndex,prop)"
      >
        <div class="flex flex-row">
          <a style="color: blue;font-weight: bold">
            {{ buttonSelectShow(true,dataIndex,dataItem,row, rowIndex, prop) }}
          </a>
          <button v-if="buttonSelectShowEdit(dataIndex,dataItem,row,rowIndex,prop)" type="button" class="circle_edit_button" aria-label="edit this tag"
                  @click.stop="buttonSelectEdit(dataIndex,dataItem,row,rowIndex,prop)">
            <iconify-icon-online icon="ri:edit-fill"/>
          </button>
        </div>
      </el-tag>
    </div>
  </template>
  <template v-else>
    <el-button v-if="isNullOrUnDef(row[prop])"  :size="size" @click.stop="buttonSelectAdd(row,rowIndex,prop)">
      {{ buttonSelectAddName(false,row, rowIndex, prop) }}
    </el-button>
    <el-tag v-else disable-transitions closable @close="buttonSelectRemove(false,null,null,row,rowIndex,prop)">
      <div class="flex flex-row">
        <a style="color: blue;font-weight: bold">
          {{ buttonSelectShow(false,null,null,row, rowIndex, prop) }}
        </a>
        <button v-if="buttonSelectShowEdit(null,null,row,rowIndex,prop)" type="button" class="circle_edit_button" aria-label="edit this tag"
                @click.stop="buttonSelectEdit(null,null,row,rowIndex,prop)">
          <iconify-icon-online icon="ri:edit-fill"/>
        </button>
      </div>
    </el-tag>
  </template>
</template>

<style scoped lang="scss">

</style>