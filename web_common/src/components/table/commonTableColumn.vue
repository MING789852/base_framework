<script setup lang="ts">
import {isNullOrUnDef} from "@pureadmin/utils";
import TableFnClass from "@/class/TableFnClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import TableColumnValue from "@/components/table/tableColumnValue/tableColumnValue.vue";
import {computed} from "vue";
defineSlots<{
  columnItem(props: { item: ColumnDefine}): any
}>()

//有文件字段时必传
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:false,default:new Map<String,File>()})
const deleteFileIdList = defineModel('deleteFileIdList',{type:Array<string>,required:false,default:[]})
interface Props{
  size?:"" | "default" | "small" | "large",
  item: ColumnDefine,
  dictList: any,
  tableFn?:TableFnClass
}
const  props = withDefaults(defineProps<Props>(),{
  size: ()=>'small',
  dictList: ()=> {return {}},
  tableFn:()=> new TableFnClass()
})
const showColumn = (item:ColumnDefine) => {
  if (isNullOrUnDef(item)){
    return  true
  }
  let show = item.show
  if (isNullOrUnDef(show)){
    return true
  }
  return show
}
const judgeSortable = (item:ColumnDefine) => {
  let sortable = item.sortable;
  if (isNullOrUnDef(sortable)){
    return false
  }
  return sortable?'custom':false
}
const getHeaderAlign = (item:ColumnDefine)=>{
  let headerAlign = item.headerAlign
  if (isNullOrUnDef(headerAlign)){
    return 'center'
  }else {
    return headerAlign
  }
}
const getAlign = (item:ColumnDefine)=>{
  let align = item.align
  if (isNullOrUnDef(align)){
    return 'center'
  }else {
    return align
  }
}

const standardColumnTypeMap = new Map(Object.values(ColumnTypeEnum).map(item => [item, item]))
const standardColumnType = () => {
  return standardColumnTypeMap.has(props.item.type)
}

const getToolTipContent = (row: any, column: any,prop: string,position: 'header'|'cell')=>{
  let data = {row:row,column:column,prop:prop,position:position}
  return props?.tableFn?.getToolTipContent?.(data)??''
}
const showToolTip = (row: any, column: any,prop: string,position: 'header'|'cell')=>{
  let data = {row:row,column:column,prop:prop,position:position}
  return props?.tableFn?.showToolTip?.(data)??false
}

const dictMapping = computed(()=>{
  return props.dictList
})
</script>

<template>
    <el-table-column v-if="standardColumnType()&&showColumn(item)"  :fixed="item.fixed"
                      :show-overflow-tooltip="!(item.tooltip===false)"
                      :header-align="getHeaderAlign(item)"
                      :align="getAlign(item)" :label="item.label" :width="item.width"
                      :min-width="item.minWidth" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #header="headerItem">
        <el-tooltip
            :disabled="!showToolTip({},item,item.prop,'header')"
            style="white-space: pre-wrap"
            placement="top"
            effect="dark"
        >
          <template #content>
            <div style="width: 200px;white-space: pre-wrap">
              {{getToolTipContent({},item,item.prop,'header')}}
            </div>
          </template>
          <div v-if="item.type === ColumnTypeEnum.LEVEL" class="bg-[#c0c0c3] text-white">
            {{headerItem.column.label}}
          </div>
          <div v-else>
            {{headerItem.column.label}}
          </div>
        </el-tooltip>
      </template>
      <template  #default="scope">
        <!-- 多级表头-->
        <template v-if="item.type === ColumnTypeEnum.LEVEL&&!isNullOrUnDef(item.children)">
          <template v-for="levelItem in item.children" :key="levelItem.prop">
            <CommonTableColumn
                v-model:upload-map="uploadMap"
                v-model:delete-file-id-list="deleteFileIdList"
                :size="size"
                :table-fn="tableFn"
                :item="levelItem" :dict-list="dictMapping"/>
          </template>
        </template>
       <table-column-value v-else v-bind="props"
                           :scope="scope"
                           v-model:delete-file-id-list="deleteFileIdList"
                           v-model:upload-map="uploadMap"/>
      </template>
    </el-table-column>
    <slot v-else name="columnItem" :item="item"/>
</template>

<style scoped lang="scss">
.circle_edit_button{
  padding: 0;
  background-color: transparent;
  border-radius: 50%;
  border: none;
  outline: none;
  overflow: hidden;
  margin-left: 6px;
}
.circle_edit_button:hover{
  background-color: #409eff;
  color: white;
}
</style>
