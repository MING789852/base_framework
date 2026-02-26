<script setup lang="ts">

import {computed, nextTick, onMounted, ref} from "vue";
import common from "@/utils/common";
import {cloneDeep, isNullOrUnDef} from "@pureadmin/utils";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import QueryConditionEnum from "@/enums/QueryConditionEnum";
import {message} from "@/utils/message";
import ChooseData from "@/components/chooseData/chooseData.vue";

interface Prop{
  multiple:boolean,
  isJson?:boolean,
  withProp?:boolean,
  propList?:Array<DetailColumnDefine>,
  dictMapping?:any
}
const props =withDefaults(defineProps<Prop>(),{
  multiple:()=>false,
  isJson:()=>false,
  withProp: ()=>false,
  propList:()=>[],
  dictMapping:()=>{return {}}
})
const buttonName = ref('+新增用户')
const dataListProp = defineModel<Array<ChooseUserProp>|string>('dataList',{required:false})
const dataList = computed<Array<ChooseUserProp>>({
  get() {
    if (props.isJson){
      let json:string = dataListProp.value as string
      if (!isNullOrUnDef(json)){
        return JSON.parse(json)
      }else {
        return []
      }
    }else {
      return dataListProp.value
    }
  },
  set(val) {
    if (props.isJson){
      dataListProp.value = JSON.stringify(val)
      console.log(dataList.value)
    }else {
      dataListProp.value = val
    }
  }
})

onMounted(async () => {
  if (isNullOrUnDef(dataList.value)) {
    dataList.value = []
    await nextTick()
  }
})
const handleDelete = (index:number)=>{
  dataList.value = dataList.value.filter((_, i) => i !== index)
}
const allowAdd = computed(() => {
  if (props.multiple){
    return true
  }else {
    return common.isEmptyArr(dataList.value);
  }
})

/**
 * 显示propList
 */
const showPropsValue = (data)=>{
  let propsShowStrList:string[]=[]
  if (props.withProp){
    if (!common.isEmptyArr(props.propList)){
      props.propList.forEach((value)=>{
        if (!isNullOrUnDef(data[value.prop])){
          propsShowStrList.push(`${value.label} : ${data[value.prop]}`)
        }
      })
    }
  }
  if (common.isEmptyArr(propsShowStrList)){
    return ''
  }else {
    return `(${propsShowStrList.join(',')})`
  }
}

/**
 * 用户选择
 */
const chooseDictMapping = ref({})
const chooseColumns = ref<Array<ColumnDefine>>([
  {prop: "nickName", label: "姓名", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE},
  {prop: "jobNumber", label: "工号", type: ColumnTypeEnum.COMMON, query: true, queryType: QueryTypeEnum.INPUT, queryCondition: QueryConditionEnum.LIKE}
]);
const queryUrl = ref('/user/selectByPage')
const selectIsOpen = ref(false)

const openSelect = () => {
  selectIsOpen.value = true
}
const closeSelect = (val) => {
  console.log(val)
  selectIsOpen.value = false
}
const chooseConfirm =  (val: Array<ChooseUserProp>) => {
  if (val.length===0){
    return message('请选中数据后操作',{type:'error'})
  }
  if (props.withProp){
    if (val.length!==1){
      return message('请选中一条数据',{type:'error'})
    }
  }
  if (!props.multiple){
    if (val.length!==1){
      return message('请选中一条数据',{type:'error'})
    }
  }
  let list:Array<ChooseUserProp> = cloneDeep(val)
  list = list.map(item=>{
    return {
      id:item.id,
      nickName:item.nickName,
      jobNumber:item.jobNumber
    }
  })
  if (props.withProp){
    let userPropList:Array<DetailColumnDefine> = [
      {prop: "nickName", label: "姓名", type: QueryTypeEnum.COMMON},
      {prop: "jobNumber", label: "工号", type: QueryTypeEnum.COMMON}
    ]
    userPropList = [...userPropList,...props.propList]
    let params:OpenInputDialogDefine = {
      columns: userPropList,
      dictMapping: props.dictMapping,
      defaultValue: list[0],
      callBack: (result) => {
        dataList.value = [...dataList.value,...[result.data]]
        result.done()
        //关闭用户选择框
        selectIsOpen.value = false
      }
    }
    common.openInputDialog(params)
  }else {
    dataList.value =[...dataList.value,...list]
    //关闭用户选择框
    selectIsOpen.value = false
  }
}
</script>

<template>
  <div class="w-full h-full flex flex-wrap gap-2">
    <el-button v-if="allowAdd" class="button-new-tag" size="small" @click="openSelect">
      {{buttonName}}
    </el-button>
    <el-tag
        v-for="(item,index) in dataList"
        :key="index"
        closable
        :disable-transitions="false"
        @close="handleDelete(index)"
    >
      {{ item.nickName }}
      <template v-if="props.withProp">
        {{showPropsValue(item)}}
      </template>
    </el-tag>
    <ChooseData v-if="selectIsOpen"
                :query-in-header="true"
                :columns="chooseColumns"
                :dict-list="chooseDictMapping"
                :query-url="queryUrl"
                @close="closeSelect"
                @choose="chooseConfirm"/>
  </div>
</template>

<style scoped lang="scss">

</style>