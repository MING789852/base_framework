<script setup lang="ts">
import {isNullOrUnDef} from "@pureadmin/utils";
import common from '@/utils/common'
import {message} from "@/utils/message";

interface ImageFile {
  fileId:string,
  fileName:string,
  tempFileUrl?:string,
  createUser?:string,
  createDate?:Date
}

const imgData = defineModel('imgDataProp',{type:Array<ImageFile>,required:false})
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:true})
const tempFilePrefix = common.getTempFilePrefix()

const addImg = () => {
  if (isNullOrUnDef(imgData.value)){
    imgData.value = []
  }
  common.uploadFile((e:any)=>{
    let fileList:File[] = e.target.files
    let tempFileId = tempFilePrefix + common.generateUUID()
    let data:ImageFile = {
      fileId: tempFileId,
      fileName:fileList[0].name,
      tempFileUrl:common.createObjectURLByFile(fileList[0])
    }
    imgData.value.push(data)
    uploadMap.value.set(tempFileId,fileList[0])
  },false)
}
const downloadImg = (item:ImageFile,index:number) => {
  let fileId = item.fileId
  if (fileId.startsWith(tempFilePrefix)){
    let file:File=uploadMap.value.get(fileId)
    let url = common.createObjectURLByFile(file)
    common.downLoadFileByUrl(url,file.name)
    common.revokeObjectURLByUrl(url)
  }else {
    common.downLoadFileById(fileId)
  }
}
const deleteImg = (item:ImageFile,index:number) => {
  let delImg:ImageFile[] = imgData.value.splice(index,1)
  let fileId = delImg[0].fileId
  if (fileId.startsWith(tempFilePrefix)){
    uploadMap.value.delete(fileId)
    common.revokeObjectURLByUrl(delImg[0].tempFileUrl)
  }
}
const viewFn = (index)=> {
  let convertImgUrlList = convertImgUrlListFn();
  if (convertImgUrlList.length===0){
    return message('无图片可预览',{type:'error'})
  }
  common.showImg(index,convertImgUrlList)
}
const convertImgUrlListFn = () => {
  let imgUrlList = []
  imgData.value.forEach((value)=>{
    imgUrlList.push(convertImgUrlFn(value))
  })
  return imgUrlList
}
const convertImgUrlFn = (data: ImageFile) => {
  if (isNullOrUnDef(data)) {
    return ''
  }
  if (data.fileId.startsWith(tempFilePrefix)) {
    return data.tempFileUrl
  } else {
    return '/file/view?id=' + data.fileId
  }
}

</script>

<template>
  <div class="w-full flex flex-row flex-wrap gap-5">
    <div class="img-container img-upload flex justify-center items-center" @click="addImg">
      <iconify-icon-online icon="material-symbols:add" class="img-upload-icon"/>
    </div>
    <div v-for="(item,index) in imgData" :key="index" class="img-container img-border">
      <el-tooltip
        class="box-item"
        effect="dark"
        placement="top"
      >
        <template #content>
          <div>文件名：{{item.fileName}}</div>
          <template v-if="!isNullOrUnDef(item.createUser)">
            <div>上传人员：{{item.createUser}}</div>
            <div>上传日期：{{item.createDate}}</div>
          </template>
        </template>
        <div class="img-select" @click="viewFn(index)">
          <div class="img-select-header">
            <iconify-icon-online style="font-size: 25px;color: red;font-weight: bold" icon="material-symbols:download-sharp" @click.stop="downloadImg(item,index)"/>
            <iconify-icon-online style="font-size: 25px;color: red;font-weight: bold" icon="material-symbols:delete-outline" @click.stop="deleteImg(item,index)"/>
          </div>
          <div class="img-select-body">
            <div/>
          </div>
        </div>
      </el-tooltip>

      <div class="w-full h-full flex justify-center">
        <el-image
          :src="convertImgUrlFn(item)"
          fit="contain"
          class="w-full"
        >
          <template #error>
            <div class="img-error w-full h-full flex flex-col justify-center items-center">
              <div>非浏览器可展示图片类型</div>
            </div>
          </template>
        </el-image>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .img-error{
    background: var(--el-fill-color-light);
    color: var(--el-text-color-placeholder);
  }
  .img-container{
    width: 200px;
    height: 200px;
    cursor: pointer;
    position: relative;
    transition: var(--el-transition-duration-fast);
  }
  .img-border{
    border: 1px solid var(--el-border-color);
    border-radius: 6px;
    overflow: hidden;
  }
  .img-select{
    z-index: 10;
    width: 100%;
    height: 100%;
    position: absolute;
    visibility: hidden;
    //visibility: visible;
    opacity: 0.6;
    background-color: #ecf5ff;
    transition: var(--el-transition-duration-fast);
    .img-select-header{
      width: 100%;
      display: flex;
      justify-content: flex-end;
      padding-right: 10px;
      padding-top: 10px;
    }
    .img-select-body{
      width: 100%;
    }
  }
  .img-container:hover{
    .img-select{
      visibility: visible;
    }
  }
  .img-upload{
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
  }
  .img-upload:hover{
    border: 1px dashed var(--el-color-primary);
  }
  .img-upload-icon{
    color: var(--el-border-color);
    font-size: 24px
  }
</style>
