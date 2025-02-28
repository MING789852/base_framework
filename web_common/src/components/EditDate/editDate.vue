<script setup lang="ts">
import {defineProps, defineOptions, nextTick, ref, computed} from "vue";
import {IDatePickerType} from "element-plus/es/components/date-picker/src/date-picker.type";
import {isNullOrUnDef} from "@pureadmin/utils";
defineOptions({
  name: "editDate"
});
const props = defineProps({
  value: [Date, Number, String],
  //"yyyy-MM-dd"
  formatStr: String,
  //month、date
  dateType: String,
  onUpdateValue: [Function]
});
const inputValue = computed({
  // getter
  get() {
    return props.value
  },
  // setter
  set(newValue) {
    props.onUpdateValue(newValue);
  }
})
const dateTypeStr = ref(props.dateType as IDatePickerType)
const isEdit = ref(false);
const inputRef = ref(null);

const formatStrRef = ref('')
if (isNullOrUnDef(props.formatStr)){
  if (dateTypeStr.value === 'date'){
    formatStrRef.value='YYYY-MM-DD'
  }
  if (dateTypeStr.value === 'month'){
    formatStrRef.value='YYYY-MM'
  }
  if (dateTypeStr.value === 'year'){
    formatStrRef.value='YYYY'
  }
}

function handleOnClick() {
  isEdit.value = true;
  nextTick(() => {
    inputRef.value.focus();
  });
}
function handleChange() {
  isEdit.value = false;
}
</script>

<template>
  <div style="display: flex;min-height: 23px">
    <div v-if="isEdit === false" style="flex: 1" @click="handleOnClick">{{inputValue}}</div>
    <el-date-picker
      v-else
      size="small"
      ref="inputRef"
      v-model="inputValue"
      :type="dateTypeStr"
      :value-format="formatStrRef"
      @blur="handleChange"
    />
  </div>
</template>

<style scoped lang="scss"></style>
