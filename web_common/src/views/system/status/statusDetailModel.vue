<script setup lang="ts">
import TableFnClass from "@/class/TableFnClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import {onMounted, ref} from "vue";
import statusDetailModelApi from "@/api/statusDetailModelApi"
import commonTable from "@/components/table/commonTable.vue";
import common from '@/utils/common'
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {message} from "@/utils/message";

defineOptions({
  name: "status-detailModel"
});
const props = defineProps({
  parentRow: {type:Object as PropType<any>,required:true}
})

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
  {label: "序号", prop: "sequence", type: ColumnTypeEnum.COMMON},
  {label: "状态编码", prop: "code", type: ColumnTypeEnum.COMMON},
  {label: "状态名称", prop: "name", type: ColumnTypeEnum.COMMON},
  {label: "表单模型", prop: "formMainModelId", type: ColumnTypeEnum.DICT_COMMON},
  {label: "创建日期", prop: "createDate", type: ColumnTypeEnum.COMMON},
  {label: "创建人", prop: "createUser", type: ColumnTypeEnum.COMMON},
  {label: "修改日期", prop: "updateDate", type: ColumnTypeEnum.COMMON},
  {label: "修改人", prop: "updateUser", type: ColumnTypeEnum.COMMON}
])
const commonTableRef = ref<CommonTableType>(null)
const dictMapping=ref({})
const queryTemplate=ref({
  current: 1,
  size:20,
  total: 0,
  queryParams:{
    statusMainModelId_$_eq:props.parentRow.id
  }
})
onMounted(()=>{
  common.handleRequestApi(statusDetailModelApi.getDictMapping()).then(res=>{
    dictMapping.value = res.data
  })
})

const saveOrUpdate = (data) => {
  let columns: Array<DetailColumnDefine> = [
    {label: "序号", prop: "sequence", type: QueryTypeEnum.INPUT},
    {label: "状态编码", prop: "code", type: QueryTypeEnum.INPUT},
    {label: "状态名称", prop: "name", type: QueryTypeEnum.INPUT},
    {label: "表单模型", prop: "formMainModelId",type: QueryTypeEnum.OPTION}
  ]
  let defaultData = ref(data)
  common.openInputDialog(columns,dictMapping,defaultData,(result)=>{
    let data = {...result.data,...{statusMainModelId:props.parentRow.id}}
    common.handleRequestApi(statusDetailModelApi.saveOrUpdateData([data])).then(res=>{
      message(res.msg,{type:'success'})
      commonTableRef.value.getData()
      result.done()
    })
  })
}
tableFn.addFn = () => {
  saveOrUpdate({})
}
tableFn.rowDblclick=(row: any, column: any, event: Event)=>{
  saveOrUpdate(row)
}
</script>

<template>
  <commonTable ref="commonTableRef" class="commonTable" :api="statusDetailModelApi" :columns="columns" height="75vh"
                :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping" :queryTemplate="queryTemplate">
    <template #header_right>
      <div style="font-weight: bold;display: flex;flex-direction: column;font-size: 12px;gap: 2px">
        <div style="color: red;">状态模型: {{parentRow.name + '【' + parentRow.code + '】' }}</div>
      </div>
    </template>
  </commonTable>
</template>

<style scoped lang="scss">

</style>