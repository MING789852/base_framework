<script setup lang="ts">
import DbDyForm from "@/components/DbDyForm/DbDyForm.vue";
import {isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import statusDetailModelApi from '@/api/statusDetailModelApi'
import statusMainModelApi from '@/api/statusMainModelApi'
import {message} from "@/utils/message";
import {onMounted, ref, watch} from "vue";

interface StatusNode {
  formMainModelId?:string,
  formMainInstanceId?:string,
  formData?: any,
  refId: string,
  refType: string,
  statusMainModelId: string,
  statusMainInstanceId: string,
  statusDetailModelId: string,
  active: boolean,
  createDate: string,
  name:string,
  code:string
}

const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:false})
const statusList = defineModel<StatusNode[]>('statusList',{required: false})
const statusInsId = defineModel<string>('statusInsId',{required: false})
const statusModelId = defineModel<string>('statusModelId',{required: false})
if (isNullOrUnDef((statusList.value))){
  statusList.value = []
}
const refreshData = ()=>{
  if (statusInsId.value!=null){
    let data = {
      statusMainInstanceId: statusInsId.value
    }
    common.handleRequestApi(statusMainModelApi.getStatusModelResult(data)).then(modelResult=>{
      statusList.value = modelResult.data.detailInstanceDtoList
    })
  }
}
onMounted(()=>{
  refreshData()
})
watch([statusInsId], (newDateRange, oldDateRange) => {
  refreshData()
})
const statusChange = () => {
  common.handleRequestApi(statusDetailModelApi.getDetailModelMappingByMainModelId(statusModelId.value)).then(dictRes=>{
    let columns: Array<DetailColumnDefine> = [
      {prop: 'statusDetailModelId', label: '状态', type: QueryTypeEnum.OPTION, disabled: false}
    ]
    let defaultData = ref({})
    let dictMapping = ref({
      statusDetailModelId:dictRes.data
    })
    common.openInputDialog(columns, dictMapping, defaultData, (result) => {
      if (common.isStrBlank(result.data.statusDetailModelId)){
        return message('请选中后操作',{type:'error'})
      }
      let data = {
        statusMainModelId: statusModelId.value,
        statusDetailModelId: result.data.statusDetailModelId,
        statusList: statusList.value
      }
      common.handleRequestApi(statusMainModelApi.statusModelChangeAdd(data)).then(res => {
        statusList.value = res.data
        result.done()
      })
    })
  })
}
</script>

<template>
  <el-button class="font-bold" type="primary" size="small" @click="statusChange">状态转换</el-button>
  <el-divider/>
  <el-timeline>
    <el-timeline-item v-for="(item,index) in statusList"  :key="index" :timestamp="item.createDate" placement="top" :type="item.active?'success':'info'">
      <el-card>
        <h2>{{item.name}}</h2>
        <div style="height: 10px;"></div>
        <db-dy-form v-if="!isNullOrUnDef(item.formMainModelId)" v-model:form-data="item.formData"
                    v-model:upload-map="uploadMap"
                    v-model:form-ins-id="item.formMainInstanceId"
                    v-model:form-model-id="item.formMainModelId"/>
      </el-card>
    </el-timeline-item>
  </el-timeline>
</template>

<style scoped lang="scss">

</style>
