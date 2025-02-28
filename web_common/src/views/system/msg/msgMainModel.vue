<script setup lang="ts">
import TableFnClass from "@/class/TableFnClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import {onMounted, ref} from "vue";
import msgMainModelApi from "@/api/msgMainModelApi"
import commonTable from "@/components/table/commonTable.vue";
import common from '@/utils/common'
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {message} from "@/utils/message";
import systemUserApi from "@/api/systemUserApi";
import QueryConditionEnum from "@/enums/QueryConditionEnum";

defineOptions({
  name: "msg-mainModel"
});
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: true,
  saveFn: false,
  deleteFn: true,
  refreshFn: true,
  initQueryFn: false,
  exportExcelFn: false
})
const columns=ref<ColumnDefine[]>([
  {label: "标题", prop: "title", type:ColumnTypeEnum.COMMON},
  {label: "内容", prop: "content", type:ColumnTypeEnum.COMMON},
  {label: "业务类型", prop: "businessType", type: ColumnTypeEnum.COMMON},
  {label: "业务编码", prop: "businessKey", type: ColumnTypeEnum.COMMON},
  {label: "消息类型", prop: "type", type: ColumnTypeEnum.DICT_COMMON},
  {label: "额外信息", prop: "info", type: ColumnTypeEnum.COMMON},
  {label: "是否完结", prop: "judgeFinish", type: ColumnTypeEnum.DICT_COMMON},
  {label: "接收人", prop: "userId", type: ColumnTypeEnum.DICT_COMMON},
  {label: "创建时间", prop: "createDate", type:ColumnTypeEnum.COMMON},
  {label: "更新时间", prop: "updateDate", type:ColumnTypeEnum.COMMON},
])
const queryColumns = ref<Array<ColumnDefine>>([
  {label: "标题", prop: "title", query: true, queryType:QueryTypeEnum.INPUT,queryCondition: QueryConditionEnum.LIKE},
  {label: "内容", prop: "content", query: true, queryType:QueryTypeEnum.INPUT,queryCondition: QueryConditionEnum.LIKE},
  {label: "业务类型", prop: "businessType", query: true, queryType:QueryTypeEnum.INPUT,queryCondition: QueryConditionEnum.LIKE},
  {label: "业务编码", prop: "businessKey", query: true, queryType:QueryTypeEnum.INPUT,queryCondition: QueryConditionEnum.LIKE},
  {label: "消息类型", prop: "type", query: true, queryType:QueryTypeEnum.OPTION,queryCondition: QueryConditionEnum.IN},
  {label: "额外信息", prop: "info", query: true, queryType:QueryTypeEnum.INPUT,queryCondition: QueryConditionEnum.LIKE},
  // {label: "接收人", prop: "userId", query: true, queryType:QueryTypeEnum.INPUT,queryCondition: QueryConditionEnum.LIKE},
  {label: "创建时间", prop: "createDate", query: true, queryType:QueryTypeEnum.DATE_RANGE,queryCondition: QueryConditionEnum.BETWEEN_DATE},
  {label: "更新时间", prop: "updateDate", query: true, queryType:QueryTypeEnum.DATE_RANGE,queryCondition: QueryConditionEnum.BETWEEN_DATE},
]);
const commonTableRef = ref<CommonTableType>(null)
const dictMapping=ref({})
const detailFlag=ref(false)
const detailRow=ref({})

onMounted(async () => {
  let dictRes = await msgMainModelApi.getDictMapping()
  dictMapping.value = dictRes.data
  let userRes = await systemUserApi.getUserIdAndNickNameMapping()
  dictMapping.value['toUserIdList'] = userRes.data
  dictMapping.value['userId'] = userRes.data
})

/**
 * @param data 新增或修改数据
 * @param type 0 新增修改 1 查看
 */
const saveOrUpdate = (data:any,type:number) => {
  let columns: Array<DetailColumnDefine>
  let callBack:(result: InputDialogResult) => void
  if (type===0){
    columns = [
      {prop: 'title', label: '消息标题',type: QueryTypeEnum.AREA_INPUT},
      {prop: 'content', label: '消息内容',type: QueryTypeEnum.AREA_INPUT},
      {prop: 'businessType', label: '业务类型',type: QueryTypeEnum.INPUT},
      {prop: 'businessKey', label: '业务编码',type: QueryTypeEnum.INPUT},
      {prop: 'type', label: '消息类型',type: QueryTypeEnum.OPTION},
      {prop: 'toUserIdList', label: '接收人',type: QueryTypeEnum.MULTIPLE_OPTION},
    ]
    callBack = (result)=>{
      common.handleRequestApi(msgMainModelApi.saveOrUpdateMsg([result.data])).then(res=>{
        message(res.msg,{type:'success'})
        commonTableRef.value.getData()
        result.done()
      })
    }
  }else {
    columns = [
      {prop: 'title', label: '消息标题',type: QueryTypeEnum.COMMON},
      {prop: 'content', label: '消息内容',type: QueryTypeEnum.COMMON},
      {prop: 'businessType', label: '业务类型',type: QueryTypeEnum.COMMON},
      {prop: 'businessKey', label: '业务编码',type: QueryTypeEnum.COMMON},
      {prop: 'type', label: '消息类型',type: QueryTypeEnum.COMMON},
      {prop: 'toUserIdList', label: '接收人',type: QueryTypeEnum.COMMON},
      {prop: 'judgeFinish', label: '是否完结',type: QueryTypeEnum.COMMON},
      {prop: 'createDate', label: '创建时间',type: QueryTypeEnum.COMMON},
      {prop: 'updateDate', label: '更新时间',type: QueryTypeEnum.COMMON},
      {prop: 'info', label: '额外信息',type: QueryTypeEnum.AREA_INPUT}
    ]
    callBack = (result)=>{
      result.done()
    }
  }
  let defaultData = ref(data)
  common.openInputDialog(columns,dictMapping,defaultData,callBack)
}
tableFn.addFn = () => {
  saveOrUpdate({},0)
}
tableFn.rowDblclick=(row: any, column: any, event: Event)=>{
  saveOrUpdate(row,1)
}
tableFn.buttonAction={
  detail: (row:any)=>{
    if (common.isStrBlank(row.id)){
      return message('请保存数据后操作',{type:'error'})
    }
    detailFlag.value = true
    detailRow.value = row
  }
}
const refFn = () =>{
  let selectList = commonTableRef.value.selectTableData
  if (selectList.length===0){
    return message('请选择一条数据后操作',{type:'error'})
  }
  saveOrUpdate(selectList[0],0)
}
</script>

<template>
  <div class="parent">
    <common-table ref="commonTableRef" :query-in-header="false" class="commonTable" :api="msgMainModelApi" :columns="columns" :query-columns="queryColumns"
                  :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping">
      <template #button_end>
        <el-button  class="font-bold" size="small" type="primary" @click="refFn">引用</el-button>
      </template>
    </common-table>
  </div>
</template>

<style scoped lang="scss">
</style>