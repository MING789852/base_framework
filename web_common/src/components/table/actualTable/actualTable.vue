<script setup lang="ts">

import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import {CommonTableProp, useTableData} from "@/components/table/action";
import TableFnClass from "@/class/TableFnClass";
import FormFnClass from "@/class/FormFnClass";
import {reactive} from "vue";


const props = withDefaults(defineProps<CommonTableProp>(), {
  version: () => 'v1',
  saveOrUpdateType: () => 'onRow',
  tableHeightFitContent: () => false,
  //是否可编辑EditInput、EditOption、EditFile、EditDate
  // canOptEditComponentFn: undefined,
  //分页数量
  showPageSizeChange: () => false,
  pageSizeChangeList: () => [10, 15, 50, 100, 200],
  // 展示头部
  showHeader: () => true,
  // action: 操作表  report: 查询报表
  tableType: () => 'action',
  // 默认统计值
  summaryDefault: () => {
    return {0: '合计'}
  },
  // 忽略统字段
  ignoreSummaryPropList: () => [],
  // 行尾统计方法
  // showSummaryFn: undefined,
  // 展示行尾统计
  showSummary: () => false,
  // 展示行项选择
  showSelection: () => true,
  // 展示序号
  showSequence: () => false,
  // 高亮选中行
  highlightCurrentRow: () => false,
  // 展示分页
  showPage: () => true,
  // 斑马表格
  stripe: () => false,
  // 边框
  border: () => false,
  // 表格高度
  height: () => '100%',
  // 按钮是否展示
  tableButtonShow: () => true,
  tableButton: () => {
    return {
      queryFn: true,
      addFn: true,
      editFn: true,
      saveFn: true,
      deleteFn: true,
      refreshFn: true,
      initQueryFn: true,
      exportExcelFn: false,
      fullscreenFn: true
    }
  },
  // 查询条件在弹窗，默认true
  queryWithCondition: () => true,
  // 查询条件在标头  默认false
  queryInHeaderWithCondition: () => false,
  // 查询模板
  queryTemplate: () => {
    return {
      current: 1,
      size: 10,
      total: 0,
      queryParams: {}
    }
  },
  //查询列定义
  queryColumns: () => [],
  //查询组件是否显示在表头
  queryInHeader: () => true,
  //列定义
  columns: () => [],
  //字典表
  dictList: () => {
    return {}
  },
  //表格方法const tableFn = new TableFnClass()
  tableFn: () => new TableFnClass(),
  formFn: () => new FormFnClass(),
  cellClassNameMap: () => {
    return {}
  },
  rowClassNameMap: () => {
    return {}
  },
  rowColumnSpanMap: () => {
    return {}
  },
  rowColumnSpanAutoZero: () => false
})
const uploadMap = defineModel<Map<String, File>>('uploadMap', {required: false, default: new Map<String, File>()})
const deleteFileIdList = defineModel<string[]>('deleteFileIdList', {required: false, default: []})
const tableDataModel = defineModel<any[]>('tableData', {
  required: false, default: () => reactive([])
})
const selectTableDataModel = defineModel<any[]>('selectTableData', {
  required: false, default: () => reactive([])
})
const queryModel = defineModel<QueryDefine>('query', {
  required: false,
  default: () => {
    return reactive({
      current: 1,
      size: 10,
      total: 0,
      queryParams: {},
      orderByParams: {}
    })
  }
})
const {
  tableData,
  loading,
  dictMapping,
  columnsComputed,
  cellStyleFn,
  headerCellStyleFn,
  rowStyleFn,
  rowClassNameFn,
  cellClassNameFn,
  spanMethod,
  summaryMethod,
  rowDblclick,
  rowClick,
  cellDblclick,
  cellClick,
  sortChange,
  handleSelectionChange,
  cellMouseEnter
} = useTableData(props,{
  selectTableData: selectTableDataModel,
  tableData: tableDataModel,
  query: queryModel,
  uploadMap: uploadMap,
  deleteFileIdList: deleteFileIdList
})

defineSlots<{
  columnItem(props: { item: ColumnDefine}): any,
  preSelectionColumn():any,
  endSelectionColumn():any,
  column():any
}>()
</script>

<template>
  <el-table
      v-loading="loading"
      style="font-size: 12px"
      element-loading-text="加载中..."
      :data="tableData"
      :stripe="stripe"
      :border="border"
      :height="height"
      :show-summary="showSummary"
      :summary-method="summaryMethod"
      :cell-style="(data)=>cellStyleFn(data)"
      :header-cell-style="(data)=>headerCellStyleFn(data)"
      :row-style="(data)=>rowStyleFn(data)"
      :row-class-name="(data)=>rowClassNameFn(data)"
      :cell-class-name="(data)=>cellClassNameFn(data)"
      :span-method="spanMethod"
      :highlight-current-row="highlightCurrentRow"
      @rowDblclick="rowDblclick"
      @rowClick="rowClick"
      @cellMouseEnter="cellMouseEnter"
      @cellClick="cellClick"
      @cellDblclick="cellDblclick"
      @selection-change="handleSelectionChange"
      @sort-change="sortChange">
    <slot name="preSelectionColumn"/>
    <el-table-column v-if="showSelection" type="selection" width="50" header-align="center" align="center"  fixed/>
    <el-table-column v-if="showSequence" width="40" header-align="center" align="center" label="序" fixed>
      <template #default="scope">
        {{scope.$index + 1}}
      </template>
    </el-table-column>
    <slot name="endSelectionColumn"/>
    <template v-for="item in columnsComputed" :key="item.prop">
      <CommonTableColumn :table-fn="tableFn"
                         :item="item" :dict-list="dictMapping" v-model:delete-file-id-list="deleteFileIdList"
                         v-model:upload-map="uploadMap">
        <template #columnItem="columItem">
          <slot  name="columnItem" :item="columItem.item"/>
        </template>
      </CommonTableColumn>
    </template>
    <slot name="column"/>
  </el-table>
</template>

<style scoped lang="scss">

</style>