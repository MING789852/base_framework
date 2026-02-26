<script setup lang="ts">

import IconifyIconOnline from "@/components/ReIcon/src/iconifyIconOnline";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import {onMounted, ref} from "vue";
import fileTemplateApi from "@/api/fileTemplateApi"
import common from "@/utils/common";
import TableFnClass from "@/class/TableFnClass";


interface FileTemplateProp {
  //操作类型
  actionType: 'write'|'read',
  //查看文件方式
  fileViewActionType?: FileViewActionType
  code: string
}

interface FileTemplateData {
  templateType: string,
  templateFile: string
}

const props = withDefaults(defineProps<FileTemplateProp>(), {
  fileViewActionType: ()=>'PreviewFileComponent'
})
const fileTemplateFlag =  defineModel<boolean>("fileTemplateFlag")

const dictMapping = ref({})
const dataList = ref<FileTemplateData[]>([])
const columns = ref<ColumnDefine[]>([
  {label: "模板类型", prop: "templateType", type: ColumnTypeEnum.INPUT},
  {label: "模板文件", prop: "templateFile", type: ColumnTypeEnum.FILE},
])
let deleteList: FileTemplateData[] = []
const uploadMap = ref(new Map<string, File>())
onMounted( () => {
  columns.value = [
    {label: "模板类型", prop: "templateType", type: ColumnTypeEnum.COMMON},
    {label: "模板文件", prop: "templateFile", type: ColumnTypeEnum.FILE}
  ]
  //加载数据
  common.handleRequestApi(fileTemplateApi.getDataByCode(props.code)).then(dataResponse=>{
    dataList.value = dataResponse.data
  })
})
const tableFn = new TableFnClass()
tableFn.fileAction = {
  fileViewActionTypeFn: ()=> props.fileViewActionType,
  canUploadFileFn:()=>props.actionType === 'write',
  canDeleteFileFn:()=>props.actionType === 'write',
}

const addFn = () => {
  dataList.value.push({
    templateType: '',
    templateFile: '[]'
  })
}

const deleteFn = (row:any,index:number) => {
  let deleteData = dataList.value.splice(index,1)
  deleteList = [...deleteData,...deleteList]
}
const saveFn = () => {
  common.uploadFileMap(uploadMap.value).then(tempIdAndFileIdMap => {
    let data = {
      dataList:dataList.value,
      deleteList:deleteList,
      code:props.code,
      tempIdAndFileIdMap:Object.fromEntries(tempIdAndFileIdMap)
    }
    common.handleRequestApi(fileTemplateApi.saveOrUpdateData(data)).then(res=>{
      if (res.code == 200){
        fileTemplateFlag.value = false
      }
    })
  })
}
</script>

<template>
  <div class="w-full">
    <div v-if="actionType === 'write'" class="w-full flex flex-row justify-start">
      <el-button type="primary" size="small" @click="addFn">新增</el-button>
      <el-button type="primary" size="small" @click="saveFn">保存</el-button>
    </div>
    <div class="w-full mt-5">
      <el-table
        element-loading-text="加载中..."
        :data="dataList"
        stripe
        :border="true">
        <el-table-column v-if="actionType === 'write'" header-align="center" align="center" label="操作" width="100">
          <template #default="scope">
            <div class="w-full h-full flex justify-center">
              <iconify-icon-online icon="ri:delete-bin-5-fill"
                                   class="text-red-600 cursor-pointer w-[20px] h-[20px]"
                                   @click="deleteFn(scope.row,scope.$index)"/>
            </div>
          </template>
        </el-table-column>
        <template v-for="item in columns" :key="item.prop">
          <CommonTableColumn v-model:upload-map="uploadMap" :table-fn="tableFn" :item="item" size="default" :dict-list="dictMapping"/>
        </template>
      </el-table>
    </div>
  </div>
</template>

<style scoped lang="scss">

</style>
