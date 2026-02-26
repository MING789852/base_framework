<script setup lang="ts">
import {computed, defineOptions, ref} from "vue";
import {isNullOrUnDef} from "@pureadmin/utils";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import ObjectInput from "@/components/ObjectInput/ObjectInput.vue";
import FormFnClass from "@/class/FormFnClass";
import common from "@/utils/common";
import EditImageList from "@/components/EditImageList/editImageList.vue";

defineOptions({
  name: "detailForm"
});

interface Prop {
  columns: Array<DetailColumnDefine>,
  propData: any,
  dictMapping?: any,
  formFn?:FormFnClass
}

const props = withDefaults(defineProps<Prop>(),{
  columns: ()=>[],
  propData: ()=>{return {}},
  dictMapping: ()=>{return {}},
  formFn: ()=>new FormFnClass()
})
//有文件字段时必传
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:false,default:new Map<String,File>()})
const deleteFileIdList = defineModel('deleteFileIdList',{type:Array<string>,required:false,default:[]})
const newColumns = ref<Array<DetailColumnDefine>>(props.columns)
const data = computed(() =>props.propData)

const getValueByDic=(row:any,prop:string)=>{
  let dict = props.dictMapping[prop]
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

const buttonSelectAddName = (isMulti:boolean,row:any,prop:string)=>{
  if (props.formFn?.buttonSelect?.addButtonName){
    return props.formFn.buttonSelect.addButtonName(row,null,prop)
  }else {
    if (isMulti){
      return '+新增'
    }else {
      return '选择'
    }
  }
}
const buttonSelectAdd = (row:any,prop:string)=>{
  props.formFn?.buttonSelect?.addFn?.(row,null,prop)
}
const buttonSelectRemove  = (isMulti:boolean,dataIndex:any,dataItem:any,row:any,prop:string) => {
  if (props.formFn.buttonSelect?.removeFn) {
    props.formFn.buttonSelect.removeFn(dataIndex,dataItem,row,null,prop);
  } else {
    if (isMulti){
      let isJson = props.formFn?.buttonSelect?.isJson??false
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
const buttonSelectShow = (isMulti:boolean,dataIndex:any,dataItem:any,row:any,prop:string)=>{
  if (props.formFn?.buttonSelect?.showFn){
    return props.formFn.buttonSelect.showFn(dataIndex,dataItem,row,null,prop)
  }else {
    if (isMulti){
      return dataItem
    }else {
      return row[prop]
    }
  }
}
const buttonMultiSelectDataList = (row:any,prop:string)=>{
  let isJson = props.formFn?.buttonSelect?.isJson??false
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

  <el-form ref="queryFrom" label-position="right" label-width="auto">
    <template v-for="(item, index) in newColumns" :key="index">
        <el-form-item :label="item.label">
          <el-input
            v-if="item.type === QueryTypeEnum.INPUT"
            v-model="data[item.prop]"
            :placeholder="item.placeholder"
            :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
          />
          <ObjectInput
              v-if="item.type === QueryTypeEnum.OBJECT_INPUT"
              v-model:data-prop="data"
              :placeholder="item.placeholder"
              :prop-key="item.prop"
              :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"/>
          <el-input
            v-else-if="item.type === QueryTypeEnum.AREA_INPUT"
            v-model="data[item.prop]"
            :placeholder="item.placeholder"
            :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
            :autosize="{ minRows: 2, maxRows: 4 }"
            type="textarea"
          />
          <el-date-picker
            v-else-if="item.type === QueryTypeEnum.DATE"
            v-model="data[item.prop]"
            style="width: 100%"
            class="date"
            :placeholder="item.placeholder"
            :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
            type="date"
            clearable
            value-format="YYYY-MM-DD"
          />
          <el-date-picker
              v-else-if="item.type === QueryTypeEnum.DATE_TIME"
              v-model="data[item.prop]"
              style="width: 100%"
              :placeholder="item.placeholder"
              :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
              type="datetime"
              clearable
              value-format="YYYY-MM-DD HH:mm:ss"
          />
          <el-date-picker
            v-else-if="item.type === QueryTypeEnum.MONTH"
            v-model="data[item.prop]"
            style="width: 100%"
            class="date"
            :placeholder="item.placeholder"
            :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
            type="month"
            clearable
            value-format="YYYY-MM"
          />
          <el-date-picker
              v-else-if="item.type === QueryTypeEnum.DATE_RANGE"
              v-model="data[item.prop]"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="To"
              start-placeholder="Start date"
              end-placeholder="End date"
          />
          <el-select v-else-if="item.type === QueryTypeEnum.OPTION" v-model="data[item.prop]"  clearable  filterable :placeholder="item.placeholder"   :disabled="isNullOrUnDef(item.disabled)?false:item.disabled">
            <el-option
              v-for="(value,key) in dictMapping[item.prop]"
              :key="key"
              :label="value"
              :value="key"
            />
          </el-select>
          <el-select v-else-if="item.type === QueryTypeEnum.MULTIPLE_OPTION" v-model="data[item.prop]"  clearable filterable :placeholder="item.placeholder"  :disabled="isNullOrUnDef(item.disabled)?false:item.disabled" multiple>
            <el-option
              v-for="(value,key) in dictMapping[item.prop]"
              :key="key"
              :label="value"
              :value="key"
            />
          </el-select>
          <div class="w-full text-wrap" v-else-if="item.type === QueryTypeEnum.COMMON" >
            {{data[item.prop]}}
          </div>
          <div class="w-full whitespace-pre-wrap" v-else-if="item.type === QueryTypeEnum.COMMON_OVER" >
            {{data[item.prop]}}
          </div>
          <div class="w-full text-wrap" v-else-if="item.type === QueryTypeEnum.DICT_COMMON" >
            {{getValueByDic(data,item.prop)}}
          </div>
          <edit-image-list
              v-else-if="item.type === QueryTypeEnum.IMG_LIST"
              v-model:delete-file-id-list="deleteFileIdList"
              v-model:upload-map="uploadMap"
              v-model:model-value="data[item.prop]"
              :file-action="formFn.fileAction"
              :img-prop="formFn.imgProp"
              :other-prop="{
              data:data[item.prop],
              dataIndex:0,
              dataPropName:item.prop
            }"/>
          <div class="w-full text-wrap" v-else-if="item.type === QueryTypeEnum.BUTTON_SELECT" >
            <el-button v-if="isNullOrUnDef(data[item.prop])"  @click.stop="buttonSelectAdd(data,item.prop)">
              {{buttonSelectAddName(false,data,item.prop)}}
            </el-button>
            <el-tag v-else disable-transitions closable @close="buttonSelectRemove(false,null,null,data,item.prop)">
              <a style="color: blue;font-weight: bold"  @click.stop="buttonSelectAdd(data,item.prop)">
                {{buttonSelectShow(false,null,null,data,item.prop)}}
              </a>
            </el-tag>
          </div>
          <div class="w-full h-full flex flex-wrap gap-2" v-else-if="item.type === QueryTypeEnum.BUTTON_MULTI_SELECT">
            <el-button  size="small" @click="buttonSelectAdd(data,item.prop)">
              {{ buttonSelectAddName(true,data,item.prop) }}
            </el-button>
            <el-tag
                v-for="(dataItem,dataIndex) in buttonMultiSelectDataList(data,item.prop)"
                :key="dataIndex"
                closable
                :disable-transitions="true"
                @close="buttonSelectRemove(true,dataIndex,dataItem,data,item.prop)"
            >
              {{ buttonSelectShow(true,dataIndex,dataItem,data,item.prop) }}
            </el-tag>
          </div>
        </el-form-item>
    </template>
  </el-form>
</template>

<style scoped lang="scss">
//$content-width: 200px;
//
//.form-inline .el-select {
//  width: $content-width;
//}
//.form-inline .el-input {
//  width: $content-width;
//}
//.form-inline ::v-deep(.el-date-editor){
//  width: $content-width;
//}
//.date{
//  :deep(.el-input__wrapper) {
//    width: unset !important;
//  }
//}
</style>
