<script setup lang="ts">

import {computed, ref} from "vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import common from '@/utils/common'
import {message} from "@/utils/message";

interface UploadFile {
  fileId:string,
  fileName:string,
  tempFileUrl?:string,
  createUser?:string,
  createDate?:Date
}
interface DyFileProp {
  fileAction?: FileAction
}

const props = withDefaults(defineProps<DyFileProp>(),{})
const tableDataProp = defineModel('tableDataProp',{type:Array<UploadFile>,required:false})
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:true})

const dictMapping = ref({})
const tableData = computed({
  get(){
    if (isNullOrUnDef(tableDataProp.value)){
      tableDataProp.value = []
    }
    return tableDataProp.value
  },
  set(val){
    tableDataProp.value = val
  }
})
const columns=ref<ColumnDefine[]>([
  {label: "文件名称", prop: "fileName", query: false, type: ColumnTypeEnum.COMMON},
  {label: "上传人员",prop: "createUser",query: false, type: ColumnTypeEnum.COMMON},
  {label: "上传日期",prop: "createDate",query: false, type: ColumnTypeEnum.COMMON}
])
const tempFilePrefix = common.getTempFilePrefix()

const fileViewActionType = () => {
  let fileActionEvent:FileActionEvent = {
    data: cloneDeep(tableData.value),
    rowIndex: null,
    fileIndex: null,
    fileTotalNum: null,
    propName: null,
    fileName: null
  }
  return props.fileAction?.fileViewActionTypeFn?.(fileActionEvent)??'PreviewFileComponent'
}
const canUploadFn = () => {
  let fileActionEvent:FileActionEvent = {
    data: cloneDeep(tableData.value),
    rowIndex: null,
    fileIndex: null,
    fileTotalNum: null,
    propName: null,
    fileName: null
  }
  return props.fileAction?.canUploadFileFn?.(fileActionEvent)?? true
}
const canDeleteFn = (row:any,index:number) => {
  let fileActionEvent:FileActionEvent = {
    data: row,
    rowIndex: index,
    propName: null,
    fileIndex: null,
    fileTotalNum: null,
    fileName: null
  }
  return props.fileAction?.canDeleteFileFn?.(fileActionEvent)?? true
}
const canViewFile = (row:any,index:number) => {
  let fileActionEvent:FileActionEvent = {
    data: row,
    rowIndex: index,
    propName: null,
    fileIndex: null,
    fileTotalNum: null,
    fileName: null
  }
  return props.fileAction?.canViewFileFn?.(fileActionEvent)?? true
}
const canDownloadFn = (row:any,index:number) => {
  let fileActionEvent:FileActionEvent = {
    data: row,
    rowIndex: index,
    propName: null,
    fileIndex: null,
    fileTotalNum: null,
    fileName: null
  }
  return props.fileAction?.canDownloadFileFn?.(fileActionEvent)?? true
}
const uploadFn = async () => {
  let fileActionEvent:FileActionEvent = {
    data: cloneDeep(tableData.value),
    rowIndex: null,
    fileIndex: null,
    fileTotalNum: null,
    propName: null,
    fileName: null
  }
  let result=await common.getReturnDataPromiseLike(props.fileAction?.beforeUploadFileFn?.(fileActionEvent)??true)
  if (!result){
    return
  }
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
const previewFn = (row:UploadFile, index:number) => {
  if (!canViewFile(row, index)){
    return
  }
  if (fileViewActionType() === 'PreviewFileComponent'){
    common.viewFileById(row.fileId,row.fileName)
  }else {
    let tempFilePrefix = common.getTempFilePrefix()
    if (row.fileId.startsWith(tempFilePrefix)) {
      return message('临时文件无法预览,请保存后操作', {type: 'error'})
    }
    let url = common.getViewUrlByFileId(row.fileId);
    common.openUrl(url)
  }
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
            <el-link v-if="canViewFile(scope.row,scope.$index)" :underline="'never'" type="info" class="text-nowrap" @click="previewFn(scope.row,scope.$index)">预览</el-link>
            <el-link v-if="canDownloadFn(scope.row,scope.$index)" :underline="'never'" type="primary" class="text-nowrap" @click="downloadFn(scope.row)">下载</el-link>
            <el-link v-if="canDeleteFn(scope.row,scope.$index)" :underline="'never'" type="danger" class="text-nowrap" @click="deleteFn(scope.row,scope.$index)">删除</el-link>
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
