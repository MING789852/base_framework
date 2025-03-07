<script setup lang="ts">
import {ref} from "vue";
import commonTable from "@/components/table/commonTable.vue";
import reportApi from "@/api/reportApi";
import TableFnClass from "@/class/TableFnClass";
import UploadFileClass from "@/class/UploadFileClass";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import common from "@/utils/common";
import {message} from "@/utils/message";

defineOptions({
  name: "report"
});

const props = defineProps({
  reportType: {type: String, required: true},
  firstQuery: {type: Boolean,required: false,default: true},
  dictMapping: {type:Object,required:false,default: {}},
  queryTemplate: {
    type: Object as PropType<QueryDefine>,
    required: false,
    default: {
      current: 1,
      size:10,
      total: 0,
      queryParams:{}
    } as QueryDefine
  },
})

let commonTableRef = ref<CommonTableType>(null)
const tableFn = new TableFnClass()
const uploadFile = new UploadFileClass([], new Map() as UploadMap, 'deletFileIdList', 'uploadFileKeyList')
const columns = ref<Array<ColumnDefine>>([]);
const queryColumns = ref<Array<ColumnDefine>>([]);
const dictList = ref(props.dictMapping)
const tableButton = ref<CommonTableButton>({
  queryFn: true,
  initQueryFn: true,
  addFn: false,
  saveFn: false,
  deleteFn: false,
  refreshFn: false,
  exportExcelFn: true
})

tableFn.initFn = () => {
  reportApi.getConfig(props.reportType).then((res:HttpResult)=>{
    if (res.code===200){
      queryColumns.value = res.data.queryColumnList
      columns.value = res.data.showColumnList
      if (props.firstQuery){
        commonTableRef.value.getData()
      }
    }else {
      message(res.msg,{type:"error"})
    }
  })
}

tableFn.exportExcelFn = () => {
  let data = {
    columnPropsList: columns.value,
    queryData: commonTableRef.value.query,
    reportType: props.reportType
  }
  common.showGlobalLoading()
  reportApi.exportExcel(data).then((res:any) => {
    common.handleBlob(res)
  }).catch((e) => {
    message('请求失败', {type: 'error'})
  }).finally(() => {
    common.closeGlobalLoading()
  })
}

tableFn.getData = () => {
  commonTableRef.value.loading = true
  let data = {
    reportType: props.reportType,
    queryData: commonTableRef.value.query
  }
  reportApi.selectByPage(data).then((res:any) => {
    let code = res.code
    let data = res.data
    let msg  = res.msg
    if (code === 200) {
      commonTableRef.value.query.current = data.current
      commonTableRef.value.query.total = data.total
      commonTableRef.value.query.size = data.size
      commonTableRef.value.tableData = data.records
    } else  {
      message(msg,{type:'error'})
    }
  }).catch(res => {
    message('请求失败',{type:'error'})
  }).finally(() => {
    commonTableRef.value.loading = false
  })
}

const columnConfigFlag = ref(false)
const columnConfigData = ref({
  dbTypeName: '',
  sql: '',
  dataSource: '',
  showColumns: [],
  queryColumns: []
})
const columnConfigDictList = ref({})
const showColumnsSelect = ref([])
const queryColumnsSelect = ref([])
const columnConfigType = {
  show:'show',
  query: 'query'
}
const showColumnConfigColumns = ref<Array<ColumnDefine>>([
  {prop: "prop", label: "字段", type: ColumnTypeEnum.INPUT, query: false},
  {prop: "label", label: "名称", type: ColumnTypeEnum.INPUT, query: false},
  {prop: "fixed", label: "是否固定", type: ColumnTypeEnum.OPTION, query: false},
  {prop: "width", label: "宽度", type: ColumnTypeEnum.INPUT, query: false},
  {prop: "minWidth", label: "最小宽度", type: ColumnTypeEnum.INPUT, query: false},
  {prop: "headerAlign", label: "标题对齐方式", type: ColumnTypeEnum.OPTION, query: false},
  {prop: "align", label: "数据对齐方式", type: ColumnTypeEnum.OPTION, query: false},
  {prop: "type", label: "数据类型", type: ColumnTypeEnum.OPTION, query: false}
]);
const queryColumnConfigColumns = ref<Array<ColumnDefine>>([
  {prop: "prop", label: "字段", type: ColumnTypeEnum.INPUT, query: false},
  {prop: "label", label: "名称", type: ColumnTypeEnum.INPUT, query: false},
  {prop: "queryCondition", label: "查询条件", type: ColumnTypeEnum.OPTION, query: false},
  {prop: "queryType", label: "查询类型", type: ColumnTypeEnum.OPTION, query: false}
]);

const saveColumnConfig = () => {
  let data = {
    queryColumnList: columnConfigData.value.queryColumns,
    showColumnList: columnConfigData.value.showColumns,
    dbTypeName: columnConfigData.value.dbTypeName,
    sql: columnConfigData.value.sql,
    dataSource: columnConfigData.value.dataSource,
    reportType: props.reportType
  }
  common.showGlobalLoading()
  reportApi.saveConfig(data).then((res:HttpResult)=>{
    if (res.code===200){
      message('操作成功',{type:"success"})
    }else {
      message(res.msg,{type:"error"})
    }
  }).finally(()=>{
    common.closeGlobalLoading()
  })
}

const clearDataCache = () => {
  common.handleRequestApi(reportApi.clearDataCache()).then((res)=>{
    message(res.data,{type:"success"})
  })
}

const openColumnConfig = () => {
  reportApi.getConfig(props.reportType).then((res:HttpResult)=>{
    if (res.code===200){
      columnConfigDictList.value =res.data.dictList
      columnConfigData.value.sql = res.data.sql
      columnConfigData.value.queryColumns = res.data.queryColumnList
      columnConfigData.value.showColumns = res.data.showColumnList
      columnConfigData.value.dbTypeName = res.data.dbTypeName
      columnConfigData.value.dataSource = res.data.dataSource
      columnConfigFlag.value = true
    }else {
      message(res.msg,{type:"error"})
    }
  })
}

const generateColumnByShowColumnsFn = () => {
  common.showGlobalLoading()
  reportApi.generateQueryColumnsByShowColumns(columnConfigData.value.showColumns).then((res:HttpResult)=>{
    if (res.code===200){
      columnConfigData.value.queryColumns = res.data
    }else {
      message(res.msg,{type:"error"})
    }
  }).finally(()=>{
    common.closeGlobalLoading()
  })
}

const generateColumnFn = (type:string) => {
  let data = {
    sql: columnConfigData.value.sql,
    dbTypeName: columnConfigData.value.dbTypeName
  }
  common.showGlobalLoading()
  reportApi.generateColumnsBySql(data).then((res:HttpResult)=>{
    if (res.code===200){
      if (type === columnConfigType.show){
        columnConfigData.value.showColumns = res.data
      }
      if (type === columnConfigType.query){
        columnConfigData.value.queryColumns = res.data
      }
    }else {
      message(res.msg,{type:"error"})
    }
  }).finally(()=>{
    common.closeGlobalLoading()
  })
}
const addColumnFn = (type:string) => {
  if (type === columnConfigType.show){
    columnConfigData.value.showColumns.push({})
  }
  if (type === columnConfigType.query){
    columnConfigData.value.queryColumns.push({})
  }
}
const deleteColumnFn = (type:string) => {
  if (type === columnConfigType.show){
    showColumnsSelect.value.forEach((value)=>{
      common.removeListItem(columnConfigData.value.showColumns,value)
    })
  }
  if (type === columnConfigType.query){
    queryColumnsSelect.value.forEach((value)=>{
      common.removeListItem(columnConfigData.value.queryColumns,value)
    })
  }
}

const handleColumnSelectionChange = (val: Array<Object>,type:string) => {
  if (type === columnConfigType.show){
    showColumnsSelect.value = val
  }
  if (type === columnConfigType.query){
    queryColumnsSelect.value = val
  }
}

const delShowColumn = (row,index) => {
  let tableDataProp=columnConfigData.value.showColumns
  tableDataProp.splice(index,1)
}
const upShowColumn = (row,index) => {
  let upIndex = index-1
  if (upIndex<0){
    return
  }
  let tableDataProp=columnConfigData.value.showColumns
  let currentItem=tableDataProp[index]
  tableDataProp[index]=tableDataProp[upIndex]
  tableDataProp[upIndex]=currentItem
}
const downShowColumn = (row,index) => {
  let downIndex = index+1
  let tableDataProp=columnConfigData.value.showColumns
  if (downIndex>tableDataProp.length-1){
    return
  }
  let currentItem=tableDataProp[index]
  tableDataProp[index]=tableDataProp[downIndex]
  tableDataProp[downIndex]=currentItem
}

const delQueryColumn = (row,index) => {
  let tableDataProp=columnConfigData.value.queryColumns
  tableDataProp.splice(index,1)
}
const upQueryColumn = (row,index) => {
  let upIndex = index-1
  if (upIndex<0){
    return
  }
  let tableDataProp=columnConfigData.value.queryColumns
  let currentItem=tableDataProp[index]
  tableDataProp[index]=tableDataProp[upIndex]
  tableDataProp[upIndex]=currentItem
}
const downQueryColumn = (row,index) => {
  let downIndex = index+1
  let tableDataProp=columnConfigData.value.queryColumns
  if (downIndex>tableDataProp.length-1){
    return
  }
  let currentItem=tableDataProp[index]
  tableDataProp[index]=tableDataProp[downIndex]
  tableDataProp[downIndex]=currentItem
}


defineExpose({
  'commonTableRef': commonTableRef,
  'columns': columns.value,
  'queryColumns': queryColumns.value,
  'dictList': dictList.value
})
</script>

<template>
  <commonTable ref="commonTableRef" :columns="columns" :queryColumns="queryColumns" tableType="report" height="82vh"
               :dictList="dictList" :api="reportApi" :tableFn="tableFn" :uploadFile="uploadFile" :queryTemplate="queryTemplate"
               :table-button="tableButton" :border="true">
    <template #button_end>
      <slot name="button_start"/>
      <el-button style="font-weight: bold" size="small" type="primary" @click="clearDataCache">清空数据缓存</el-button>
      <el-button v-if="common.authUserButton(['admin'])"  style="font-weight: bold" size="small" type="primary" @click="openColumnConfig">列配置</el-button>
      <slot name="button_end"/>
    </template>

    <template #dialog>
      <el-dialog v-model="columnConfigFlag" append-to-body destroy-on-close fullscreen>
        <div class="w-full h-full flex-col">
          <el-button-group>
            <el-button  style="font-weight: bold" @click="saveColumnConfig">保存</el-button>
          </el-button-group>

          <el-divider>数据库类型</el-divider>
          <el-select  v-model="columnConfigData.dbTypeName" placeholder="数据库类型" filterable>
            <el-option
              v-for="(value,key) in columnConfigDictList['dbTypeName']"
              :key="key"
              :label="value"
              :value="key"
            />
          </el-select>

          <el-divider>数据源</el-divider>
          <el-select  v-model="columnConfigData.dataSource" placeholder="数据源" filterable>
            <el-option
              v-for="(value,key) in columnConfigDictList['dataSource']"
              :key="key"
              :label="value"
              :value="key"
            />
          </el-select>

          <el-divider>SQL</el-divider>
          <el-input v-model="columnConfigData.sql" type="textarea" :autosize="{ minRows: 4}"/>

          <el-divider>展示列</el-divider>
          <div class="w-full">
            <el-button-group>
              <el-button  style="font-weight: bold" @click="generateColumnFn(columnConfigType.show)">解析SQL列</el-button>
              <el-button  style="font-weight: bold" @click="addColumnFn(columnConfigType.show)">新增</el-button>
              <el-button  style="font-weight: bold" @click="deleteColumnFn(columnConfigType.show)">删除</el-button>
            </el-button-group>

            <el-table
              element-loading-text="加载中..."
              :data="columnConfigData.showColumns"
              stripe
              @selection-change="(val)=>handleColumnSelectionChange(val,columnConfigType.show)">
              <el-table-column type="selection" width="55"  fixed/>
              <el-table-column header-align="center" align="center" label="操作" width="120">
                <template #default="scope">
                  <div class="flex gap-2" style="font-size: 12px">
                    <el-link :underline="false" type="primary" class="text-nowrap" @click="delShowColumn(scope.row,scope.$index)">删除</el-link>
                    <el-link :underline="false" type="primary" class="text-nowrap" @click="upShowColumn(scope.row,scope.$index)">上移</el-link>
                    <el-link :underline="false" type="primary" class="text-nowrap" @click="downShowColumn(scope.row,scope.$index)">下移</el-link>
                  </div>
                </template>
              </el-table-column>
              <template v-for="item in showColumnConfigColumns" :key="item.prop">
                <!-- 可编辑文本框 -->
                <el-table-column  v-if="item.type === ColumnTypeEnum.INPUT" header-align="center" align="center" :label="item.label" :width="item.width">
                  <template #default="scope">
                    <el-input v-model="scope.row[item.prop]"/>
                  </template>
                </el-table-column>
                <!-- 可编辑选择器-->
                <el-table-column    v-else-if="item.type === ColumnTypeEnum.OPTION" header-align="center" align="center" :label="item.label" :width="item.width">
                  <template #default="scope">
                    <el-select  v-model="scope.row[item.prop]"  filterable>
                      <el-option
                        v-for="(value,key) in columnConfigDictList[item.prop]"
                        :key="key"
                        :label="value"
                        :value="key"
                      />
                    </el-select>
                  </template>
                </el-table-column>
              </template>
            </el-table>
          </div>
          <el-divider>查询列</el-divider>
          <div class="w-full">
            <el-button-group>
              <el-button  style="font-weight: bold" @click="generateColumnFn(columnConfigType.query)">解析SQL列</el-button>
              <el-button  style="font-weight: bold" @click="generateColumnByShowColumnsFn()">解析展示列</el-button>
              <el-button  style="font-weight: bold" @click="addColumnFn(columnConfigType.query)">新增</el-button>
              <el-button  style="font-weight: bold" @click="deleteColumnFn(columnConfigType.query)">删除</el-button>
            </el-button-group>

            <el-table
              element-loading-text="加载中..."
              :data="columnConfigData.queryColumns"
              stripe
              @selection-change="(val)=>handleColumnSelectionChange(val,columnConfigType.query)">
              <el-table-column type="selection" width="55"  fixed/>
              <el-table-column header-align="center" align="center" label="操作" width="120">
                <template #default="scope">
                  <div class="flex gap-2" style="font-size: 12px">
                    <el-link :underline="false" type="primary" class="text-nowrap" @click="delQueryColumn(scope.row,scope.$index)">删除</el-link>
                    <el-link :underline="false" type="primary" class="text-nowrap" @click="upQueryColumn(scope.row,scope.$index)">上移</el-link>
                    <el-link :underline="false" type="primary" class="text-nowrap" @click="downQueryColumn(scope.row,scope.$index)">下移</el-link>
                  </div>
                </template>
              </el-table-column>
              <template v-for="item in queryColumnConfigColumns" :key="item.prop">
                <!-- 可编辑文本框 -->
                <el-table-column  v-if="item.type === ColumnTypeEnum.INPUT" header-align="center" align="center" :label="item.label" :width="item.width">
                  <template #default="scope">
                    <el-input v-model="scope.row[item.prop]"/>
                  </template>
                </el-table-column>
                <!-- 可编辑选择器-->
                <el-table-column    v-else-if="item.type === ColumnTypeEnum.OPTION" header-align="center" align="center" :label="item.label" :width="item.width">
                  <template #default="scope">
                    <el-select  v-model="scope.row[item.prop]"  filterable>
                      <el-option
                        v-for="(value,key) in columnConfigDictList[item.prop]"
                        :key="key"
                        :label="value"
                        :value="key"
                      />
                    </el-select>
                  </template>
                </el-table-column>
              </template>
            </el-table>
          </div>
        </div>
      </el-dialog>
    </template>
  </commonTable>
</template>

<style scoped lang="scss">
</style>
