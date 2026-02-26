<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import DyForm from "@/components/DyForm/DyForm.vue";
import formMainModelApi from "@/api/formMainModelApi"
import common from "@/utils/common";

const props = defineProps({
  //标签宽度
  labelWith: {type:String,required: false,default: 'auto'},
  //标签位置
  labelPosition: {type:String as PropType<"left" | "right" | "top">,required: false,default: 'right'},
  //行内表单
  inline: {type: Boolean, required: false, default: false},
  //是否禁止编辑(全部)
  disableEdit: {type: Boolean, required: false, default: false}
})
const uploadMap = defineModel('uploadMap',{type:Map<String,File>,required:false})
const formData = defineModel('formData',{required: false, type: Object})
const formInsId = defineModel('formInsId',{required: false, type: String})
const formModelId = defineModel('formModelId',{required: false, type: String})
const dictMapping = defineModel('dictMapping',{required: false, type: Object, default: {} as Object})
const formDefines = ref<Array<DyFormColumn>>([])
const dictQueryType = ref<DictQueryType[]>([])

const refreshData = ()=>{
  if (!common.isStrBlank(formModelId.value)){
    let data = {
      mainModelId: formModelId.value,
      formInsId: formInsId.value
    }
    common.handleRequestApi(formMainModelApi.getFormModelResultById(data)).then(formModelResult=>{
      formData.value = {...formModelResult.data.dataMap,...formData.value}
      formDefines.value = formModelResult.data.formDefines
      dictQueryType.value = formModelResult.data.dictQueryType
    })
  }
}

onMounted(()=>{
  refreshData()
})

watch([formModelId,formInsId], (newDateRange, oldDateRange) => {
    refreshData()
})
</script>

<template>
  <dy-form v-model:upload-map="uploadMap"
           v-model:form-data-prop="formData"
           v-model:dict-mapping="dictMapping"
           :label-with="labelWith"
           :form-defines="formDefines" :inline="inline"
           :label-position="labelPosition"
           :disable-edit="disableEdit"
           :dict-query-type="dictQueryType">
    <template #formItem="{item,index}">
      <slot name="formItem" :item="item" :index="index"/>
    </template>
  </dy-form>
</template>

<style scoped lang="scss">

</style>
