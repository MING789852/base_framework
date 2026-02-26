<script setup lang="tsx">
import {Column} from "element-plus/es/components/table-v2/src/types";
import TableFnClass from "@/class/TableFnClass";
import FormFnClass from "@/class/FormFnClass";
import {CommonTableProp, useTableData} from "@/components/table/action";
import {computed, reactive, ref, watch} from "vue";
import common from "@/utils/common";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import VirtualizedMergeRow from "@/components/table/virtualizedTable/virtualizedMergeRow.vue";
import {SortBy,SortState,TableV2SortOrder} from 'element-plus'

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
const tableDataModel = defineModel<any []>('tableData', {
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
  rowClassNameFn,
  summaryMethod
} = useTableData(props, {
  selectTableData: selectTableDataModel,
  tableData: tableDataModel,
  query: queryModel,
  uploadMap: uploadMap,
  deleteFileIdList: deleteFileIdList
})

//行尾统计
const summaryList = computed(() => {
  return summaryMethod({columns: props.columns, data: tableData.value})
})
//把level类型都拍成一级
let indexColumns = computed(() => {
  return common.columnsToIndexColumn(props.columns)
})
const convertV2Columns = ref([])
const getColumnWidth = (colIndex) => {
  if (props.tableWidth === 0) {
    return 0
  }
  let fixedWidth = 0
  let fixedWidthCount = 0
  indexColumns.value.forEach((value) => {
    if (!isNullOrUnDef(value.width)) {
      fixedWidth += parseInt(value.width)
      fixedWidthCount = fixedWidthCount + 1
    }
  })
  let remainWidth = (props.tableWidth - fixedWidth)
  let remainCount = indexColumns.value.length - fixedWidthCount
  let defaultColumWidth = Math.max(remainCount === 0 ? 0 : remainWidth / remainCount,100)
  let colWidth = indexColumns.value[colIndex].width
  return parseInt(colWidth ?? defaultColumWidth)
}
const getRowHeight = () => {
  return props.tableType === 'action' ? 50 : 25
}
const rowHeight = getRowHeight()
watch(() => [props.tableHeight, props.tableWidth,props.columns], () => {
  if (props.tableHeight !== 0 && props.tableWidth !== 0) {
    convertV2Columns.value = []
    indexColumns.value.forEach((value, index) => {
      let columnData = cloneDeep(value)
      columnData.width=getColumnWidth(index)
      columnData.minWidth=parseInt(value.minWidth ?? 0)
      columnData.align = value.align ?? 'center'
      columnData.key = value.prop
      columnData.dataKey = value.prop
      columnData.title = value.label
      columnData.sortable = value.sortable
      convertV2Columns.value.push(columnData)
    })
  }
})
const rowClassNameV2Fn = (data: { row: any, rowIndex: number, columns?: Column[] }) => {
  let rowIndex = data.rowIndex
  let defaultClassName = ''
  if (props.stripe) {
    if (rowIndex % 2 === 0) {
      defaultClassName = 'stripe_v2'
    }
  }
  return defaultClassName + ' ' + rowClassNameFn(data)
}
const parentClass = () => {
  return 'parent'
}

const mergeZeroMap = ref({
  rowSpanMap:{},
  colSpanMap:{}
})


const sortState = ref<SortState>({
  '': TableV2SortOrder.ASC,
})
const onSort = (sortBy: SortBy) => {
  let orderBy = isNullOrUnDef(sortBy.order)?TableV2SortOrder.ASC:sortBy.order
  sortState.value = {
    [sortBy.key]: orderBy,
  }
  let data = {
    column: sortBy['column'],
    prop: sortBy.key.toString(),
    order: (orderBy===TableV2SortOrder.ASC?'ascending':'descending') as 'ascending' | 'descending'
  }
  //ascending
  //descending
  props.tableFn.sortChange?.(data)
}
</script>

<template>
  <el-table-v2
      v-model:sort-state="sortState"
      :class="parentClass()"
      :columns="convertV2Columns"
      :data="tableData"
      :width="tableWidth"
      :height="tableHeight"
      :row-class="(data)=>rowClassNameV2Fn({row:data.rowData,rowIndex:data.rowIndex,columns:data.columns})"
      :row-height="rowHeight"
      :header-height="rowHeight"
      @column-sort="onSort"
      fixed>
    <template #footer v-if="showSummary">
      <div :class="`w-full min-h-[${rowHeight}px] bg-[#409eff] flex flex-row`">
        <div
            v-for="(col, index) in convertV2Columns"
            :key="index"
            class="footer-cell"
            :style="{
              width: col.width + 'px',
              textAlign: col.align || 'left'
            }"
        >
          {{ summaryList[index] }}
        </div>
      </div>
    </template>
    <template #row="rowProps">
      <VirtualizedMergeRow
          :tableHeight="tableHeight"
          :tableWidth="tableWidth"
          :columns="convertV2Columns"
          :row-index="rowProps.rowIndex"
          :row-data="rowProps.rowData"
          :dict-list="dictList"
          :row-height="rowHeight"
          :table-fn="tableFn"
          :border="border"
          :row-column-span-map="rowColumnSpanMap"
          v-model:upload-map="uploadMap"
          v-model:delete-file-id-list="deleteFileIdList"
          v-model:mergeZeroMap="mergeZeroMap"
      />
    </template>
  </el-table-v2>
</template>

<style scoped lang="scss">
.parent {
  :deep(.stripe_v2) {
    background-color: #f5f7fa;
  }
  :deep(.el-table-v2__row) {
    border-bottom: none;
  }
  .footer-cell {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 8px;
    border-right: 1px solid #e8e8e8;
    border-left: 1px solid #e8e8e8;
    font-size: 14px;
    color: white;
    font-weight: 600;

    &:first-child {
      border-left: none;
    }

    &:last-child {
      border-right: none;
    }
  }
}
</style>