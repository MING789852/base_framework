<template>
  <commonTable ref="commonTableRef" :columns="columns" :queryColumns="queryColumns" :dictList="dictList"
               :api="businessApi" :tableFn="tableFn" :uploadFile="uploadFile" :table-button="tableButton"
               :show-page="showPage" :query-in-header="false">
    <template #button_end>
      <el-button size="small" type="primary" style="font-weight: bold" @click="addFn">新增</el-button>
      <el-button size="small" type="primary" style="font-weight: bold" @click="updateFn">修改</el-button>
      <el-button size="small" type="primary" v-show="common.authRoleButton(['businessStarter'])" style="font-weight: bold" @click="rollBackFn">
        撤回评审
      </el-button>
      <el-button size="small" type="primary" v-show="common.authRoleButton(['businessStarter'])" style="font-weight: bold" @click="startFn">启动评审
      </el-button>
      <el-button size="small" type="primary" style="font-weight: bold" @click="changeDisplay">
        {{ displayTypeEnum.all === displayType ? '展示我的待审批' : '展示全部' }}
      </el-button>
      <el-button size="small" type="primary" style="font-weight: bold" @click="exportExcel">导出excel</el-button>
      <el-button size="small" type="primary" v-show="common.authUserButton(['admin'])" style="font-weight: bold" @click="taskBusinessAll">执行一批
      </el-button>
      <el-button size="small" type="primary" v-show="common.authUserButton(['admin'])" style="font-weight: bold" @click="rollBackAndDeleteDeployment">
        回滚和删除流程定义
      </el-button>
      <el-button size="small" type="primary" v-show="common.authUserButton(['admin'])" style="font-weight: bold" @click="projectConfigOpen">项目配置
      </el-button>
    </template>
    <template #endSelectionColumn>
      <el-table-column header-align="center" align="center" label="操作" width="200px" :fixed="true">
        <template #default="scope">
          <el-button size="small" @click="viewFn(scope.row,projectActionTypeEnum.view)">详情</el-button>
        </template>
      </el-table-column>
    </template>
    <template #column>
      <template v-for="project in projectConfig" :key="project.id">
        <template v-for="projectItem in project.businessProjectItemList" :key="projectItem.id">
          <el-table-column header-align="center" align="center" :label="projectItem.name" width="200px">
            <template #default="scope">
              {{ scope.row.dynamicField[projectItem.fieldKey] }}
            </template>
          </el-table-column>
        </template>
      </template>
    </template>
    <template #dialog>
      <dialog-over :open-or-close-flag="projectConfigFlag">
        <business-project :judgeComponent="true" :businessType="businessType" :version="version"
                          @componentBack="projectConfigClose"/>
      </dialog-over>
      <el-dialog v-if="viewFlag" v-model="viewFlag" append-to-body fullscreen :close-on-click-modal="false">
        <view-data v-model="viewFlag" :id="viewId" :form-name="formName" :action-type="actionType"
                   :business-type="businessType" :version="version" @finish="viewDataFinish"/>
      </el-dialog>
    </template>
  </commonTable>
</template>

<script setup lang="tsx">
import {computed, ref} from "vue";
import viewData from "@/components/business/action/viewData.vue"
import {message} from "@/utils/message";
import businessApi from "@/api/businessApi";
import businessProjectApi from "@/api/businessProjectApi";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import commonTable from "@/components/table/commonTable.vue";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import UploadFileClass from "@/class/UploadFileClass";
import TableFnClass from "@/class/TableFnClass";
import DialogOver from "@/components/DialogOver/dialogOver.vue";
import BusinessProject from '@/components/business/project/index.vue';
import fieldTypeEnum from "@/components/business/enums/fieldTypeEnum";
import common from "@/utils/common";
import businessStatusEnum from "@/components/business/enums/businessStatusEnum";
import projectActionTypeEnum from "@/components/business/enums/projectActionTypeEnum";


const props = defineProps({
  version: {
    type: String,
    required: true,
    default: 'default'
  },
  businessType: {
    type: String,
    required: true
  },
  formName: {
    type: String,
    required: true,
    default: '评审表'
  }
})
const version = computed(() => props.version)
const businessType = computed(() => props.businessType)
const formName = computed(() => props.formName)
/**
 * commonTable参数
 */
const commonTableRef = ref<CommonTableType>(null)
const showPage = ref(true)
const tableFn = new TableFnClass()
const uploadFile = new UploadFileClass([], new Map() as UploadMap, 'deleteFileIdList', 'uploadFileKeyList')
const tableButton = {
  queryFn: true,
  addFn: false,
  saveFn: false,
  deleteFn: true,
  refreshFn: true,
} as CommonTableButton
const columns = ref<Array<ColumnDefine>>([
  {
    prop: "status",
    label: "状态",
    width: '100',
    fixed: true,
    type: ColumnTypeEnum.DICT_COMMON,
    query: true,
    queryType: QueryTypeEnum.MULTIPLE_OPTION,
    queryCondition: QueryConditionEnum.IN
  },
  {
    prop: "businessNo",
    label: "业务编码",
    width: '200',
    fixed: true,
    type: ColumnTypeEnum.COMMON,
    query: true,
    queryType: QueryTypeEnum.INPUT,
    queryCondition: QueryConditionEnum.LIKE
  }
]);
const queryColumns = ref<Array<ColumnDefine>>([
  {
    prop: "status",
    label: "状态",
    type: ColumnTypeEnum.DICT_COMMON,
    query: true,
    queryType: QueryTypeEnum.MULTIPLE_OPTION,
    queryCondition: QueryConditionEnum.IN
  },
  {
    prop: "businessNo",
    label: "业务编码",
    type: ColumnTypeEnum.COMMON,
    query: true,
    queryType: QueryTypeEnum.INPUT,
    queryCondition: QueryConditionEnum.LIKE
  }
]);
const dictList = ref({})
/**
 * 页面事件
 */
//所有字段配置
const projectConfig = ref<BusinessProjectConfig[]>([])
//add新增 update修改 view查看
const actionType = ref('')
//项目配置展示
const projectConfigFlag = ref(false)
//all 展示全部  approve 展示当前已审批
const displayTypeEnum = {
  all: 'all',
  approving: 'approving'
}
const displayType = ref(displayTypeEnum.all)
//显示数据
const viewId = ref('')
const viewFlag = ref(false)

// const reviewProjectIdList = ref([])
tableFn.initFn = async () => {
  common.showGlobalLoading()
  let res: HttpResult = await businessProjectApi.getProjectConfig(businessType.value, version.value)
  //获取字典和字段配置
  if (res.code !== 200) {
    message('获取字典和字段配置失败->' + res.msg, {type: 'error'})
  } else {
    dictList.value = res.data.dictList
    projectConfig.value = res.data.projectWithItemList
  }
  //设置审批状态
  dictList.value['status'] = businessStatusEnum
  commonTableRef.value.getData()
  //配置动态查询字段
  let dynamicQueryList: Array<ColumnDefine> = []
  projectConfig.value.forEach((project) => {
    if (!common.isEmptyArr(project.businessProjectItemList)) {
      project.businessProjectItemList.forEach((value) => {
        if (value.canQuery) {
          let queryType: QueryTypeEnum
          let queryCondition: QueryConditionEnum
          if (fieldTypeEnum[value.fieldType] === fieldTypeEnum.option) {
            queryType = QueryTypeEnum.MULTIPLE_OPTION
            queryCondition = QueryConditionEnum.IN
          } else if (fieldTypeEnum[value.fieldType] === fieldTypeEnum.date) {
            queryType = QueryTypeEnum.DATE_RANGE
            queryCondition = QueryConditionEnum.BETWEEN_DATE
          } else {
            queryType = QueryTypeEnum.INPUT
            queryCondition = QueryConditionEnum.LIKE
          }
          let dynamicQuery: ColumnDefine = {
            prop: 'dynamic_' + value.fieldKey,
            label: value.name,
            type: ColumnTypeEnum.COMMON,
            query: true,
            queryType: queryType,
            queryCondition: queryCondition
          }
          dynamicQueryList.push(dynamicQuery)
        }
      })
    }
  })
  queryColumns.value = [...queryColumns.value, ...dynamicQueryList]
  common.closeGlobalLoading()
}
tableFn.getData = () => {
  commonTableRef.value.loading = true
  let data = {
    queryData: commonTableRef.value.query,
    version: version.value,
    businessType: businessType.value
  }
  let functionName: string
  if (displayType.value === displayTypeEnum.all) {
    functionName = 'selectByPage'
    showPage.value = true
  } else {
    functionName = 'selectApprovingList'
    showPage.value = false
  }
  businessApi[functionName](data).then((res: any) => {
    if (res.code === 200) {
      if (displayType.value === displayTypeEnum.all) {
        commonTableRef.value.query.current = res.data.current
        commonTableRef.value.query.total = res.data.total
        commonTableRef.value.query.size = res.data.size
        commonTableRef.value.tableData = res.data.records
      } else {
        commonTableRef.value.tableData = res.data
      }
    } else {
      message(res.msg, {type: 'error'})
    }
  }).catch(res => {
    message('请求失败->' + JSON.stringify(res), {type: 'error'})
  }).finally(() => {
    commonTableRef.value.loading = false
    commonTableRef.value.isAdd = false
  })
}
const projectConfigOpen = () => {
  projectConfigFlag.value = true
}
const projectConfigClose = () => {
  projectConfigFlag.value = false
}
const changeDisplay = () => {
  if (displayType.value === displayTypeEnum.all) {
    displayType.value = displayTypeEnum.approving
  } else {
    commonTableRef.value.query.current = 1
    displayType.value = displayTypeEnum.all
  }
  tableFn.getData()
}
const taskBusinessAll = () => {
  if (commonTableRef.value.selectTableData.length !== 1) {
    return message('请选中一条数据后操作', {type: 'error'})
  }
  common.showGlobalLoading()
  businessApi.taskBusinessAll(commonTableRef.value.selectTableData[0]).then((res: HttpResult) => {
    if (res.code !== 200) {
      return message(res.msg, {type: 'error'})
    } else {
      message('操作成功', {type: 'success'})
      commonTableRef.value.getData()
    }
  }).finally(() => {
    common.closeGlobalLoading()
  })
}
const rollBackAndDeleteDeployment = () => {
  if (commonTableRef.value.selectTableData.length !== 1) {
    return message('请选中一条数据后操作', {type: 'error'})
  }
  common.showGlobalLoading()
  businessApi.rollBackAndDeleteDeployment(commonTableRef.value.selectTableData[0]).then((res: HttpResult) => {
    if (res.code !== 200) {
      return message(res.msg, {type: 'error'})
    } else {
      message('操作成功', {type: 'success'})
      commonTableRef.value.getData()
    }
  }).finally(() => {
    common.closeGlobalLoading()
  })
}
const rollBackFn = () => {
  if (commonTableRef.value.selectTableData.length !== 1) {
    return message('请选中一条数据后操作', {type: 'error'})
  }
  common.showGlobalLoading()
  businessApi.rollBackBusiness(commonTableRef.value.selectTableData[0]).then((res: HttpResult) => {
    if (res.code !== 200) {
      return message(res.msg, {type: 'error'})
    } else {
      message('操作成功', {type: 'success'})
      commonTableRef.value.getData()
    }
  }).finally(() => {
    common.closeGlobalLoading()
  })
}
const startFn = () => {
  if (commonTableRef.value.selectTableData.length !== 1) {
    return message('请选中一条数据后操作', {type: 'error'})
  }
  common.showGlobalLoading()
  businessApi.startBusiness(commonTableRef.value.selectTableData[0]).then((res: HttpResult) => {
    if (res.code !== 200) {
      return message(res.msg, {type: 'error'})
    } else {
      message('操作成功', {type: 'success'})
      commonTableRef.value.getData()
    }
  }).finally(() => {
    common.closeGlobalLoading()
  })
}
const addFn = () => {
  viewId.value = ''
  actionType.value = projectActionTypeEnum.add
  viewFlag.value = true
}
const updateFn = () => {
  if (commonTableRef.value.selectTableData.length !== 1) {
    return message('请选中一条数据后操作', {type: 'error'})
  }
  viewId.value = commonTableRef.value.selectTableData[0].id
  actionType.value = projectActionTypeEnum.update
  viewFlag.value = true
}
const viewFn = (row: any, type: string) => {
  viewId.value = row.id
  actionType.value = type
  viewFlag.value = true
}
const viewDataFinish = () => {
  viewFlag.value = false
  commonTableRef.value.getData()
}
const exportExcel = () => {
  let data = {
    queryData: commonTableRef.value.query,
    version: version.value,
    businessType: businessType.value
  }
  common.showGlobalLoading()
  businessApi.exportExcel(data).then((res: Blob) => {
    common.handleBlob(res,'导出.xlsx')
  }).finally(() => {
    common.closeGlobalLoading()
  })
}
</script>

<style scoped lang="scss">
.parent {
  height: 100%;
}

.parent > :first-child {
  height: 100%;
}
</style>
