<script setup lang="ts">
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import businessStatusEnum from "@/components/business/enums/businessStatusEnum";
import {taskStatusEnum} from "@/components/flowable/enums/taskStatusEnum";
import fieldTypeEnum from "@/components/business/enums/fieldTypeEnum";
import common from "@/utils/common";
import FlowableDetail from "@/components/flowable/flowableDetail.vue";
import {computed, onMounted, ref} from "vue";
import projectActionTypeEnum from "@/components/business/enums/projectActionTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {message} from "@/utils/message";
import businessApi from "@/api/businessApi";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import ChooseData from "@/components/chooseData/chooseData.vue";
import QueryConditionEnum from "@/enums/QueryConditionEnum";

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
  id: {
    type: String,
    required: true
  },
  actionType: {
    type: String,
    required: true
  },
  formName: {
    type: String,
    required: true,
    default: '评审表'
  }
})

//add新增 update修改 view查看
const actionType = computed(() => props.actionType)
const version = computed(() => props.version)
const businessType = computed(() => props.businessType)
const formName = computed(() => props.formName)

//显示数据
const reviewData = ref<ReviewData>({
  dynamicField: {}
} as ReviewData)
//评审时字段配置
const currentProjectConfig = ref<BusinessProjectConfig[]>([])
//字典表
const dictList = ref({})
//定义emit
const emit = defineEmits(['finish', 'update:modelValue'])

//删除历史
let deleteFileIdList = []
//图片处理
const uploadFileIdMapping = new Map()

const isMobile = ref(false)

onMounted(() => {
  isMobile.value = common.isMobile()
  deleteFileIdList = []
  uploadFileIdMapping.clear()
  let data = {
    id: props.id,
    version: props.version,
    businessType: props.businessType,
    actionType: props.actionType
  }
  common.showGlobalLoading()
  businessApi.selectOne(data).then((res: HttpResult) => {
    if (res.code === 200) {
      reviewData.value = res.data.business
      dictList.value = res.data.dictList
      currentProjectConfig.value = res.data.projectWithItemList
    } else {
      message(res.msg, {type: 'error'})
    }
  }).catch(e => {
    message(e, {type: 'error'})
  }).finally(() => {
    common.closeGlobalLoading()
  })
})

/**
 * 输入权限判断
 */
const disableEdit = (project: BusinessProjectConfig) => {
  return project.disabled
}
const showUpdateButton = (row: ReviewData) => {
  let status = businessStatusEnum[row.status]
  //中止和完成不显示修改按钮
  if ([businessStatusEnum.SUSPEND, businessStatusEnum.FINISH].includes(status)) {
    return false
  }
  return [projectActionTypeEnum.update, projectActionTypeEnum.add].includes(actionType.value);
}
const showApproveButton = (row: ReviewData, roleList: string[]) => {
  let status = businessStatusEnum[row.status]
  if ([projectActionTypeEnum.update, projectActionTypeEnum.add].includes(actionType.value)) {
    return false
  }
  return status === businessStatusEnum.APPROVING && common.authRoleButton(roleList);
}
/**
 * 页面操作
 */
const getFormData = (data:any) => {
  let formData=new FormData()
  formData.append("data",JSON.stringify(data))
  uploadFileIdMapping.forEach((value, key)=>{
    formData.append(key,value)
  })
  formData.append("deleteFileIdList",JSON.stringify(deleteFileIdList))
  return formData
}

const saveBusiness = () => {
  let data = {
    businessType: businessType.value,
    version: version.value,
    businessList: [reviewData.value]
  }
  common.handleRequestApi(businessApi.saveOrUpdateData(getFormData(data))).then(res=>{
    message('操作成功', {type: 'success'})
    emit('finish')
    // emit('update:modelValue', false)
  })
}
const taskBusiness = (status: string) => {
  let msg: string
  if (status == taskStatusEnum.success) {
    msg = '同意'
  } else {
    msg = '拒绝'
  }
  let defaultValue = ref({msg: msg})
  common.openInputDialog([{
    prop: 'msg',
    type: QueryTypeEnum.AREA_INPUT,
    label: '审批意见'
  }], {}, defaultValue,(result)=>{
    let res = result.data
    let msg = res.msg
    if (common.isStrBlank(msg)) {
      return message('审批意见不能为空', {type: 'error'})
    }
    let task = {
      status: status,
      businessList: [reviewData.value],
      msg: msg,
      businessType: businessType.value,
      version: version.value
    }
    common.handleRequestApi(businessApi.taskBusiness(getFormData(task))).then(res=>{
      console.log(res)
      message('操作成功', {type: 'success'})
      result.done()
      emit('finish')
      // emit('update:modelValue', false)
    })
  })
}

/**
 * 客户编码选择
 */
const chooseCusColumns = ref<Array<ColumnDefine>>([
  {
    prop: "code",
    label: "客户编码",
    type: ColumnTypeEnum.COMMON,
    query: true,
    queryType: QueryTypeEnum.INPUT,
    queryCondition: QueryConditionEnum.LIKE
  }
]);
const cusQueryUrl = ref('/trialCustomer/selectByPage')
const cusSelectIsOpen = ref(false)
const openCusSelect = () => {
  cusSelectIsOpen.value = true
}
const closeCusSelect = (val) => {
  cusSelectIsOpen.value = val
}
const chooseCus = (val: Array<any>) => {
  let list = cloneDeep(val)
  if (list.length !== 1) {
    return message('请选中一条数据', {type: "error"})
  }
  let code = list[0].code
  if (!isNullOrUnDef(currentProjectConfig)) {
    currentProjectConfig.value.forEach((value) => {
      if (!isNullOrUnDef(value.businessProjectItemList)) {
        value.businessProjectItemList.forEach((item) => {
          if (fieldTypeEnum[item.fieldType] === fieldTypeEnum.customer_code) {
            reviewData.value.dynamicField[item.fieldKey] = code
          }
        })
      }
    })
  }
  //关闭选择框
  cusSelectIsOpen.value = false
}
/**
 * 供应商选择
 */
const chooseColumns = ref<Array<ColumnDefine>>([
  {
    prop: "supplierCode",
    label: "供应商编码",
    type: ColumnTypeEnum.COMMON,
    query: true,
    queryType: QueryTypeEnum.INPUT,
    queryCondition: QueryConditionEnum.LIKE
  },
  {
    prop: "supplierName",
    label: "供应商名称",
    type: ColumnTypeEnum.COMMON,
    query: true,
    queryType: QueryTypeEnum.INPUT,
    queryCondition: QueryConditionEnum.LIKE
  }
]);
const supQueryUrl = ref('/supplierOutBase/selectByPage')
const supSelectIsOpen = ref(false)
const openSupSelect = () => {
  supSelectIsOpen.value = true
}
const closeSupSelect = (val) => {
  supSelectIsOpen.value = val
}
const chooseSup = (val: Array<any>) => {
  let list = cloneDeep(val)
  if (list.length !== 1) {
    return message('请选中一条数据', {type: "error"})
  }
  let supplierCode = list[0].supplierCode
  let supplierName = list[0].supplierName
  if (!isNullOrUnDef(currentProjectConfig)) {
    currentProjectConfig.value.forEach((value) => {
      if (!isNullOrUnDef(value.businessProjectItemList)) {
        value.businessProjectItemList.forEach((item) => {
          if (fieldTypeEnum[item.fieldType] === fieldTypeEnum.supplier_name) {
            reviewData.value.dynamicField[item.fieldKey] = supplierName
          }
          if (fieldTypeEnum[item.fieldType] === fieldTypeEnum.supplier_code) {
            reviewData.value.dynamicField[item.fieldKey] = supplierCode
          }
        })
      }
    })
  }
  //关闭供应商选择框
  supSelectIsOpen.value = false
}

/**
 * 文件处理
 */
const tempFilePrefix = common.getTempFilePrefix()
type DisplayFileType = {
  id: string,
  fileName: string
}
const downloadFile = (id:string) => {
  if (id.startsWith(tempFilePrefix)){
    let file:File=uploadFileIdMapping.get(id)
    let url = common.createObjectURLByFile(file)
    common.downLoadFileByUrl(url,file.name)
    common.revokeObjectURLByUrl(url)
  }else {
    common.downLoadFileById(id)
  }
}

const deleteFile = (index:number, fieldKey:string) => {
  if (isNullOrUnDef(reviewData.value.dynamicField[fieldKey])){
    reviewData.value.dynamicField[fieldKey] = '[]'
  }
  let displayFileList:DisplayFileType[]=JSON.parse(reviewData.value.dynamicField[fieldKey])
  let delFileList= displayFileList.splice(index,1)
  let delFile = delFileList[0]
  if (!isNullOrUnDef(delFile)){
    if (delFile.id.startsWith(tempFilePrefix)){
      uploadFileIdMapping.delete(delFile.id)
    }else {
      deleteFileIdList.push(delFile.id)
    }
  }
  reviewData.value.dynamicField[fieldKey] = JSON.stringify(displayFileList)
}

const viewFile = (id:string,fileName:string) => {
  common.viewFileById(id,fileName)
}

const uploadFile = (fieldKey:string) => {
  if (isNullOrUnDef(reviewData.value.dynamicField[fieldKey])){
    reviewData.value.dynamicField[fieldKey] = '[]'
  }
  let displayFileList:DisplayFileType[]=JSON.parse(reviewData.value.dynamicField[fieldKey])
  common.uploadFile((e:any)=>{
    let fileList:File[] = e.target.files
    let tempId:string = tempFilePrefix + common.generateUUID()
    uploadFileIdMapping.set(tempId,fileList[0])
    let data = {
      id: tempId,
      fileName:fileList[0].name
    } as DisplayFileType
    displayFileList.push(data)
    reviewData.value.dynamicField[fieldKey] = JSON.stringify(displayFileList)
  },false)
}
const getFileList = (fieldKey: string) => {
  if (isNullOrUnDef(reviewData.value.dynamicField[fieldKey])) {
    reviewData.value.dynamicField[fieldKey] = '[]'
  }
  let fileList: DisplayFileType[] = JSON.parse(reviewData.value.dynamicField[fieldKey])
  return fileList
}
</script>

<template>
  <div>
    <ChooseData v-if="supSelectIsOpen" :columns="chooseColumns" :dict-list="dictList" :query-url="supQueryUrl"
                @close="closeSupSelect" @choose="chooseSup"/>
    <ChooseData v-if="cusSelectIsOpen" :columns="chooseCusColumns" :dict-list="dictList" :query-url="cusQueryUrl"
                @close="closeCusSelect" @choose="chooseCus"/>
    <el-tabs type="border-card">
      <el-tab-pane
        :label="isNullOrUnDef(businessStatusEnum[reviewData.status])?'新增':'状态:' + businessStatusEnum[reviewData.status]">
        <div  class="flex flex-row flex-wrap gap-1 content-center">
          <el-button v-if="showUpdateButton(reviewData)" class="font-bold" size="small" type="primary" @click="saveBusiness">保存</el-button>
          <el-button v-if="showApproveButton(reviewData,['businessReviewer','businessApprover'])" class="font-bold" size="small" type="primary" @click="taskBusiness(taskStatusEnum.success)">批准</el-button>
<!--          <el-button v-if="showApproveButton(reviewData,['businessReviewer'])" class="font-bold" size="small" type="primary" @click="taskBusiness(taskStatusEnum.reject)">驳回</el-button>-->
          <el-button v-if="showApproveButton(reviewData,['businessApprover'])" class="font-bold" size="small" type="primary" @click="taskBusiness(taskStatusEnum.suspend)">不通过(中止)</el-button>
          <el-button v-if="showApproveButton(reviewData,['businessApprover'])" class="font-bold" size="small" type="primary" @click="taskBusiness(taskStatusEnum.rollback)">驳回一级评审</el-button>
          <el-button v-if="showApproveButton(reviewData,['businessApprover'])" class="font-bold" size="small" type="primary" @click="taskBusiness(taskStatusEnum.reject)">驳回发起人</el-button>
        </div>


        <div style="height: 20px"/>
        <div class="w-full flex flex-col items-center">
          <h1>{{ reviewData.createUser }} {{ reviewData.createDate }} {{ formName }}</h1>
        </div>

        <template v-for="project in currentProjectConfig" :key="project.id">
          <div style="height: 20px"/>
          <el-descriptions
            :column="isMobile?1:3"
            :direction="isMobile?'vertical':'horizontal'"
            border
          >
            <template #title>
              <div class="font-bold">{{ project.name }}</div>
            </template>
            <template v-for="projectItem in project.businessProjectItemList" :key="projectItem.id">
              <el-descriptions-item v-if="fieldTypeEnum[projectItem.fieldType] === fieldTypeEnum.input"
                                    :label="projectItem.name">
                <el-input
                  v-model="reviewData.dynamicField[projectItem.fieldKey]"
                  :disabled="disableEdit(project)"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                  type="textarea"
                />
              </el-descriptions-item>
              <el-descriptions-item v-else-if="fieldTypeEnum[projectItem.fieldType] === fieldTypeEnum.areaInput"
                                    :label="projectItem.name">
                <el-input
                  v-model="reviewData.dynamicField[projectItem.fieldKey]"
                  :disabled="disableEdit(project)"
                  :autosize="{ minRows: 2, maxRows: 4 }"
                  type="textarea"
                />
              </el-descriptions-item>
              <el-descriptions-item v-else-if="fieldTypeEnum[projectItem.fieldType] === fieldTypeEnum.option"
                                    :label="projectItem.name">
                <el-select v-model="reviewData.dynamicField[projectItem.fieldKey]" allow-create
                           :disabled="disableEdit(project)" filterable>
                  <el-option
                    v-for="(value,key) in dictList[projectItem.fieldKey]"
                    :key="key"
                    :value="key"
                    :label="value"
                  />
                </el-select>
              </el-descriptions-item>
              <el-descriptions-item v-else-if="fieldTypeEnum[projectItem.fieldType] === fieldTypeEnum.date"
                                    :label="projectItem.name">
                <el-date-picker v-model="reviewData.dynamicField[projectItem.fieldKey]" value-format="YYYY-MM-DD"
                                :disabled="disableEdit(project)" style="width: 100%"/>
              </el-descriptions-item>
              <el-descriptions-item v-else-if="fieldTypeEnum[projectItem.fieldType] === fieldTypeEnum.customer_code"
                                    :label="projectItem.name">
                <el-button :disabled="disableEdit(project)" style="width: 100%" @click="openCusSelect">
                  {{
                    isNullOrUnDef(reviewData.dynamicField[projectItem.fieldKey]) ? '选中' : reviewData.dynamicField[projectItem.fieldKey]
                  }}
                </el-button>
              </el-descriptions-item>
              <el-descriptions-item v-else-if="fieldTypeEnum[projectItem.fieldType] === fieldTypeEnum.supplier_code"
                                    :label="projectItem.name">
                <el-button :disabled="disableEdit(project)" style="width: 100%" @click="openSupSelect">
                  {{
                    isNullOrUnDef(reviewData.dynamicField[projectItem.fieldKey]) ? '选中' : reviewData.dynamicField[projectItem.fieldKey]
                  }}
                </el-button>
              </el-descriptions-item>
              <el-descriptions-item v-else-if="fieldTypeEnum[projectItem.fieldType] === fieldTypeEnum.supplier_name"
                                    :label="projectItem.name">
                <el-button :disabled="disableEdit(project)" style="width: 100%" @click="openSupSelect">
                  {{
                    isNullOrUnDef(reviewData.dynamicField[projectItem.fieldKey]) ? '选中' : reviewData.dynamicField[projectItem.fieldKey]
                  }}
                </el-button>
              </el-descriptions-item>

              <el-descriptions-item v-else-if="fieldTypeEnum[projectItem.fieldType] === fieldTypeEnum.file"
                                    :label="projectItem.name">
                <el-button size="small" type="primary" @click="uploadFile(projectItem.fieldKey)">上传</el-button>
                <div style="height: 5px"/>
                <div class="flex flex-col gap-1">
                  <div v-for="(item,index) in getFileList(projectItem.fieldKey)" :key="index" class="flex justify-between">
                    <a class="cursor-pointer text-blue-600 font-bold text-nowrap overflow-hidden text-ellipsis w-2/5" @click="viewFile(item.id,item.fileName)">{{item.fileName}}</a>
                    <div>
                      <el-button size="small" type="primary" @click="downloadFile(item.id)">下载</el-button>
                      <el-button v-if="!disableEdit(project)" size="small" type="danger"  @click="deleteFile(index,projectItem.fieldKey)">删除</el-button>
                    </div>
                  </div>
                </div>
              </el-descriptions-item>
            </template>
          </el-descriptions>
        </template>
      </el-tab-pane>
      <el-tab-pane v-if="!common.isStrBlank(reviewData.processId)" label="查看审批流程">
        <flowable-detail :process-id="reviewData.processId"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped lang="scss">

</style>
