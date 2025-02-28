<script setup lang="ts">
import {computed, defineOptions, ref} from "vue";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import DyInputRange from "@/components/DyForm/DyInputRange.vue";

  defineOptions({
    name: "searchForm"
  });

  const props = defineProps({
    columns: {type: Array<ColumnDefine>, required: true},
    query: {type: Object as PropType<QueryDefine>, required: true},
    dictList: {type: Object, default: {} as Object},
    labelWidth: {type: String, default: '100px'}
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
            v-model="newQuery.queryParams[item.prop+'_$_'+item.queryCondition]"
          />
          <el-date-picker
            v-else-if="item.queryType === QueryTypeEnum.DATE"
            v-model="newQuery.queryParams[item.prop+'_$_'+item.queryCondition]"
            class="date"
            type="date"
            value-format="YYYY-MM-DD"
          />
          <el-date-picker
            v-else-if="item.queryType === QueryTypeEnum.MONTH"
            v-model="newQuery.queryParams[item.prop+'_$_'+item.queryCondition]"
            class="date"
            type="month"
            value-format="YYYY-MM"
          />
          <el-date-picker
            v-else-if="item.queryType === QueryTypeEnum.YEAR"
            v-model="newQuery.queryParams[item.prop+'_$_'+item.queryCondition]"
            class="date"
            type="year"
            value-format="YYYY"
          />
          <el-date-picker
            v-else-if="item.queryType === QueryTypeEnum.DATE_RANGE"
            v-model="newQuery.queryParams[item.prop+'_$_'+item.queryCondition]"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="To"
            start-placeholder="Start date"
            end-placeholder="End date"
          />
          <dy-input-range  v-else-if="item.queryType === QueryTypeEnum.INPUT_RANGE"
                           v-model:rangeProp="newQuery.queryParams[item.prop+'_$_'+item.queryCondition]"/>
          <el-select v-else   v-model="newQuery.queryParams[item.prop+'_$_'+item.queryCondition]" allow-create  filterable :multiple="true">
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
