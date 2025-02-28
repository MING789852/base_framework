<script setup lang="ts">

import QueryTypeEnum from "@/enums/QueryTypeEnum";

interface Props {
  queryColumns?: Array<ColumnDefine>,
  dictMapping?: Object
}
const query = defineModel<QueryDefine>('query', {required:false})
withDefaults(defineProps<Props>(),{
  queryColumns: ()=>[],
  dictMapping: ()=>{return {}}
})
const clearOption = (prop) => {
  delete query.value.queryParams[prop]
}
</script>

<template>
  <template  v-for="item in queryColumns" :key="item.prop">
    <div class="flex flex-row content-center">
      <div class="text-nowrap text-xs flex items-center" style="min-width: 40px;height: 24px">{{item.label}}：</div>
      <template v-if="item.queryType === QueryTypeEnum.INPUT">
        <el-input v-model="query.queryParams[item.prop]" style="width: 160px;height: 24px" size="small"/>
      </template>
      <template v-else-if="item.queryType === QueryTypeEnum.OPTION">
        <el-select v-model="query.queryParams[item.prop]" style="width: 160px;height: 24px" size="small"
                   filterable placeholder="请选择" clearable @clear="clearOption(item.prop)">
          <el-option
              v-for="(value,key) in dictMapping[item.prop]"
              :key="key"
              :label="value"
              :value="key"
          />
        </el-select>
      </template>
      <template v-else-if="item.queryType === QueryTypeEnum.OPTION_LIKE">
        <el-select v-model="query.queryParams[item.prop]" style="width: 200px;height: 24px" size="small" multiple
                   collapse-tags-tooltip collapse-tags default-first-option :reserve-keyword="false"
                   filterable allow-create placeholder="多项模糊查询" clearable @clear="clearOption(item.prop)"/>
      </template>
      <template v-else-if="item.queryType === QueryTypeEnum.MULTIPLE_OPTION">
        <el-select v-model="query.queryParams[item.prop]" style="width: 220px;height: 24px" size="small" collapse-tags multiple
                   filterable placeholder="请选择" clearable @clear="clearOption(item.prop)">
          <el-option
              v-for="(value,key) in dictMapping[item.prop]"
              :key="key"
              :label="value"
              :value="key"
          />
        </el-select>
      </template>
      <template v-else-if="item.queryType === QueryTypeEnum.DATE_RANGE">
        <el-date-picker
            v-model="query.queryParams[item.prop]"
            clearable
            size="small"
            type="daterange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
        />
      </template>
      <template v-else-if="item.queryType === QueryTypeEnum.MONTH">
        <el-date-picker
            v-model="query.queryParams[item.prop]"
            size="small"
            class="date"
            type="month"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </template>
      <template v-else-if="item.queryType === QueryTypeEnum.YEAR">
        <el-date-picker
            v-model="query.queryParams[item.prop]"
            size="small"
            class="date"
            type="year"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </template>
      <template v-else-if="item.queryType === QueryTypeEnum.DATE">
        <el-date-picker
            v-model="query.queryParams[item.prop]"
            size="small"
            class="date"
            type="date"
            value-format="YYYY-MM-DD HH:mm:ss"
        />
      </template>
    </div>
  </template>
</template>

<style scoped lang="scss">

</style>