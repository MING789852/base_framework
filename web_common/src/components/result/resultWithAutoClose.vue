<template>
  <div class="background-wrapper" :style="backgroundStyle">
    <div class="approval-container">
      <div
          class="status-badge"
          :style="badgeStyle"
      >
        {{ mergedStatusText }}
      </div>
      <h1>{{ mergedTitleText }}</h1>

      <!-- 倒计时部分，仅在 autoClose 为 true 时显示 -->
      <template v-if="mergedAutoClose">
        <div class="countdown-wrapper">
          <div
              class="countdown"
              :style="{ color: countdownColor }"
          >
            {{ remainingSeconds }}
          </div>
        </div>
        <p class="message">
          {{ mergedContentText || `页面将在 ${remainingSeconds} 秒后自动关闭` }}<br>
          {{ closeMessage }}
        </p>
        <div class="progress-bar">
          <div
              class="progress"
              :style="progressStyle"
          ></div>
        </div>
      </template>

      <!-- 非自动关闭时的内容显示 -->
      <template v-else>
        <p class="message">
          {{ mergedContentText || '操作已完成' }}
        </p>
      </template>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, onBeforeUnmount, watch} from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const props = defineProps({
  // 基础配置
  autoClose: {
    type: [Boolean, String],
    default: true,
    validator: value => {
      if (typeof value === 'string') {
        return value === 'true' || value === 'false'
      }
      return typeof value === 'boolean'
    }
  },
  duration: {
    type: [Number, String],
    default: 3,
    validator: value => {
      const num = Number(value)
      return !isNaN(num) && num > 0
    }
  },
  // 状态配置
  statusType: {
    type: String,
    default: 'success',
    validator: value => ['success', 'warn', 'danger', 'info', 'primary'].includes(value)
  },
  statusText: {
    type: String,
    default: '已完成'
  },
  // 文字内容
  titleText: {
    type: String,
    default: '操作成功'
  },
  contentText: {
    type: String,
    default: ''
  },
  // 颜色自定义（可选）
  colors: {
    type: [Object, String],
    default: () => ({
      primary: '#3498db',
      success: '#4cd964',
      warn: '#f39c12',
      danger: '#e74c3c',
      info: '#9b59b6'
    }),
    validator: value => {
      if (typeof value === 'string') {
        try {
          const obj = JSON.parse(value)
          return typeof obj === 'object' && obj !== null
        } catch {
          return false
        }
      }
      return typeof value === 'object' && value !== null
    }
  }
})

// 处理URL参数和props的合并
const getMergedProp = (propName) => {
  const urlParam = route.query[propName]
  if (urlParam !== undefined) {
    return urlParam
  }
  return props[propName]
}

// 处理合并后的属性
const mergedAutoClose = computed(() => {
  const value = getMergedProp('autoClose')
  return typeof value === 'string' ? value === 'true' : Boolean(value)
})

const mergedDuration = computed(() => {
  const value = getMergedProp('duration')
  return Number(value) || 3
})

const mergedStatusType = computed(() => getMergedProp('statusType') || 'success')
const mergedStatusText = computed(() => getMergedProp('statusText') || '已完成')
const mergedTitleText = computed(() => getMergedProp('titleText') || '操作成功')
const mergedContentText = computed(() => getMergedProp('contentText') || '')

const mergedColors = computed(() => {
  const value = getMergedProp('colors')
  if (typeof value === 'string') {
    try {
      return JSON.parse(value)
    } catch {
      return {
        primary: '#3498db',
        success: '#4cd964',
        warn: '#f39c12',
        danger: '#e74c3c',
        info: '#9b59b6'
      }
    }
  }
  return value || {
    primary: '#3498db',
    success: '#4cd964',
    warn: '#f39c12',
    danger: '#e74c3c',
    info: '#9b59b6'
  }
})

// 响应式数据
const remainingSeconds = ref(mergedDuration.value)
const progressPercent = ref(100)
const closeMessage = ref(mergedAutoClose.value ? '您可以手动关闭窗口或等待自动关闭' : '')
let startTime = Date.now()
let endTime = startTime + mergedDuration.value * 1000
let animationFrameId = null

// 添加对路由变化的监听
watch(() => route.query, (newQuery) => {
  remainingSeconds.value = mergedDuration.value
  progressPercent.value = 100
  startTime = Date.now()
  endTime = startTime + mergedDuration.value * 1000

  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }

  if (mergedAutoClose.value) {
    updateCountdown()
  }
}, { deep: true })

// 颜色计算
const statusColor = computed(() => mergedColors.value[mergedStatusType.value] || mergedColors.value.success)
const countdownColor = computed(() => {
  if (remainingSeconds.value <= 2) return mergedColors.value.danger
  return statusColor.value
})

// 背景样式计算
const backgroundStyle = computed(() => {
  const gradients = {
    success: `radial-gradient(circle at 10% 20%, ${hexToRgba(mergedColors.value.success, 0.1)}, transparent 60%),
              radial-gradient(circle at 90% 80%, ${hexToRgba(mergedColors.value.success, 0.1)}, transparent 60%)`,
    warn: `radial-gradient(circle at 10% 20%, ${hexToRgba(mergedColors.value.warn, 0.1)}, transparent 60%),
           radial-gradient(circle at 90% 80%, ${hexToRgba(mergedColors.value.warn, 0.1)}, transparent 60%)`,
    danger: `radial-gradient(circle at 10% 20%, ${hexToRgba(mergedColors.value.danger, 0.1)}, transparent 60%),
             radial-gradient(circle at 90% 80%, ${hexToRgba(mergedColors.value.danger, 0.1)}, transparent 60%)`,
    info: `radial-gradient(circle at 10% 20%, ${hexToRgba(mergedColors.value.info, 0.1)}, transparent 60%),
           radial-gradient(circle at 90% 80%, ${hexToRgba(mergedColors.value.info, 0.1)}, transparent 60%)`,
    primary: `radial-gradient(circle at 10% 20%, ${hexToRgba(mergedColors.value.primary, 0.1)}, transparent 60%),
              radial-gradient(circle at 90% 80%, ${hexToRgba(mergedColors.value.primary, 0.1)}, transparent 60%)`
  }

  return {
    background: `${gradients[mergedStatusType.value] || gradients.success}, #ffffff`,
    '--status-color': statusColor.value
  }
})

// 徽章样式计算
const badgeStyle = computed(() => {
  const gradients = {
    success: `linear-gradient(135deg, ${mergedColors.value.success}, #2ecc71)`,
    warn: `linear-gradient(135deg, ${mergedColors.value.warn}, #f1c40f)`,
    danger: `linear-gradient(135deg, ${mergedColors.value.danger}, #c0392b)`,
    info: `linear-gradient(135deg, ${mergedColors.value.info}, #8e44ad)`,
    primary: `linear-gradient(135deg, ${mergedColors.value.primary}, #2980b9)`
  }

  return {
    background: gradients[mergedStatusType.value] || gradients.success,
    color: 'white',
    boxShadow: `0 4px 12px ${hexToRgba(statusColor.value, 0.3)}`,
    textShadow: '0 1px 1px rgba(0, 0, 0, 0.2)'
  }
})

const progressStyle = computed(() => ({
  width: `${progressPercent.value}%`,
  background: remainingSeconds.value <= 2
      ? `linear-gradient(90deg, ${mergedColors.value.danger}, ${mergedColors.value.warn})`
      : `linear-gradient(90deg, ${statusColor.value}, ${mergedColors.value.success})`
}))

// 工具函数：HEX转RGBA
const hexToRgba = (hex, alpha) => {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

// 倒计时更新函数
const updateCountdown = () => {
  if (!mergedAutoClose.value) return

  const now = Date.now()
  const timeRemaining = endTime - now

  remainingSeconds.value = Math.max(0, Math.ceil(timeRemaining / 1000))
  progressPercent.value = Math.max(0, (timeRemaining / (mergedDuration.value * 1000)) * 100)

  if (now >= endTime) {
    cancelAnimationFrame(animationFrameId)
    remainingSeconds.value = 0
    progressPercent.value = 0

    setTimeout(() => {
      try {
        window.close()
        if (!window.closed) {
          handleCloseBlocked()
        }
      } catch (e) {
        handleCloseBlocked()
      }
    }, 300)
  } else {
    animationFrameId = requestAnimationFrame(updateCountdown)
  }
}

// 处理浏览器阻止关闭
const handleCloseBlocked = () => {
  closeMessage.value = '页面关闭被浏览器阻止，请手动关闭此窗口'
}

// ESC键关闭
const handleKeydown = (e) => {
  if (e.key === 'Escape') {
    window.close()
  }
}

// 生命周期
onMounted(() => {
  if (mergedAutoClose.value) {
    updateCountdown()
    document.addEventListener('keydown', handleKeydown)
  } else {
    closeMessage.value = ''
  }
})

onBeforeUnmount(() => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.background-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  transition: background 0.5s ease;
}

.approval-container {
  text-align: center;
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
}

.approval-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--status-color);
}

.status-badge {
  padding: 8px 20px;
  border-radius: 50px;
  display: inline-block;
  margin-bottom: 20px;
  font-weight: bold;
  font-size: 0.95rem;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  z-index: 1;
}

.status-badge::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
      to bottom right,
      rgba(255, 255, 255, 0.3),
      rgba(255, 255, 255, 0.1),
      rgba(255, 255, 255, 0)
  );
  transform: rotate(30deg);
  animation: shine 3s infinite;
}

h1 {
  color: #2c3e50;
  margin: 0 0 15px 0;
  font-size: 1.8rem;
  font-weight: 600;
}

.countdown-wrapper {
  margin: 20px 0;
}

.countdown {
  font-size: 3.5rem;
  font-weight: bold;
  position: relative;
  display: inline-block;
  line-height: 1;
  animation: bounce 0.8s infinite;
}

.countdown::after {
  content: '秒';
  font-size: 1.2rem;
  position: absolute;
  right: -1.8rem;
  bottom: 0.8rem;
  color: #7f8c8d;
}

.message {
  color: #7f8c8d;
  font-size: 1rem;
  line-height: 1.5;
  margin: 0 0 20px 0;
}

.progress-bar {
  height: 5px;
  background: #f1f3f4;
  border-radius: 3px;
  margin-top: 25px;
  overflow: hidden;
}

.progress {
  height: 100%;
  transition: width 0.1s linear;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

@keyframes shine {
  0% {
    transform: translateX(-100%) rotate(30deg);
  }
  100% {
    transform: translateX(100%) rotate(30deg);
  }
}

/* 移动端适配 */
@media (max-width: 480px) {
  .approval-container {
    padding: 25px 20px;
    border-radius: 12px;
  }

  h1 {
    font-size: 1.5rem;
    margin-bottom: 12px;
  }

  .countdown {
    font-size: 3rem;
  }

  .countdown::after {
    font-size: 1rem;
    right: -1.5rem;
    bottom: 0.6rem;
  }

  .message {
    font-size: 0.95rem;
  }

  .status-badge {
    padding: 6px 16px;
    font-size: 0.9rem;
    margin-bottom: 15px;
  }
}

@media (max-width: 360px) {
  .countdown {
    font-size: 2.5rem;
  }

  h1 {
    font-size: 1.3rem;
  }
}
</style>