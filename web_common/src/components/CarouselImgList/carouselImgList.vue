<script setup lang="ts">
import {onMounted, ref} from 'vue'
import common from "@/utils/common";

interface Prop {
  name:string,
  imgList:TcFile[]
}
withDefaults(defineProps<Prop>(),{
  name: ()=>'',
  imgList: ()=>[]
})

const container = ref()
const height = ref('')
const activeIndex = ref(0) // 当前激活的轮播图索引

onMounted(() => {
  height.value = container.value.clientHeight + 'px'
})

// 轮播图切换事件
const handleChange = (current: number, prev: number) => {
  activeIndex.value = current
}

const getImgUrl = (item: TcFile) => {
  return common.getViewUrlWithCacheControlByFileId(item.id)
}

</script>

<template>
  <div ref="container" class="flex flex-col gap-2 justify-start items-center w-full h-full">
    <div class="flex justify-start w-full p-2 font-bold text-lg">{{ name }}</div>
    <el-carousel
        :interval="2000"
        type="card"
        class="img-carousel w-full h-full"
        :autoplay="true"
        :pause-on-hover="false"
        indicator-position="none"
        @change="handleChange"
    >
      <el-carousel-item
          v-for="(item, index) in imgList"
          :key="getImgUrl(item)"
          style="background-color: transparent;"
      >
        <!-- 只有当前激活的图片和前后相邻的图片才加载 -->
        <el-image
            v-if="Math.abs(index - activeIndex) <= 1"
            style="height:100%;width: 100%;background-color: transparent;"
            :src="getImgUrl(item)"
            :lazy="true"
            fit="contain"
        />
        <div
            class="flex justify-center items-center w-full h-full bg-gray-100"
            style="background-color: transparent;"
        >
          <div class="text-gray-400"> </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<style scoped lang="scss">
.img-carousel{
  ::v-deep(.el-carousel__mask){
    background-color: transparent;
  }
  ::v-deep(.el-carousel__container){
    height: 100%;
  }
}
</style>
