<script setup lang="ts">
import {onMounted, ref, useTemplateRef} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import common from "@/utils/common";
import mappingStringConfigApi from "@/api/mappingStringConfigApi";
import {message} from "@/utils/message";
import CommonTable from "@/components/table/commonTable.vue";
import TableFnClass from "@/class/TableFnClass";

interface Prop {
  type?: 'fixedNameList'|'freeNameList',
  configKey: string,
  nameList?: Array<string>
}
const props = withDefaults(defineProps<Prop>(), {
  type:()=>'fixedNameList'
})
const columns = ref<ColumnDefine[]>([
  {label: "名称", prop: "name", type: props.type === 'freeNameList' ?ColumnTypeEnum.INPUT: ColumnTypeEnum.COMMON },
  {label: "值", prop: "value", type: ColumnTypeEnum.INPUT},
])
const commonTableRef = useTemplateRef<InstanceType<typeof CommonTable>>('commonTableRef')
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: false,
  addFn: props.type === 'freeNameList',
  saveFn: true,
  deleteFn: props.type === 'freeNameList',
  refreshFn: true,
  initQueryFn: false,
  exportExcelFn: false
})
const openFlag = defineModel<boolean>('openFlag',{required: true})
onMounted(()=>{
  common.handleRequestApi(mappingStringConfigApi.getConfig(props.configKey,props.nameList)).then(res=>{
    commonTableRef.value.tableData = res.data
  })
})
tableFn.saveFn=()=>{
  common.handleRequestApi(mappingStringConfigApi.saveConfig(props.configKey,commonTableRef.value.tableData)).then(()=>{
    message('操作成功', {type: 'success'})
    openFlag.value = false
  })
}

tableFn.deleteFn = () => {
  //前端界面删除
  for (let i = 0; i < commonTableRef.value.selectTableData.length; i++) {
    //删除数据
    common.removeListItem(commonTableRef.value.tableData,commonTableRef.value.selectTableData[i])
  }
}

</script>

<template>
  <common-table ref="commonTableRef" class="commonTable" :columns="columns"
                :show-page="false" :border="true"
                :queryInHeader="true"
                :show-selection="type === 'freeNameList'"
                :table-fn="tableFn" :table-button="tableButton"/>
</template>

<style scoped lang="scss">

</style>
