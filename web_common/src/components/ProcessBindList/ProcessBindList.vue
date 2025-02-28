<script setup lang="ts">

import IconifyIconOnline from "@/components/ReIcon/src/iconifyIconOnline";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import {onMounted, ref} from "vue";
import {isNullOrUnDef} from "@pureadmin/utils";
import processBindApi from "@/api/processBindApi"
import common from "@/utils/common";


interface ProcessBindData {
  value: string,
  processModel: string
}
interface ProcessBindListProp {
  saveProcessBindFn?: (data:ProcessBindData[])=>void,
  groupName: string
}

const props = withDefaults(defineProps<ProcessBindListProp>(), {})
const processBindFlag =  defineModel<boolean>("processBindFlag")

const dictMapping = ref({})

onMounted(async () => {
  common.showGlobalLoading()
  //加载数据字典
  let dictResponse:HttpResult = await processBindApi.getDictMapping()
  dictMapping.value = dictResponse.data
  //加载数据
  let dataResponse:HttpResult = await processBindApi.getProcessBindData(props.groupName)
  processBindData.value = dataResponse.data
  common.closeGlobalLoading()
})

const processBindColumns = ref<ColumnDefine[]>([
  {label: "值", prop: "value", type: ColumnTypeEnum.INPUT},
  {label: "模型", prop: "processModel", type: ColumnTypeEnum.OPTION},
])
const processBindData = ref<ProcessBindData[]>([])
const addProcessBind = () => {
  processBindData.value.push({
    value: '',
    processModel: ''
  })
}
const deleteProcessBind = (row:any,index:number) => {
  processBindData.value.splice(index,1)
}
const saveProcessBind = () => {
  if (!isNullOrUnDef(props.saveProcessBindFn)){
    props.saveProcessBindFn(processBindData.value)
  }
  common.handleRequestApi(processBindApi.saveProcessBindData({groupName:props.groupName,processBindDataList:processBindData.value})).then(res=>{
    if (res.code == 200){
      processBindFlag.value = false
    }
  })
}
</script>

<template>
  <div class="w-full">
    <div class="w-full flex flex-row justify-start">
      <el-button type="primary" size="small" @click="addProcessBind">新增</el-button>
      <el-button type="primary" size="small" @click="saveProcessBind">保存</el-button>
    </div>
    <div class="w-full mt-5">
      <el-table
          element-loading-text="加载中..."
          :data="processBindData"
          stripe
          :border="true">
        <el-table-column width="40" header-align="center" align="center" label="序" fixed>
          <template #default="scope">
            {{scope.$index + 1}}
          </template>
        </el-table-column>
        <el-table-column header-align="center" align="center" label="操作" width="100">
          <template #default="scope">
            <div class="w-full h-full flex justify-center">
              <iconify-icon-online icon="ri:delete-bin-5-fill"
                                   class="text-red-600 cursor-pointer w-[20px] h-[20px]"
                                   @click="deleteProcessBind(scope.row,scope.$index)"/>
            </div>
          </template>
        </el-table-column>
        <template v-for="item in processBindColumns" :key="item.prop">
          <CommonTableColumn  :item="item" size="default" :dict-list="dictMapping"/>
        </template>
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">

</style>