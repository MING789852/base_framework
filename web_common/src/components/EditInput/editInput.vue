<script setup lang="ts">
import {defineProps, defineOptions, nextTick, ref, computed} from "vue";
defineOptions({
  name: "editInput"
});
const props = defineProps({
  value: [String, Number],
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
    <div v-if="isEdit === false" style="flex: 1" @click="handleOnClick">{{ inputValue }}</div>
    <el-input v-else ref="inputRef" size="small" v-model="inputValue" @blur="handleChange"/>
  </div>
</template>

<style scoped lang="scss"></style>
