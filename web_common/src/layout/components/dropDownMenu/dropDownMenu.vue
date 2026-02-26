<script setup lang="ts">

import UserSettingsFill from "@iconify-icons/ri/user-settings-fill";
import PencilFill from "@iconify-icons/ri/pencil-fill";
import LogoutCircleRLine from "@iconify-icons/ri/logout-circle-r-line";
import QueryTypeEnum from "@/enums/QueryTypeEnum";
import common from "@/utils/common";
import {useNav} from "@/layout/hooks/useNav";
import {useTranslationLang} from "@/layout/hooks/useTranslationLang";
import systemUserApi from "@/api/systemUserApi";
import {message} from "@/utils/message";

const {
  logout,
} = useNav();
const {t} = useTranslationLang();

const updatePassword = () => {
  let columns: Array<DetailColumnDefine> = [
    {prop: 'oldPassword', label: '旧密码', type: QueryTypeEnum.INPUT,placeholder:'请输入旧密码', disabled: false},
    {prop: 'newPassword', label: '新密码', type: QueryTypeEnum.INPUT,placeholder:'请输入新密码', disabled: false}
  ]
  let params:OpenInputDialogDefine = {
    columns: columns,
    callBack: (result) => {
      common.handleRequestApi(systemUserApi.updatePassword(result.data)).then(()=>{
        message('操作成功',{type:'success'})
        result.done()
      })
    }
  }
  common.openInputDialog(params)
}
const updateUserInfo = () => {
  let columns: Array<DetailColumnDefine> = [
    {prop: 'email', label: '邮箱', type: QueryTypeEnum.INPUT,placeholder:'请输入邮箱', disabled: false}
  ]
  let currentUserInfo = common.getCurrentUserInfo();
  let defaultValue = {
    email: currentUserInfo.email
  }
  let params:OpenInputDialogDefine = {
    columns: columns,
    defaultValue: defaultValue,
    callBack: (result) => {
      common.handleRequestApi(systemUserApi.updateUserInfo(result.data)).then(()=>{
        message('操作成功',{type:'success'})
        //更新本地邮件
        common.updateCurrentUserInfo({email:result.data.email})
        result.done()
      })
    }
  }
  common.openInputDialog(params)
}
</script>

<template>
  <el-dropdown-menu class="nav-dropdown-menu">
    <el-dropdown-item @click="updateUserInfo">
      <IconifyIconOffline
          :icon="UserSettingsFill"
          style="margin: 5px"
      />
      {{ t("buttons.hsUpdateUserInfo") }}
    </el-dropdown-item>
    <el-dropdown-item @click="updatePassword">
      <IconifyIconOffline
          :icon="PencilFill"
          style="margin: 5px"
      />
      {{ t("buttons.hsUpdatePassword") }}
    </el-dropdown-item>
    <el-dropdown-item @click="logout">
      <IconifyIconOffline
          :icon="LogoutCircleRLine"
          style="margin: 5px"
      />
      {{ t("buttons.hsLoginOut") }}
    </el-dropdown-item>
  </el-dropdown-menu>
</template>

<style scoped lang="scss">
.nav-dropdown-menu {
  max-width: 120px;

  ::v-deep(.el-dropdown-menu__item) {
    display: inline-flex;
    flex-wrap: wrap;
    min-width: 100%;
  }
}
</style>