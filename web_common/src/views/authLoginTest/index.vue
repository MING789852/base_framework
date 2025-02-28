<script setup lang="ts">
import {ref, shallowRef} from "vue";

import {useRouter} from "vue-router";
import {isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";
import { useLayout } from "@/layout/hooks/useLayout";
import {useDataThemeChange} from "@/layout/hooks/useDataThemeChange";

const { initStorage} = useLayout();
initStorage();
const {dataThemeChange} = useDataThemeChange();
dataThemeChange();

const router = useRouter();
defineOptions({
  name: "authLogin"
});
const showLog = (msg) => {
  if (msg instanceof Error) {
    common.showMsgDialog(msg.stack)
  } else {
    common.showMsgDialog(msg)
  }
}
const handleUnLogin = (actionType, configKey, callBack: Function) => {
  console.log(actionType)
  console.log(configKey)
  callBack()
}

const authActionTypeEnum = {
  redirectUrl: 'redirectUrl',
  openDialog: 'openDialog'
}

interface DialogComponent {
  path: string,
  props: Object
}

const dialogComponent = ref<DialogComponent>()
const dialogFlag = ref(false)
const componentName = shallowRef(null);

const loginType = router.currentRoute.value.query.loginType
const actionType = router.currentRoute.value.query.actionType
const configKey = router.currentRoute.value.query.configKey

try {
  if (!isNullOrUnDef(loginType)) {
    if ("dingdingLogin" === loginType) {
      //登录
      handleUnLogin(actionType, configKey, () => {
        if (actionType === authActionTypeEnum.redirectUrl) {
          let redirectUrl = router.currentRoute.value.query.redirectUrl as string
          if (!isNullOrUnDef(redirectUrl)) {
            redirectUrl = decodeURIComponent(redirectUrl);
          }
          router.push(redirectUrl)
        }
        if (actionType === authActionTypeEnum.openDialog) {
          let openDialog = router.currentRoute.value.query.openDialog as string
          if (!isNullOrUnDef(openDialog)) {
            dialogComponent.value = JSON.parse(openDialog)
            let componentPromise = common.getVue('/' + dialogComponent.value.path)
            componentPromise().then((res: any) => {
              componentName.value = res.default
              dialogFlag.value = true
            })
          }
        }
      })
    }
  }
} catch (reason) {
  showLog(reason)
}

const finish = (data) => {
  console.log(data)
  dialogComponent.value = {props:{}} as DialogComponent
  let componentPromise = common.getVue('/src/components/result/result.vue')
  componentPromise().then((res: any) => {
    componentName.value = res.default
    dialogFlag.value = true
  })
}

</script>

<template>
  <div element-loading-text="授权登录跳转测试......"
       style="position: relative;width: 100%;height: 100%;background-color: burlywood">
    <el-dialog v-show="dialogFlag" v-model="dialogFlag"  append-to-body fullscreen :show-close="false">
      <component
          v-model="dialogFlag"
          v-bind="dialogComponent.props"
          :is="componentName"
          @finish="finish"
      />
    </el-dialog>
  </div>
</template>
