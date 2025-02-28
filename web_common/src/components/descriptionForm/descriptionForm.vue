<script setup lang="ts">
import {computed, onMounted} from 'vue'
import {isNullOrUnDef} from "@pureadmin/utils";
import DesFormColumnTypeEnum from "@/enums/DesFormColumnTypeEnum";

const props= defineProps({
  //数值
  detailData: {type: Object, required: false, default: {} as Object},
  //尺寸
  size: {type: String as PropType<"" | "default" | "small" | "large">,required: false, default: ''},
  //字段定义
  desFormDefine: {type: Array as PropType<DesFormDefine[]>, required: true, default: [] as DesFormDefine[]},
  //字典表
  dictList: {type: Object, required: false, default: {} as Object},
  //选项远程搜索
  optionRemoteLoadingMapping: {type:Object as PropType<Record<string, boolean>>, required: false,default: {} as Record<string, boolean>},
  optionRemoteMethodFn: {type:Function as PropType<(columnConfig:DesFormContentDefine,query: string) =>void>, required: false,default:()=>{}},
  //打开选项
  openSelectFn: {type: Function as PropType<(columnConfig:DesFormContentDefine,value:any)=>{}>,required: false,default:()=>{}},
  //列数
  columnNum: {type: Number, required: false, default:3},
  //初始化
  initFn: {type: Function as PropType<()=>{}>,required: false},
  //禁用填写函数
  disableFn: {type: Function as PropType<(prop:string,value:any)=>boolean>,required: false,default:()=>{return false}},
  //option类型值变化时调用
  optionChangeFn: {type: Function as PropType<(columnConfig:DesFormContentDefine,value:any)=>{}>,required: false,default:()=>{}},
})
const emits = defineEmits(['update:detailData'])

onMounted(()=>{
  if (props.initFn!=undefined){
    props.initFn()
  }
})

const viewData = computed({
  get() {
    if (isNullOrUnDef(props.detailData)){
      let temp={}
      emits('update:detailData',temp)
      return temp
    }else {
      return props.detailData
    }
  },
  set(newValue) {
    emits('update:detailData',newValue)
  }
})

defineExpose({
  viewData
})
</script>

<template>
  <div class="w-full h-full">
    <template v-for="desForm in desFormDefine" :key="desForm.title">
      <div style="height: 20px"/>
      <el-descriptions
          :column="columnNum"
          :size="size"
          border
      >
        <template #title>
          <div class="font-bold">{{ desForm.title }}</div>
        </template>
        <template v-for="item in desForm.content" :key="item.prop">
          <el-descriptions-item v-if="item.type === DesFormColumnTypeEnum.INPUT"
                                :label="item.label">
            <el-input
                v-model="viewData[item.prop]"
                :disabled="disableFn(item.prop,viewData)"/>
          </el-descriptions-item>
          <el-descriptions-item v-else-if="item.type === DesFormColumnTypeEnum.COMMON"
                                :label="item.label">
            {{viewData[item.prop]}}
          </el-descriptions-item>
          <el-descriptions-item v-else-if="item.type === DesFormColumnTypeEnum.DICT_COMMON"
                                :label="item.label">
            {{isNullOrUnDef(dictList[item.prop])?viewData[item.prop]:dictList[item.prop][viewData[item.prop]]}}
          </el-descriptions-item>
          <el-descriptions-item v-else-if="item.type === DesFormColumnTypeEnum.AREA_INPUT"
                                :label="item.label">
            <el-input
                v-model="viewData[item.prop]"
                :disabled="disableFn(item.prop,viewData)"
                :autosize="{ minRows: 2, maxRows: 4 }"
                type="textarea"
            />
          </el-descriptions-item>
          <el-descriptions-item v-else-if="item.type === DesFormColumnTypeEnum.OPTION"
                                :label="item.label">
            <el-select v-model="viewData[item.prop]" :disabled="disableFn(item.prop,viewData)" filterable clearable @change="(changeValue: any) => {optionChangeFn(item,changeValue)}">
              <el-option
                  v-for="(value,key) in dictList[item.prop]"
                  :key="key"
                  :value="key"
                  :label="value"
              />
            </el-select>
          </el-descriptions-item>
          <el-descriptions-item v-else-if="item.type === DesFormColumnTypeEnum.OPTION_WITH_CREATE"
                                :label="item.label">
            <el-select v-model="viewData[item.prop]" :disabled="disableFn(item.prop,viewData)" filterable clearable allow-create @change="(changeValue: any) => {optionChangeFn(item,changeValue)}">
              <el-option
                  v-for="(value,key) in dictList[item.prop]"
                  :key="key"
                  :value="key"
                  :label="value"
              />
            </el-select>
          </el-descriptions-item>
          <el-descriptions-item v-else-if="item.type ===  DesFormColumnTypeEnum.CHOOSE"
                                :label="item.label">
            <el-button :disabled="disableFn(item.prop,viewData)" style="width: 100%" @click="openSelectFn(item,viewData)">
              {{
                isNullOrUnDef(viewData[item.prop]) ? '选择' : viewData[item.prop]
              }}
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item v-else-if="item.type === DesFormColumnTypeEnum.REMOTE_OPTION"
                                :label="item.label">
            <el-select v-model="viewData[item.prop]"
                       :disabled="disableFn(item.prop,viewData)"
                       filterable
                       remote
                       :loading="optionRemoteLoadingMapping[item.prop]"
                       :remote-method="(query:string)=>{optionRemoteMethodFn(item,query)}"
                       @change="(changeValue: any) => {optionChangeFn(item,changeValue)}">
              <el-option
                  v-for="(value,key) in dictList[item.prop]"
                  :key="key"
                  :value="key"
                  :label="value"
              />
            </el-select>
          </el-descriptions-item>
        </template>
      </el-descriptions>
    </template>
  </div>
</template>

<style scoped lang="scss">

</style>
