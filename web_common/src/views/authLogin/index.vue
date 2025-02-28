<script setup lang="ts">
import {ref, shallowRef} from "vue";

import {useUserStoreHook} from "@/store/modules/user";
import {getTopMenu, initRouter} from "@/router/utils";
import {message} from "@/utils/message";
import * as dd from 'dingtalk-jsapi';
import {useRouter} from "vue-router";
import {isNullOrUnDef} from "@pureadmin/utils";
import common from "@/utils/common";
import {removeToken} from "@/utils/auth";
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
  } else if (msg instanceof String){
    common.showMsgDialog(msg)
  }else {
    common.showMsgDialog(JSON.stringify(msg))
  }
}
const handleUnLogin = (actionType, configKey, callBack: Function) => {
  try {
    common.showGlobalLoading()
    dd.runtime.permission.requestAuthCode({
      corpId: "ding604ca88369c9094535c2f4657eb6378f"
    }).then(result => {
      let authCode = result.code
      // 设置登录类型为钉钉
      useUserStoreHook().SET_LOGINTYPE(1)
      useUserStoreHook()
          .loginByAuthCode(authCode, configKey)
          .then(res => {
            if (res.code === 200) {
              // 获取后端路由
              initRouter().then(() => {
                if (isNullOrUnDef(actionType)) {
                  router.push(getTopMenu(true).path);
                  message("登录成功,开始跳转首页", {type: "success"});
                } else {
                  message("登录成功,开始跳转特定页面", {type: "success"});
                  callBack()
                }
              });
            } else {
              message('登录失败=>' + res.msg, {type: 'error'})
            }
          }).catch(res => {
        message('登录失败=>' + res.message, {type: 'error'})
      }).catch((reason) => {
        showLog(reason)
      })
    }).catch((reason) => {
      showLog(reason)
    })
  } catch (reason) {
    showLog(reason)
  } finally {
    common.closeGlobalLoading()
  }
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
      //清空缓存
      removeToken();
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
  dialogComponent.value = {props:{}} as DialogComponent
  let componentPromise = common.getVue('/src/components/result/result.vue')
  componentPromise().then((res: any) => {
    componentName.value = res.default
    dialogFlag.value = true
  })
}
</script>

<template>
  <div element-loading-text="授权登录中......"
       style="position: relative;width: 100%;height: 100%;background-color: burlywood">
    <el-dialog v-model="dialogFlag" append-to-body fullscreen :show-close="false">
      <component
          v-model="dialogFlag"
          v-bind="dialogComponent.props"
          :is="componentName"
          @finish="finish"
      />
    </el-dialog>
  </div>
</template>
