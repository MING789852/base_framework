<script setup lang="ts">

import {onMounted, ref} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import {isNullOrUnDef} from "@pureadmin/utils";
import common from '@/utils/common'

interface UploadFile {
  fileId:string,
  fileName:string,
  tempFileUrl?:string,
  createUser?:string,
  createDate?:Date
}
interface DyFileProp {
  canUploadFn?: ()=>boolean,
  canDeleteFn?: (row:any,index:number)=>boolean
}

const props = withDefaults(defineProps<DyFileProp>(),{
  canDeleteFn: undefined,
  canUploadFn: undefined
})
const tableData = defineModel('tableDataProp',{type:Array<UploadFile>,required:false})
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:true})

const dictMapping = ref({})
onMounted(()=>{
  if (isNullOrUnDef(tableData.value)){
    tableData.value = []
  }
})

const columns=ref<ColumnDefine[]>([
  {label: "文件名称", prop: "fileName", query: false, type: ColumnTypeEnum.COMMON},
  {label: "上传人员",prop: "createUser",query: false, type: ColumnTypeEnum.COMMON},
  {label: "上传日期",prop: "createDate",query: false, type: ColumnTypeEnum.COMMON}
])
const tempFilePrefix = common.getTempFilePrefix()
const uploadFn = () => {
  common.uploadFile((e:any)=>{
    let fileList:File[] = e.target.files
    let tempFileId = tempFilePrefix + common.generateUUID()
    let data:UploadFile = {
      fileId: tempFileId,
      fileName:fileList[0].name
    }
    tableData.value.push(data)
    uploadMap.value.set(tempFileId,fileList[0])
  },false)
}
const previewFn = (row:UploadFile) => {
  common.viewFileById(row.fileId,row.fileName)
}
const downloadFn = (row:UploadFile) => {
  let fileId = row.fileId
  if (fileId.startsWith(tempFilePrefix)){
    let file:File=uploadMap.value.get(fileId)
    let url = common.createObjectURLByFile(file)
    common.downLoadFileByUrl(url,file.name)
    common.revokeObjectURLByUrl(url)
  }else {
    common.downLoadFileById(fileId)
  }
}
const deleteFn = (row,index) => {
  let delRowList:UploadFile[]=tableData.value.splice(index,1)
  let fileId = delRowList[0].fileId
  if (fileId.startsWith(tempFilePrefix)){
    uploadMap.value.delete(fileId)
  }
}
const canUploadFn = () => {
  if (isNullOrUnDef(props.canUploadFn)){
    return true
  }else {
    return props.canUploadFn()
  }
}
const canDeleteFn = (row:any,index:number) => {
  if (isNullOrUnDef(props.canDeleteFn)){
    return true
  }else {
    return props.canDeleteFn(row,index)
  }
}
</script>

<template>
  <div class="w-full">
    <div class="w-full flex justify-start">
      <el-button v-if="canUploadFn()" size="small" type="primary" @click="uploadFn">上传</el-button>
    </div>
    <el-table
      style="font-size: 12px;"
      element-loading-text="加载中..."
      :data="tableData"
      :border="false">
      <el-table-column header-align="center" align="center" label="操作" width="150">
        <template #default="scope">
          <div class="flex gap-2" style="font-size: 12px">
            <el-link :underline="false" type="info" class="text-nowrap" @click="previewFn(scope.row)">预览</el-link>
            <el-link :underline="false" type="primary" class="text-nowrap" @click="downloadFn(scope.row)">下载</el-link>
            <el-link v-if="canDeleteFn(scope.row,scope.$index)" :underline="false" type="danger" class="text-nowrap" @click="deleteFn(scope.row,scope.$index)">删除</el-link>
          </div>
        </template>
      </el-table-column>
      <template v-for="item in columns" :key="item.prop">
        <CommonTableColumn  :item="item" :dict-list="dictMapping"/>
      </template>
    </el-table>
  </div>
</template>

<style scoped lang="scss">

</style>
