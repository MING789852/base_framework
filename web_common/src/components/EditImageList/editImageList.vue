<script setup lang="ts">


import {computed, defineOptions} from "vue";
import common from "@/utils/common";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import {message} from "@/utils/message";

defineOptions({
  name: "editImageList"
});


interface EditImageListProp {
  //用于传递参数
  fileAction?: FileAction,
  imgProp?: ImgProp,
  otherProp?: OtherProp,
  center?: boolean,
  isJson?: boolean,
  disabled?:boolean
}

const props = withDefaults(defineProps<EditImageListProp>(), {
  imgProp() {
    return {
      width: '200px',
      height: '200px',
      border: true,
      fit:'contain',
      onlyShow: false,
      toolTip: true,
      //#ebeef5
      //#dcdfe6
      uploadColor: '#ebeef5'
    } as ImgProp
  },
  otherProp() {
    return {
      data: {},
      dataIndex: 0,
      dataPropName: ''
    }
  },
  isJson() {
    return true
  },
  center() {
    return true
  },
  disabled:()=>false
})
const tempFilePrefix = common.getTempFilePrefix()
const modelValue = defineModel<any>('modelValue', {required: true,
  get(v) {
    if (props.isJson){
      if (common.isStrBlank(v)){
        v='[]'
        modelValue.value = v
      }
    }else {
      if (isNullOrUnDef(v)) {
        v=[]
        modelValue.value = v
      }
    }
    return v;
  }})
const deleteFileIdList = defineModel<string[]>('deleteFileIdList', {required: false, default: []})
const uploadMap = defineModel<Map<String, File>>('uploadMap', {required: false, default: new Map<String, File>()})

const fileList = computed<TcFile[]>({
  get(oldValue){
    let arrFile: TcFile[]
    if (props.isJson){
      try {
        arrFile = JSON.parse(modelValue.value)
      } catch (e) {
        arrFile = []
      }
    }else {
      arrFile = modelValue.value
    }
    return arrFile
  },
  set<S>(newValue: S): void {
    if (props.isJson){
      modelValue.value = JSON.stringify(newValue)
    }else {
      modelValue.value = newValue
    }
  }
})

/*
权限管控
 */
const canUploadFile = (index) => {
  if (props.disabled){
    return false
  }
  if (props.imgProp.onlyShow){
    return false
  }
  let fileActionEvent: FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: index,
    fileTotalNum: fileList.value?.length ?? 0,
    fileName: null
  }
  return props.fileAction?.canUploadFileFn?.(fileActionEvent) ?? true
}
const canDownloadFile = (index) => {
  if (props.imgProp.onlyShow){
    return false
  }
  let fileActionEvent: FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: index,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  return props.fileAction?.canDownloadFileFn?.(fileActionEvent) ?? true
}
const canDeleteFile = (index) => {
  if (props.disabled){
    return false
  }
  if (props.imgProp.onlyShow){
    return false
  }
  let fileActionEvent: FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: index,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  return props.fileAction?.canDeleteFileFn?.(fileActionEvent) ?? true
}
const canViewFile = (index) => {
  let fileActionEvent: FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: index,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  return props.fileAction?.canViewFileFn?.(fileActionEvent) ?? true
}


/*
界面操作
 */
const setData = (list: TcFile[]) => {
  fileList.value = list
}

const deleteFile = (item: TcFile, index: number) => {
  let tempFileList: Array<TcFile> = cloneDeep(fileList.value)
  let deleteFile = tempFileList.splice(index, 1)
  if (deleteFile[0].id.startsWith(tempFilePrefix)) {
    uploadMap.value.delete(deleteFile[0].id)
  }
  deleteFileIdList.value.push(deleteFile[0].id)
  setData(tempFileList)
}
const downloadFile = (item: TcFile, index: number) => {
  let fileId = item.id
  if (fileId.startsWith(tempFilePrefix)) {
    let file: File = uploadMap.value.get(fileId)
    let url = common.createObjectURLByFile(file)
    common.downLoadFileByUrl(url, file.name)
    common.revokeObjectURLByUrl(url)
  } else {
    common.downLoadFileById(fileId)
  }
}
const viewFile = (item: TcFile, index: number) => {
  if (!canViewFile(index)) {
    return
  }
  let convertImgUrlList = convertImgUrlListFn();
  if (convertImgUrlList.length === 0) {
    return message('无图片可预览', {type: 'error'})
  }
  common.showImg(index, convertImgUrlList)
}
const uploadFile = async () => {
  let fileActionEvent: FileActionEvent = {
    data: props.otherProp.data,
    rowIndex: props.otherProp.dataIndex,
    propName: props.otherProp.dataPropName,
    fileIndex: 0,
    fileTotalNum: fileList.value.length,
    fileName: null
  }
  let result = await common.getReturnDataPromiseLike(props.fileAction?.beforeUploadFileFn?.(fileActionEvent) ?? true)
  if (!result) {
    return
  }
  common.uploadFile((e: any) => {
    let uploadFileList: Array<File> = e.target.files
    if (!isNullOrUnDef(uploadFileList)) {
      for (let i = 0; i < uploadFileList.length; i++) {
        let file = uploadFileList[i]
        let tempFileId = tempFilePrefix + common.generateUUID()
        let fileName: string = uploadFileList[i].name
        let tempFileList = cloneDeep(fileList.value)

        let fieActionEvent: FileActionEvent = {
          data: props.otherProp.data,
          rowIndex: props.otherProp.dataIndex,
          propName: props.otherProp.dataPropName,
          fileIndex: i,
          fileTotalNum: tempFileList.length,
          fileName: fileName,
          file: file
        }
        if (props.fileAction?.renameUploadFileName) {
          fileName = props.fileAction?.renameUploadFileName(fieActionEvent)
        }
        let data: TcFile = {
          id: tempFileId,
          originalFileName: fileName,
          tempUrl: common.createObjectURLByFile(file)
        }
        tempFileList.push(data)

        uploadMap.value.set(tempFileId, file)
        setData(tempFileList)
        props.fileAction?.afterUploadFileFn?.(fieActionEvent)
      }
    }
  }, true)
}
const convertImgUrlListFn = () => {
  let imgUrlList = []
  fileList.value.forEach((value) => {
    imgUrlList.push(convertImgUrlFn(value))
  })
  return imgUrlList
}
const convertImgUrlFn = (data: TcFile) => {
  if (isNullOrUnDef(data)) {
    return ''
  }
  if (data.id.startsWith(tempFilePrefix)) {
    return data.tempUrl
  } else {
    return common.getViewUrlWithCacheControlByFileId(data.id)
  }
}
</script>

<template>
  <div :class="center?'w-full flex flex-row flex-wrap gap-2 justify-center':'w-full flex flex-row flex-wrap gap-2'">
    <div v-if="canUploadFile(0)" class="img-container img-upload flex justify-center items-center" @click="uploadFile">
      <iconify-icon-online icon="material-symbols:add" class="img-upload-icon"/>
    </div>
    <div v-for="(item,index) in fileList" :key="index"
         :class="imgProp?.border? 'img-container img-border':'img-container'">
      <el-tooltip
          :disabled="!imgProp.toolTip"
          class="box-item"
          effect="dark"
          placement="top"
      >
        <template #content>
          <div>文件名：{{ item.originalFileName }}</div>
          <template v-if="!isNullOrUnDef(item.createUser)">
            <div>上传人员：{{item.createUser}}</div>
            <div>上传日期：{{item.createDate}}</div>
          </template>
        </template>
        <div class="img-select" @click="viewFile(item,index)">
          <div class="img-select-header">
            <iconify-icon-online v-if="canDownloadFile(index)" style="font-size: 25px;color: red;font-weight: bold"
                                 icon="material-symbols:download-sharp" @click.stop="downloadFile(item,index)"/>
            <iconify-icon-online v-if="canDeleteFile(index)" style="font-size: 25px;color: red;font-weight: bold"
                                 icon="material-symbols:delete-outline" @click.stop="deleteFile(item,index)"/>
          </div>
          <div class="img-select-body">
            <div/>
          </div>
        </div>
      </el-tooltip>

      <div class="w-full h-full flex justify-center">
        <el-image
            :src="convertImgUrlFn(item)"
            :fit="imgProp.fit"
            class="w-full"
        >
          <template #error>
            <div class="img-error w-full h-full flex flex-col justify-center items-center">
              <div>{{item.originalFileName}}</div>
              <div>非浏览器可展示图片类型</div>
            </div>
          </template>
        </el-image>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.img-error {
  background: var(--el-fill-color-light);
  color: var(--el-text-color-placeholder);
}

.img-container {
  width: v-bind('imgProp.width');
  height: v-bind('imgProp.height');
  cursor: pointer;
  position: relative;
  transition: var(--el-transition-duration-fast);
}

.img-border {
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
}

.img-select {
  z-index: 10;
  width: 100%;
  height: 100%;
  position: absolute;
  visibility: hidden;
  //visibility: visible;
  opacity: 0.6;
  background-color: #ecf5ff;
  transition: var(--el-transition-duration-fast);

  .img-select-header {
    width: 100%;
    display: flex;
    justify-content: flex-end;
    padding-right: 10px;
    padding-top: 10px;
  }

  .img-select-body {
    width: 100%;
  }
}

.img-container:hover {
  .img-select {
    visibility: visible;
  }
}

.img-upload {
  border-width: 1px;
  border-style: dashed;
  border-color: v-bind('imgProp.uploadColor');
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.img-upload:hover {
  border: 1px dashed var(--el-color-primary);
}

.img-upload-icon {
  color: v-bind('imgProp.uploadColor');
  font-size: 24px
}
</style>