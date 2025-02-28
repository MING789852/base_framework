<template>
  <el-dialog v-model="dialogIsOpen" :fullscreen="true" append-to-body :show-close="false">
    <commonTable ref="commonTableRef" :columns="columns"  :dictList="dictList"
                 :table-button-show="false" :table-fn="tableFn" height="75vh"
                 :query-in-header="false">
      <template #header_left>
        <div class="flex flex-row gap-3 content-center">
          <el-button class="font-bold" size="small" type="primary" @click="queryFn">查询</el-button>
          <el-button class="font-bold" size="small" type="primary" @click="addFn">选中</el-button>
          <el-button class="font-bold" size="small" type="primary" @click="refreshFn">刷新</el-button>
          <el-button class="font-bold" size="small" type="primary" @click="closeFn">关闭</el-button>
        </div>
      </template>
    </commonTable>
  </el-dialog>
</template>

<script setup lang="ts">
import {ref} from "vue";
import {http} from "@/utils/http";
import {message} from "@/utils/message";
import {addDialog, closeDialog} from "@/components/ReDialog/index";
import SearchForm from "@/components/searchForm/searchForm.vue";
import commonTable from "@/components/table/commonTable.vue";
import common from "@/utils/common"
import TableFnClass from "@/class/TableFnClass";

const dialogIsOpen = ref(true)
const props = defineProps({
  columns: {type: Array<ColumnDefine>, required: true,default: []},
  queryUrl: String,
  dictList: {type: Object, default: {} as Object}
})

const emits=defineEmits(['close','choose'])
const commonTableRef = ref<CommonTableType>(null)
const dictList = ref(props.dictList)
const tableFn = new TableFnClass()

tableFn.initFn = () => {
  commonTableRef.value.getData()
}
tableFn.getData = () => {
  common.handleRequestApi(http.post(props.queryUrl,{data:commonTableRef.value.query})).then((res) => {
    common.handleResponsePage(commonTableRef.value,res.data)
  })
}

const queryFn = () => {
  let width
  if (common.isMobile()){
    width="90%"
  }else {
    width="40%"
  }
  console.log(width)
  addDialog({
    width: width,
    title: "查询",
    props: {
      columns: props.columns,
      query: commonTableRef.value.query,
      dictList: props.dictList
    },
    footerButtons: [
      {
        label: "取消",
        text: true,
        bg: true,
        type: "info",
        btnClick: ({dialog: {options, index}, button}) => {
          closeDialog(options, index, {command: 'cancel'});
        }
      },
      {
        label: "清空",
        text: true,
        bg: true,
        type: "info",
        btnClick: ({dialog: {options, index}, button}) => {
          commonTableRef.value.query.queryParams = {}
        }
      },
      {
        label: "确认",
        text: true,
        bg: true,
        type: "primary",
        btnClick: ({dialog: {options, index}, button}) => {
          closeDialog(options, index, {command: 'sure'});
        }
      }
    ],
    contentRenderer: () => SearchForm,
    closeCallBack: ({ options, args }) => {
      commonTableRef.value.query = options.props.query as QueryDefine
      if (args?.command === "cancel") {
        // 点击取消按钮
      } else if (args?.command === "sure") {
        // 查询重置第一页
        commonTableRef.value.query.current = 1
        commonTableRef.value.getData()
      } else {
        // 关闭查询窗口
      }
    }
  });
}

const addFn = () => {
  if (commonTableRef.value.selectTableData.length === 0){
    return message("请选中数据后操作",{type:"warning"})
  }
  emits('choose',commonTableRef.value.selectTableData)
}

const refreshFn = () => {
  commonTableRef.value.getData()
}

const closeFn = () => {
  emits('close',false)
}

</script>

<style scoped lang="scss">

</style>
