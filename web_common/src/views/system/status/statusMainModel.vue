<script setup lang="ts">
import TableFnClass from "@/class/TableFnClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import {ref} from "vue";
import statusMainModelApi from "@/api/statusMainModelApi"
import commonTable from "@/components/table/commonTable.vue";
import common from '@/utils/common'
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {message} from "@/utils/message";
import StatusDetailModel from "@/views/system/status/statusDetailModel.vue";

defineOptions({
  name: "status-mainModel"
});
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: false,
  addFn: true,
  saveFn: false,
  deleteFn: true,
  refreshFn: true,
  initQueryFn: false,
  exportExcelFn: false
})
const columns=ref<ColumnDefine[]>([
  {label: "编码", prop: "code", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "名称", prop: "name", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "创建日期", prop: "createDate", type: ColumnTypeEnum.COMMON},
  {label: "创建人", prop: "createUser", type: ColumnTypeEnum.COMMON},
  {label: "修改日期", prop: "updateDate", type: ColumnTypeEnum.COMMON},
  {label: "修改人", prop: "updateUser", type: ColumnTypeEnum.COMMON},
  {label: "明细", prop: "detail", type:ColumnTypeEnum.BUTTON}
])
const commonTableRef = ref<CommonTableType>(null)
const dictMapping=ref({})
const detailFlag=ref(false)
const detailRow=ref({})
const saveOrUpdate = (data) => {
  let columns: Array<DetailColumnDefine> = [
    {prop: 'code', label: '编码',type: QueryTypeEnum.INPUT},
    {prop: 'name', label: '名称',type: QueryTypeEnum.INPUT}
  ]
  let defaultData = ref(data)
  let params:OpenInputDialogDefine = {
    columns: columns,
    defaultValue: defaultData,
    callBack: (result) => {
      common.handleRequestApi(statusMainModelApi.saveOrUpdateData([result.data])).then(res=>{
        message(res.msg,{type:'success'})
        commonTableRef.value.getData()
        result.done()
      })
    }
  }
  common.openInputDialog(params)
}
tableFn.addFn = () => {
  saveOrUpdate({})
}
tableFn.rowDblclick=(row: any, column: any, event: Event)=>{
  saveOrUpdate(row)
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
</script>

<template>
  <div class="parent">
    <common-table ref="commonTableRef" class="commonTable" :api="statusMainModelApi" :columns="columns"
                  :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping">
      <template #dialog>
        <el-dialog v-if="detailFlag"  v-model="detailFlag" destroy-on-close append-to-body fullscreen>
          <status-detail-model :parent-row="detailRow"/>
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