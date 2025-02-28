<script setup lang="ts">
import EditOption from "@/components/EditOption/editOption.vue";
import EditDate from "@/components/EditDate/editDate.vue";
import EditInput from "@/components/EditInput/editInput.vue";
import EditFile from "@/components/EditFile/editFile.vue";
import {isNullOrUnDef} from "@pureadmin/utils";
import TableFnClass from "@/class/TableFnClass";
import {computed} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import common from "../../utils/common";

defineSlots<{
  columnItem(props: { item: ColumnDefine}): any
}>()

//有文件字段时必传
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:false,default:new Map<String,File>()})
const deleteFileIdList = defineModel('deleteFileIdList',{type:Array<string>,required:false,default:[]})

const  props = defineProps({
  //输入大小
  size: {type: String as PropType<"" | "default" | "small" | "large">,required: false,default: 'small'},
  //列定义
  item: {type: Object as PropType<ColumnDefine>},
  //字典表
  dictList: {type: Object, default: {} as Object},
  //输入是否禁用
  inputDisabledList: {type: Object as PropType<InputDisabled>, required: false, default: {} as InputDisabled},
  //是否可编辑EditInput、EditOption、EditFile、EditDate
  canOptEditComponentFn: {type: Function as PropType<(row:any,rowIndex:number,prop:string)=>Boolean>, required: false},
  //表格方法const tableFn = new TableFnClass()
  tableFn: {type: TableFnClass, required: false, default: new TableFnClass()}
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
    splitList.forEach((value, index)=>{
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
  let tagObj=getValueOrNullByDic(row,prop + 'Tag',prop)
  if (tagObj==null){
    return 'primary'
  }else {
    return tagObj.type
  }
}
// const getTagStyle=(row:any,prop:string)=>{
//   let tagObj=getValueOrNullByDic(row,prop + 'Tag',prop)
//   if (tagObj==null){
//     return ''
//   }else {
//     return tagObj.style
//   }
// }
const showTag=(row:any,prop:string)=>{
  return !isNullOrUnDef(dictMapping.value[prop])&&!isNullOrUnDef(dictMapping.value[prop + 'Tag'])
}
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

const displayObject = (row:any,prop:string) => {
  if (common.isStrBlank(prop)){
    return ''
  }
  if (isNullOrUnDef(row)){
    return ''
  }
  return common.getNestedProperty(row, prop, '')
}

const displayDate = (row:any,prop:string)=>{
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
  return common.formatDate(new Date(temp), 'yyyy-MM-dd')
}

const displayMonth = (row:any,prop:string) => {
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
  return common.formatDate(new Date(temp), 'yyyy-MM')
}

const toolTipContent = (columnDefine:ColumnDefine,row:any) => {
  if (isNullOrUnDef(props.tableFn.toolTipContent)){
    return ''
  }else {
    return props.tableFn.toolTipContent(columnDefine,row)
  }
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

const buttonAction = (prop:string,row:any)=>{
  if (!isNullOrUnDef(props.tableFn.buttonAction)){
    let fn = props.tableFn.buttonAction[prop]
    if (!isNullOrUnDef(fn)){
      fn(row)
    }
  }
}

const clickLink = (prop:string,row:any) => {
  let url = row[prop]
  if (!isNullOrUnDef(props.tableFn.buttonAction)){
    let fn = props.tableFn.buttonAction[prop]
    if (!isNullOrUnDef(fn)){
      fn(row)
    }else {
      common.openUrl(url)
    }
  }else {
    common.openUrl(url)
  }
}
</script>

<template>
    <!-- 可编辑文本框 -->
    <el-table-column  v-if="showColumn(item)&&item.type === ColumnTypeEnum.EDIT_INPUT"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <template v-if="isNullOrUnDef(canOptEditComponentFn)?true:canOptEditComponentFn(scope.row,scope.$index,item.prop)">
          <EditInput v-if="isNullOrUnDef(scope.row.add)" :value="scope.row[item.prop]" :on-update-value="value => scope.row[item.prop] = value"/>
          <el-input v-else :size="size"  v-model="scope.row[item.prop]"/>
        </template>
        <template v-else>
          {{scope.row[item.prop]}}
        </template>
      </template>
    </el-table-column>
    <!-- 可编辑月份选择器 -->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.EDIT_MONTH"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <EditDate v-if="isNullOrUnDef(scope.row.add)" :value="scope.row[item.prop]" date-type="month" :on-update-value=" (value) => scope.row[item.prop] = value"/>
        <el-date-picker v-else :size="size" v-model="scope.row[item.prop]" type="month" value-format="YYYY-MM"/>
      </template>
    </el-table-column>
    <!-- 可编辑日期选择器-->
    <el-table-column  v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.EDIT_DATE"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <EditDate v-if="isNullOrUnDef(scope.row.add)" :value="scope.row[item.prop]" date-type="date" :on-update-value=" (value) => scope.row[item.prop] = value"/>
        <el-date-picker v-else :size="size" v-model="scope.row[item.prop]" type="date" value-format="YYYY-MM-DD"/>
      </template>
    </el-table-column>
    <!-- 可编辑选择器-->
    <el-table-column    v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.EDIT_OPTION"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <EditOption v-if="isNullOrUnDef(scope.row.add)" :value="scope.row[item.prop]" :dict="dictMapping[item.prop]" :on-update-value=" (val) => scope.row[item.prop] = val"/>
        <el-select v-else :size="size"  v-model="scope.row[item.prop]"  filterable>
          <el-option
            v-for="(value,key) in dictMapping[item.prop]"
            :key="key"
            :label="value"
            :value="key"
          />
        </el-select>
      </template>
    </el-table-column>
    <!-- 日期-->
    <el-table-column  v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.DATE"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <el-date-picker :size="size" v-model="scope.row[item.prop]" type="date" value-format="YYYY-MM-DD"/>
      </template>
    </el-table-column>
    <!-- 文件操作-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.FILE"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :show-overflow-tooltip="false" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <EditFile
          v-model:delete-file-id-list="deleteFileIdList"
          v-model:upload-map="uploadMap"
          v-model:model-value="scope.row[item.prop]"
          :table-fn="tableFn"
          :row="scope.row"
          :row-index="scope.$index"
          :prop-name="item.prop"/>
      </template>
    </el-table-column>
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.ONLY_READ_FILE"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :show-overflow-tooltip="false" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <EditFile
            v-model:delete-file-id-list="deleteFileIdList"
            v-model:upload-map="uploadMap"
            v-model:model-value="scope.row[item.prop]"
            :can-upload-file = "false"
            :can-delete-file = "false"
            :table-fn="tableFn"
            :row="scope.row"
            :row-index="scope.$index"
            :prop-name="item.prop"/>
      </template>
    </el-table-column>
    <!-- 单选框-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.RADIO"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <el-radio-group v-model="scope.row[item.prop]">
          <el-radio v-for="(label,value) in dictMapping[item.prop]" :size="size" :key="value" :value="value">{{label}}</el-radio>
        </el-radio-group>
      </template>
    </el-table-column>
    <!-- 选择器-->
    <el-table-column    v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.OPTION"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <el-select v-model="scope.row[item.prop]" :size="size"  filterable>
          <el-option
            v-for="(value,key) in dictMapping[item.prop]"
            :key="key"
            :label="value"
            :value="key"
          />
        </el-select>
      </template>
    </el-table-column>
    <!-- 输入框-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.INPUT"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :min-width="item.minWidth" :label="item.label" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #header>
        <el-tooltip
          v-if="!isNullOrUnDef(item.tooltip)"
          effect="dark"
          style="white-space: pre-wrap"
          placement="top"
        >
          <template #content>
            <div style="width: 400px;white-space: pre-wrap">
              {{item.tooltip}}
            </div>
          </template>
          {{item.label}}
        </el-tooltip>
      </template>
      <template #default="scope">
        <el-input  v-model="scope.row[item.prop]" :size="size"  :disabled="inputDisabledList[item.prop]===undefined?false:inputDisabledList[item.prop](scope.row,item.prop)"/>
      </template>
    </el-table-column>
    <!--普通按钮-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.BUTTON"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :min-width="item.minWidth" :label="item.label" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <el-button :size="size" @click.stop="buttonAction(item.prop,scope.row)">{{item.label}}</el-button>
      </template>
    </el-table-column>
    <!--选择按钮-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.BUTTON_SELECT"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <el-button v-if="isNullOrUnDef(scope.row[item.prop])"  :size="size" @click.stop="buttonAction(item.prop,scope.row)">选择</el-button>
        <a v-else style="color: blue;font-weight: bold"  @click.stop="buttonAction(item.prop,scope.row)">{{scope.row[item.prop]}}</a>
      </template>
    </el-table-column>
    <!-- 普通字典-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.DICT_COMMON"  :fixed="item.fixed" show-overflow-tooltip :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        {{getValueByDic(scope.row,item.prop)}}
      </template>
    </el-table-column>
    <!-- 多值普通字典-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.MULTIPLE_DICT_COMMON"  :fixed="item.fixed" show-overflow-tooltip :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        {{getMultipleValueByDic(scope.row,item.prop)}}
      </template>
    </el-table-column>
    <!-- 标签字典-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.DICT_TAG"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <el-tag v-if="showTag(scope.row,item.prop)" :type="getTagType(scope.row,item.prop)">{{getValueByDic(scope.row,item.prop)}}</el-tag>
      </template>
    </el-table-column>
    <!-- 多级表头-->
    <el-table-column  v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.LEVEL"  :fixed="item.fixed" show-overflow-tooltip :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #header="headerItem">
        <div class="bg-[#c0c0c3] text-white">
          {{headerItem.column.label}}
        </div>
      </template>
      <template #default v-if="!isNullOrUnDef(item.children)">
        <template v-for="levelItem in item.children" :key="levelItem.prop">
          <CommonTableColumn
              v-model:upload-map="uploadMap"
              v-model:delete-file-id-list="deleteFileIdList"
            :size="size"
            :table-fn="tableFn" :input-disabled-list="inputDisabledList"
            :item="levelItem" :dict-list="dictMapping"/>
        </template>
      </template>
    </el-table-column>
    <!-- 普通-->
    <el-table-column  v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.COMMON"  :fixed="item.fixed" show-overflow-tooltip :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :width="item.width" :min-width="item.minWidth" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        {{displayObject(scope.row,item.prop)}}
      </template>
    </el-table-column>
    <!-- 普通链接-->
    <el-table-column  v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.COMMON_LINK"  :fixed="item.fixed" show-overflow-tooltip :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :width="item.width" :min-width="item.minWidth" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <a class="cursor-pointer text-blue-600" @click.stop="clickLink(item.prop,scope.row)" target="_blank">{{displayObject(scope.row,item.prop)}}</a>
      </template>
    </el-table-column>
    <!-- 普通日期转换yyyy-MM-dd HH:mm:ss-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.DATE_TIME_COMMON"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        {{isNullOrUnDef(scope.row[item.prop])?'':common.formatDate(scope.row[item.prop],'yyyy-MM-dd HH:mm:ss')}}
      </template>
    </el-table-column>
    <!-- 转换yyyy-MM-->
    <el-table-column  v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.MONTH_COMMON"  :fixed="item.fixed" show-overflow-tooltip :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :width="item.width" :min-width="item.minWidth" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        {{displayMonth(scope.row,item.prop)}}
      </template>
    </el-table-column>
    <!-- 转换yyyy-MM-dd-->
    <el-table-column   v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.DATE_COMMON"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :min-width="item.minWidth" :width="item.width" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        {{displayDate(scope.row,item.prop)}}
      </template>
    </el-table-column>
    <!-- 图片列表-->
    <el-table-column  v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.IMG_LIST"  :fixed="item.fixed"  :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :width="item.width" :min-width="item.minWidth" :prop="item.prop">
      <template #default="scope">
        <template v-for="(imgItem,imgIndex) in scope.row[item.prop]" :key="imgIndex">
          <el-image preview-teleported :preview-src-list="scope.row[item.prop]" style="width: 50px;height: 50px" fit="scale-down" :src="imgItem" :initial-index="imgIndex"/>
        </template>
      </template>
    </el-table-column>
    <!-- HTML -->
    <el-table-column  v-else-if="showColumn(item)&&item.type === ColumnTypeEnum.HTML"  :fixed="item.fixed" show-overflow-tooltip :header-align="getHeaderAlign(item)" :align="getAlign(item)" :label="item.label" :width="item.width" :min-width="item.minWidth" :prop="item.prop" :sortable="judgeSortable(item)">
      <template #default="scope">
        <div v-html="scope.row[item.prop]"/>
      </template>
    </el-table-column>
    <slot v-else name="columnItem" :item="item"/>
</template>

<style scoped lang="scss">

</style>
