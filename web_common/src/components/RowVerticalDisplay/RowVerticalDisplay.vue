<script setup lang="ts">
import {onMounted, ref} from "vue";
import CommonTableColumn from "@/components/table/commonTableColumn.vue";
import ColumnTypeEnum from "@/enums/ColumnTypeEnum";
import {isNullOrUnDef} from "@pureadmin/utils";

interface Prop {
  columns: ColumnDefine[],
  row: any,
  dictMapping: any,
  drawerSize?: string,
}
const show = defineModel<boolean>('show', {default:false})
const props=withDefaults(defineProps<Prop>(),{
  dictMapping:()=>{return {}},
  row:()=>{return {}},
  columns:()=>{return []},
  drawerSize:()=>{return '40%'}
})

interface DisplayData{
  name: string,
  value: string
}

const data = ref<DisplayData[]>([])
const columns = ref<Array<ColumnDefine>>([
  {prop: "name", label: "名称", type: ColumnTypeEnum.COMMON_OVER},
  {prop: "value", label: "值", type: ColumnTypeEnum.COMMON_OVER}
]);
const getValueByDic=(row:any,prop:string)=>{
  let dict = props.dictMapping[prop]
  let value = row[prop]
  if (isNullOrUnDef(dict)){
    return value
  }else {
    let dictValue=dict[value]
    if (isNullOrUnDef(dictValue)){
      return value
    }else {
      return dictValue
    }
  }
}
onMounted(()=>{
  let dataList:DisplayData[] = []
  if (props.columns.length!=0){
    props.columns.forEach((column) => {
      if (column.type === ColumnTypeEnum.COMMON){
        dataList.push({name: column.label, value: props.row[column.prop]})
      }else if ([ColumnTypeEnum.DICT_COMMON,ColumnTypeEnum.DICT_TAG,ColumnTypeEnum.MULTIPLE_DICT_COMMON,
        ColumnTypeEnum.OPTION ,ColumnTypeEnum.MULTIPLE_OPTION,ColumnTypeEnum.OPTION_WITH_CREATE,
        ColumnTypeEnum.EDIT_OPTION,ColumnTypeEnum.EDIT_MULTIPLE_OPTION].includes(column.type as ColumnTypeEnum)){
        dataList.push({name: column.label, value: getValueByDic(props.row,column.prop)})
      }
    })
    data.value = dataList
  }
})


</script>

<template>
  <el-drawer v-model="show" :size="drawerSize" append-to-body>
    <template #header>
      <div class="flex justify-between items-center">
        <div style="font-size: 18px;font-weight: bolder;color: black">行数据竖直展示</div>
      </div>
    </template>
    <el-table
        style="font-size: 12px;"
        element-loading-text="加载中..."
        :data="data"
        :border="true">
      <template v-for="item in columns" :key="item.prop">
        <CommonTableColumn  :item="item" :dict-list="dictMapping"/>
      </template>
    </el-table>
  </el-drawer>
</template>

<style scoped lang="scss">

</style>