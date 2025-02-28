
<script setup lang="tsx">
import {ref} from "vue";
import commonTable from "@/components/table/commonTable.vue";
import dictApi from "@/api/dictApi";
import {message} from "@/utils/message";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import common from '@/utils/common'
import TableFnClass from "@/class/TableFnClass";
import DialogOver from "@/components/DialogOver/dialogOver.vue";

defineOptions({
  name: "dict"
});

const props = defineProps({
  parentDictGroupProp: {type: Object, default: {} as Object},
  parentDictProp: {type: Object, default: null},
  hasParentDict: {type: Boolean, required: true, default: false},
  judgeComponent: {type: Boolean, required: true, default: false}
})
const emits = defineEmits(['componentBack'])

let commonTableRef = ref(null)
const tableFn = new TableFnClass()
const columns = ref<Array<ColumnDefine>>([
  {prop: "dictCode", label: "字典编码", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "dictLabel", label: "字典描述", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);

const queryColumns = ref<Array<ColumnDefine>>([
  {prop: "dictCode_$_like", label: "字典编码", queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "dictLabel_$_like", label: "字典描述", queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);
const dictList= ref({})
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

tableFn.handleQuery = () => {
  commonTableRef.value.query.queryParams['groupKey_$_eq'] = props.parentDictGroupProp.groupKey
  if (props.hasParentDict){
    commonTableRef.value.query.queryParams['parentId_$_eq'] = props.parentDictProp.id
  }else {
    commonTableRef.value.query.queryParams['level_$_eq'] = 0
  }
}

tableFn.queryFn = () => {
  commonTableRef.value.getData()
}

tableFn.saveFn = () => {
  let data
  if (props.hasParentDict){
    data = {
      parentDictGroup: props.parentDictGroupProp,
      parentDict: props.parentDictProp,
      childrenList: commonTableRef.value.tableData
    }
  }else {
    data = {
      parentDictGroup: props.parentDictGroupProp,
      parentDict: null,
      childrenList: commonTableRef.value.tableData
    }
  }
  commonTableRef.value.loading = true
  dictApi.saveOrUpdateData(data).then((res:any) => {
    let code = res.code
    let msg = res.msg
    if (code === 200) {
      message('操作成功', { type: 'success' })
      commonTableRef.value.getData()
    } else {
      message(msg, { type: 'error' })
    }
  }).catch(res => {
    console.log(res)
  }).finally(() => {
    commonTableRef.value.loading = false
  })
}

const componentBack = () => {
  emits('componentBack')
}

const itemFlag = ref(false)
const itemClose = () => {
  itemFlag.value = false
}
const selectParentDict=ref(null)
const openChildrenDict = (row)=>{
  selectParentDict.value = row
  itemFlag.value = true
}
</script>

<template>
  <commonTable ref="commonTableRef"  :columns="columns"
               :queryColumns="queryColumns" :dictList="dictList"
               :api="dictApi" :tableFn="tableFn" :table-button="tableButton">
    <template #button_start>
      <el-button v-if="judgeComponent" size="small" type="primary" @click="componentBack">返回</el-button>
    </template>

    <template #header_right>
      <div style="font-weight: bold;display: flex;flex-direction: column;font-size: 12px;gap: 2px">
        <div style="color: red;">字典组: {{parentDictGroupProp.groupName + '【' + parentDictGroupProp.groupKey + '】' }}</div>
        <div style="color: #1f0c38" v-if="hasParentDict">父字典项: {{parentDictProp.dictLabel + '【' + parentDictProp.dictCode + '】' }}</div>
      </div>
    </template>

    <template #column>
      <el-table-column header-align="center" align="center" label="子项" width="120">
        <template #default="scope">
          <el-button v-if="!common.isStrBlank(scope.row.id)" @click="openChildrenDict(scope.row)" size="small">子项</el-button>
        </template>
      </el-table-column>
    </template>

    <template #dialog>
      <dialog-over :open-or-close-flag="itemFlag">
        <dict v-if="itemFlag"  :judgeComponent="true" :hasParentDict="true" :parentDictProp="selectParentDict" :parentDictGroupProp="parentDictGroupProp" @componentBack="itemClose"/>
      </dialog-over>
    </template>
  </commonTable>
</template>

<style scoped lang="scss">
</style>
