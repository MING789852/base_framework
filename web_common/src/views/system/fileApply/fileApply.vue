<script setup lang="ts">
import fileApplyApi from "@/api/fileApplyApi";
import common from "@/utils/common";
import commonTable from "@/components/table/commonTable.vue";
import {ref} from "vue";
import TableFnClass from "@/class/TableFnClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import FileApplyDetail from "@/views/system/fileApply/fileApplyDetail.vue";

defineOptions({
  name: "apply-file"
});
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
  deleteFn: common.authActionCode('delete'),
  refreshFn: true,
  initQueryFn: false,
  exportExcelFn: false
})
const columns=ref<ColumnDefine[]>([
  // {label: "关联类型",prop: "refType", type: ColumnTypeEnum.COMMON,width:'100'},
  {label: "状态", prop: "status", type: ColumnTypeEnum.DICT_COMMON,width:'200'},
  {label: "有效时间", prop: "effectiveTime", type: ColumnTypeEnum.COMMON,width:'200'},
  // {label: "申请文件", prop: "fileName", type: ColumnTypeEnum.COMMON},
  {label: "申请用户",prop: "userName", type: ColumnTypeEnum.COMMON,width:'100'},
  {label: "申请时间",prop: "applyTime", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "申请备注",prop: "applyRemark", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "审批人员", prop: "operateUser", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "审批时间", prop: "operateTime", type: ColumnTypeEnum.COMMON,width:'150'}
])
const commonTableRef = ref<CommonTableType>()
const dictMapping=ref({
  status: {
    0: '审批中',
    1: '同意',
    2: '拒绝'
  }
})
const detailFlag=ref(false)
const detailId=ref('')
const detailType=ref('')
tableFn.rowDblclick = (row: any, column: any, event: Event)=>{
  detailId.value = row.id
  detailType.value = 'view'
  detailFlag.value = true
}
const finishDetail = () => {
  detailFlag.value = false
  commonTableRef.value.getData()
}
</script>

<template>
  <common-table ref="commonTableRef" class="commonTable" :api="fileApplyApi" :columns="columns" height="75vh"
                :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping" :queryTemplate="queryTemplate">
    <template #dialog>
      <el-dialog v-model="detailFlag" destroy-on-close append-to-body fullscreen>
        <file-apply-detail :id="detailId" :detail-type="detailType" @finish="finishDetail"/>
      </el-dialog>
    </template>
  </common-table>
</template>

<style scoped lang="scss">

</style>
