<script setup lang="ts">
import fileApplyApi from "@/api/fileApplyApi";
import {computed, onMounted, ref, watch} from "vue";
import DyFormColumnTypeEnum from "@/enums/DyFormColumnTypeEnum";
import common from "@/utils/common";
import {message} from "@/utils/message";
import DyForm from "@/components/DyForm/DyForm.vue";
import {isNullOrUnDef} from "@pureadmin/utils";
const props = defineProps({
  id: {type: String, required: true},
  detailType: {type: String as PropType<'view'>,required:false,default:'view'}
})
const emits = defineEmits(['finish'])
interface FileApplyDetailList {
  fileId:string,
  fileName:string
}
interface FileApplyDetail {
  status?: number,
  effectiveTime?: string,
  detailList?: FileApplyDetailList[]
}

interface FileApplyStatusObj {
  value:number,
  label:string,
  type:'success' | 'warning' | 'info' | 'primary' | 'danger'
}

type FileApplyStatusProp = Record<string, FileApplyStatusObj>

const FileApplyStatus:FileApplyStatusProp = {
  approving: {
    value: 0,
    label: '审批中',
    type: 'warning'
  },
  success: {
    value: 1,
    label: '同意',
    type: 'success'
  },
  reject: {
    value: 2,
    label: '拒绝',
    type: 'danger'
  }
}
const uploadMap = ref(new Map<string,File>())
const detailData = ref<FileApplyDetail>({})
const formItemShowMapping = ref<Record<string, boolean>>({
  //联合申报控制
  operateUser:false,
  operateTime: false
})
const formDefines = ref<Array<DyFormColumn>>([
  {label: "有效时间", prop: "effectiveTime", type: DyFormColumnTypeEnum.DATETIME,required:true},
  {label: "状态",prop: "status", type: 'fileApplyStatus',required:false},
  {label: "申请用户",prop: "userName", type: DyFormColumnTypeEnum.COMMON,required:false},
  {label: "申请时间",prop: "applyTime", type: DyFormColumnTypeEnum.COMMON,required:false},
  {label: "申请备注",prop: "applyRemark", type: DyFormColumnTypeEnum.COMMON,required:false},
  {label: "授权用户",prop: "operateUser", type: DyFormColumnTypeEnum.COMMON,required:false},
  {label: "授权日期",prop: "operateTime", type: DyFormColumnTypeEnum.COMMON,required:false},
  {label: "申请文件", prop: "fileName", type: 'fileListView',required:false}
])

watch(detailData,(newValue,oldValue)=>{
  if (!isNullOrUnDef(detailData.value.status)){
    if (FileApplyStatus.approving.value !== detailData.value.status){
      formItemShowMapping.value = {
        operateUser:true,
        operateTime: true
      }
    }
  }
})

const  detailList= computed<FileApplyDetailList[]>(()=>detailData.value['detailList'])
onMounted(()=>{
  common.handleRequestApi(fileApplyApi.fillData({id:props.id})).then(res=>{
    detailData.value = res.data
  })
})
const operateApply = (row:FileApplyDetail,status:number)=>{
  let data = {
    list: [row],
    applyStatus: status,
    effectiveTime: row.effectiveTime
  }
  common.handleRequestApi(fileApplyApi.fileAuth(data)).then(()=>{
    message('操作成功',{type:'success'})
    emits('finish')
  })
}

const getStatusType = (status:number) => {
  for (let [key, value] of Object.entries(FileApplyStatus)) {
    if (value.value === status){
      return value.type
    }
  }
}
const getStatusLabel = (status:number) => {
  for (let [key, value] of Object.entries(FileApplyStatus)) {
    if (value.value === status){
      return value.label
    }
  }
}
</script>

<template>
  <div class="w-full h-full">
    <div class="flex justify-between items-center">
      <div class="flex gap-1" v-if="FileApplyStatus.approving.value === detailData.status">
        <el-button  type="primary" size="small" @click="operateApply(detailData,1)">同意</el-button>
        <el-button  type="primary" size="small" @click="operateApply(detailData,2)">拒绝</el-button>
      </div>
    </div>
    <el-divider/>
    <DyForm v-model:upload-map="uploadMap" v-model:form-data-prop="detailData" :form-defines="formDefines" :form-item-show-mapping="formItemShowMapping">
      <template #formItem="formItem">
        <el-form-item  v-if="formItem.item.type === 'fileListView'" :required="formItem.item.required" :label="formItem.item.label">
          <div  class="w-full h-full flex-col gap-2">
            <div v-for="(item,index) in detailList">{{item.fileName}}</div>
          </div>
        </el-form-item>
        <el-form-item  v-if="formItem.item.type === 'fileApplyStatus'" :required="formItem.item.required" :label="formItem.item.label">
          <el-tag :type="getStatusType(detailData.status)">{{getStatusLabel(detailData.status)}}</el-tag>
        </el-form-item>
      </template>
    </DyForm>
  </div>
</template>

<style scoped lang="scss">

</style>
