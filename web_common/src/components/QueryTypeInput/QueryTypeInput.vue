<script setup lang="ts">

import QueryTypeEnum from "@/enums/QueryTypeEnum";
import {CSSProperties} from "vue";


interface Props {
  item: ColumnDefine,
  size?: 'large' | 'default' | 'small',
  dictMapping?: any,
  queryInHeaderWithCondition?: boolean,
  getStyle?: (item:ColumnDefine) => CSSProperties
}
const  props= withDefaults(defineProps<Props>(),{
  size: ()=>'default',
  dictMapping: ()=>({}),
  queryInHeaderWithCondition: ()=> false,
  getStyle: ()=> ({})
})
const query = defineModel<QueryDefine>('query', {required:false})
const clearOption = (prop) => {
  delete query.value.queryParams[prop]
}
const getProp = (columnDefine:ColumnDefine) => {
  return props.queryInHeaderWithCondition?columnDefine.prop+'_$_'+columnDefine.queryCondition:columnDefine.prop
}
</script>

<template>
  <div>
    <template v-if="item.queryType === QueryTypeEnum.INPUT">
      <el-input v-model="query.queryParams[getProp(item)]" :style="getStyle(item)" :size="size"/>
    </template>
    <template v-else-if="item.queryType === QueryTypeEnum.OPTION">
      <el-select v-model="query.queryParams[getProp(item)]" :style="getStyle(item)" :size="size"
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
      <el-select v-model="query.queryParams[getProp(item)]" :style="getStyle(item)" :size="size" multiple
                 collapse-tags-tooltip collapse-tags default-first-option :reserve-keyword="false"
                 filterable allow-create placeholder="多项模糊查询" clearable @clear="clearOption(item.prop)"/>
    </template>
    <template v-else-if="item.queryType === QueryTypeEnum.MULTIPLE_OPTION">
      <el-select v-model="query.queryParams[getProp(item)]" :style="getStyle(item)" :size="size" collapse-tags multiple
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
          v-model="query.queryParams[getProp(item)]"
          :style="getStyle(item)"
          clearable
          :size="size"
          type="daterange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
      />
    </template>
    <template v-else-if="item.queryType === QueryTypeEnum.MONTH_RANGE">
      <el-date-picker
          v-model="query.queryParams[getProp(item)]"
          :style="getStyle(item)"
          clearable
          :size="size"
          type="monthrange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始月份"
          end-placeholder="结束月份"
      />
    </template>
    <template v-else-if="item.queryType === QueryTypeEnum.MONTH">
      <el-date-picker
          v-model="query.queryParams[getProp(item)]"
          :style="getStyle(item)"
          :size="size"
          class="date"
          type="month"
          value-format="YYYY-MM-DD HH:mm:ss"
      />
    </template>
    <template v-else-if="item.queryType === QueryTypeEnum.YEAR">
      <el-date-picker
          v-model="query.queryParams[getProp(item)]"
          :style="getStyle(item)"
          :size="size"
          class="date"
          type="year"
          value-format="YYYY-MM-DD HH:mm:ss"
      />
    </template>
    <template v-else-if="item.queryType === QueryTypeEnum.DATE">
      <el-date-picker
          v-model="query.queryParams[getProp(item)]"
          :style="getStyle(item)"
          :size="size"
          class="date"
          type="date"
          value-format="YYYY-MM-DD HH:mm:ss"
      />
    </template>
    <template v-else-if="item.queryType === QueryTypeEnum.DATE_TIME">
      <el-date-picker
          v-model="query.queryParams[getProp(item)]"
          :style="getStyle(item)"
          :size="size"
          class="date"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
      />
    </template>
  </div>
</template>

<style scoped lang="scss">

</style>