<script setup lang="ts">
import {computed, defineOptions, ref} from "vue";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import DyInputRange from "@/components/DyForm/DyInputRange.vue";

defineOptions({
  name: "searchForm"
});

interface SearchFormProp {
  columns: Array<ColumnDefine>
  query: QueryDefine
  dictList?: Object
  queryWithCondition?: boolean
}
const props = withDefaults(defineProps<SearchFormProp>(), {
  dictList:()=>{
    return {}
  },
  queryWithCondition:()=>true
})

// 过滤掉不需要查询条件的
const tempColumn = []
props.columns.forEach(item => {
  if (item.query === true) {
    tempColumn.push(item)
  }
})

const newColumns = ref(tempColumn)
const newQuery = computed(() =>props.query)
</script>

<template>

<el-form ref="queryFrom" label-position="right" label-width="auto">
  <template v-for="(item, index) in newColumns" :key="index">
      <el-form-item :label="item.label">
        <el-input
          v-if="item.queryType === QueryTypeEnum.INPUT"
          v-model="newQuery.queryParams[queryWithCondition?item.prop+'_$_'+item.queryCondition:item.prop]"
        />
        <el-date-picker
          v-else-if="item.queryType === QueryTypeEnum.DATE"
          v-model="newQuery.queryParams[queryWithCondition?item.prop+'_$_'+item.queryCondition:item.prop]"
          clearable
          class="date"
          type="date"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
        <el-date-picker
          v-else-if="item.queryType === QueryTypeEnum.MONTH"
          v-model="newQuery.queryParams[queryWithCondition?item.prop+'_$_'+item.queryCondition:item.prop]"
          clearable
          class="date"
          type="month"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
        <el-date-picker
          v-else-if="item.queryType === QueryTypeEnum.YEAR"
          v-model="newQuery.queryParams[queryWithCondition?item.prop+'_$_'+item.queryCondition:item.prop]"
          clearable
          class="date"
          type="year"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
        <el-date-picker
          v-else-if="item.queryType === QueryTypeEnum.DATE_RANGE"
          v-model="newQuery.queryParams[queryWithCondition?item.prop+'_$_'+item.queryCondition:item.prop]"
          clearable
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="To"
          start-placeholder="Start date"
          end-placeholder="End date"
        />
        <el-date-picker
            v-else-if="item.queryType === QueryTypeEnum.MONTH_RANGE"
            v-model="newQuery.queryParams[queryWithCondition?item.prop+'_$_'+item.queryCondition:item.prop]"
            clearable
            type="monthrange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="To"
            start-placeholder="Start month"
            end-placeholder="end month"
        />
        <dy-input-range  v-else-if="item.queryType === QueryTypeEnum.INPUT_RANGE"
                         v-model:rangeProp="newQuery.queryParams[queryWithCondition?item.prop+'_$_'+item.queryCondition:item.prop]"/>
        <el-select v-else   v-model="newQuery.queryParams[queryWithCondition?item.prop+'_$_'+item.queryCondition:item.prop]" allow-create  filterable :multiple="true">
          <el-option
            v-for="(value,key) in dictList[item.prop]"
            :key="key"
            :label="value"
            :value="key"
          />
        </el-select>
      </el-form-item>
  </template>
</el-form>
</template>

<style scoped lang="scss">
//$content-width: 300px;
//
//.form-inline .el-select {
//  width: $content-width;
//}
//.form-inline .el-input {
//  width: $content-width;
//}
//.form-inline ::v-deep(.el-date-editor){
//  width: $content-width;
//}
</style>
