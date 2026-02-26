<script setup lang="ts">
import {onMounted} from 'vue'
import {isNullOrUnDef} from "@pureadmin/utils";
import DesFormColumnTypeEnum from "@/enums/DesFormColumnTypeEnum";
import DesFormFnClass from "@/class/DesFormFnClass";
import common from "@/utils/common";
import EditFile from "@/components/EditFile/editFile.vue";
import EditImageList from "@/components/EditImageList/editImageList.vue";
import EditCheckBox from "@/components/EditCheckBox/editCheckBox.vue";


interface Props {
  desFormDefineList?: DesFormDefine[],
  dictMapping?: any,
  desFormFn?: DesFormFnClass
}

const viewData = defineModel<any>('detailData', {
  required: false,
  get(v) {
    if (isNullOrUnDef(v)) {
      v = {}
      viewData.value = v
    }
    return v;
  }
})
//有文件字段时必传
const uploadMap = defineModel('uploadMap', {type: Map<String, File>, required: false, default: new Map<String, File>()})
const deleteFileIdList = defineModel('deleteFileIdList', {type: Array<string>, required: false, default: []})

onMounted(() => {
  props.desFormFn?.initFn?.()
})

const props = withDefaults(defineProps<Props>(), {
  desFormDefineList: () => [],
  dictMapping: () => {
    return {}
  },
  desFormFn: () => {
    return new DesFormFnClass()
  }
})
const displayObject = (row: any, prop: string) => {
  if (common.isStrBlank(prop)) {
    return ''
  }
  if (isNullOrUnDef(row)) {
    return ''
  }
  return common.getNestedProperty(row, prop, '')
}
const getValueByDic = (row: any, prop: string) => {
  let dict = props.dictMapping[prop]
  let value = row[prop]
  if (isNullOrUnDef(dict)) {
    return value
  } else {
    let dictValue = dict[value]
    if (isNullOrUnDef(dictValue)) {
      return value
    } else {
      return dictValue
    }
  }
}

const getDesDirection = (define: DesFormDefine) => {
  if (common.isMobile()){
    return 'vertical'
  }
  return define.direction ?? 'horizontal'
}
const getDesColumn = (define: DesFormDefine) => {
  if (common.isMobile()){
    return 1
  }
  return define.column ?? 3
}

const getDesRowSpan = (define: DesFormContentDefine) => {
  return define.rowSpan ?? 1
}
const getDesColSpan = (define: DesFormContentDefine) => {
  if (common.isMobile()){
    return 1
  }
  return define.colSpan ?? 1
}

//按钮操作
const buttonAction = (prop: string, rowIndex: number, row: any) => {
  props?.desFormFn?.buttonAction?.actionFn?.(row, prop, rowIndex)
}
const getButtonActionName = (columnDefine: ColumnDefine, rowIndex: number, row: any) => {
  return props?.desFormFn?.buttonAction?.buttonName?.(row, columnDefine.prop, rowIndex) ?? columnDefine.label
}
//选择按钮操作
const buttonSelectAdd = (row: any, index: number, prop: string) => {
  props.desFormFn?.buttonSelect?.addFn?.(row, index, prop)
}
const buttonSelectRemove = (isMulti: boolean, dataIndex: any, dataItem: any, row: any, index: number, prop: string) => {
  if (props.desFormFn?.buttonSelect?.removeFn) {
    props.desFormFn.buttonSelect.removeFn(dataIndex, dataItem, row, index, prop)
  } else {
    if (isMulti) {
      let isJson = props.desFormFn?.buttonSelect?.isJson ?? false
      if (isJson) {
        let list = JSON.parse(row[prop])
        list = list.filter((_, i) => i !== dataIndex)
        row[prop] = JSON.stringify(list)
      } else {
        row[prop] = row[prop].filter((_, i) => i !== dataIndex)
      }
    } else {
      row[prop] = null
    }
  }
}
const buttonSelectShow = (isMulti: boolean, dataIndex: any, dataItem: any, row: any, index: number, prop: string) => {
  if (props.desFormFn?.buttonSelect?.showFn) {
    return props.desFormFn.buttonSelect.showFn(dataIndex, dataItem, row, index, prop)
  } else {
    if (isMulti) {
      return dataItem
    } else {
      return row[prop]
    }
  }
}
const buttonSelectAddName = (isMulti: boolean, row: any, index: number, prop: string) => {
  if (props.desFormFn?.buttonSelect?.addButtonName) {
    return props.desFormFn.buttonSelect.addButtonName(row, index, prop)
  } else {
    if (isMulti) {
      return '+新增'
    } else {
      return '选择'
    }
  }
}
const buttonMultiSelectDataList = (row: any, index: number, prop: string) => {
  let isJson = props.desFormFn?.buttonSelect?.isJson ?? false
  if (isJson) {
    if (common.isStrBlank(row[prop])) {
      return []
    } else {
      return JSON.parse(row[prop])
    }
  } else {
    if (common.isEmptyArr(row[prop])) {
      return []
    } else {
      return row[prop]
    }
  }
}

//组件属性
const disabledFn = (data: any,index: number, prop: string,desFormDefine: DesFormDefine) => {
  let flag = props.desFormFn?.editAction?.canEditFn?.(data,index, prop,desFormDefine)??true
  return !flag
}
const textAreaSize = (data: any, index: number, prop: string,desFormDefine: DesFormDefine) => {
  return props.desFormFn?.editAction?.textAreaSize?.(data, index, prop,desFormDefine)??{ minRows: 4 }
}
</script>

<template>
  <div class="w-full h-full">
    <template v-for="(desFormDefine,index) in desFormDefineList" :key="index">
      <div v-if="desFormDefineList.length>1" style="height: 20px"/>
      <el-descriptions
          :direction="getDesDirection(desFormDefine)"
          :column="getDesColumn(desFormDefine)"
          :size="desFormDefine.size"
          :label-width="desFormDefine.labelWidth"
          :border="desFormDefine?.border??true"
      >
        <template v-if="desFormDefine.showTitle??true" #title>
          <slot name="desTitle" :item="desFormDefine">
            <el-alert type="warning" :closable="false">
              <div class="font-bold">{{desFormDefine.title}}</div>
            </el-alert>
          </slot>
        </template>

          <el-descriptions-item
              v-for="(item,index) in desFormDefine.content" :key="item.prop"
              :label="item.label" :rowspan="getDesRowSpan(item)" :span="getDesColSpan(item)"
                                :label-width="item.labelWidth" :width="item.width" :min-width="item.minWidth"
                                :align="item.align" :label-align="item.labelAlign">
            <div v-if="item.type === DesFormColumnTypeEnum.COMMON">
              {{ displayObject(viewData, item.prop) }}
            </div>
            <div v-else-if="item.type === DesFormColumnTypeEnum.DICT_COMMON">
              {{ getValueByDic(viewData, item.prop) }}
            </div>
            <!--文本输入-->
            <el-input
                v-else-if="item.type === DesFormColumnTypeEnum.INPUT"
                v-model="viewData[item.prop]"
                :disabled="disabledFn(viewData,index,item.prop,desFormDefine)"/>
            <el-input
                v-else-if="item.type === DesFormColumnTypeEnum.AREA_INPUT"
                v-model="viewData[item.prop]"
                :disabled="disabledFn(viewData,index,item.prop,desFormDefine)"
                :autosize="textAreaSize(viewData,index,item.prop,desFormDefine)"
                type="textarea"
            />
            <!-- 选择器-->
            <el-select
                v-else-if="[DesFormColumnTypeEnum.OPTION,DesFormColumnTypeEnum.OPTION_WITH_CREATE,DesFormColumnTypeEnum.MULTIPLE_OPTION].includes(item.type)"
                clearable
                :allow-create="item.type === DesFormColumnTypeEnum.OPTION_WITH_CREATE"
                :multiple="item.type === DesFormColumnTypeEnum.MULTIPLE_OPTION"
                v-model="viewData[item.prop]"
                :size="desFormDefine.size"
                filterable
                :disabled="disabledFn(viewData,index,item.prop,desFormDefine)">
              <el-option
                  v-for="(value,key) in dictMapping[item.prop]"
                  :key="key"
                  :label="value"
                  :value="key"
              />
            </el-select>
            <!--普通按钮-->
            <el-button
                style="width: 100%"
                v-else-if="item.type ===  DesFormColumnTypeEnum.BUTTON"
                :disabled="disabledFn(viewData,index,item.prop,desFormDefine)"
                :size="desFormDefine.size" @click.stop="buttonAction(item.prop,index,viewData)">
              {{ getButtonActionName(item, index, viewData) }}
            </el-button>
            <!--单选-->
            <template v-else-if="item.type ===  DesFormColumnTypeEnum.RADIO">
              <el-radio-group v-model="viewData[item.prop]">
                <el-radio
                    v-for="(label,value) in dictMapping[item.prop]" :key="value"
                    :value="value"
                    :disabled="disabledFn(viewData,index,item.prop,desFormDefine)">
                  {{label}}
                </el-radio>
              </el-radio-group>
            </template>
            <!--多选-->
            <template v-else-if="[DesFormColumnTypeEnum.CHECK_BOX_LIST].includes(item.type)">
              <edit-check-box v-if="DesFormColumnTypeEnum.CHECK_BOX_LIST" v-model:model-value="viewData[item.prop]"
                              :disabled="disabledFn(viewData,index,item.prop,desFormDefine)"
                              :is-json="true" :item-list="dictMapping[item.prop]"/>
            </template>
            <!--选择按钮-->
            <template v-else-if="item.type ===  DesFormColumnTypeEnum.BUTTON_SELECT">
              <el-button v-if="isNullOrUnDef(viewData[item.prop])" :size="desFormDefine.size"
                         :disabled="disabledFn(viewData,index,item.prop,desFormDefine)"
                         @click.stop="buttonSelectAdd(viewData,index,item.prop)">
                {{ buttonSelectAddName(false, viewData, index, item.prop) }}
              </el-button>
              <el-tag v-else disable-transitions closable
                      @close="buttonSelectRemove(false,null,null,viewData,index,item.prop)">
                <a style="color: blue;font-weight: bold" @click.stop="buttonSelectAdd(viewData,index,item.prop)">
                  {{ buttonSelectShow(false, null, null, viewData, index, item.prop) }}
                </a>
              </el-tag>
            </template>
            <!--日期-->
            <template v-else-if="item.type === DesFormColumnTypeEnum.DATE">
              <el-date-picker
                              style="width: 100%"
                              v-model="viewData[item.prop]"
                              type="date" value-format="YYYY-MM-DD HH:mm:ss"
                              :disabled="disabledFn(viewData,index,item.prop,desFormDefine)"/>
            </template>
            <!--多选择按钮-->
            <template v-else-if="item.type === DesFormColumnTypeEnum.BUTTON_MULTI_SELECT">
              <div class="w-full h-full flex flex-wrap gap-2">
                <el-button size="small" @click="buttonSelectAdd(viewData, index,item.prop)"
                           :disabled="disabledFn(viewData,index,item.prop,desFormDefine)">
                  {{ buttonSelectAddName(true, viewData, index, item.prop) }}
                </el-button>
                <el-tag
                    v-for="(dataItem,dataIndex) in buttonMultiSelectDataList(viewData, index,item.prop)"
                    :key="dataIndex"
                    closable
                    :disable-transitions="true"
                    @close="buttonSelectRemove(true,dataIndex,dataItem,viewData, index,item.prop)"
                >
                  {{ buttonSelectShow(true, dataIndex, dataItem, viewData, index, item.prop) }}
                </el-tag>
              </div>
            </template>
            <!-- 文件上传-->
            <template v-else-if="item.type === DesFormColumnTypeEnum.FILE">
              <EditFile
                  v-model:delete-file-id-list="deleteFileIdList"
                  v-model:upload-map="uploadMap"
                  v-model:model-value="viewData[item.prop]"
                  :disabled="disabledFn(viewData,index,item.prop,desFormDefine)"
                  :file-action="desFormFn.fileAction"
                  :other-prop="{
                    data:viewData,
                    dataIndex:index,
                    dataPropName:item.prop
                  }"/>
            </template>
            <!-- 图片列表-->
            <template v-else-if="item.type === DesFormColumnTypeEnum.IMG_LIST">
              <edit-image-list
                  v-model:delete-file-id-list="deleteFileIdList"
                  v-model:upload-map="uploadMap"
                  v-model:model-value="viewData[item.prop]"
                  :file-action="desFormFn.fileAction"
                  :disabled="disabledFn(viewData,index,item.prop,desFormDefine)"
                  :img-prop="desFormFn.imgProp"
                  :center="false"
                  :other-prop="{
                    data:viewData,
                    dataIndex:index,
                    dataPropName:item.prop
                  }"/>
            </template>
          </el-descriptions-item>
      </el-descriptions>
    </template>
  </div>
</template>

<style scoped lang="scss">

</style>
