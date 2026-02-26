<script setup lang="ts">
import TableFnClass from "@/class/TableFnClass";
import common from "@/utils/common";
import {ref} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import commonTable from "@/components/table/commonTable.vue";
import UserSelect from "@/components/UserSelect/UserSelect.vue";
import mappingUserConfigApi from "@/api/mappingUserConfigApi";
import {message} from "@/utils/message";

const columns = ref<ColumnDefine[]>([
  {label: "名称", prop: "name", show: true, type: ColumnTypeEnum.COMMON},
  {label: "提醒用户", prop: "userList", show: true, type: 'userSelect'},
])
interface Props {
  configKey: string,
  nameList: Array<string>
}
const props = withDefaults(defineProps<Props>(), {})
const openFlag = defineModel<boolean>('openFlag',{required: false})
const commonTableRef = ref()
const tableFn = new TableFnClass()
const tableButton = ref<CommonTableButton>({
  queryFn: false,
  addFn: false,
  saveFn: true,
  deleteFn: false,
  refreshFn: true,
  initQueryFn: false,
  exportExcelFn: false
})
const dictMapping = ref({})
tableFn.initFn =  () => {
  commonTableRef.value.getData()
}
tableFn.getData = () => {
  common.handleRequestApi(mappingUserConfigApi.getConfig(props.configKey,props.nameList)).then(res=>{
    commonTableRef.value.tableData = res.data
  })
}
tableFn.saveFn = () => {
  common.handleRequestApi(mappingUserConfigApi.saveConfig(props.configKey,commonTableRef.value.tableData)).then(()=>{
    message('操作成功', {type: 'success'})
    openFlag.value = false
  })
}
</script>

<template>
  <common-table ref="commonTableRef" class="commonTable" :columns="columns"
                :show-page="false" :border="true"
                :queryInHeader="true" :show-selection="false"
                :table-fn="tableFn" :table-button="tableButton" :dict-list="dictMapping">
    <template #button_end>
      <slot name="button_end"/>
    </template>
    <template #columnItem="columnItem">
      <el-table-column v-if="columnItem.item.type === 'userSelect'" header-align="center" align="center" :label="columnItem.item.label" :width="columnItem.item.width">
        <template #default="scope">
          <UserSelect v-model:data-list="scope.row[columnItem.item.prop]" :multiple="true"/>
        </template>
      </el-table-column>
    </template>
    <template #dialog>
      <slot name="dialog"/>
    </template>
  </common-table>
</template>

<style scoped lang="scss">

</style>
