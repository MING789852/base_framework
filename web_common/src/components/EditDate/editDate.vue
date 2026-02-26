<script setup lang="ts">
import {defineProps, defineOptions, nextTick, ref, computed} from "vue";

defineOptions({
  name: "editDate"
});

interface Props {
  onlyShowEdit?: boolean,
  value: string | number | Date | undefined,
  disabled?: boolean;
  dateType?: 'year' | 'years' | 'month' | 'months' | 'date' | 'dates' | 'datetime' | 'week' | 'datetimerange' | 'daterange' | 'monthrange' | 'yearrange',
  onUpdateValue?: (value: string | number | Date) => void
}

const props = withDefaults(defineProps<Props>(), {
  disabled: () => false,
  onlyShowEdit: () => false
})
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
const dateTypeStr = ref(props.dateType)
const isEdit = ref(false);
const inputRef = ref(null);


function handleOnClick() {
  if (props.disabled){
     return
  }
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
    <el-date-picker
        v-if="onlyShowEdit||isEdit"
        size="small"
        ref="inputRef"
        v-model="inputValue"
        :type="dateTypeStr"
        value-format="YYYY-MM-DD HH:mm:ss"
        @blur="handleChange"
    />
    <div v-else style="flex: 1" @click="handleOnClick">{{ inputValue }}</div>
  </div>
</template>

<style scoped lang="scss"></style>
