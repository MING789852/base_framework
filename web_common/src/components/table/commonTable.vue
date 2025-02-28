<script setup lang="ts">
import {message} from "@/utils/message";
import {cloneDeep, isEmpty, isNullOrUnDef} from "@pureadmin/utils";
import {addDialog, closeDialog,} from "@/components/ReDialog";
import common from '@/utils/common'
import SearchForm from "@/components/searchForm/searchForm.vue";
import contentConfig from "@/config/content";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import showDialog from "@/utils/ConfirmDialog";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import {VNode,ref,computed,onMounted} from "vue";
import Big from "big.js";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import TableFnClass from "@/class/TableFnClass";
import QueryInputHeader from "@/components/QueryHeader/QueryInputHeader.vue";

type TableType = 'action'|'report'

defineOptions({
  name:'commonTable'
})
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

//有文件字段时必传
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:false,default:new Map<String,File>()})
const deleteFileIdList = defineModel('deleteFileIdList',{type:Array<string>,required:false,default:[]})

interface CommonTableProp{
  //是否可编辑EditInput、EditOption、EditFile、EditDate
  canOptEditComponentFn?: (row:any,rowIndex:number,prop:string)=>Boolean,
  //分页数量
  showPageSizeChange?: boolean,
  pageSizeChangeList?: Array<number>,
  // 展示头部
  showHeader?: boolean,
  // action: 操作表  report: 查询报表
  tableType?: TableType,
  // 默认统计值
  summaryDefault?: Record<string,number>,
  // 忽略统字段
  ignoreSummaryPropList?: Array<string>,
  // 行尾统计方法
  showSummaryFn?: (data: { columns: any[], data: any[] }) => (VNode | string)[],
  // 展示行尾统计
  showSummary?: boolean,
  // 展示行项选择
  showSelection?: boolean,
  // 展示序号
  showSequence?: boolean,
  // 高亮选中行
  highlightCurrentRow?: boolean,
  // 展示分页
  showPage?: boolean,
  // 斑马表格
  stripe?: boolean,
  // 边框
  border?: boolean,
  // 按钮是否展示
  tableButtonShow?: boolean,
  tableButton?: CommonTableButton,
  // 查询模板
  queryTemplate?: QueryDefine,
  //查询列定义
  queryColumns?: Array<ColumnDefine>,
  //查询组件是否显示在表头
  queryInHeader?: boolean,
  //高度
  height?: string | number,
  //列定义
  columns: Array<ColumnDefine>,
  //输入是否禁用
  inputDisabledList?: InputDisabled,
  //字典表
  dictList?: Object,
  //api方法
  api?: ApiBase,
  //表格方法const tableFn = new TableFnClass()
  tableFn?: TableFnClass,
  cellClassNameMap?:Object,
  rowColumnSpanMap?:Object,
  rowClassNameMap?:Object
}

const props=withDefaults(defineProps<CommonTableProp>(),{
  //是否可编辑EditInput、EditOption、EditFile、EditDate
  // canOptEditComponentFn: undefined,
  //分页数量
  showPageSizeChange: ()=>false,
  pageSizeChangeList: ()=>[10,15,50,100,200],
  // 展示头部
  showHeader: ()=>true,
  // action: 操作表  report: 查询报表
  tableType: ()=>'action',
  // 默认统计值
  summaryDefault: ()=>{
    return {'合计':0}
  },
  // 忽略统字段
  ignoreSummaryPropList: ()=>[],
  // 行尾统计方法
  // showSummaryFn: undefined,
  // 展示行尾统计
  showSummary: ()=>false,
  // 展示行项选择
  showSelection: ()=>true,
  // 展示序号
  showSequence: ()=>false,
  // 高亮选中行
  highlightCurrentRow: ()=>false,
  // 展示分页
  showPage: ()=>true,
  // 斑马表格
  stripe: ()=>false,
  // 边框
  border: ()=>false,
  // 按钮是否展示
  tableButtonShow: ()=>true,
  tableButton: ()=>{
    return {
      queryFn: true,
      addFn: true,
      saveFn: true,
      deleteFn: true,
      refreshFn: true,
      initQueryFn: true,
      exportExcelFn: false
    }
  },
  // 查询模板
  queryTemplate: ()=>{
    return {
      current: 1,
      size:10,
      total: 0,
      queryParams:{}
    }
  },
  //查询列定义
  queryColumns: ()=>[],
  //查询组件是否显示在表头
  queryInHeader: ()=>true,
  //高度
  height: ()=>contentConfig.dataHeight,
  //列定义
  columns: ()=>[],
  //输入是否禁用
  inputDisabledList: ()=>{return {} as InputDisabled},
  //字典表
  dictList:  ()=>{return {}},
  //api方法
  api: ()=>{return {} as ApiBase},
  //表格方法const tableFn = new TableFnClass()
  tableFn: ()=>new TableFnClass(),
  cellClassNameMap: ()=>{return {}},
  rowColumnSpanMap: ()=>{return {}},
  rowClassNameMap: ()=>{return {}}
})

const pageLayoutComputed = computed(()=>{
  if (props.showPageSizeChange){
    return 'total,sizes,prev,pager,next'
  }else {
    return 'total,prev,pager,next'
  }
})

// const tableData  = defineModel('tableData',{type:Array<any>,required:false,default:[]})
const tableData  = ref<any[]>([])

const isAdd = ref(false)
const loading = ref(false)
const selectTableData = ref([])
const query = ref<QueryDefine>({
  current: 1,
  size:10,
  total: 0,
  queryParams:{},
  orderByParams:{}
})
const dictMapping = computed(()=>{
  if (isNullOrUnDef(props.dictList)){
    return {}
  }else {
    return props.dictList
  }
})
const tableButtonComputed = computed(()=>{
  let tableButton = cloneDeep(props.tableButton)
  if (!props.queryInHeader){
    tableButton.initQueryFn = false
  }
  return tableButton
})
onMounted(()=>{
  initQuery()
  if (!isNullOrUnDef(props.tableFn)&&!isNullOrUnDef(props.tableFn.initFn)){
    props.tableFn.initFn()
  }else {
    //加载数据
    getData()
  }
})
const exportExcelFn =()=>{
  if (isNullOrUnDef(props.tableFn.exportExcelFn)){
    let data = {
      columnPropsList: props.columns,
      conditions: query.value
    }
    common.showGlobalLoading()
    props.api.exportExcel(data).then((res:Blob) => {
      common.handleBlob(res)
    }).catch(() => {
      message('请求失败', {type: 'error'})
    }).finally(() => {
      common.closeGlobalLoading()
    })
  }else {
    props.tableFn.exportExcelFn()
  }
}
const clearOption = (prop) => {
  delete query.value.queryParams[prop]
}

const rowClassNameFn = (data: { row: any,rowIndex: number }) => {
  if (isNullOrUnDef(props.tableFn.rowClassName)){
    if (isNullOrUnDef(props.rowClassNameMap)){
      return ''
    }else {
      let rowIndex = data.rowIndex
      let tableRowClassName=props.rowClassNameMap[rowIndex]
      if (isNullOrUnDef(tableRowClassName)){
        return ''
      }else {
        return tableRowClassName
      }
    }
  }else {
    return props.tableFn.rowClassName(data)
  }
}
const spanMethod = (data: { row: any, column: any, rowIndex: number, columnIndex: number }) => {
  if (isNullOrUnDef(props.tableFn.cellSpanMethod)){
    if (isNullOrUnDef(props.rowColumnSpanMap)){
      return [1,1]
    }else {
      let rowIndex = data.rowIndex
      let columnIndex = data.columnIndex
      let rowColumnSpan=props.rowColumnSpanMap[rowIndex+"_"+columnIndex]
      if (isNullOrUnDef(rowColumnSpan)){
        return {
          rowspan: 1,
          colspan: 1,
        }
      }else {
        return rowColumnSpan
      }
    }
  }else {
    return props.tableFn.cellSpanMethod(data)
  }
}
//把level类型都拍成一级
const columnsToIndexColumn=(columns:ColumnDefine [])=>{
  let newColumns:IndexColumn[] = []
  if (common.isEmptyArr(columns)){
    return newColumns
  }else {
    columns.forEach((value)=>{
      if (value.type!==ColumnTypeEnum.LEVEL){
        newColumns.push({
          label: value.label,
          prop: value.prop,
          show:value.show,
          fixed: value.fixed
        })
      }else {
        newColumns = [...newColumns,...columnsToIndexColumn(value.children)]
      }
    })
    return newColumns
  }
}
const summaryMethod=(data: { columns: any[], data: any[] })=>{
  if (isNullOrUnDef(props.showSummaryFn)){
    let summaryArr = []
    //无数据或者无列配置时，默认不处理
    if (Object.keys(data.data).length===0||Object.keys(data.columns).length===0){
      return summaryArr
    }
    //初始化数组
    props.columns.forEach(()=>{
      summaryArr.push('')
    })
    //数据统计的列映射
    let indexMapping = new Map<string,number>()
    //把level类型都拍成一级
    let indexColumns= columnsToIndexColumn(props.columns)
    //过滤掉不展示的数据
    indexColumns = indexColumns.filter((value)=>{
      if (isNullOrUnDef(value.show)){
        return true
      }
      return value.show
    })
    //处理固定列（处于最前面）
    let columnIndex = 0
    indexColumns.forEach((value)=>{
      if (value.fixed){
        indexMapping.set(value.prop,columnIndex)
        columnIndex++
      }
    })
    //处理普通列 (在固定列后面)
    indexColumns.forEach((value)=>{
      if (!value.fixed){
        indexMapping.set(value.prop,columnIndex)
        columnIndex++
      }
    })
    //执行数据统计
    indexMapping.forEach((index, prop)=>{
      //不需要统计列的数据
      if (props.ignoreSummaryPropList.includes(prop)){
        summaryArr[index] = ''
        return
      }
      //需要统计列的数据
      let sum =new Big(0);
      data.data.forEach((item)=>{
        if (!isNullOrUnDef(item[prop])){
          if (common.isString(item[prop])&&common.isStrBlank(item[prop])){
          }else {
            if (common.isNumber(item[prop])){
              sum=sum.add(new Big(item[prop]))
            }else if (common.isString(item[prop])&&common.isNumberStr(item[prop])) {
              try {
                sum=sum.add(new Big(item[prop]))
              }catch (e){
                sum=sum.add(new Big(0))
              }
            }else {
              sum=sum.add(new Big(0))
            }
          }
        }
      })
      summaryArr[index] = sum.toString()
    })
    Object.keys(props.summaryDefault).forEach((value)=>{
      summaryArr[props.summaryDefault[value]] = value
    })
    return summaryArr
  }else {
    return props.showSummaryFn(data)
  }
}
const rowDblclick=(row: any, column: any, event: Event)=>{
  // 行点击事件处理
  if (!isNullOrUnDef(props.tableFn.rowDblclick)){
    props.tableFn.rowDblclick(row,column,event)
  }
}
const cellDblclick = (row: any, column: any, cell: HTMLTableCellElement, event: Event)=> {
  // 单元格点击事件处理
  if (!isNullOrUnDef(props.tableFn.cellDblclick)){
    props.tableFn.cellDblclick(row,column,cell,event)
  }
}
const sortChange = (data: {column: any, prop: string, order: any })=>{
  // 排序事件处理
  if (!isNullOrUnDef(props.tableFn.sortChange)){
    props.tableFn.sortChange(data)
  }else {
    //初始化
    query.value.orderByParams = {}
    //判断是否需要排序
    if (isNullOrUnDef(data.order)){
      delete query.value.orderByParams[data.column.property]
    }else {
      query.value.orderByParams[data.column.property] = data.order
    }
    getData()
  }
}
const handleSelectionChange=(val: Array<Object>)=>{
  if (!isNullOrUnDef(props.tableFn.handleSelectionChange)){
    props.tableFn.handleSelectionChange(val)
  }else {
    selectTableData.value = val
  }
}
const initQuery= ()=> {
  if (!isNullOrUnDef(props.tableFn.initQuery)){
    props.tableFn.initQuery()
  }else {
    let template:QueryDefine = cloneDeep(props.queryTemplate)
    query.value.queryParams = template.queryParams
    query.value.current = template.current
    query.value.size = template.size
  }
}
const getData = ()=>{
  if (!isNullOrUnDef(props.tableFn.getData)){
    props.tableFn.getData()
  }else {
    loading.value = true
    if (!isNullOrUnDef(props.tableFn.handleQuery)) {
      props.tableFn.handleQuery()
    }
    props.api.selectByPage(query.value).then((res:any) => {
      let code = res.code
      let data = res.data
      let msg  = res.msg
      if (code === 200) {
        query.value.current = data.current
        query.value.total = data.total
        query.value.size = data.size
        tableData.value = data.records
      } else  {
        message(msg,{type:'error'})
      }
    }).catch(res => {
      console.log(res)
      message('请求失败',{type:'error'})
    }).finally(() => {
      loading.value = false
      isAdd.value = false
    })
  }
}
const handleSizeChange=(val: number)=>{
  if (!isNullOrUnDef(props.tableFn.handleSizeChange)){
    props.tableFn.handleSizeChange(val)
  }else {
    query.value.size = val
    getData()
  }
}
const handleCurrentChange=(val: number)=>{
  if (!isNullOrUnDef(props.tableFn.handleCurrentChange)){
    props.tableFn.handleCurrentChange(val)
  }else {
    query.value.current = val
    getData()
  }
}
const openQueryFn=()=>{
  addDialog({
    top: '5vh',
    width: "60%",
    title: "查询",
    props: {
      columns: props.queryColumns,
      query: query,
      dictList: dictMapping
    },
    contentRenderer: () => SearchForm,
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
          initQuery()
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
    closeCallBack: ({ options, args }) => {
      query.value = options.props.query as QueryDefine
      if (args?.command === "cancel") {
        // 您点击了取消按钮
        message(`取消查询`);
      } else if (args?.command === "sure") {
        // 查询重置第一页
        query.value.current = 1
        getData()
      } else {
        message(
            `关闭查询窗口`
        );
      }
    }
  });
}
const queryFn=()=>{
  if (!isNullOrUnDef(props.tableFn.queryFn)){
    props.tableFn.queryFn()
  }else {
    if (props.queryInHeader){
      // 查询重置第一页
      query.value.current = 1
      getData()
    }else {
      if (!isNullOrUnDef(props.tableFn.beforeQueryDialogOpenFn)){
        props.tableFn.beforeQueryDialogOpenFn().then(()=>{
          openQueryFn()
        })
      }else {
        openQueryFn()
      }
    }
  }
}
const addFn=()=>{
  if (!isNullOrUnDef(props.tableFn.addFn)){
    props.tableFn.addFn()
  }else {
    let item = {
      add: true
    }
    isAdd.value =true
    tableData.value.push(item)
  }
}
const saveFn=()=>{
  if (!isNullOrUnDef(props.tableFn.saveFn)){
    props.tableFn.saveFn()
  }else {
    loading.value = true
    props.api.saveOrUpdateData(tableData.value).then((res:any) => {
      let code = res.code
      let msg = res.msg
      if (code === 200) {
        message('操作成功', { type: 'success' })
        getData()
      } else {
        message(msg, { type: 'error' })
      }
    }).catch(res => {
      console.log(res)
    }).finally(() => {
      loading.value = false
    })
  }
}
const deleteFn=() =>{
  if (!isNullOrUnDef(props.tableFn.deleteFn)){
    props.tableFn.deleteFn()
  }else {
    if(isNullOrUnDef(selectTableData.value)||isEmpty(selectTableData.value)){
      message('请选择需要删除的数据后操作', { type: 'warning' })
    } else {
      showDialog('是否确认删除').then(() => {
        loading.value = true
        props.api.deleteData(selectTableData.value).then((res:any) => {
          let code = res.code
          let msg = res.msg
          if (code === 200) {
            if (isAdd.value) {
              //前端界面删除
              for (let i = 0; i < selectTableData.value.length; i++) {
                //删除数据
                common.removeListItem(tableData.value,selectTableData.value[i])
              }
            } else {
              getData()
            }
            message('操作成功', { type: 'success' })
          } else {
            message(msg, { type: 'error' })
          }
        }).catch(res => {
          console.log(res)
        }).finally(() => {
          loading.value = false
        })
      })
    }
  }
}
const refreshFn=()=>{
  if (!isNullOrUnDef(props.tableFn.refreshFn)){
    props.tableFn.refreshFn()
  }else {
    getData()
  }
}

const cellClassNameFn:(data: { row: any, column: any, rowIndex: number, columnIndex: number }) => string = (data) => {
  if (isNullOrUnDef(props.tableFn.cellClassName)){
    //如果带有选择框则需要列定位需要再-1
    let columnIndex= data.columnIndex
    if (props.showSelection){
      columnIndex = data.columnIndex-1
    }
    let columnMap:Object=props.cellClassNameMap[data.rowIndex]
    if (!isNullOrUnDef(columnMap)){
      let className=columnMap[columnIndex]
      if (!isNullOrUnDef(className)){
        return className
      }else {
        return ''
      }
    }else {
      return ''
    }
  }else {
    return props.tableFn.cellClassName(data)
  }
}

defineExpose({
  isAdd,
  selectTableData,
  tableData,
  loading,
  query,
  queryFn,
  addFn,
  deleteFn,
  saveFn,
  getData
})
</script>

<template>
  <div :class="tableType==='action'?'parent':'parent report-table'">
    <el-card shadow="never" style="height: 100%">
      <template v-if="showHeader" #header>
        <slot name="header">
          <div class="flex flex-row w-full h-full justify-between">
            <slot name="header_left">
              <div class="flex flex-row gap-3 content-center flex-wrap">
                <template v-if="queryInHeader">
                  <QueryInputHeader v-model:query="query" :query-columns="queryColumns" :dict-mapping="dictList"/>
                </template>
                <slot name="button_start"/>
                <template v-if="tableButtonShow">
                  <el-button v-show="tableButtonComputed.queryFn" class="font-bold" size="small" type="primary" @click="queryFn">查询</el-button>
                  <el-button v-show="tableButtonComputed.initQueryFn" class="font-bold" size="small" type="primary" @click="initQuery">清空查询条件</el-button>
                  <el-button v-show="tableButtonComputed.addFn" class="font-bold" size="small" type="primary" @click="addFn">新增</el-button>
                  <el-button v-show="tableButtonComputed.saveFn" class="font-bold" size="small" type="primary" @click="saveFn">保存</el-button>
                  <el-button v-show="tableButtonComputed.deleteFn" class="font-bold" size="small" type="primary" @click="deleteFn">删除</el-button>
                  <el-button v-show="tableButtonComputed.refreshFn" class="font-bold" size="small" type="primary" @click="refreshFn">刷新</el-button>
                  <el-button v-show="tableButtonComputed.exportExcelFn" class="font-bold" size="small" type="primary" @click="exportExcelFn">导出excel</el-button>
                </template>
                <slot name="button_end"/>
              </div>
            </slot>
            <slot name="header_right"/>
          </div>
        </slot>
      </template>

      <el-table
          v-loading="loading"
          style="font-size: 12px"
          element-loading-text="加载中..."
          :height="height"
          :data="tableData"
          :stripe="stripe"
          :border="border"
          :show-summary="showSummary"
          :summary-method="summaryMethod"
          :row-style="(data)=>isNullOrUnDef(tableFn.rowStyle)?{}:tableFn.rowStyle(data)"
          :row-class-name="(data)=>rowClassNameFn(data)"
          :cell-class-name="(data)=>cellClassNameFn(data)"
          :span-method="spanMethod"
          :highlight-current-row="highlightCurrentRow"
          @rowDblclick="rowDblclick"
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
        <template v-for="item in columns" :key="item.prop">
          <CommonTableColumn :table-fn="tableFn"  :input-disabled-list="inputDisabledList"
                             :canOptEditComponentFn="canOptEditComponentFn"
                             :item="item" :dict-list="dictMapping" v-model:delete-file-id-list="deleteFileIdList"
                             v-model:upload-map="uploadMap">
            <template #columnItem="columItem">
              <slot  name="columnItem" :item="columItem.item"/>
            </template>
          </CommonTableColumn>
        </template>
        <slot name="column"/>
      </el-table>


      <template v-if="showPage" #footer>
        <slot  name="footer">
          <div v-if="isAdd===false" style="display: flex;width: 100%;justify-content: center">
            <el-pagination
                :current-page="query.current"
                :page-size="query.size"
                size="small"
                :page-sizes="pageSizeChangeList"
                :layout="pageLayoutComputed"
                :total="query.total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
          </div>
        </slot>
      </template>
    </el-card>

    <slot name="dialog"/>
  </div>
</template>

<style scoped lang="scss">
.parent {
  height: 100%;
  ::v-deep(.el-button+.el-button){
    margin-left: 0;
  }
  ::v-deep(.el-table__row){
    --el-table-row-hover-bg-color: #d8ecff;
    &:hover {
      cursor: pointer;
      //transition: transform 0.3s;
      //transform: translateX(10px);
    }
  }
}
.parent> :first-child{
  height: 100%;
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
  ::v-deep(.el-card__body){
    padding: 2px 2px;
  }
  ::v-deep(.el-button+.el-button){
    margin-left: 0;
  }
  ::v-deep(.el-table__header-wrapper .el-table__cell){
    padding: 0 0;
    font-size: 12px;
  }
  ::v-deep(.el-table__body-wrapper .el-table__cell){
    padding: 0 0;
    font-size: 12px;
  }
  ::v-deep(.el-table__footer-wrapper .el-table__cell){
    font-size: 14px;
    color: white;
    padding: 0 0 0 0;
    font-weight: bolder;
    background-color: #409EFF;
  }
  ::v-deep(.el-card__header){
    padding: 10px 10px;
  }
  ::v-deep(.el-table .cell){
    padding: 0 0;
  }
}
</style>
