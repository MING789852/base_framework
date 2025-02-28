<template>
  <common-table ref="commonTableRef" :columns="columns" :queryColumns="columns" :dictList="dictList"
                :api="businessProjectItemApi" :tableFn="tableFn" :uploadFile="uploadFile" :query-template="queryTemplate" :query-in-header="false">
    <template #header_right>
      <div style="color: red;font-weight: bold;display: flex;align-items: center">
        <div>{{parentRow.name}}</div>
      </div>
    </template>
    <template #endSelectionColumn>
      <el-table-column   header-align="center" align="center" label="操作" width="100px">
        <template #default="scope">
          <el-button v-if="!isNullOrUnDef(scope.row.id)&&fieldTypeEnum[scope.row.fieldType] === fieldTypeEnum.option" size="small" @click="openProjectItemConfig(scope.row)">配置</el-button>
        </template>
      </el-table-column>
    </template>
    <template #button_start>
      <el-button v-if="judgeComponent" style="font-weight: bold" @click="componentBack">返回</el-button>
    </template>
    <template #dialog>
      <dialog-over :openOrCloseFlag="projectItemConfigFlag">
        <business-project-dict v-model:show = "projectItemConfigFlag" :parentRow="itemParentRow"  :judgeComponent="true"/>
      </dialog-over>
    </template>
  </common-table>
</template>

<script setup lang="tsx">
import {ref} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import businessProjectItemApi from "@/api/businessProjectItemApi";
import TableFnClass from "@/class/TableFnClass";
import UploadFileClass from "@/class/UploadFileClass";
import commonTable from "@/components/table/commonTable.vue";
import common from "@/utils/common";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import {message} from "@/utils/message";
import fieldTypeEnum from "@/components/business/enums/fieldTypeEnum";
import DialogOver from "@/components/DialogOver/dialogOver.vue";
import BusinessProjectDict from '@/components/business/project/itemDict.vue';

defineOptions({
  name: "business_project_item"
});
const props = defineProps({
  judgeComponent: {type: Boolean, required: true, default: false},
  parentRow: {type: Object, default: {} as Object},
  version: {type: String, default: 'default'},
  show: {type: Boolean},
  businessType: {type: String}
})
const emits = defineEmits(['update:show'])

let commonTableRef = ref<CommonTableType>(null)
const tableFn = new TableFnClass()
const uploadFile = new UploadFileClass([], new Map() as UploadMap, 'deletFileIdList', 'uploadFileKeyList')
const columns = ref<Array<ColumnDefine>>([
  {prop: "sequence", label: "序号",width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "name", label: "名称",width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "fieldKey", label: "字段编码",width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "fieldType", label: "字段类型",width: 200, type: ColumnTypeEnum.EDIT_OPTION, query: true, queryType: QueryTypeEnum.OPTION, queryCondition: QueryConditionEnum.IN},
  {prop: "canQuery", label: "是否可查询",width: 200, type: ColumnTypeEnum.EDIT_OPTION, query: true, queryType: QueryTypeEnum.OPTION, queryCondition: QueryConditionEnum.IN},
  {prop: "des", label: "描述",width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
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
    'projectId_$_eq': props.parentRow.id
  }
})

tableFn.initFn = () => {
  let temp = {}
  temp['fieldType'] = fieldTypeEnum
  temp['canQuery'] = {
    0: '否',
    1: '是',
  }
  dictList.value = temp
  commonTableRef.value.getData()
}

/**
 * 页面事件
 */
const projectItemConfigFlag = ref(false)
const itemParentRow=ref({});

const openProjectItemConfig = (row) => {
  itemParentRow.value = row
  projectItemConfigFlag.value = true
}

const fillData = (data: any[]) => {
  if (common.isEmptyArr(data)) {
    return []
  }
  data.forEach((value) => {
    value.version = props.version
    value.businessType = props.businessType
    value.projectId = props.parentRow.id
  })
}

tableFn.saveFn = () => {
  let list = cloneDeep(commonTableRef.value.tableData);
  fillData(list)
  commonTableRef.value.loading = true
  businessProjectItemApi.saveOrUpdateData(list).then((res: any) => {
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
