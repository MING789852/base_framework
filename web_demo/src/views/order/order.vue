<script setup lang="ts">
import commonTable from '@/components/table/commonTable.vue'
import orderApi from "@root/api/orderApi";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import {ref} from "vue";
import TableFnClass from "@/class/TableFnClass";
import common from "@/utils/common";
import {message} from "@/utils/message";


defineOptions({
  name: "business-order"
});
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: common.authActionCode('add'),
  saveFn: false,
  deleteFn: common.authActionCode('delete'),
  refreshFn: true,
  initQueryFn: false,
  exportExcelFn: true
})
const queryTemplate = ref<QueryDefine>({
  current: 1,
  size: 15,
  total: 0,
  queryParams:{}
})
const columns=ref<ColumnDefine[]>([
  {label: "订单详情", prop: "",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.LEVEL,children:[
      {label: "订单号", prop: "orderNo",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
      {label: "订单名称", prop: "orderName",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
    ]},
  {label: "订单价格",prop: "orderPrice",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON,width:'100'},
  {label: "创建时间",prop: "createDate",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.DATE_COMMON},
  {label: "修改时间",prop: "updateDate",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.DATE_COMMON},
  {label: "创建人员",prop: "createUser",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
  {label: "修改人员", prop: "updateUser",show:true,fixed:false,sortable: false, type: ColumnTypeEnum.COMMON},
])
const queryColumns=ref<ColumnDefine[]>([
  {label: "订单号", prop: "orderNo", query:true,queryType:QueryTypeEnum.INPUT,queryCondition:QueryConditionEnum.LIKE},
  {label: "订单名称",prop: "orderName", query:true,queryType:QueryTypeEnum.INPUT,queryCondition:QueryConditionEnum.LIKE},
  {label: "订单价格",prop: "orderPrice", query:true,queryType:QueryTypeEnum.INPUT_RANGE,queryCondition:QueryConditionEnum.RANGE},
  {label: "创建时间",prop: "createDate", query:true,queryType:QueryTypeEnum.DATE_RANGE,queryCondition:QueryConditionEnum.BETWEEN_DATE},
  {label: "修改时间",prop: "updateDate", query:true,queryType:QueryTypeEnum.DATE_RANGE,queryCondition:QueryConditionEnum.BETWEEN_DATE},
  {label: "创建人员", prop: "createUser", query:true,queryType:QueryTypeEnum.INPUT,queryCondition:QueryConditionEnum.LIKE},
  {label: "修改人员", prop: "updateUser", query:true,queryType:QueryTypeEnum.INPUT,queryCondition:QueryConditionEnum.LIKE},
])
const commonTableRef = ref<CommonTableType>(null)
const dictMapping=ref({})

// 覆盖自定义方法
tableFn.exportExcelFn = () => {
  let data = {
    queryData: commonTableRef.value.query,
    mainColumns: columns.value
  }
  common.showGlobalLoading()
  orderApi.exportExcel(data).then((res:Blob) => {
    common.handleBlob(res)
  }).catch(() => {
    message('请求失败', {type: 'error'})
  }).finally(() => {
    common.closeGlobalLoading()
  })
}

tableFn.addFn = () => {
  message('暂无实现',{type:'warning'})
}
</script>

<template>
  <common-table ref="commonTableRef" class="commonTable" :api="orderApi" :query-in-header="false"
                :columns="columns" :query-columns="queryColumns" :query-template="queryTemplate"
                  :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping"/>
</template>

<style scoped lang="scss">
</style>
