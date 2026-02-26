<template>
  <div  class="flex flex-col justify-start items-center">
    <div class="flex justify-start">
      <el-button v-if="canUploadFile()"  size="small" type="primary" @click="uploadFile">上传</el-button>
    </div>
    <div class="flex flex-col w-full">
      <div class="flex justify-between"  v-for="(item, index) in fileList" :key="index">
        <el-tooltip
                    effect="dark"
                    style="white-space: pre-wrap"
                    placement="top">
          <template #content>
            {{showFileName(item,index)}}
          </template>
          <a style="color: blue;font-weight: bold;white-space: nowrap;overflow: hidden;
              text-overflow: ellipsis;text-align: left" @click="viewFile(item)">
            {{showFileName(item,index)}}
          </a>
        </el-tooltip>
        <div class="flex gap-2 min-w-[65px]">
          <a v-if="canDownloadFile()" class="text-gray-500 cursor-pointer font-bold" size="small" type="info" @click="downloadFile(item)">下载</a>
          <a v-if="canDeleteFile()" class="text-red-600 cursor-pointer font-bold" size="small" type="danger" @click="deleteFile(item,index)">删除</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {defineOptions, computed} from "vue";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";
import {message} from "@/utils/message";

defineOptions({
  name: "editFile"
});
interface EditFileProp {
  //用于传递参数
  fileAction?:FileAction,
  otherProp?: OtherProp,
  disabled?:boolean,
}

const  props = withDefaults(defineProps<EditFileProp>(),{
  otherProp() {
    return {
      data: {},
      dataIndex: 0,
      dataPropName: ''
    }
  },
  disabled:()=>false
})

const tempFilePrefix = common.getTempFilePrefix()
const modelValue = defineModel('modelValue',{type:String,required:false,default:'[]'})
const deleteFileIdList = defineModel('deleteFileIdList',{type:Array<string>,required:false,default:[]})
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:false,default:new Map<String,File>()})


const fileList = computed(()=>{
  if (!isNullOrUnDef(modelValue.value)){
    if (modelValue.value.trim() === ''){
      modelValue.value = '[]'
    }
  }else {
    modelValue.value = '[]'
  }
  let arrFile:Array<TcFile>
  try {
    arrFile = JSON.parse(modelValue.value)
  }catch (e){
    arrFile = []
  }
  return arrFile
})
/*
显示事件
 */
const canUploadFile = () => {
  if (props.disabled){
    return false
  }
  let fileActionEvent:FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: 0,
    fileTotalNum: fileList.value?.length??0,
    fileName: null
  }
  return props.fileAction?.canUploadFileFn?.(fileActionEvent)??true
}
const canDownloadFile = () => {
  let fileActionEvent:FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: 0,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  return props.fileAction?.canDownloadFileFn?.(fileActionEvent)??true
}
const canDeleteFile = () => {
  if (props.disabled){
    return false
  }
  let fileActionEvent:FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: 0,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  return props.fileAction?.canDeleteFileFn?.(fileActionEvent)??true
}
const canViewFile = () => {
  let fileActionEvent:FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: 0,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  return props.fileAction?.canViewFileFn?.(fileActionEvent)??true
}
const fileViewActionType:()=>FileViewActionType = () => {
  let fileActionEvent:FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: 0,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  return props.fileAction?.fileViewActionTypeFn?.(fileActionEvent)??'PreviewFileComponent'
}
const showFileName = (item:TcFile,index:number) =>{
  let fileActionEvent:FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: index,
    fileTotalNum: fileList.value.length,
    fileName: item.originalFileName,
    fileDataProp: item
  }
  return props.fileAction?.showFileName?.(fileActionEvent)??item.originalFileName
}

/*
点击事件
 */
const deleteFile = (item:TcFile,index:number) => {
  let tempFileList:Array<TcFile> = cloneDeep(fileList.value)
  let deleteFile = tempFileList.splice(index,1)
  if (deleteFile[0].id.startsWith(tempFilePrefix)){
    uploadMap.value.delete(deleteFile[0].id)
  }
  deleteFileIdList.value.push(deleteFile[0].id)
  modelValue.value = JSON.stringify(tempFileList)
}
const downloadFile = (item: TcFile) => {
  let fileId = item.id
  if (fileId.startsWith(tempFilePrefix)){
    let file:File=uploadMap.value.get(fileId)
    let url = common.createObjectURLByFile(file)
    common.downLoadFileByUrl(url,file.name)
    common.revokeObjectURLByUrl(url)
  }else {
    common.downLoadFileById(fileId)
  }
}
const viewFile = (item: TcFile) => {
  if (!canViewFile()){
    return
  }
  if (fileViewActionType() === 'PreviewFileComponent'){
    common.viewFileById(item.id,item.originalFileName)
  }else {
    let tempFilePrefix = common.getTempFilePrefix()
    if (item.id.startsWith(tempFilePrefix)) {
      return message('临时文件无法预览,请保存后操作', {type: 'error'})
    }
    let url = common.getViewUrlByFileId(item.id);
    common.openUrl(url)
  }
}
const uploadFile = async () => {
  let fileActionEvent:FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: 0,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  let result=await common.getReturnDataPromiseLike(props.fileAction?.beforeUploadFileFn?.(fileActionEvent)??true)
  if (!result){
    return
  }
  common.uploadFile((e:any)=>{
    let uploadFileList:Array<File> = e.target.files
    if (!isNullOrUnDef(uploadFileList)){
      for (let i = 0; i < uploadFileList.length; i++) {
        let file = uploadFileList[i]
        let tempFileId = tempFilePrefix + common.generateUUID()
        let fileName:string = uploadFileList[i].name
        let tempFileList = cloneDeep(fileList.value)

        let fieActionEvent:FileActionEvent = {
          data: props.otherProp.data,
          rowIndex: props.otherProp.dataIndex,
          propName: props.otherProp.dataPropName,
          fileIndex: i,
          fileTotalNum: tempFileList.length,
          fileName:fileName,
          file:file
        }
        if (props.fileAction?.renameUploadFileName){
          fileName = props.fileAction?.renameUploadFileName(fieActionEvent)
        }
        let data:TcFile = {
          id: tempFileId,
          originalFileName:fileName,
          tempUrl: common.createObjectURLByFile(file)
        }
        tempFileList.push(data)

        uploadMap.value.set(tempFileId,file)
        modelValue.value = JSON.stringify(tempFileList)

        props.fileAction?.afterUploadFileFn?.(fieActionEvent)
      }
    }
  },true)
}
</script>


<style scoped lang="scss"></style>
