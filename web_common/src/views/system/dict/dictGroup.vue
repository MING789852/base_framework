
<script setup lang="tsx">
import {ref} from "vue";
import commonTable from "@/components/table/commonTable.vue";
import {message} from "@/utils/message";
import {isNullOrUnDef} from "@pureadmin/utils";
import dictGroupApi from "@/api/dictGroupApi";
import dict from '@/views/system/dict/dict.vue'
import DialogOver from "@/components/DialogOver/dialogOver.vue";
import TableFnClass from "@/class/TableFnClass";
import UploadFileClass from "@/class/UploadFileClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";

defineOptions({
  name: "system-dictGroup"
});

let commonTableRef = ref(null)
const tableFn = new TableFnClass()
const uploadFile = new UploadFileClass([],new Map() as UploadMap,'deletFileIdList','uploadFileKeyList')
const columns = ref<Array<ColumnDefine>>([
  {prop: "groupKey", label: "字典组KEY", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "groupName", label: "字典组名称", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);
const queryColumns = ref<Array<ColumnDefine>>([
    {prop: "groupKey_$_like", label: "字典组KEY",  queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
    {prop: "groupName_$_like", label: "字典组名称",  queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);
const dictList= ref({})
tableFn.queryFn = () => {
  commonTableRef.value.getData()
}
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: true,
  saveFn: true,
  deleteFn: true,
  refreshFn: true,
  initQueryFn: true,
  exportExcelFn: false
})


/**
 * 页面事件
 */
const parentRow = ref({})
const itemFlag = ref(false)
const itemOpen = (row) => {
  parentRow.value = row
  itemFlag.value = true
}
const itemClose = () => {
  itemFlag.value = false
}

const importExcel = () => {
  message('功能暂未实现')
  // let inputObj = document.createElement('input')
  // inputObj.type = 'file'
  // // 多文件选择
  // inputObj.multiple = false
  // inputObj.accept = '.xlsx'
  // inputObj.onchange = (e:any) => {
  //   let uploadFileList:Array<File> = e.target.files
  //   if (!isNullOrUnDef(uploadFileList)&&uploadFileList.length===1){
  //     const formData = new FormData();
  //     formData.set('file',uploadFileList[0])
  //     commonTableRef.value.loading = true
  //     dictGroupApi.importExcel(formData).then((res:any) => {
  //       let code = res.code
  //       let msg = res.msg
  //       if (code === 200) {
  //         message('操作成功', { type: 'success' })
  //         commonTableRef.value.getData()
  //       } else {
  //         message(msg, { type: 'error' })
  //       }
  //     }).catch(res => {
  //       console.log(res)
  //     }).finally(() => {
  //       commonTableRef.value.loading = false
  //     })
  //   }
  // }
  // inputObj.click()
  // inputObj.remove()
}
</script>

<template>
  <div class="parent">
    <commonTable ref="commonTableRef"  :columns="columns" :queryColumns="queryColumns"
                 :dictList="dictList" :api="dictGroupApi" :tableFn="tableFn" :uploadFile="uploadFile" :table-button="tableButton">
      <template #column>
        <el-table-column label="详情"   header-align="center" align="center">
          <template #default="scope">
            <el-button v-if="isNullOrUnDef(scope.row.add)" size="small" @click="itemOpen(scope.row)">明细</el-button>
          </template>
        </el-table-column>
      </template>

      <template #dialog>
        <dialog-over :open-or-close-flag="itemFlag">
          <dict v-if="itemFlag"  :judgeComponent="true" :hasParentDict="false"  :parentDictGroupProp="parentRow"  @componentBack="itemClose"/>
        </dialog-over>
      </template>
    </commonTable>
  </div>
</template>

<style scoped lang="scss">
</style>
