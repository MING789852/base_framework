<script setup lang="ts">
import {nextTick, onMounted, ref} from "vue";
import common from "@/utils/common";
import {isNullOrUnDef} from "@pureadmin/utils";
import {message} from "@/utils/message";
import {InputInstance} from "element-plus";
import formLabelApi from "@/api/formLabelApi";
import QueryTypeEnum from "@/enums/QueryTypeEnum";

const labelList=defineModel('labelList',{type:Array<any>,required:false})
onMounted(()=>{
  if (isNullOrUnDef(labelList.value)){
    labelList.value = []
  }
})
const labelInputVisible = ref(false)
const labelInputValue = ref('')
const handleDelLabel = (value,index)=>{
  labelList.value.splice(index,1)
}
const handleLabelInputConfirm = () => {
  if (!common.isStrBlank(labelInputValue.value)){
    //校验标签是否存在
    for (let i=0;i<labelList.value.length;i++){
      if (labelList.value[i].labelName===labelInputValue.value){
        labelInputVisible.value=false
        return message(labelInputValue.value+' 标签已存在',{type:'error'})
      }
    }
    let data = {
      "labelName":labelInputValue.value
    }
    labelList.value.push(data)
    labelInputValue.value=''
    labelInputVisible.value=false
  }else {
    labelInputVisible.value=false
  }
}
const labelInputRef = ref<InputInstance>(null)
const showLabelInput = () => {
  labelInputVisible.value = true
  nextTick(() => {
    labelInputRef.value!.focus()
  })
}
const selectLabel = async () => {
  let res = await formLabelApi.getAllLabelNameMapping()
  let columns: Array<DetailColumnDefine> = [
    {prop: 'labelSelectList', label: '标签选择',type: QueryTypeEnum.MULTIPLE_OPTION, disabled: false}
  ]
  let defaultData = ref({
    labelSelectList:[]
  })
  const dictMapping = {
    "labelSelectList":res.data
  }
  let params:OpenInputDialogDefine = {
    columns: columns,
    dictMapping: dictMapping,
    defaultValue: defaultData,
    callBack: (result) => {
      let labelSelectList:string[]=result.data.labelSelectList
      if(labelSelectList.length===0){
        return message('请选中后操作',{type:'error'})
      }
      //校验标签是否存在
      for (let i=0;i<labelList.value.length;i++){
        if (labelSelectList.includes(labelList.value[i].labelName)){
          return message(labelList.value[i].labelName+' 标签已存在',{type:'error'})
        }
      }
      let labelSelectObjList=labelSelectList.map((value)=>{
        return {
          "labelName": value
        }
      })
      labelList.value.push(...labelSelectObjList)
      result.done()
    }
  }
  common.openInputDialog(params)
}
</script>

<template>
  <div class="flex gap-1 flex-wrap label-edit">
    <el-tag
      v-for="(labelItem,labelIndex) in labelList"
      :key="labelIndex"
      type="primary"
      closable
      :disable-transitions="false"
      @close="handleDelLabel(labelItem,labelIndex)"
    >
      {{ labelItem['labelName'] }}
    </el-tag>
    <el-input
      v-if="labelInputVisible"
      ref="labelInputRef"
      v-model="labelInputValue"
      style="width: 100px"
      size="small"
      @keyup.enter="handleLabelInputConfirm"
      @blur="handleLabelInputConfirm"
    />
    <el-button v-else class="button-new-tag" size="small" @click="showLabelInput">
      新增标签
    </el-button>
    <el-button  class="button-new-tag" size="small" @click="selectLabel">
      选择已有标签
    </el-button>
  </div>
</template>

<style scoped lang="scss">
.label-edit{
  ::v-deep(.el-button+.el-button){
    margin-left: 0;
  }
}
</style>
