<script setup lang="ts">
import Search from "../search/index.vue";
import Notice from "../notice/index.vue";
import SidebarItem from "./sidebarItem.vue";
import { isAllEmpty } from "@pureadmin/utils";
import { ref, nextTick, computed } from "vue";
import { useNav } from "@/layout/hooks/useNav";
import { useTranslationLang } from "../../hooks/useTranslationLang";
import { usePermissionStoreHook } from "@/store/modules/permission";
import globalization from "@/assets/svg/globalization.svg?component";
import LogoutCircleRLine from "@iconify-icons/ri/logout-circle-r-line";
import Setting from "@iconify-icons/ri/settings-3-line";
import Check from "@iconify-icons/ep/check";
import DropDownMenu from "@/layout/components/dropDownMenu/dropDownMenu.vue";

const menuRef = ref();

const { t, route, locale, translationCh, translationEn } =
  useTranslationLang(menuRef);
const {
  title,
  logout,
  backTopMenu,
  onPanel,
  getLogo,
  username,
  nickName,
  userAvatar,
  avatarsStyle,
  getDropdownItemStyle,
  getDropdownItemClass
} = useNav();

const defaultActive = computed(() =>
  !isAllEmpty(route.meta?.activePath) ? route.meta.activePath : route.path
);

nextTick(() => {
  menuRef.value?.handleResize();
});
</script>

<template>
  <div
    v-loading="usePermissionStoreHook().wholeMenus.length === 0"
    class="horizontal-header"
  >
    <div class="horizontal-header-left" @click="backTopMenu">
      <img :src="getLogo()" alt="logo" />
      <span>{{ title }}</span>
    </div>
    <el-menu
      ref="menuRef"
      router
      mode="horizontal"
      popper-class="pure-scrollbar"
      class="horizontal-header-menu"
      :default-active="defaultActive"
    >
      <sidebar-item
        v-for="route in usePermissionStoreHook().wholeMenus"
        :key="route.path"
        :item="route"
        :base-path="route.path"
      />
    </el-menu>
    <div class="horizontal-header-right">
<!--      &lt;!&ndash; 菜单搜索 &ndash;&gt;-->
<!--      <Search id="header-search" />-->
<!--      &lt;!&ndash; 通知 &ndash;&gt;-->
<!--      <Notice id="header-notice" />-->
<!--      &lt;!&ndash; 国际化 &ndash;&gt;-->
<!--      <el-dropdown id="header-translation" trigger="click">-->
<!--        <globalization-->
<!--          class="navbar-bg-hover w-[40px] h-[48px] p-[11px] cursor-pointer outline-none"-->
<!--        />-->
<!--        <template #dropdown>-->
<!--          <el-dropdown-menu class="translation">-->
<!--            <el-dropdown-item-->
<!--              :style="getDropdownItemStyle(locale, 'zh')"-->
<!--              :class="['dark:!text-white', getDropdownItemClass(locale, 'zh')]"-->
<!--              @click="translationCh"-->
<!--            >-->
<!--              <span v-show="locale === 'zh'" class="check-zh">-->
<!--                <IconifyIconOffline :icon="Check" />-->
<!--              </span>-->
<!--              简体中文-->
<!--            </el-dropdown-item>-->
<!--            <el-dropdown-item-->
<!--              :style="getDropdownItemStyle(locale, 'en')"-->
<!--              :class="['dark:!text-white', getDropdownItemClass(locale, 'en')]"-->
<!--              @click="translationEn"-->
<!--            >-->
<!--              <span v-show="locale === 'en'" class="check-en">-->
<!--                <IconifyIconOffline :icon="Check" />-->
<!--              </span>-->
<!--              English-->
<!--            </el-dropdown-item>-->
<!--          </el-dropdown-menu>-->
<!--        </template>-->
<!--      </el-dropdown>-->
      <!-- 退出登录 -->
      <el-dropdown trigger="click">
        <span class="el-dropdown-link navbar-bg-hover">
          <img :src="userAvatar" :style="avatarsStyle" />
<!--          <p v-if="username" class="dark:text-white">{{ username }}</p>-->
          <p v-if="nickName" class="dark:text-white">{{ nickName }}</p>
        </span>
        <template #dropdown>
          <drop-down-menu/>
<!--          <el-dropdown-menu class="logout">-->
<!--            <el-dropdown-item @click="logout">-->
<!--              <IconifyIconOffline-->
<!--                :icon="LogoutCircleRLine"-->
<!--                style="margin: 5px"-->
<!--              />-->
<!--              {{ t("buttons.hsLoginOut") }}-->
<!--            </el-dropdown-item>-->
<!--          </el-dropdown-menu>-->
        </template>
      </el-dropdown>
      <span
        class="set-icon navbar-bg-hover"
        :title="t('buttons.hssystemSet')"
        @click="onPanel"
      >
        <IconifyIconOffline :icon="Setting" />
      </span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
:deep(.el-loading-mask) {
  opacity: 0.45;
}

.translation {
  ::v-deep(.el-dropdown-menu__item) {
    padding: 5px 40px;
  }

  .check-zh {
    position: absolute;
    left: 20px;
  }

  .check-en {
    position: absolute;
    left: 20px;
  }
}

.logout {
  max-width: 120px;

  ::v-deep(.el-dropdown-menu__item) {
    display: inline-flex;
    flex-wrap: wrap;
    min-width: 100%;
  }
}
</style>
