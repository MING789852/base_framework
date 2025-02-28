<template>
  <div  class="flex flex-col justify-start items-center">
    <div class="flex justify-start">
      <el-button v-if="canUploadFile"  size="small" type="primary" @click="uploadFile">上传</el-button>
    </div>
    <div class="flex flex-col w-full">
      <div class="flex justify-between"  v-for="(item, index) in fileList" :key="index">
        <el-tooltip
                    effect="dark"
                    style="white-space: pre-wrap"
                    placement="top">
          <template #content>
            {{item.originalFileName}}
          </template>
          <a style="color: blue;font-weight: bold;white-space: nowrap;overflow: hidden;
              text-overflow: ellipsis;text-align: left" @click="viewFile(item)">
            {{item.originalFileName}}
          </a>
        </el-tooltip>
        <div class="flex gap-2 min-w-[65px]">
          <a v-if="canDownloadFile" class="text-gray-500 cursor-pointer font-bold" size="small" type="info" @click="downloadFile(item)">下载</a>
          <a v-if="canDeleteFile" class="text-red-600 cursor-pointer font-bold" size="small" type="danger" @click="deleteFile(item,index)">删除</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {defineOptions, computed} from "vue";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";
import TableFnClass from "@/class/TableFnClass";

defineOptions({
  name: "editFile"
});
interface EditFileProp {
  //用于传递参数
  tableFn?:TableFnClass,
  row?:any,
  rowIndex?:number,
  propName?:string,

  //用于是否展示按钮
  canUploadFile?: boolean,
  canDownloadFile?: boolean,
  canViewFile?: boolean,
  canDeleteFile?: boolean
}

const  props = withDefaults(defineProps<EditFileProp>(),{
  tableFn(){
    return new TableFnClass()
  },
  row(){
    return {}
  },
  rowIndex(){
    return 0
  },
  propName(){
    return ''
  },
  canUploadFile(){
    return true
  },
  canDownloadFile(){
    return true
  },
  canViewFile(){
    return true
  },
  canDeleteFile(){
    return true
  }
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
  }
  let arrFile:Array<TcFile>
  try {
    arrFile = JSON.parse(modelValue.value)
  }catch (e){
    arrFile = []
  }
  return arrFile
})

//点击删除文件时被调用
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
  if (!props.canViewFile){
    return
  }
  common.viewFileById(item.id,item.originalFileName)
}
const uploadFile = () => {
  if (!isNullOrUnDef(props.tableFn.allowUploadFile)){
    if (!props.tableFn.allowUploadFile(props.row,props.rowIndex,props.propName)){
      return
    }
  }
  common.uploadFile((e:any)=>{
    let uploadFileList:Array<File> = e.target.files
    if (!isNullOrUnDef(uploadFileList)){
      for (let i = 0; i < uploadFileList.length; i++) {
        let file = uploadFileList[i]
        let tempFileId = tempFilePrefix + common.generateUUID()
        let fileName:string = uploadFileList[i].name
        let tempFileList = cloneDeep(fileList.value)
        if (!isNullOrUnDef(props.tableFn.renameUploadFileName)){
          fileName = props.tableFn.renameUploadFileName(props.row,props.rowIndex,tempFileList.length+1,props.propName,fileName)
        }
        let data:TcFile = {
          id: tempFileId,
          originalFileName:fileName,
          tempUrl: common.createObjectURLByFile(file)
        }
        tempFileList.push(data)

        uploadMap.value.set(tempFileId,file)
        modelValue.value = JSON.stringify(tempFileList)
      }
    }
  },true)
}
</script>


<style scoped lang="scss"></style>
