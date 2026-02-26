<script setup lang="ts">

import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {isNullOrUnDef} from "@pureadmin/utils";
import QueryTypeInput from "@/components/QueryTypeInput/QueryTypeInput.vue";

interface Props {
  queryColumns?: Array<ColumnDefine>,
  dictMapping?: Object,
  queryInHeaderWithCondition?: boolean
}
const query = defineModel<QueryDefine>('query', {required:false})
withDefaults(defineProps<Props>(),{
  queryColumns: ()=>[],
  dictMapping: ()=>{return {}},
  queryInHeaderWithCondition: ()=> false
})
const getStyle = (item:ColumnDefine) => {
  if (item.queryType === QueryTypeEnum.INPUT) {
    return {width: '160px', height: '24px'}
  }else if (item.queryType === QueryTypeEnum.OPTION){
    return {width: '160px', height: '24px'}
  }else if (item.queryType === QueryTypeEnum.OPTION_LIKE){
    return {width: '200px', height: '24px'}
  }else if (item.queryType === QueryTypeEnum.MULTIPLE_OPTION){
    return {width: '220px', height: '24px'}
  }else {
    return {}
  }
}
</script>

<template>
  <template  v-for="item in queryColumns" :key="item.prop">
    <div v-if="isNullOrUnDef(item.query)||item.query===true"  class="flex flex-row content-center">
      <div class="text-nowrap text-xs flex items-center" style="min-width: 40px;height: 24px">{{item.label}}：</div>
      <QueryTypeInput
          :item="item"
          :dict-mapping="dictMapping"
          size="small"
          :query-in-header-with-condition="queryInHeaderWithCondition"
          :get-style="getStyle"
          v-model:query="query"/>
    </div>
  </template>
</template>

<style scoped lang="scss">

</style>