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
import {cloneDeep} from "@pureadmin/utils";

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
  {label: "接收群组", prop: "groupId", type: ColumnTypeEnum.COMMON},
  {label: "群组@所有人", prop: "groupAtAll", type: ColumnTypeEnum.DICT_COMMON},
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
  {label: "接收群组", prop: "groupId", query: true, queryType:QueryTypeEnum.INPUT,queryCondition: QueryConditionEnum.LIKE},
  {label: "群组@所有人", prop: "groupAtAll", query: true, queryType:QueryTypeEnum.OPTION,queryCondition: QueryConditionEnum.IN},
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
  dictMapping.value['groupAtAll'] = {
    0:'否',
    1:'是'
  }
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
      {prop: 'jumpUrl', label: '跳转链接',placeholder:'支持点击消息跳转链接时，可选',type: QueryTypeEnum.INPUT},
      {prop: 'groupId', label: '接收群组',placeholder:'消息类型为群组时,必填',type: QueryTypeEnum.INPUT},
      {prop: 'groupAtAll', label: '群组@所有人',placeholder:'消息类型为群组时,必填',type: QueryTypeEnum.OPTION},
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
      {prop: 'content', label: '消息内容',type: QueryTypeEnum.COMMON_OVER},
      {prop: 'businessType', label: '业务类型',type: QueryTypeEnum.COMMON},
      {prop: 'businessKey', label: '业务编码',type: QueryTypeEnum.COMMON},
      {prop: 'type', label: '消息类型',type: QueryTypeEnum.COMMON},
      {prop: 'toUserIdList', label: '接收人',type: QueryTypeEnum.COMMON},
      {prop: 'judgeFinish', label: '是否完结',type: QueryTypeEnum.COMMON},
      {prop: 'createDate', label: '创建时间',type: QueryTypeEnum.COMMON},
      {prop: 'updateDate', label: '更新时间',type: QueryTypeEnum.COMMON},
      {prop: 'groupId', label: '接收群组',type: QueryTypeEnum.COMMON},
      {prop: 'groupAtAll', label: '群组@所有人',type: QueryTypeEnum.DICT_COMMON},
      {prop: 'info', label: '额外信息',type: QueryTypeEnum.COMMON_OVER}
    ]
    callBack = (result)=>{
      result.done()
    }
  }
  let defaultData = ref(data)
  let params:OpenInputDialogDefine = {
    width: '40%',
    columns:columns,
    dictMapping:dictMapping,
    defaultValue:defaultData,
    callBack:callBack
  }
  common.openInputDialog(params)
}
tableFn.addFn = () => {
  saveOrUpdate({},0)
}
tableFn.rowDblclick=(row: any, column: any, event: Event)=>{
  saveOrUpdate(row,1)
}
tableFn.buttonAction={
  actionFn(row: any, propName: string, index: number): void {
    if (propName === 'detail'){
      if (common.isStrBlank(row.id)){
        message('请保存数据后操作',{type:'error'})
        return
      }
      detailFlag.value = true
      detailRow.value = row
    }
  }
}
const refFn = () =>{
  let selectList = commonTableRef.value.selectTableData
  if (selectList.length===0){
    return message('请选择一条数据后操作',{type:'error'})
  }
  saveOrUpdate(cloneDeep(selectList[0]),0)
}
const finishMsg = () =>{
  let selectList = commonTableRef.value.selectTableData
  if (selectList.length===0){
    return message('请选择数据后操作',{type:'error'})
  }
  common.handleRequestApi(msgMainModelApi.finishMsg(selectList)).then(res=>{
    message('操作成功',{type:'success'})
    commonTableRef.value.getData()
  })
}
</script>

<template>
  <common-table ref="commonTableRef" :query-in-header="false" class="commonTable" :api="msgMainModelApi" :columns="columns" :query-columns="queryColumns"
                :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping">
    <template #button_end>
      <el-button  class="font-bold" size="small" type="primary" @click="finishMsg">完结消息</el-button>
      <el-button  class="font-bold" size="small" type="primary" @click="refFn">引用</el-button>
    </template>
  </common-table>
</template>

<style scoped lang="scss">
</style>