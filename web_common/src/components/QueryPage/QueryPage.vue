<script setup lang="ts">
import TableFnClass from "@/class/TableFnClass";
import {isNullOrUnDef} from "@pureadmin/utils";

interface Props {
  layout?: string,
  pageSizeChangeList?: number[],
  background?: boolean,
  tableFn?: TableFnClass,
  size?: 'large' | 'default' | 'small',
  getData: ()=>any,
}
const props = withDefaults(defineProps<Props>(), {
  layout: ()=>'total, sizes, prev, pager, next, jumper',
  pageSizeChangeList:()=>[10, 15, 20, 50, 100, 200],
  background: ()=>false,
  tableFn: ()=>new TableFnClass(),
  size: ()=>'default',
  getData: ()=>{},
})
const query = defineModel<QueryDefine>('query', {required:false})
const handleSizeChange = (val: number) => {
  if (!isNullOrUnDef(props.tableFn.handleSizeChange)) {
    props.tableFn.handleSizeChange(val)
  } else {
    query.value.current = 1
    query.value.size = val
    props.getData()
  }
}
const handleCurrentChange = (val: number) => {
  if (!isNullOrUnDef(props.tableFn.handleCurrentChange)) {
    props.tableFn.handleCurrentChange(val)
  } else {
    query.value.current = val
    props.getData()
  }
}
</script>

<template>
  <el-pagination
      v-model:current-page="query.current"
      v-model:page-size="query.size"
      :total="query.total"
      :page-sizes="pageSizeChangeList"
      :layout="layout"
      :size="size"
      :background="background"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
  />
</template>

<style scoped lang="scss">

</style>