<script setup lang="ts">
import {defineProps, defineOptions, nextTick, ref, computed} from "vue";
import {cloneDeep} from "@pureadmin/utils";
defineOptions({
  name: "editOption"
});
const props = defineProps({
  value: [String, Number, Array<String>, Array<Number>],
  dict: {
    type: Object,
    default: {} as Object
  },
  dictReverse: {
    type: Boolean,
    default: false
  },
  multiple: {
    type: Boolean,
    default: false
  },
  onUpdateValue: [Function]
});

const inputValue = computed({
  // getter
  get() {
    if (props.multiple){
      let list:Array<String>=cloneDeep(props.value)
      return list
    }else {
      return props.value + ''
    }
  },
  // setter
  set(newValue) {
    props.onUpdateValue(newValue);
  }
})
const isEdit = ref(false);
const inputRef = ref(null);

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
    <div v-if="isEdit === false" style="flex: 1" @click="handleOnClick">{{ inputValue!=null?dict[inputValue.toString()]:'' }}</div>
    <el-select  v-else ref="inputRef" size="small" v-model="inputValue" :multiple="multiple" filterable @blur="handleChange">
      <el-option
        v-for="(value,key) in dict"
        :key="key"
        :label="value"
        :value="key"
      />
    </el-select>
  </div>
</template>

<style scoped lang="scss"></style>

