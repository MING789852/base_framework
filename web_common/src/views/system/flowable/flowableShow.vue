<script setup lang="ts">
import common from "@/utils/common";
import CommonTable from "@/components/table/commonTable.vue";
import TableFnClass from "@/class/TableFnClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import flowableShowApi from "@/api/flowableShowApi";
import {ref, useTemplateRef} from 'vue';
import {useRouter} from "vue-router";
import FlowableInsAction from "@/components/flowable/flowableInsAction.vue";


const router = useRouter();
defineOptions({
  name: 'flowable-show'
})
const queryTemplate = ref<QueryDefine>({
  current: 1,
  size: 15,
  total: 0,
  queryParams:{}
})
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: false,
  saveFn: false,
  deleteFn: false,
  refreshFn: true,
  initQueryFn: true,
  exportExcelFn: false
})
const columns=ref<ColumnDefine[]>([
  {label: "操作",prop: "viewAction",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.BUTTON,width:120},
  {label: "操作",prop: "flowableDetail",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.BUTTON,width:120},
  {label: "流程名称", prop: "flowableName",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
  {label: "业务类型编码",prop: "businessType",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
  {label: "流程创建人",prop: "createUser",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
  {label: "流程开始时间",prop: "startDate",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
  {label: "流程结束时间",prop: "endDate",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
])
const queryColumns=ref<ColumnDefine[]>([
  {label: "类型", prop: "businessType", query:true,queryType:QueryTypeEnum.OPTION,queryCondition:QueryConditionEnum.IN},
])
const commonTableRef = useTemplateRef<InstanceType<typeof CommonTable>>('commonTableRef')
const dictMapping=ref({})

const finish = ref(0)
const  getData = (finishVal:number)=>{
  finish.value = finishVal
  let data = {
    current: commonTableRef.value.query.current,
    size: commonTableRef.value.query.size,
    finish:finish.value,
    businessType: commonTableRef.value.query.queryParams['businessType']
  }
  common.handleRequestApi(flowableShowApi.getData(data)).then(res=>{
    common.handleResponsePage(commonTableRef.value,res.data)
  })
}
const currentProcessId = ref('')
const detailFlag = ref(false)
tableFn.buttonAction = {
  actionFn(row: any, propName: string, index: number): void {
    if (propName === 'viewAction'){
      //开始跳转路由
      router.push(row.jumpUrl)
    }
    if (propName === 'flowableDetail'){
      currentProcessId.value = row.instance.id
      detailFlag.value = true
    }
  },
  buttonName(row: any, propName: string, index: number): string {
    if (propName === 'viewAction'){
      return "跳转详情";
    }
    if (propName === 'flowableDetail'){
      return "审批详情";
    }
  },
}
tableFn.initFn = ()=>{
  let query=common.getCurrentRouteQuery()
  let businessType = query.businessType
  if (businessType){
    commonTableRef.value.query.queryParams['businessType'] = businessType
  }
  common.handleRequestApi(flowableShowApi.getDictMapping()).then(res=>{
    dictMapping.value = {...res.data,...dictMapping.value}
    commonTableRef.value.getData()
  })
}
tableFn.getData = ()=>{
  getData(finish.value)
}

</script>

<template>
  <common-table ref="commonTableRef" class="commonTable"
                :columns="columns" :queryColumns="queryColumns"
                :queryInHeader="true"
                :query-template="queryTemplate"
                :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping">
    <template #header_right>
      <div class="flex flex-row gap-3 content-center flex-wrap justify-end">
        <el-button :type="finish==0?'success':'info'" size="small" @click="getData(0)">待审批</el-button>
        <el-button :type="finish==1?'success':'info'" size="small" @click="getData(1)">已审批</el-button>
      </div>
    </template>
    <template #dialog>
      <el-dialog v-if="detailFlag" v-model="detailFlag" fullscreen destroy-on-close>
<!--        <flowable-detail :process-id="currentProcessId"/>-->
        <flowable-ins-action :process-id="currentProcessId" :allow-action="common.authActionCode('action')"/>
      </el-dialog>
    </template>
  </common-table>
</template>

<style scoped lang="scss">

</style>