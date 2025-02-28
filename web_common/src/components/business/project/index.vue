<template>
  <common-table ref="commonTableRef" :columns="columns" :queryColumns="columns" :dictList="dictList"
               :api="businessProjectApi" :tableFn="tableFn" :uploadFile="uploadFile" :query-in-header="false" :table-button="tableButton">
    <template #button_start>
      <el-button v-if="judgeComponent" class="font-bold" size="small" type="primary" @click="componentBack">返回</el-button>
    </template>
    <template #button_end>
      <el-button v-if="judgeComponent" class="font-bold" size="small" type="primary" @click="startConfig">发起项目配置</el-button>
      <el-button v-if="judgeComponent" class="font-bold" size="small" type="primary" @click="reviewConfig">评审项目配置</el-button>
      <el-button v-if="judgeComponent" class="font-bold" size="small" type="primary" @click="approveConfig">审核人员配置</el-button>
    </template>
    <template #endSelectionColumn>
      <el-table-column   header-align="center" align="center" label="操作" width="100px">
        <template #default="scope">
          <el-button v-if="!isNullOrUnDef(scope.row.id)" size="small" @click="openProjectItemConfig(scope.row)">详情</el-button>
        </template>
      </el-table-column>
    </template>
    <template #dialog>
      <dialog-over :openOrCloseFlag="projectItemConfigFlag">
        <business-project-item v-model:show = "projectItemConfigFlag" :version="version"
                               :parentRow="parentRow" :businessType="businessType" :judgeComponent="true"/>
      </dialog-over>
    </template>
  </common-table>
</template>

<script setup lang="tsx">
import {ref} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import businessProjectApi from "@/api/businessProjectApi";
import businessConfigApi from "@/api/businessConfigApi";
import TableFnClass from "@/class/TableFnClass";
import UploadFileClass from "@/class/UploadFileClass";
import commonTable from "@/components/table/commonTable.vue";
import common from "@/utils/common";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import {message} from "@/utils/message";
import DialogOver from "@/components/DialogOver/dialogOver.vue";
import BusinessProjectItem from '@/components/business/project/item.vue';
import DetailForm from "@/components/detailForm/detailForm.vue";
import {addDialog} from "@/components/ReDialog/index";
import configCodeEnum from "@/components/business/enums/configCodeEnum";
import systemUserApi from "@/api/systemUserApi";

defineOptions({
  name: "business_project"
});
const props = defineProps({
  judgeComponent: {type: Boolean, required: true, default: false},
  version: {type: String, default: 'default'},
  businessType: {type: String}
})
const emits = defineEmits(['componentBack'])

let commonTableRef = ref<CommonTableType>(null)
const tableButton = {
  addFn: true,
  deleteFn: true,
  exportExcelFn: false,
  initQueryFn: false,
  queryFn: true,
  refreshFn: true,
  saveFn: true
} as CommonTableButton
const tableFn = new TableFnClass()
const uploadFile = new UploadFileClass([], new Map() as UploadMap, 'deletFileIdList', 'uploadFileKeyList')
const columns = ref<Array<ColumnDefine>>([
  {prop: "sequence", label: "序号",width: 200, type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "name", label: "项目名称", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "code", label: "项目编码", type: ColumnTypeEnum.EDIT_INPUT, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "executor", label: "执行人员", type: ColumnTypeEnum.EDIT_OPTION, query: true, queryType: QueryTypeEnum.OPTION, queryCondition: QueryConditionEnum.IN},
  {prop: "createUser", label: "创建人员", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "createDate", label: "创建时间", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.DATE, queryCondition: QueryConditionEnum.EQ},
  {prop: "updateUser", label: "更新人员", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.EQ},
  {prop: "updateDate", label: "更新时间", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.DATE, queryCondition: QueryConditionEnum.EQ}
]);
const dictList = ref({})
const userIdAndNickNameDict = ref({})

tableFn.initFn = async () => {
  //获取字典
  let res: any = await systemUserApi.getUserIdAndNickNameMapping()
  let temp = {}
  if (res.code === 200) {
    userIdAndNickNameDict.value = res.data
    temp['executor'] = userIdAndNickNameDict.value
    dictList.value = temp
  }
  commonTableRef.value.getData()
}

/**
 * 页面事件
 */

const projectItemConfigFlag = ref(false)
const parentRow=ref({});

const openProjectItemConfig = (row) => {
  parentRow.value = row
  projectItemConfigFlag.value = true
}

const fillData = (data:any[]) => {
  if (common.isEmptyArr(data)){
    return []
  }
  data.forEach((value)=>{
    value.version = props.version
    value.businessType = props.businessType
  })
}

tableFn.saveFn = () => {
  let list = cloneDeep(commonTableRef.value.tableData);
  fillData(list)
  commonTableRef.value.loading = true
  businessProjectApi.saveOrUpdateData(list).then((res:any) => {
    if (res.code === 200) {
      message('操作成功',{type:'success'})
      commonTableRef.value.getData()
    } else  {
      message(res.msg,{type:'error'})
    }
  }).catch(res => {
    message('请求失败->' + JSON.stringify(res),{type:'error'})
  }).finally(() => {
    commonTableRef.value.loading = false
  })
}

const componentBack = () => {
  emits('componentBack')
}

/**
 * 人员配置
 */
const configData = ref({})
const configColumName = 'config'
const configColumns: Array<DetailColumnDefine> = [
  {prop: configColumName, label: "配置值", type: QueryTypeEnum.MULTIPLE_OPTION, placeholder: ''}
];
const configDictList = ref({})

const startConfig = async () => {
  //获取配置
  let configRes:HttpResult = await businessConfigApi.getConfig(configCodeEnum.startConfig,props.businessType)
  if (configRes.code !== 200) {
    return message('获取配置失败->' + configRes.msg, {type: 'error'})
  }
  let resData = configRes.data
  if (!isNullOrUnDef(resData)){
    let reviewConfig = resData.value
    if (!common.isStrBlank(reviewConfig)){
      configData.value[configColumName] = JSON.parse(reviewConfig)
    }else {
      configData.value[configColumName] = []
    }
  }else {
    configData.value[configColumName] = []
  }
  //设置字典
  let tempDict = {}
  commonTableRef.value.tableData.forEach((value) => {
    // tempDict[value.name] = value.id
    tempDict[value.id] = value.name
  })
  configDictList.value[configColumName] = tempDict

  addDialog({
    width: "40%",
    title: "发起项目配置",
    props: {
      columns: configColumns,
      propData: configData,
      dictList: configDictList
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, {options}) {
      let propData: any = options.props.propData
      if (common.isEmptyArr(propData[configColumName])){
        return message('请选中后操作',{type: 'error'})
      }
      let data = {
        value: JSON.stringify(propData[configColumName]),
        code: configCodeEnum.startConfig,
        businessType: props.businessType,
        des: '发起项目配置'
      }
      businessConfigApi.saveConfig(data).then((res: any) => {
        if (res.code === 200) {
          message('操作成功', {type: 'success'})
          done()
        } else {
          message(res.msg, {type: 'error'})
        }
      }).catch(e => {
        message(e, {type: 'error'})
      })
    }
  })
}

const reviewConfig = async () => {
  //获取配置
  let configRes:HttpResult = await businessConfigApi.getConfig(configCodeEnum.reviewConfig,props.businessType)
  if (configRes.code !== 200) {
    return message('获取配置失败->' + configRes.msg, {type: 'error'})
  }
  let resData = configRes.data
  if (!isNullOrUnDef(resData)){
    let reviewConfig = resData.value
    if (!common.isStrBlank(reviewConfig)){
      configData.value[configColumName] = JSON.parse(reviewConfig)
    }else {
      configData.value[configColumName] = []
    }
  }else {
    configData.value[configColumName] = []
  }
  //设置字典
  let tempDict = {}
  commonTableRef.value.tableData.forEach((value) => {
    tempDict[value.id] = value.name
    // tempDict[value.name] = value.id
  })
  configDictList.value[configColumName] = tempDict

  addDialog({
    width: "40%",
    title: "评审项目配置",
    props: {
      columns: configColumns,
      propData: configData,
      dictList: configDictList
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, {options}) {
      let propData: any = options.props.propData
      if (common.isEmptyArr(propData[configColumName])){
        return message('请选中后操作',{type: 'error'})
      }
      let data = {
        value: JSON.stringify(propData[configColumName]),
        code: configCodeEnum.reviewConfig,
        businessType: props.businessType,
        des: '评审项目配置'
      }
      businessConfigApi.saveConfig(data).then((res: any) => {
        if (res.code === 200) {
          message('操作成功', {type: 'success'})
          done()
        } else {
          message(res.msg, {type: 'error'})
        }
      }).catch(e => {
        message(e, {type: 'error'})
      })
    }
  })
}


const approveConfig = async () => {
  //获取配置
  let configRes:HttpResult = await businessConfigApi.getConfig(configCodeEnum.approveConfig,props.businessType)
  if (configRes.code !== 200) {
    return message('获取配置失败->' + configRes.msg, {type: 'error'})
  }
  let resData = configRes.data
  if (!isNullOrUnDef(resData)){
    let reviewConfig = resData.value
    if (!common.isStrBlank(reviewConfig)){
      configData.value[configColumName] = JSON.parse(reviewConfig)
    }else {
      configData.value[configColumName] = []
    }
  }else {
    configData.value[configColumName] = []
  }
  //设置字典
  // configDictList.value[configColumName] = common.reverseDict(userIdAndNickNameDict.value)
  configDictList.value[configColumName] = userIdAndNickNameDict.value

  addDialog({
    width: "40%",
    title: "审核人员配置",
    props: {
      columns: configColumns,
      propData: configData,
      dictList: configDictList
    },
    contentRenderer: () => DetailForm,
    beforeSure(done, {options}) {
      let propData: any = options.props.propData
      if (common.isEmptyArr(propData[configColumName])){
        return message('请选中后操作',{type: 'error'})
      }
      let data = {
        value: JSON.stringify(propData[configColumName]),
        code: configCodeEnum.approveConfig,
        businessType: props.businessType,
        des: '审核人员配置'
      }
      businessConfigApi.saveConfig(data).then((res: any) => {
        if (res.code === 200) {
          message('操作成功', {type: 'success'})
          done()
        } else {
          message(res.msg, {type: 'error'})
        }
      }).catch(e => {
        message(e, {type: 'error'})
      })
    }
  })
}
</script>

<style scoped lang="scss">
  .parent {
    height: 100%;
  }
  .parent> :first-child{
    height: 100%;
  }
</style>
