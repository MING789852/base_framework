<script setup lang="ts">
import {computed, onMounted, onUnmounted, reactive, ref} from 'vue'
import flowableShowApi from '@/api/flowableShowApi'
import common from "@/utils/common";
import router from "@/router";

// 响应式数据
const isDragging = ref(false)
const containerPos = reactive({ x: 0, y: 0 })
const minimized = ref(false)
const dragOffset = reactive({ x: 0, y: 0 })

// 待办事项数据
interface TodoItem {
  content: string,
  tagContent: string,
  businessType:string
}


const todoList = ref<TodoItem[]>([])
const ballSize = 50
const ballSizePx = `${ballSize}px`
const todoBottomPx = `${ballSize+10}px`

// 设置默认位置
const setDefaultPosition = () => {
  minimized.value = common.isMobile()
  const windowWidth = window.innerWidth
  const windowHeight = window.innerHeight
  const offset = 50
  containerPos.x = windowWidth - ballSize - offset
  containerPos.y = windowHeight - ballSize - offset
}
const containerStyle = computed(() => ({
  left: `${containerPos.x}px`,
  top: `${containerPos.y}px`,
  transition: isDragging.value ? 'none' : 'all 0.3s ease'
}))

// 防文字选中功能
const disableTextSelection = () => {
  document.body.style.userSelect = 'none'
  document.body.style.webkitUserSelect = 'none'
}
const enableTextSelection = () => {
  document.body.style.userSelect = ''
  document.body.style.webkitUserSelect = ''
}

// 优化后的拖拽功能
const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  //计算鼠标相对于拖拽元素左上角的偏移
  dragOffset.x = e.clientX - containerPos.x
  dragOffset.y = e.clientY - containerPos.y

  disableTextSelection()
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}
const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return

  let newX = e.clientX - dragOffset.x
  let newY = e.clientY - dragOffset.y

  // 边界检查
  const maxX = window.innerWidth - ballSize
  const maxY = window.innerHeight - ballSize

  newX = Math.max(0, Math.min(maxX, newX))
  newY = Math.max(0, Math.min(maxY, newY))

  containerPos.x = newX
  containerPos.y = newY
}
const removeEventListeners = () => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}
const stopDrag = () => {
  isDragging.value = false
  enableTextSelection()
  removeEventListeners()
}

// 窗口控制
const toggleMinimize = () => {
  minimized.value = !minimized.value
}

// 窗口大小变化监听
const handleResize = () => {
  setDefaultPosition()
}

// 生命周期
onMounted(() => {
  setDefaultPosition()
  window.addEventListener('resize', handleResize)
  refresh()
})

onUnmounted(() => {
  removeEventListeners()
  window.removeEventListener('resize', handleResize)
  enableTextSelection()
})

const todoItemClick = (item:TodoItem,index) => {
  router.push('/flowable/show?businessType='+item.businessType)
}
const refresh = ()=>{
  common.handleRequestApi(flowableShowApi.getTodoList()).then(res=>{
    todoList.value = res.data
  })
}
</script>

<template>
  <div
      class="floating-todo"
      :style="containerStyle"
      @mousedown="startDrag"
  >
    <!-- 收起状态显示 -->
    <div class="todo-icon" @click.stop="toggleMinimize">📝</div>

    <!-- 展开状态 -->
    <div class="todo-container" v-if="!minimized">
      <div class="drag-handle">
        <div class="w-full h-full flex  justify-between items-center">
          <h2 class="text-lg font-bold">待办事项({{ todoList.length }})</h2>
          <div class="flex gap-2">
            <el-button type="primary" size="small" @click.stop="refresh">
              刷新
            </el-button>
          </div>
        </div>
      </div>

      <div class="todo-content">
        <div class="todo-list">
          <div
              v-for="(todo,index) in todoList"
              :key="index"
              class="todo-item"
              @click.stop="todoItemClick(todo,index)"
          >
            <div class="todo-info">
              <el-tag :type="'primary'" size="small" class="w-[10%]">
                {{ todo.tagContent }}
              </el-tag>
              <span>
                {{ todo.content }}
              </span>
            </div>
          </div>

          <div v-if="todoList.length === 0" class="empty-tip">
            暂无待办事项
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.floating-todo {
  position: fixed;
  z-index: 10000;
  width: v-bind(ballSizePx);
  height: v-bind(ballSizePx);
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #5EA8F2 100%);
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  user-select: none;
  -webkit-user-select: none;
}

.floating-todo:active {
  cursor: grabbing;
}

.todo-icon {
  font-size: 20px;
  color: white;
}

.todo-container {
  position: absolute;
  right: 0;
  bottom: v-bind(todoBottomPx);
  width: 300px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  border: 1px solid #e0e0e0;
  overflow: hidden;
  animation: scaleIn 0.2s ease;
}

.drag-handle {
  padding: 16px;
  background: #409eff;
  color: white;
  cursor: move;
  user-select: none;
}

.drag-handle h2 {
  margin: 0;
  font-size: 16px;
}

.todo-content {
  padding: 0;
}

.todo-list {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px 0;
}

.todo-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.todo-item:hover {
  background-color: #a5d6ff;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-info {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 8px;
}

.todo-info span {
  font-size: 14px;
}

.todo-info span.completed {
  text-decoration: line-through;
  color: #999;
}

.empty-tip {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}

/* 动画效果 */
@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* 滚动条样式 */
.todo-list::-webkit-scrollbar {
  width: 4px;
}

.todo-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.todo-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.todo-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>