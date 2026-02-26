<script setup lang="ts">
interface CardContainerProps {
  showHeader?: boolean;
  showFooter?: boolean;
}
const props = withDefaults(defineProps<CardContainerProps>(),{
  showHeader: true,
  showFooter: true
})
</script>

<template>
  <div  ref="containerRef" class="cardContainer">
    <el-card shadow="never" v-bind="$attrs">
      <template v-if="showHeader" #header>
        <slot name="header">
          <div class="flex flex-row w-full h-full justify-between">
            <slot name="header_left"/>
            <slot name="header_right"/>
          </div>
        </slot>
      </template>
      <template #default>
        <slot  name="default"></slot>
      </template>
      <template v-if="showFooter" #footer>
        <slot  name="footer"></slot>
      </template>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.cardContainer {
  display: flex;
  width: 100%;
  height: 100%;
  ::v-deep(.el-card){
    display: flex;
    flex-direction: column;
    width: 100%;
  }
  ::v-deep(.el-card__header){
    padding: 15px 15px;
  }
  ::v-deep(.el-card__body){
    width: 100%;
    display: flex;
    padding: 2px 2px;
    flex: 1;
    //flex-grow: 1;
  }
}
</style>