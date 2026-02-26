<template>
  <div>
    <el-dialog v-if="dialogIsOpen"  v-model="dialogIsOpen" :fullscreen="true" append-to-body :show-close="true" @close="closeFn">
      <commonTable ref="commonTableRef"
                   :columns="columns"  :dictList="dictList" :query-columns="columns"
                   :table-fn="tableFn" :table-button="tableButton"
                   :query-in-header-with-condition="true"
                   :queryInHeader="queryInHeader">
        <template #button_end>
          <el-button class="font-bold" size="small" type="primary" @click="selectFn">选中</el-button>
          <el-button class="font-bold" size="small" type="primary" @click="closeFn">关闭</el-button>
        </template>
      </commonTable>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {ref} from "vue";
import {http} from "@/utils/http";
import {message} from "@/utils/message";
import commonTable from "@/components/table/commonTable.vue";
import common from "@/utils/common"
import TableFnClass from "@/class/TableFnClass";
import {isNullOrUnDef} from "@pureadmin/utils";
const dialogIsOpen = ref(true)
interface Props {
  columns: Array<ColumnDefine>
  queryUrl:string,
  dictList?: Object,
  queryInHeader?:boolean,
  preInitDataFn?:(callback:()=>void)=>void
}
const props = withDefaults(defineProps<Props>(),{
  columns: ()=> [],
  queryUrl: ()=> '',
  dictList: ()=> {
    return {}
  },
  queryInHeader: ()=> false
})
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  addFn: false,
  saveFn: false,
  deleteFn: false,
  refreshFn: true,
  initQueryFn: true,
  exportExcelFn: false
})

const emits=defineEmits(['close','choose'])
const commonTableRef = ref<CommonTableType>(null)
const dictList = ref(props.dictList)
const tableFn = new TableFnClass()
tableFn.initFn = () => {
  if (!isNullOrUnDef(props.preInitDataFn)){
    props.preInitDataFn(()=>{
      commonTableRef.value.getData()
    })
  }else {
    commonTableRef.value.getData()
  }
}
tableFn.getData = () => {
  common.handleRequestApi(http.post(props.queryUrl,{data:commonTableRef.value.query})).then((res) => {
    common.handleResponsePage(commonTableRef.value,res.data)
  })
}
const selectFn = () => {
  if (commonTableRef.value.selectTableData.length === 0){
    return message("请选中数据后操作",{type:"warning"})
  }
  emits('choose',commonTableRef.value.selectTableData)
}
const closeFn = () => {
  emits('close',false)
}

</script>

<style scoped lang="scss">

</style>
