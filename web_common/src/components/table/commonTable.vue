<script setup lang="ts">
import {onMounted, reactive, ref, useTemplateRef} from "vue";
import {useTableData,useComputedModel,CommonTableProp} from "@/components/table/action";
import VirtualizedTable from "@/components/table/virtualizedTable/virtualizedTable.vue";
import FlexFullScrollContainer from "@/components/FlexFullScrollContainer/FlexFullScrollContainer.vue";
import CardContainer from "@/components/CardContainer/CardContainer.vue";
import QueryInputHeader from "@/components/QueryHeader/QueryInputHeader.vue";
import {isNullOrUnDef} from "@pureadmin/utils";
import TableFnClass from "@/class/TableFnClass";
import FormFnClass from "@/class/FormFnClass";
import {useElementSize} from "@vueuse/core";
import ActualTable from "@/components/table/actualTable/actualTable.vue";
import QueryPage from "@/components/QueryPage/QueryPage.vue";


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
//有文件字段时必传
const uploadMap = defineModel<Map<String, File>>('uploadMap', {required: false, default: new Map<String, File>()})
const deleteFileIdList = defineModel<string[]>('deleteFileIdList', {required: false, default: []})
const tableDataModel = defineModel<any []>('tableData', {
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
const loading = ref(false)
const isAdd = ref(false)
const selectTableData = ref([])
defineSlots<{
  columnItem(props: { item: ColumnDefine}): any,
  header():any,
  header_left():any,
  button_start():any,
  button_end():any,
  header_right():any,
  preSelectionColumn():any,
  endSelectionColumn():any,
  column():any,
  footer():any,
  dialog():any
}>()
const {tableData,query} =useComputedModel(tableDataModel,queryModel)
const {
  tableButtonComputed,
  exportExcelFn,
  initQuery,
  queryFn,
  saveFn,
  pageLayoutComputed,
  isFullscreen,
  addFn,
  editFn,
  deleteFn,
  refreshFn,
  fullscreenFn,
  getData,
  openEditDialog
} = useTableData(props,{
  tableData,
  selectTableData,
  query,
  uploadMap,
  deleteFileIdList,
  loading,
  isAdd
})
onMounted(() => {
  initQuery()
  if (!isNullOrUnDef(props.tableFn) && !isNullOrUnDef(props.tableFn.initFn)) {
    props.tableFn.initFn()
  } else {
    //加载数据
    getData()
  }
})
defineExpose({
  isAdd,
  selectTableData,
  tableData,
  loading,
  query,
  queryFn,
  addFn,
  deleteFn,
  initQuery,
  saveFn,
  getData,
  openEditDialog
})


const tableContainer =useTemplateRef('tableContainer')
const { width: tableWidth, height: tableHeight } = useElementSize(tableContainer)
</script>

<template>
  <card-container ref="containerRef" :show-footer="showPage" :show-header="showHeader" :class="tableType==='action'?'parent':'parent report-table'">
    <template #header>
      <slot name="header">
        <div class="flex flex-row w-full h-full justify-between">
          <slot name="header_left">
            <div class="flex flex-row gap-3 content-center flex-wrap">
              <template v-if="queryInHeader">
                <QueryInputHeader v-model:query="query" :query-columns="queryColumns"
                                  :dict-mapping="dictList"
                                  :query-in-header-with-condition="queryInHeaderWithCondition"/>
              </template>
              <slot name="button_start"/>
              <template v-if="tableButtonShow">
                <el-button v-show="tableButtonComputed.queryFn" class="font-bold" size="small" type="primary" @click="queryFn">
                  <template #icon>
                    <IconifyIconOnline icon="material-symbols:search-rounded" />
                  </template>
                  查询
                </el-button>
                <el-button v-show="tableButtonComputed.initQueryFn" class="font-bold" size="small" type="primary" @click="initQuery">
                  <template #icon>
                    <IconifyIconOnline icon="material-symbols:refresh" />
                  </template>
                  清空查询条件
                </el-button>
                <el-button v-show="tableButtonComputed.addFn" class="font-bold" size="small" type="primary" @click="addFn">
                  <template #icon>
                    <IconifyIconOnline icon="material-symbols:add" />
                  </template>
                  新增
                </el-button>
                <el-button v-if="saveOrUpdateType === 'onRow'" v-show="tableButtonComputed.saveFn" class="font-bold" size="small" type="primary" @click="saveFn">
                  <template #icon>
                    <IconifyIconOnline icon="material-symbols:save" />
                  </template>
                  保存
                </el-button>
                <el-button v-if="saveOrUpdateType === 'onDialog'" v-show="tableButtonComputed.editFn" class="font-bold" size="small" type="primary" @click="editFn">
                  <template #icon>
                    <IconifyIconOnline icon="material-symbols:edit" />
                  </template>
                  修改
                </el-button>
                <el-button v-show="tableButtonComputed.deleteFn" class="font-bold" size="small" type="primary" @click="deleteFn">
                  <template #icon>
                    <IconifyIconOnline icon="material-symbols:delete" />
                  </template>
                  删除
                </el-button>
                <el-button v-show="tableButtonComputed.refreshFn" class="font-bold" size="small" type="primary" @click="refreshFn">
                  <template #icon>
                    <IconifyIconOnline icon="material-symbols:refresh" />
                  </template>
                  刷新
                </el-button>
                <el-button v-show="tableButtonComputed.exportExcelFn" class="font-bold" size="small" type="primary" @click="exportExcelFn">
                  <template #icon>
                    <IconifyIconOnline icon="material-symbols:download" />
                  </template>
                  导出excel
                </el-button>
                <el-button v-show="tableButtonComputed.fullscreenFn" class="font-bold" size="small" type="primary" @click="fullscreenFn">
                  <template #icon>
                    <IconifyIconOnline :icon="isFullscreen?'material-symbols:fullscreen':'material-symbols:fullscreen-exit'" />
                  </template>
                  {{isFullscreen?'关闭全屏':'全屏展示'}}
                </el-button>
              </template>
              <slot name="button_end"/>
            </div>
          </slot>
          <slot name="header_right"/>
        </div>
      </slot>
    </template>

    <template #default>
      <flex-full-scroll-container ref="tableContainer" :type="tableHeightFitContent?'fit':'grow'">
        <virtualized-table
            v-if="version==='v2'"
            v-bind="props"
            :tableHeight="tableHeight"
            :tableWidth="tableWidth"
            v-model:select-table-data="selectTableData"
            v-model:query="query"
            v-model:table-data="tableData"
            v-model:upload-map="uploadMap"
            v-model:delete-file-id-list="deleteFileIdList"
        />
        <actual-table
            v-if="version==='v1'"
            v-bind="props"
            :height="tableHeightFitContent?'fit-content':'100%'"
            v-model:select-table-data="selectTableData"
            v-model:query="query"
            v-model:table-data="tableData"
            v-model:upload-map="uploadMap"
            v-model:delete-file-id-list="deleteFileIdList"
        >
          <template #preSelectionColumn>
            <slot name="preSelectionColumn"/>
          </template>
          <template #endSelectionColumn>
            <slot name="endSelectionColumn"/>
          </template>
          <template #column>
            <slot name="column"/>
          </template>
          <template #columnItem="columItem">
            <slot  name="columnItem" :item="columItem.item"/>
          </template>
        </actual-table>
      </flex-full-scroll-container>
      <slot name="dialog"/>
    </template>

    <template #footer>
      <slot name="footer">
        <div v-if="isAdd===false" style="display: flex;width: 100%;justify-content: center">
          <query-page
          v-model:query="query"
          size="small"
          :layout="pageLayoutComputed"
          :page-size-change-list="pageSizeChangeList"
          :background="false"
          :get-data="getData"
          />
        </div>
      </slot>
    </template>
  </card-container>
</template>

<style scoped lang="scss">
.parent {
  :deep(.el-button+.el-button){
    margin-left: 0;
  }
  :deep(.el-table__row){
    &:hover {
      cursor: pointer;
    }
  }
}
.report-table{
  --el-select-input-font-size: 12px;
  --el-font-size-base: 12px;
  //::v-deep(.el-table__row){
  //  --el-table-row-hover-bg-color: #d8ecff;
  //  &:hover {
  //    cursor: pointer;
  //    //transition: transform 0.3s;
  //    //transform: translateX(10px);
  //  }
  //}
  :deep(.el-button+.el-button){
    margin-left: 0;
  }
  :deep(.el-table__header-wrapper .el-table__cell){
    padding: 0 0;
    font-size: 12px;
  }
  :deep(.el-table__body-wrapper .el-table__cell){
    padding: 0 0;
    font-size: 12px;
  }
  :deep(.el-table__footer-wrapper .el-table__cell){
    font-size: 14px;
    color: white;
    padding: 0 0 0 0;
    font-weight: bolder;
    background-color: #409EFF;
  }
  :deep(.el-table .cell){
    padding: 0 0;
  }
}
</style>
