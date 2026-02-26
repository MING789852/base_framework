<script setup lang="ts">
import {message} from "@/utils/message";
import EditImageList from "@/components/EditImageList/editImageList.vue";
import {computed} from "vue";
import {isNullOrUnDef} from "@pureadmin/utils";

interface ImageFile {
  fileId: string,
  fileName: string,
  tempFileUrl?: string,
  createUser?: string,
  createDate?: Date
}

const imgDataProp = defineModel('imgDataProp', {
  get(v: ImageFile[]): ImageFile[] {
    if (isNullOrUnDef(v)) {
      v = []
      imgDataProp.value = v
    }
    return v
  }, required: false, type: Array<ImageFile>
})
const uploadMap = defineModel<Map<String, File>>('uploadMap', {required: true, default: new Map<String, File>()})

const imgData = computed<TcFile[]>({
  get(): TcFile[] {
    let tempList: TcFile[] = []
    imgDataProp.value.forEach(item => {
      tempList.push({
        id: item.fileId,
        fileName: item.fileName,
        originalFileName: item.fileName,
        tempUrl: item.tempFileUrl,
        createUser: item.createUser,
        createDate: item.createDate
      })
    })
    return tempList
  },
  set(list: TcFile[]): void {
    let tempList: ImageFile[] = []
    list.forEach(item => {
      tempList.push({
        fileId: item.id,
        fileName: item.originalFileName,
        tempFileUrl: item.tempUrl,
        createUser: item.createUser,
        createDate: item.createDate
      })
    })
    imgDataProp.value = tempList
  }
})
</script>

<template>
  <edit-image-list v-model:model-value="imgData"
                   v-model:upload-map="uploadMap" :is-json="false" :center="false"/>
</template>

<style scoped lang="scss">
.img-error {
  background: var(--el-fill-color-light);
  color: var(--el-text-color-placeholder);
}

.img-container {
  width: 200px;
  height: 200px;
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
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.img-upload:hover {
  border: 1px dashed var(--el-color-primary);
}

.img-upload-icon {
  color: var(--el-border-color);
  font-size: 24px
}
</style>
