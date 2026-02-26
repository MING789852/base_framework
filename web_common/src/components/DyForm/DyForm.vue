<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import DyFormColumnTypeEnum from "@/enums/DyFormColumnTypeEnum";
import {TreeOptionProps} from "element-plus/es/components/tree/src/tree.type";
import common from "@/utils/common";
import dictApi from "@/api/dictApi";
import {isNullOrUnDef} from "@pureadmin/utils";
import DyLabel from "@/components/DyForm/DyLabel.vue";
import DyFile from "@/components/DyForm/DyFile.vue";
import DyImage from "@/components/DyForm/DyImage.vue";
import DyMultiSelect from "@/components/DyForm/DyMultiSelect.vue";
import FormFnClass from "@/class/FormFnClass";

defineSlots<{
  formItem(props: { item: DyFormColumn, index:number}): any
}>()

interface DyFormProp {
  labelWith?:string,
  labelPosition?:"left" | "right" | "top",
  inline?: boolean,
  formDefines: Array<DyFormColumn>,
  formItemShowMapping?:Record<string, boolean>,
  disableEdit?: boolean,
  formFn?:FormFnClass
}

const props = withDefaults(defineProps<DyFormProp>(), {
  labelWith:()=>"auto",
  labelPosition:()=>"right",
  inline:()=>false,
  formDefines:()=>[],
  formItemShowMapping:()=>{return {}},
  disableEdit:()=>false,
  formFn:() => new FormFnClass()
})
const dictQueryType = defineModel<DictQueryType[]>('dictQueryType',{required: false})
const uploadMap = defineModel<Map<String,File>>('uploadMap',{required:false})
const formData = defineModel('formDataProp',{required: false, type: Object})
const dictMapping = defineModel('dictMapping',{required: false, type: Object, default: {} as Object})

onMounted(()=>{
  if (isNullOrUnDef(dictMapping.value)) {
    dictMapping.value = {}
  }
  if (isNullOrUnDef(formData.value)){
    formData.value = {}
  }
  if (isNullOrUnDef(dictQueryType.value)){
    dictQueryType.value = []
  }
})
const refreshDict = () => {
  if (!common.isEmptyArr(dictQueryType.value)) {
    common.handleRequestApi(dictApi.getDictMappingByQuery(dictQueryType.value)).then(res=>{
      dictMapping.value = {...dictMapping.value, ...res.data}
    })
  }
}
onMounted(()=>{
  refreshDict()
})
watch(dictQueryType, (newDateRange, oldDateRange) => {
  refreshDict()
})
const defaultProps = ref<TreeOptionProps>({
  children: 'children',
  label: 'label',
  isLeaf: 'isLeaf'
})
const filterNodeMethod = (value, data) => data.label.includes(value)

const formItemShow = (item:DyFormColumn) => {
  if (isNullOrUnDef(props.formItemShowMapping)){
    return true
  }else {
    let show=props.formItemShowMapping[item.prop]
    if (isNullOrUnDef(show)){
      return true
    }else {
      return show
    }
  }
}

const disableEditFn = () => {
  return props.disableEdit
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
</script>

<template>
  <el-form :label-position="labelPosition" :label-width="labelWith" :inline="inline">
    <template v-for="(item,index) in formDefines" :key="index">
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.COMMON" :required="item.required" :label="item.label">
        {{formData[item.prop]}}
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.INPUT" :required="item.required" :label="item.label">
        <template v-if="!disableEditFn()">
          <el-input v-model="formData[item.prop]" :placeholder="isNullOrUnDef(item.placeholder)?'':item.placeholder"/>
        </template>
        <template v-else>
          {{formData[item.prop]}}
        </template>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.INPUT_AREA" :required="item.required"
                    :label="item.label">
        <template v-if="!disableEditFn()">
          <el-input v-model="formData[item.prop]" type="textarea" :autosize="{ minRows: 3}" :placeholder="isNullOrUnDef(item.placeholder)?'':item.placeholder"/>
        </template>
        <template v-else>
          {{formData[item.prop]}}
        </template>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.MONTH" :required="item.required"
                    :label="item.label">
        <template v-if="!disableEditFn()">
          <el-date-picker
              style="width: 100%"
              v-model="formData[item.prop]"
              :placeholder="isNullOrUnDef(item.placeholder)?'':item.placeholder"
              type="month"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </template>
        <template v-else>
          {{formData[item.prop]}}
        </template>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.DATE" :required="item.required"
                    :label="item.label">
        <template v-if="!disableEditFn()">
          <el-date-picker
              style="width: 100%"
              v-model="formData[item.prop]"
              :placeholder="isNullOrUnDef(item.placeholder)?'':item.placeholder"
              type="date"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </template>
        <template v-else>
          {{displayDate(formData,item.prop,'yyyy-MM-dd')}}
        </template>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.DATE_RANGE" :required="item.required"
                    :label="item.label">
        <template v-if="!disableEditFn()">
          <el-date-picker
              style="width: 100%"
              v-model="formData[item.prop]"
              :placeholder="isNullOrUnDef(item.placeholder)?'':item.placeholder"
              type="daterange"
              range-separator="To"
              start-placeholder="Start date"
              end-placeholder="End date"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
        </template>
        <template v-else>
          {{formData[item.prop]}}
        </template>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.DATETIME" :required="item.required"
                    :label="item.label">
        <el-date-picker
            style="width: 100%"
            v-model="formData[item.prop]"
            :placeholder="isNullOrUnDef(item.placeholder)?'':item.placeholder"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.COMMON_SELECT" :required="item.required"
                    :label="item.label">
        <el-select v-model="formData[item.prop]" :placeholder="isNullOrUnDef(item.placeholder)?'':item.placeholder"  filterable>
          <el-option
              v-for="(value,key) in dictMapping[item.prop]"
              :key="key"
              :label="value"
              :value="key"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.COMMON_MULTIPLE_SELECT" :required="item.required"
                    :label="item.label">
        <dy-multi-select v-model:select-dict="dictMapping[item.prop]" v-model:select-str-value="formData[item.prop]"/>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.IMAGE" :required="item.required"
                    :label="item.label">
        <dy-image v-model:img-data-prop="formData[item.prop]" v-model:upload-map="uploadMap"/>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.FILE" :required="item.required"
                    :label="item.label">
        <dy-file v-model:tableDataProp="formData[item.prop]" v-model:upload-map="uploadMap" :file-action="formFn.fileAction" />
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.LABEL" :required="item.required"
                    :label="item.label">
        <dy-label :label-list="formData[item.prop]"/>
      </el-form-item>
      <el-form-item v-show="formItemShow(item)" v-if="item.type === DyFormColumnTypeEnum.TREE_SELECT" :required="item.required"
                    :label="item.label">
        <el-tree-select
            v-model="formData[item.prop]"
            :placeholder="isNullOrUnDef(item.placeholder)?'':item.placeholder"
            :props="defaultProps"
            :filter-node-method="filterNodeMethod"
            filterable
            :multiple="false"
            :highlight-current="true"
            :data="dictMapping[item.prop]"
            node-key="value"
            :check-strictly="true"
            show-checkbox
        />
      </el-form-item>
      <slot  name="formItem" :item="item" :index="index"/>
    </template>
  </el-form>
</template>

<style scoped lang="scss">

</style>
