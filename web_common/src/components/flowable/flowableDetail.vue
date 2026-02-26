<script setup lang="ts">
import {onMounted, ref} from 'vue'
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import flowableApi from "@/api/flowableApi";
import common from "@/utils/common";
import {taskStatusDictEnum} from "@/enums/TaskStatusEnum"
import {message} from "@/utils/message";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";

defineOptions({
  name: 'flowableDetail'
})

interface Prop {
  processId:string
}

const prop = withDefaults(defineProps<Prop>(), {})

const tableData = ref<any[]>([])
const dictList = ref({
  status: taskStatusDictEnum
})
const base64Image = ref('')
const columns = ref<ColumnDefine[]>([
  {prop: "status", label: "状态", type: ColumnTypeEnum.DICT_COMMON},
  {prop: "name", label: "审批人",align:'left',headerAlign:'left', type: ColumnTypeEnum.COMMON_OVER},
  {prop: "msg", label: "审批意见", type: ColumnTypeEnum.COMMON_OVER},
  {prop: "createTime", label: "开始时间", type: ColumnTypeEnum.COMMON},
  {prop: "actionTime", label: "审批时间", type: ColumnTypeEnum.COMMON}
])

onMounted(()=>{
  refreshData()
})

const refreshData = () =>{
  common.showGlobalLoading()
  flowableApi.viewProcess(prop.processId).then((res:HttpResult)=>{
    if (res.code === 200) {
      tableData.value = res.data.taskInfoVoList
      base64Image.value = 'data:image/png;base64,' + res.data.base64
    }else {
      message(res.msg,{type:'error'})
    }
  }).finally(()=>{
    common.closeGlobalLoading()
  })
}
const dictView =  (item:any,scope:any)=>{
  let data = scope.row[item.prop]
  if (item.prop === 'status'){
    if (common.isStrBlank(data)){
      return '审批中'
    }
  }
  return dictList.value[item.prop][data]
}

defineExpose({
  refreshData
})
</script>

<template>
<!--  <el-dialog v-model="dialogFlag" append-to-body>-->
  <div class="w-full h-full">
    <el-table
        :data="tableData"
        :stripe="true">
      <!-- 普通-->
      <template v-for="item in columns" :key="item.prop">
        <CommonTableColumn  :item="item" :dict-list="dictList"/>
      </template>
    </el-table>
    <div style="height: 20px"/>
    <div class="w-full flex flex-col items-center">
      <el-image  :src="base64Image"/>
    </div>
  </div>
<!--  </el-dialog>-->
</template>

<style scoped lang="scss">

</style>
