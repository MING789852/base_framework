<script setup lang="ts">
import TableFnClass from "@/class/TableFnClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import {onMounted, ref} from "vue";
import formDetailModelApi from "@/api/formDetailModelApi"
import commonTable from "@/components/table/commonTable.vue";
import common from '@/utils/common'
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {message} from "@/utils/message";
import DyFormColumnTypeEnum from "@/enums/DyFormColumnTypeEnum";
import {cloneDeep} from "@pureadmin/utils";

defineOptions({
  name: "form-detailModel"
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
  {label: "序号", prop: "sequence", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "配置", prop: "config", type: ColumnTypeEnum.BUTTON,width:'150'},
  {label: "字段编码", prop: "fieldCode", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "字段名称", prop: "fieldName", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "字段类型", prop: "fieldType", type: ColumnTypeEnum.DICT_COMMON,width:'150'},
  {label: "填写提示", prop: "placeholder", type: ColumnTypeEnum.COMMON,width:'150'},
  {label: "是否必填", prop: "required", type: ColumnTypeEnum.DICT_COMMON,width:'150'},
  {label: "是否展示", prop: "judgeShow", type: ColumnTypeEnum.DICT_COMMON,width:'150'},
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
    formMainModelId_$_eq:props.parentRow.id
  }
})
onMounted(()=>{
  common.handleRequestApi(formDetailModelApi.getDictMapping()).then(res=>{
    dictMapping.value = res.data
  })
})
const saveOrUpdate = (data) => {
  let columns: Array<DetailColumnDefine> = [
    {label: "序号", prop: "sequence", type: QueryTypeEnum.INPUT},
    {label: "字段编码", prop: "fieldCode", type: QueryTypeEnum.INPUT},
    {label: "字段名称", prop: "fieldName", type: QueryTypeEnum.INPUT},
    {label: "字段类型", prop: "fieldType", type: QueryTypeEnum.OPTION},
    {label: "填写提示", prop: "placeholder", type: QueryTypeEnum.INPUT},
    {label: "是否必填", prop: "required", type: QueryTypeEnum.OPTION},
    {label: "是否展示", prop: "judgeShow", type: QueryTypeEnum.OPTION}
  ]
  let defaultData = ref(data)
  common.openInputDialog(columns,dictMapping,defaultData,(result)=>{
    let data = {...result.data,...{formMainModelId:props.parentRow.id}}
    common.handleRequestApi(formDetailModelApi.saveOrUpdateData([data])).then(res=>{
      message(res.msg,{type:'success'})
      commonTableRef.value.getData()
      result.done()
    })
  })
}
tableFn.addFn = () => {
  saveOrUpdate({
    required: "false",
    judgeShow: "true"
  })
}
tableFn.rowDblclick=(row: any, column: any, event: Event)=>{
  row['required'] = row['required'] + ''
  row['judgeShow'] = row['judgeShow'] + ''
  saveOrUpdate(row)
}
tableFn.buttonAction = {
  config:(row:any)=>{
    if (common.isStrBlank(row.id)){
      return message('请保存数据后操作',{type:'error'})
    }
    if (![DyFormColumnTypeEnum.TREE_SELECT,DyFormColumnTypeEnum.COMMON_SELECT].includes(row.fieldType)){
      return message('仅树状选择和普通选择可配置',{type:'error'})
    }
    let columns: Array<DetailColumnDefine> = [
      {label: "字典类型", prop: "other1", type: QueryTypeEnum.OPTION},
      {label: "字典组编码", prop: "other2",placeholder:'字典类型非部门必填', type: QueryTypeEnum.OPTION}
    ]
    let defaultData = ref({
      other1:row.other1,
      other2:row.other2
    })
    let cloneRow = cloneDeep(row)
    common.openInputDialog(columns,dictMapping,defaultData,(result)=>{
      row.other1 = result.data.other1
      row.other2 = result.data.other2
      common.handleRequestApi(formDetailModelApi.saveOrUpdateData([row])).then(res=>{
        message(res.msg,{type:'success'})
        commonTableRef.value.getData()
        result.done()
      })
    })
  }
}
</script>

<template>
  <commonTable ref="commonTableRef" class="commonTable" :api="formDetailModelApi" :columns="columns" height="75vh"
                :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping" :queryTemplate="queryTemplate">
    <template #header_right>
      <div style="font-weight: bold;display: flex;flex-direction: column;font-size: 12px;gap: 2px">
        <div style="color: red;">表单模型: {{parentRow.name + '【' + parentRow.code + '】' }}</div>
      </div>
    </template>
    <template #dialog>
      <div/>
    </template>
  </commonTable>
</template>

<style scoped lang="scss">

</style>