<script setup lang="ts">
import {onMounted, onUnmounted, ref} from "vue";
import common from "@/utils/common";
import onlyOffice from '@/components/onlyoffice/onlyoffice.vue'
import {message} from "@/utils/message";

const fileTypesDoc = [
  'doc', 'docm', 'docx', 'dot', 'dotm', 'dotx', 'epub', 'fb2', 'fodt', 'htm', 'html',
  'mht', 'mhtml', 'odt', 'ott', 'rtf', 'stw', 'sxw', 'txt', 'wps', 'wpt', 'xml'
]
const fileTypesCsv = [
  'csv', 'et', 'ett', 'fods', 'ods', 'ots', 'sxc', 'xls', 'xlsb', 'xlsm', 'xlsx', 'xlt', 'xltm', 'xltx', 'xml'
]
const fileTypesPPt = [
  'dps', 'dpt', 'fodp', 'odp', 'otp', 'pot', 'potm', 'potx', 'pps', 'ppsm', 'ppsx', 'ppt', 'pptm', 'pptx', 'sxi'
]
const fileTypesPDF = [
  'djvu', 'docxf', 'oform', 'oxps', 'pdf', 'xps',
]
const onlyOfficeFileTypes = [...fileTypesDoc, ...fileTypesCsv, ...fileTypesPPt, ...fileTypesPDF]

interface PreviewFileProp {
  fileName:string,
  url:string,
  fileId:string
}
const prop = withDefaults(defineProps<PreviewFileProp>(),{
  fileId: "", fileName: "", url: ""
})

const fileType = ref('other')

onMounted(()=>{
  //获取末尾后缀
  let fileExtName = common.getFileExtName(prop.fileName)
  if (["txt", "json", "css", "js", "ts"].includes(fileExtName)) {
    fileType.value = 'text'
  }  else if (onlyOfficeFileTypes.includes(fileExtName)) {
    fileType.value = 'onlyOffice'
  } else {
    message('未知类型，无法预览,请下载查看',{type:'error'})
  }
})

onUnmounted(()=>{
  console.log('关闭预览')
  common.revokeObjectURLByUrl(prop.url)
})
</script>

<template>
  <div class="w-full h-full">
    <iframe v-if="['text'].includes(fileType)" :src="url" width="100%" style="height: calc(100vh - 50px)"/>
    <template v-if="['onlyOffice'].includes(fileType)">
      <div style="height: calc(100vh - 50px);overflow-y: scroll">
        <only-office :file-id="fileId" :file-name="fileName" :url="url"/>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">

</style>