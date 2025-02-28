<script setup lang="ts">
import {computed, defineOptions, ref} from "vue";
import {isNullOrUnDef} from "@pureadmin/utils";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import ObjectInput from "@/components/ObjectInput/ObjectInput.vue";

defineOptions({
  name: "detailForm"
});

const props = defineProps({
  columns: {type: Array<DetailColumnDefine>, required: true},
  propData: {type: Object, required: true, default: {} as Object},
  dictList: {type: Object, default: {} as Object}
})

const newColums = ref(props.columns)
const data = computed(() =>props.propData)
</script>

<template>

  <el-form ref="queryFrom" label-position="right" label-width="auto">
    <template v-for="(item, index) in newColums" :key="index">
        <el-form-item :label="item.label">
          <el-input
            v-if="item.type === QueryTypeEnum.INPUT"
            v-model="data[item.prop]"
            :placeholder="item.placeholder"
            :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
          />
          <ObjectInput
              v-if="item.type === QueryTypeEnum.OBJECT_INPUT"
              v-model:data-prop="data"
              :placeholder="item.placeholder"
              :prop-key="item.prop"
              :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"/>
          <el-input
            v-else-if="item.type === QueryTypeEnum.AREA_INPUT"
            v-model="data[item.prop]"
            :placeholder="item.placeholder"
            :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
            :autosize="{ minRows: 2, maxRows: 4 }"
            type="textarea"
          />
          <el-date-picker
            v-else-if="item.type === QueryTypeEnum.DATE"
            v-model="data[item.prop]"
            class="date"
            :placeholder="item.placeholder"
            :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
            type="date"
            value-format="YYYY-MM-DD"
          />
          <el-date-picker
            v-else-if="item.type === QueryTypeEnum.MONTH"
            v-model="data[item.prop]"
            class="date"
            :placeholder="item.placeholder"
            :disabled="isNullOrUnDef(item.disabled)?false:item.disabled"
            type="month"
            value-format="YYYY-MM"
          />
          <el-select v-else-if="item.type === QueryTypeEnum.OPTION" v-model="data[item.prop]" @change="item.changeAction"  filterable :placeholder="item.placeholder"  allow-create :disabled="isNullOrUnDef(item.disabled)?false:item.disabled">
            <el-option
              v-for="(value,key) in dictList[item.prop]"
              :key="key"
              :label="value"
              :value="key"
            />
          </el-select>
          <el-select v-else-if="item.type === QueryTypeEnum.MULTIPLE_OPTION" v-model="data[item.prop]"  filterable :placeholder="item.placeholder"  :disabled="isNullOrUnDef(item.disabled)?false:item.disabled" multiple>
            <el-option
              v-for="(value,key) in dictList[item.prop]"
              :key="key"
              :label="value"
              :value="key"
            />
          </el-select>
          <div class="w-full text-wrap" v-else-if="item.type === QueryTypeEnum.COMMON" >
            {{data[item.prop]}}
          </div>
        </el-form-item>
    </template>
  </el-form>
</template>

<style scoped lang="scss">
//$content-width: 200px;
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
