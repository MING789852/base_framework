<script setup lang="ts">
import { DocumentEditor } from "@onlyoffice/document-editor-vue";
import {ref} from "vue";
import {IConfig} from "@onlyoffice/document-editor-vue/lib/model/config";
import common from "@/utils/common";
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
const getDocumentType = (fileType:string)=> {
  let docType = ''
  if (fileTypesDoc.includes(fileType)) {
    docType = 'word'
  }
  if (fileTypesCsv.includes(fileType)) {
    docType = 'cell'
  }
  if (fileTypesPPt.includes(fileType)) {
    docType = 'slide'
  }
  if (fileTypesPDF.includes(fileType)) {
    docType = 'pdf'
  }
  return docType
}
interface OnlyOfficeFile {
  fileName:string,
  url:string,
  fileId:string,
}
const props=defineProps<OnlyOfficeFile>()
const fileExtName = common.getFileExtName(props.fileName);
const documentType=getDocumentType(fileExtName)
const config = ref<IConfig>({
  document: {
    fileType: fileExtName,
    key: props.fileId,
    title: props.fileName,
    url: props.url
  },
  type: "desktop",
  height: "100%",
  width: "100%",
  documentType: documentType,
  editorConfig: {lang: "zh-CN", mode: "view"}
})
const onDocumentReady = () => {
  console.log("Document is loaded");
}
</script>

<template>
  <DocumentEditor
      id="docEditor"
      documentServerUrl="http://192.168.1.72:9997/"
      :config="config"
      :events_onDocumentReady="onDocumentReady"
  />
</template>

<style scoped lang="scss">

</style>