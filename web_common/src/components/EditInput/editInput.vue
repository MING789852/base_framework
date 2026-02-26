<script setup lang="ts">
import {defineProps, defineOptions, nextTick, ref, computed} from "vue";

defineOptions({
  name: "editInput"
});

interface Props {
  value: string | number | undefined;
  onUpdateValue: (value: string | number) => void;
  textarea?: boolean;
  disabled?: boolean;
  rowIndex?: number;
  prop?: string;
  row?: any;
  onlyShowEdit?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  textarea: () => false,
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
  <div class="flex min-h-[23px] w-full">
    <el-input v-if="onlyShowEdit||isEdit" ref="inputRef" :type="textarea?'textarea':'text'" size="small"
              :autosize="{ minRows: 4}"
              v-model="inputValue" @blur="handleChange"/>
    <div v-else class="whitespace-pre-wrap min-h-full w-full flex justify-center" @click="handleOnClick">{{ inputValue }}</div>
  </div>
</template>

<style scoped lang="scss"></style>
