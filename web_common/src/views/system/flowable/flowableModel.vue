<script setup lang="ts">
import flowableApi from "@/api/flowableApi";
import {ref} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import CommonTable from "@/components/table/commonTable.vue";
import TableFnClass from "@/class/TableFnClass";
import FlowableModelDetail from "@/views/system/flowable/flowableModelDetail.vue";
import common from "@/utils/common";
import {message} from "@/utils/message";
import FlowableDetail from "@/components/flowable/flowableDetail.vue";

defineOptions({
  name: 'flowable-model'
})
const columns = ref<Array<ColumnDefine>>([
  {prop: "processDefinitionKey", label: "流程定义KEY", type: ColumnTypeEnum.COMMON},
  {prop: "processDefinitionName", label: "流程定义名称", type: ColumnTypeEnum.COMMON},
  {prop: "createUser", label: "创建人员", type: ColumnTypeEnum.COMMON},
  {prop: "createDate", label: "创建日期", type: ColumnTypeEnum.COMMON},
]);
const dictList= ref({})
const queryColumns = ref<Array<ColumnDefine>>([
  {prop: "processDefinitionKey_$_like", label: "流程定义KEY", queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "processDefinitionName_$_like", label: "流程定义名称", queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: true,
  saveFn: false,
  deleteFn: true,
  refreshFn: true,
  initQueryFn: true,
  exportExcelFn: false
})
let commonTableRef = ref(null)
const detailFlag = ref(false)
const detailData = ref({})
const actionType = ref('')
tableFn.addFn = () => {
  detailData.value = {}
  actionType.value = 'add'
  detailFlag.value = true
}
tableFn.queryFn = () => {
  commonTableRef.value.getData()
}
tableFn.rowDblclick = (row: any, column: any, event: Event)=>{
  detailData.value = row
  actionType.value = 'view'
  detailFlag.value = true
}
const finishDetail = () => {
  commonTableRef.value.getData()
}
const initUnRecordModel = () => {
  common.handleRequestApi(flowableApi.initUnRecordModel()).then(res=>{
    message(res.msg,{type:'success'})
    commonTableRef.value.getData()
  })
}
const forceDelete = () => {
  let list:any[]=commonTableRef.value.selectTableData
  if (list.length===0){
    return message('请选中数据后操作',{type:'error'})
  }
  common.showMsgDialog('该操作异常危险，是否执行').then(()=>{
    common.handleRequestApi(flowableApi.forceDeleteData(list)).then(res=>{
      message(res.msg,{type:'success'})
      commonTableRef.value.getData()
    })
  })
}

const openProcIns = (row) => {
  procInsData.value = row
  procInsFlag.value = true
}
const procInsFlag = ref(false)
const procInsData = ref({})
const procInsHis = ref(false)
const procInsColumns = ref<Array<ColumnDefine>>([
  {prop: "name", label: "流程名称", type: ColumnTypeEnum.COMMON,width: 250},
  {prop: "description", label: "流程描述", type: ColumnTypeEnum.COMMON},
  {prop: "suspended", label: "是否暂停", type: ColumnTypeEnum.COMMON},
  {prop: "ended", label: "是否结束", type: ColumnTypeEnum.COMMON},
  {prop: "deleteReason", label: "删除原因", type: ColumnTypeEnum.COMMON},
  {prop: "businessKey", label: "业务KEY", type: ColumnTypeEnum.COMMON},
  {prop: "businessStatus", label: "业务状态", type: ColumnTypeEnum.COMMON},
  {prop: "processDefinitionKey", label: "流程定义KEY", type: ColumnTypeEnum.COMMON},
  {prop: "processDefinitionName", label: "流程定义名称", type: ColumnTypeEnum.COMMON},
  {prop: "processDefinitionVersion", label: "流程定义版本", type: ColumnTypeEnum.COMMON},
  {prop: "startTime", label: "开始时间", type: ColumnTypeEnum.COMMON},
  {prop: "endTime", label: "结束时间", type: ColumnTypeEnum.COMMON},
]);
const procInsQueryColumns = ref<Array<ColumnDefine>>([
  {prop: "name", label: "流程名称", queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "businessKey", label: "业务KEY", queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);
const procInsDetailFlag = ref(false)
const procInsDetailData = ref<any>({})
const procInsTableFn = new TableFnClass()
const procInsTableButton = ref<CommonTableButton>({
  queryFn: false,
  addFn: false,
  saveFn: false,
  deleteFn: true,
  refreshFn: true,
  initQueryFn: true,
  exportExcelFn: false
})
let procInsTableRef = ref(null)
procInsTableFn.initFn = () => {}
procInsTableFn.deleteFn = () => {
  let list:any[]=procInsTableRef.value.selectTableData
  if (list.length===0){
    return message('请选中数据后操作',{type:'error'})
  }
  common.showMsgDialog('是否执行删除操作').then(()=>{
    common.handleRequestApi(flowableApi.deleteProcessInstance(list)).then(res=>{
      procInsTableRef.value.getData()
    })
  })
}
procInsTableFn.rowDblclick = (row: any, column: any, event: Event)=>{
  procInsDetailData.value = row
  procInsDetailFlag.value = true
}
procInsTableFn.getData = () => {
  let data = {
    model: procInsData.value,
    his: procInsHis.value,
    current: procInsTableRef.value.query.current,
    size: procInsTableRef.value.query.size,
    name: procInsTableRef.value.query.queryParams.name,
    businessKey:procInsTableRef.value.query.queryParams.businessKey
  }
  common.handleRequestApi(flowableApi.getProcInsPageByProcDefKey(data)).then(res=>{
    common.handleResponsePage(procInsTableRef.value,res.data)
  })
}
const checkProcIns = (his:boolean) => {
  procInsHis.value = his
  procInsTableRef.value.getData()
}
</script>

<template>
  <div class="parent">
    <common-table ref="commonTableRef"  :columns="columns"
                  :queryColumns="queryColumns"
                  :api="flowableApi" :tableFn="tableFn" :table-button="tableButton">
      <template #button_end>
        <el-button  type="primary" size="small" @click="initUnRecordModel">初始化未记录流程定义</el-button>
        <el-button  type="primary" size="small" @click="forceDelete">强制删除</el-button>
      </template>
      <template #column>
        <el-table-column header-align="center" align="center" label="操作" >
          <template #default="scope">
            <el-button   size="small" @click="openProcIns(scope.row)">查看关联流程实例</el-button>
          </template>
        </el-table-column>
      </template>
      <template #dialog>
        <el-dialog   v-model="detailFlag" append-to-body fullscreen>
          <flowable-model-detail
              v-if="detailFlag" v-model:detailData="detailData" v-model:detailFlag="detailFlag" v-model:actionType="actionType" @finish="finishDetail"/>
        </el-dialog>
        <el-dialog v-if="procInsFlag"  v-model="procInsFlag" append-to-body fullscreen>
          <common-table ref="procInsTableRef"  :columns="procInsColumns" height="75vh" :query-columns="procInsQueryColumns"
                        :api="flowableApi" :tableFn="procInsTableFn" :table-button="procInsTableButton">
            <template #button_end>
              <el-button  type="primary" size="small" @click="checkProcIns(false)">查看正在运行</el-button>
              <el-button  type="primary" size="small" @click="checkProcIns(true)">查看历史</el-button>
            </template>
          </common-table>
        </el-dialog>
        <el-dialog v-if="procInsDetailFlag"  v-model="procInsDetailFlag" append-to-body fullscreen>
          <flowable-detail :process-id="procInsDetailData.id"/>
        </el-dialog>
      </template>
    </common-table>
  </div>
</template>

<style scoped lang="scss">
.parent {
  height: 100%;
}
.parent> :first-child{
  height: 100%;
}
</style>
