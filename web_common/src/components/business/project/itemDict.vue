<template>
  <common-table ref="commonTableRef" :columns="columns" :queryColumns="columns" :dictList="dictList"
                :api="businessDictApi" :tableFn="tableFn" :uploadFile="uploadFile" :query-template="queryTemplate" :query-in-header="false">
    <template #header_right>
      <div style="color: red;font-weight: bold;display: flex;align-items: center">
        <div>{{parentRow.name}}</div>
      </div>
    </template>
    <template #button_start>
      <el-button v-if="judgeComponent" style="font-weight: bold" @click="componentBack">返回</el-button>
    </template>
  </common-table>
</template>

<script setup lang="tsx">
import {ref} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import businessDictApi from "@/api/businessDictApi";
import TableFnClass from "@/class/TableFnClass";
import UploadFileClass from "@/class/UploadFileClass";
import commonTable from "@/components/table/commonTable.vue";
import common from "@/utils/common";
import {cloneDeep} from "@pureadmin/utils";
import {message} from "@/utils/message";

defineOptions({
  name: "business_project_dict"
});
const props = defineProps({
  judgeComponent: {type: Boolean, required: true, default: false},
  parentRow: {type: Object, default: {} as Object}
})
const emits = defineEmits(['update:show'])

let commonTableRef = ref<CommonTableType>(null)
const tableFn = new TableFnClass()
const uploadFile = new UploadFileClass([], new Map() as UploadMap, 'deletFileIdList', 'uploadFileKeyList')
const columns = ref<Array<ColumnDefine>>([
  {prop: "code", label: "字典编码",width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "label", label: "字典值",width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "des", label: "字典描述",width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "createUser", label: "创建人员", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "createDate", label: "创建时间", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.DATE, queryCondition: QueryConditionEnum.EQ},
  {prop: "updateUser", label: "更新人员", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "updateDate", label: "更新时间", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.DATE, queryCondition: QueryConditionEnum.EQ}
]);
const dictList = ref({})
const queryTemplate = ref<QueryDefine>({
  current: 1,
  size:10,
  total: 0,
  queryParams:{
    'projectItemId_$_eq': props.parentRow.id
  }
})

/**
 * 页面事件
 */
const fillData = (data: any[]) => {
  if (common.isEmptyArr(data)) {
    return []
  }
  data.forEach((value) => {
    value.projectItemId = props.parentRow.id
  })
}

tableFn.saveFn = () => {
  let list = cloneDeep(commonTableRef.value.tableData);
  fillData(list)
  commonTableRef.value.loading = true
  businessDictApi.saveOrUpdateData(list).then((res: any) => {
    if (res.code === 200) {
      message('操作成功', {type: 'success'})
      commonTableRef.value.getData()
    } else {
      message(res.msg, {type: 'error'})
    }
  }).catch(res => {
    message('请求失败->' + JSON.stringify(res), {type: 'error'})
  }).finally(() => {
    commonTableRef.value.loading = false
  })
}

const componentBack = () => {
  emits('update:show',false)
}
</script>

<style scoped lang="scss">
.parent {
  height: 100%;
}

.parent > :first-child {
  height: 100%;
}
</style>
