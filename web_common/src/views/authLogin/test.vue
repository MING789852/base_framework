<script setup lang="ts">
import authRegister from "@/views/authRegister/authRegister";
import {isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";
import { useLayout } from "@/layout/hooks/useLayout";
import {useDataThemeChange} from "@/layout/hooks/useDataThemeChange";
import authLoginAction from '@/views/authLogin/action/authLoginAction'
// import {useRouter} from "vue-router";
// const router = useRouter();
const { initStorage} = useLayout();
initStorage();
const {dataThemeChange} = useDataThemeChange();
dataThemeChange();
defineOptions({
  name: "authLoginTest"
});
//注册强制刷新页面
common.addUrlChangeForceRefreshListener()
const urlParams = common.getCurrentRouteQuery()
const loginType = urlParams.loginType as string
try {
  if (!isNullOrUnDef(loginType)) {
    authRegister.execute(loginType,{
      urlParams: urlParams,
      isTest: true,
      callBack: ()=>{
        try {
          authLoginAction.loginAfterAction()
        }catch (reason){
          common.showLog(reason)
        }
      }
    })
  }
} catch (reason) {
  common.showLog(reason)
}
</script>

<template>
  <div class="loading-wrap">
    <div class="loading-container">
      <div class="loading-text">授权登录中</div>
      <div class="loading-circle">
        <div class="circle circle-1" style="--tx: -15%; --ty: -5%"></div>
        <div class="circle circle-2" style="--tx: 5%; --ty: 5%"></div>
        <div class="circle circle-3" style="--tx: 10%; --ty: -10%"></div>
      </div>
      <div class="loading-dots">
        <div class="dot"></div>
        <div class="dot"></div>
        <div class="dot"></div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
* {
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
}

.loading-wrap {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #e6f2ff 0%, #cce0ff 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
  overflow: hidden;
  touch-action: manipulation;
}

.loading-container {
  text-align: center;
  width: 90%;
  max-width: 300px;
}

.loading-text {
  font-size: clamp(1.8rem, 5vw, 2.2rem);
  margin-bottom: 8vh;
  font-weight: 300;
  color: #4a89dc;
  animation: textFloat 2.5s infinite ease-in-out;
}

.loading-circle {
  position: relative;
  width: clamp(80px, 25vw, 120px);
  height: clamp(80px, 25vw, 120px);
  margin: 0 auto 8vh;
}

.circle {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  opacity: 0.6;
  mix-blend-mode: multiply;
}

.circle-1 {
  background: #a5d6ff;
  transform: translate(-15%, -5%);
  animation: circlePulse 3s infinite alternate;
}

.circle-2 {
  background: #89c2ff;
  transform: translate(5%, 5%);
  animation: circlePulse 3s 1s infinite alternate;
}

.circle-3 {
  background: #6aafff;
  transform: translate(10%, -10%);
  animation: circlePulse 3s 2s infinite alternate;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.dot {
  width: clamp(12px, 3.5vw, 16px);
  height: clamp(12px, 3.5vw, 16px);
  border-radius: 50%;
  background: #4a89dc;
  opacity: 0.4;
  animation: dotBounce 1.5s infinite ease-in-out;
}

.dot:nth-child(1) {
  animation-delay: 0.1s;
  background: #6aafff;
}

.dot:nth-child(2) {
  animation-delay: 0.3s;
  background: #4a89dc;
}

.dot:nth-child(3) {
  animation-delay: 0.5s;
  background: #2d6bcc;
}

@keyframes textFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes circlePulse {
  0%, 100% {
    transform: translate(var(--tx, 0), var(--ty, 0)) scale(1);
  }
  50% {
    transform: translate(var(--tx, 0), var(--ty, 0)) scale(0.9);
    opacity: 0.8;
  }
}

@keyframes dotBounce {
  0%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-12px);
    opacity: 1;
  }
}

/* 低电量模式优化 */
@media (prefers-reduced-motion: reduce) {
  .loading-text, .circle, .dot {
    animation: none !important;
  }
  .circle {
    opacity: 0.3;
  }
  .dot {
    opacity: 0.6;
    transform: translateY(0);
  }
}
</style>
