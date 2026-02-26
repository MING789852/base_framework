<script setup lang="ts">
import TableFnClass from "@/class/TableFnClass";
import {computed} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import {isNullOrUnDef} from "@pureadmin/utils";
import EditInput from "@/components/EditInput/editInput.vue";
import EditDate from "@/components/EditDate/editDate.vue";
import EditImageList from "@/components/EditImageList/editImageList.vue";
import EditOption from "@/components/EditOption/editOption.vue";
import EditFile from "@/components/EditFile/editFile.vue";
import common from "@/utils/common";
import EditButtonSelect from "@/components/EditButtonSelect/EditButtonSelect.vue";

//有文件字段时必传
const uploadMap = defineModel<Map<String,File>>('uploadMap',{required:false,default:new Map<String,File>()})
const deleteFileIdList = defineModel<string[]>('deleteFileIdList',{required:false,default:[]})
interface Scope {
  row: any,
  $index: number
}
// const scope = defineModel<Scope>('scope',{required:false,default:reactive({row:{},$index:0})})

interface Props{
  size?:"" | "default" | "small" | "large",
  item: ColumnDefine,
  dictList: any,
  tableFn?:TableFnClass
  scope?:Scope
}
const  props = withDefaults(defineProps<Props>(),{
  size: ()=>'small',
  dictList: ()=> {return {}},
  tableFn:()=> new TableFnClass()
})
const dictMapping = computed(()=>{
  return props.dictList
})

const getMultipleValueByDic = (row:any,prop:string)=>{
  let dict = dictMapping.value[prop]
  let rowValue:string = row[prop]
  if (isNullOrUnDef(dict)){
    return rowValue
  }else {
    if (isNullOrUnDef(rowValue)){
      return ''
    }
    let splitList=rowValue.split(',')
    let resultList = []
    splitList.forEach((value)=>{
      let dictValue=dict[value]
      if (isNullOrUnDef(dictValue)){
        resultList.push(value)
      }else {
        resultList.push(dictValue)
      }
    })
    return resultList.join(',')
  }
}
const getValueByDic=(row:any,prop:string)=>{
  let dict = dictMapping.value[prop]
  let value = row[prop]
  if (isNullOrUnDef(dict)){
    return value
  }else {
    let dictValue=dict[value]
    if (isNullOrUnDef(dictValue)){
      return value
    }else {
      return dictValue
    }
  }
}
const getValueOrNullByDic=(row:any,dictProp:string,rowProp:string)=>{
  let dict = dictMapping.value[dictProp]
  let value = row[rowProp]
  if (isNullOrUnDef(dict)){
    return null
  }else {
    let dictValue=dict[value]
    if (isNullOrUnDef(dictValue)){
      return null
    }else {
      return dictValue
    }
  }
}
const getTagType=(row:any,prop:string)=>{
  let tagObj:DictTag=getValueOrNullByDic(row,prop + 'Tag',prop)
  if (tagObj==null){
    return 'primary'
  }else {
    return tagObj.type
  }
}
const showTag=(row:any,prop:string)=>{
  return !isNullOrUnDef(dictMapping.value[prop])&&!isNullOrUnDef(dictMapping.value[prop + 'Tag'])
}
const displayObject = (row:any,prop:string) => {
  if (common.isStrBlank(prop)){
    return ''
  }
  if (isNullOrUnDef(row)){
    return ''
  }
  return common.getNestedProperty(row, prop, '')
}
const displayDate = (row:any,prop:string,format:string)=>{
  if (common.isStrBlank(prop)){
    return ''
  }
  if (isNullOrUnDef(row)){
    return ''
  }
  let temp = row[prop]
  if (common.isStrBlank(temp)){
    return ''
  }
  return common.formatDate(new Date(temp), format)
}
const getButtonActionName = (columnDefine:ColumnDefine,rowIndex: number,row:any) => {
  return props?.tableFn?.buttonAction?.buttonName?.(row, columnDefine.prop,rowIndex)??columnDefine.label
}
const buttonAction = (prop:string,rowIndex: number,row:any)=>{
  props?.tableFn?.buttonAction?.actionFn?.(row, prop,rowIndex)
}
const clickLink = (prop:string,rowIndex: number,row:any) => {
  let url = row[prop]
  let fn = props?.tableFn?.buttonAction?.actionFn
  if (!isNullOrUnDef(fn)){
    fn(row,prop,rowIndex)
  }else {
    common.openUrl(url)
  }
}

const canEditFn = (row:any,rowIndex:number,prop:string)=>{
  return props?.tableFn?.editAction?.canEditFn?.(row,rowIndex,prop)??true
}
</script>

<template>
  <div class="w-full h-full">
    <!-- 普通文本-->
    <template v-if="item.type === ColumnTypeEnum.COMMON">
      {{displayObject(scope.row,item.prop)}}
    </template>
    <!-- 普通字典文本-->
    <template v-else-if="item.type === ColumnTypeEnum.DICT_COMMON">
      {{getValueByDic(scope.row,item.prop)}}
    </template>
    <!-- 普通可换行文本-->
    <template v-else-if="item.type === ColumnTypeEnum.COMMON_OVER">
      <div class="whitespace-pre-wrap">
        {{displayObject(scope.row,item.prop)}}
      </div>
    </template>
    <!-- 可编辑文本框 -->
    <template v-else-if="[ColumnTypeEnum.EDIT_INPUT,ColumnTypeEnum.EDIT_INPUT_AREA].includes(item.type)">
      <EditInput :only-show-edit="!isNullOrUnDef(scope.row.add)"
                 :textarea="ColumnTypeEnum.EDIT_INPUT_AREA==item.type"
                 :value="scope.row[item.prop]"
                 :on-update-value="value => scope.row[item.prop] = value"
                 :disabled="!canEditFn(scope.row,scope.$index,item.prop)"/>
    </template>
    <!-- 可编辑时间选择器 -->
    <template v-else-if="[ColumnTypeEnum.EDIT_MONTH,ColumnTypeEnum.EDIT_DATE,ColumnTypeEnum.EDIT_DATE_TIME].includes(item.type)">
      <EditDate :only-show-edit="!isNullOrUnDef(scope.row.add)" :value="scope.row[item.prop]"
                :date-type="item.type === ColumnTypeEnum.EDIT_MONTH?'month':(item.type === ColumnTypeEnum.EDIT_DATE?'date':'datetime')"
                :on-update-value=" (value) => scope.row[item.prop] = value"
                :disabled="!canEditFn(scope.row,scope.$index,item.prop)"/>
    </template>
    <!-- 可编辑选择器-->
    <template v-else-if="[ColumnTypeEnum.EDIT_OPTION,ColumnTypeEnum.EDIT_MULTIPLE_OPTION].includes(item.type)">
      <EditOption v-model:data="scope.row[item.prop]"
                  :only-show-edit="!isNullOrUnDef(scope.row.add)"
                  :multiple="ColumnTypeEnum.EDIT_MULTIPLE_OPTION===item.type" multiple-data-type="splitStr"
                  :dict="dictMapping[item.prop]"
                  :disabled="!canEditFn(scope.row,scope.$index,item.prop)"/>
    </template>
    <!-- 日期-->
    <template v-else-if="item.type === ColumnTypeEnum.DATE">
      <el-date-picker
          style="width: unset" :size="size"
          v-model="scope.row[item.prop]"
          type="date" value-format="YYYY-MM-DD HH:mm:ss"
          :disabled="!canEditFn(scope.row,scope.$index,item.prop)"/>
    </template>
    <!-- 文件操作-->
    <template v-else-if="item.type === ColumnTypeEnum.FILE">
      <EditFile
          v-model:delete-file-id-list="deleteFileIdList"
          v-model:upload-map="uploadMap"
          v-model:model-value="scope.row[item.prop]"
          :file-action="tableFn.fileAction"
          :other-prop="{
              data:scope.row,
              dataIndex:scope.$index,
              dataPropName:item.prop
          }"/>
    </template>
    <!-- 图片列表-->
    <template v-else-if="item.type === ColumnTypeEnum.IMG_LIST">
      <edit-image-list
          v-model:delete-file-id-list="deleteFileIdList"
          v-model:upload-map="uploadMap"
          v-model:model-value="scope.row[item.prop]"
          :file-action="tableFn.fileAction"
          :img-prop="tableFn.imgProp"
          :other-prop="{
              data:scope.row,
              dataIndex:scope.$index,
              dataPropName:item.prop
            }"/>
    </template>
    <!-- 单选框-->
    <template v-else-if="item.type === ColumnTypeEnum.RADIO">
      <el-radio-group v-model="scope.row[item.prop]">
        <el-radio v-for="(label,value) in dictMapping[item.prop]" :size="size" :key="value"
                  :value="value"
                  :disabled="!canEditFn(scope.row,scope.$index,item.prop)">{{label}}</el-radio>
      </el-radio-group>
    </template>
    <!-- 选择器-->
    <template v-else-if="[ColumnTypeEnum.OPTION,ColumnTypeEnum.MULTIPLE_OPTION].includes(item.type)">
      <el-select v-model="scope.row[item.prop]" :size="size"
                 :multiple="ColumnTypeEnum.MULTIPLE_OPTION===item.type"
                 clearable filterable
                 :disabled="!canEditFn(scope.row,scope.$index,item.prop)">
        <el-option
            v-for="(value,key) in dictMapping[item.prop]"
            :key="key"
            :label="value"
            :value="key"
        />
      </el-select>
    </template>
    <!-- 输入框-->
    <template v-else-if="[ColumnTypeEnum.INPUT,ColumnTypeEnum.AREA_INPUT].includes(item.type)">
      <el-input
          v-model="scope.row[item.prop]" :size="size"
          :type="ColumnTypeEnum.AREA_INPUT===item.type?'textarea':'text'"
          :autosize="{ minRows: 4}"
          :disabled="!canEditFn(scope.row,scope.$index,item.prop)"/>
    </template>
    <!--普通按钮-->
    <template v-else-if="item.type === ColumnTypeEnum.BUTTON">
      <el-button :disabled="!canEditFn(scope.row,scope.$index,item.prop)"
                 :size="size" @click.stop="buttonAction(item.prop,scope.$index,scope.row)">{{getButtonActionName(item,scope.$index,scope.row)}}
      </el-button>
    </template>
    <!--选择按钮-->
    <template v-else-if="item.type === ColumnTypeEnum.BUTTON_SELECT">

      <EditButtonSelect
       :table-fn="tableFn"
       :multiple="false"
       :prop="item.prop"
       :row-index="scope.$index"
       :row="scope.row"
       size="small"
      />
    </template>
    <!--多选择按钮-->
    <template v-else-if="item.type === ColumnTypeEnum.BUTTON_MULTI_SELECT">
      <EditButtonSelect
          :table-fn="tableFn"
          :multiple="true"
          :prop="item.prop"
          :row-index="scope.$index"
          v-model:row="scope.row"
          size="small"
      />
    </template>
    <!-- 多值普通字典-->
    <template v-else-if="item.type === ColumnTypeEnum.MULTIPLE_DICT_COMMON">
      {{getMultipleValueByDic(scope.row,item.prop)}}
    </template>
    <!-- 标签字典-->
    <template v-else-if="item.type === ColumnTypeEnum.DICT_TAG">
      <el-tag v-if="showTag(scope.row,item.prop)" :type="getTagType(scope.row,item.prop)">{{getValueByDic(scope.row,item.prop)}}</el-tag>
    </template>
    <!-- 普通链接-->
    <template v-else-if="item.type === ColumnTypeEnum.COMMON_LINK">
      <a class="cursor-pointer text-blue-600" @click.stop="clickLink(item.prop,scope.$index,scope.row)" target="_blank">{{displayObject(scope.row,item.prop)}}</a>
    </template>
    <!-- 转换yyyy-MM-->
    <template v-else-if="item.type === ColumnTypeEnum.MONTH_COMMON">
      {{displayDate(scope.row,item.prop,'yyyy-MM')}}
    </template>
    <!-- 转换yyyy-MM-dd-->
    <template v-else-if="item.type === ColumnTypeEnum.DATE_COMMON">
      {{displayDate(scope.row,item.prop,'yyyy-MM-dd')}}
    </template>
    <!-- HTML -->
    <template v-else-if="item.type === ColumnTypeEnum.HTML">
      <div v-html="scope.row[item.prop]"/>
    </template>
  </div>
</template>

<style scoped lang="scss">

</style>