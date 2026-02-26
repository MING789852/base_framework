// import "@/utils/sso";
import Cookies from "js-cookie";
import { getConfig } from "@/config";
import NProgress from "@/utils/progress";
import { transformI18n } from "@/plugins/i18n";
import { buildHierarchyTree } from "@/utils/tree";
import rootRemainingRouter from "./modules/remaining";
import { useMultiTagsStoreHook } from "@/store/modules/multiTags";
import { usePermissionStoreHook } from "@/store/modules/permission";
import {isUrl, openLink, storageLocal, isAllEmpty, isNullOrUnDef} from "@pureadmin/utils";
import {
  ascending,
  getTopMenu,
  initRouter,
  isOneOfArray,
  getHistoryMode,
  findRouteByPath,
  handleAliveRoute,
  formatTwoStageRoutes,
  formatFlatteningRoutes
} from "./utils";
import {
  type Router,
  createRouter,
  type RouteRecordRaw,
  type RouteComponent
} from "vue-router";
import {
  type DataInfo,
  userKey,
  multipleTabsKey, removeAllAuth
} from "@/utils/auth";


const moduleRemainingRouterImport:Record<string, any> = import.meta.glob(
  ["@root/router/remaining"],
  {
    eager: true
  }
);
const moduleRemainingRouter:Array<RouteConfigsTable>=Object.values(moduleRemainingRouterImport)[0].default
const remainingRouter = [...rootRemainingRouter, ...moduleRemainingRouter]

const moduleWhiteListImport:Record<string, any> = import.meta.glob(
  ["@root/router/whiteList"],
  {
    eager: true
  }
);
const moduleWhiteList:Array<string>=Object.values(moduleWhiteListImport)[0].default

/** 自动导入全部静态路由，无需再手动引入！匹配 src/router/modules 目录（任何嵌套级别）中具有 .ts 扩展名的所有文件，除了 remaining.ts 文件
 * 如何匹配所有文件请看：https://github.com/mrmlnc/fast-glob#basic-syntax
 * 如何排除文件请看：https://cn.vitejs.dev/guide/features.html#negative-patterns
 */
const modules: Record<string, any> = import.meta.glob(
  ["./modules/**/*.ts", "!./modules/**/remaining.ts"],
  {
    eager: true
  }
);

/** 原始静态路由（未做任何处理） */
const routes = [];

Object.keys(modules).forEach(key => {
  routes.push(modules[key].default);
});

/** 导出处理后的静态路由（三级及以上的路由全部拍成二级） */
export const constantRoutes: Array<RouteRecordRaw> = formatTwoStageRoutes(
  formatFlatteningRoutes(buildHierarchyTree(ascending(routes.flat(Infinity))))
);


/** 用于渲染菜单，保持原始层级 */
export const constantMenus: Array<RouteComponent> = ascending(
  routes.flat(Infinity)
).concat(...remainingRouter);

/** 不参与菜单的路由 */
export const remainingPaths = Object.keys(remainingRouter).map(v => {
  return remainingRouter[v].path;
});

/** 创建路由实例 */
export const router: Router = createRouter({
  history: getHistoryMode(import.meta.env.VITE_ROUTER_HISTORY),
  routes: constantRoutes.concat(...(remainingRouter as any)),
  strict: true,
  scrollBehavior(to, from, savedPosition) {
    return new Promise(resolve => {
      if (savedPosition) {
        return savedPosition;
      } else {
        if (from.meta.saveSrollTop) {
          const top: number =
            document.documentElement.scrollTop || document.body.scrollTop;
          resolve({ left: 0, top });
        }
      }
    });
  }
});

/** 重置路由 */
export function resetRouter() {
  router.getRoutes().forEach(route => {
    const { name, meta } = route;
    if (name && router.hasRoute(name) && meta?.backstage) {
      router.removeRoute(name);
      router.options.routes = formatTwoStageRoutes(
        formatFlatteningRoutes(
          buildHierarchyTree(ascending(routes.flat(Infinity)))
        )
      );
    }
  });
  usePermissionStoreHook().clearAllCachePage();
}

/** 路由白名单 */
const whiteList = [...["/login",'/auth/authLogin','/auth/authLoginTest'], ...moduleWhiteList];

const { VITE_HIDE_HOME } = import.meta.env;

router.beforeEach((to: ToRouteType, _from, next) => {
  if (to.meta?.keepAlive) {
    handleAliveRoute(to, "add");
    // 页面整体刷新和点击标签页刷新
    if (_from.name === undefined || _from.name === "Redirect") {
      handleAliveRoute(to);
    }
  }
  const userInfo = storageLocal().getItem<DataInfo<number>>(userKey);
  NProgress.start();
  const externalLink = isUrl(to?.name as string);
  if (!externalLink) {
    to.matched.some(item => {
      if (!item.meta.title) return "";
      const Title = getConfig().Title;
      if (Title)
        document.title = `${transformI18n(item.meta.title)} | ${Title}`;
      else document.title = transformI18n(item.meta.title);
    });
  }
  /** 如果已经登录并存在登录信息后不能跳转到路由白名单，而是继续保持在当前页面 */
  function toCorrectRoute() {
    if (moduleWhiteList.includes(to.fullPath)){
      next()
    }else {
      whiteList.includes(to.fullPath) ? next(_from.fullPath) : next();
    }
  }
  if (Cookies.get(multipleTabsKey) && userInfo) {
    // 无权限跳转403页面
    if (to.meta?.roles && !isOneOfArray(to.meta?.roles, userInfo?.roles)) {
      next({ path: "/error/403" });
    }
    // 开启隐藏首页后在浏览器地址栏手动输入首页welcome路由则跳转到404页面
    if (VITE_HIDE_HOME === "true" && to.fullPath === "/welcome") {
      next({ path: "/error/404" });
    }
    if (_from?.name) {
      // name为超链接
      if (externalLink) {
        openLink(to?.name as string);
        NProgress.done();
      } else {
        toCorrectRoute();
      }
    } else {
      // 刷新
      if (usePermissionStoreHook().wholeMenus.length === 0 && to.path !== "/login") {
        // 授权登录直接跳转
        let toName = to.name
        if (toName === 'authLogin'){
          next()
        } else {
          initRouter().then((router: Router) => {
            if (!useMultiTagsStoreHook().getMultiTagsCache) {
              const { path } = to;
              const route = findRouteByPath(
                path,
                router.options.routes[0].children
              );
              //设置首页tag
              getTopMenu(true);
              // query、params模式路由传参数的标签页不在此处处理
              if (route && route.meta?.title) {
                if (isAllEmpty(route.parentId) && route.meta?.backstage) {
                  // 此处为动态顶级路由（目录）
                  const { path, name, meta } = route.children[0];
                  useMultiTagsStoreHook().handleTags("push", {
                    path,
                    name,
                    meta
                  });
                } else {
                  const { path, name, meta } = route;
                  useMultiTagsStoreHook().handleTags("push", {
                    path,
                    name,
                    meta
                  });
                }
              }
            }
            // 确保动态路由完全加入路由列表并且不影响静态路由（注意：动态路由刷新时router.beforeEach可能会触发两次，第一次触发动态路由还未完全添加，第二次动态路由才完全添加到路由列表，如果需要在router.beforeEach做一些判断可以在to.name存在的条件下去判断，这样就只会触发一次）
            if (isAllEmpty(to.name)) router.push(to.fullPath);
          });
        }
      }
      toCorrectRoute();
    }
  } else {
    if (to.path !== "/login") {
      if (isNullOrUnDef(to.query.token)){
        //白名单列表直接放行
        if (whiteList.indexOf(to.path) !== -1) {
          next();
        } else {
          removeAllAuth()
          next({ path: "/login" });
        }
      } else {
        //todo token登录未完成
        // useUserStoreHook()
        //   .loginByToken('eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJVU0VSIjoidGVzdCIsIlRZUEUiOiJMT0dJTl9UT0tFTiIsImV4cCI6MTcxMTY3Nzg4OX0.gwS7Wh9K0U340yGXQK4E7ymXu1RBGlXDrk0fltjhJCI')
        //   .then(res => {
        //     if (res.code === 200) {
        //       // 获取后端路由
        //       initRouter().then(() => {
        //         message("登录成功", { type: "success" });
        //         // router.push(to.path);
        //         // 递归调用直到找到路由，一定要注意存在动态路由才调用
        //         next({ ...to, replace: true })
        //       });
        //     }
        //   }).catch(res => {
        //   message('登录失败=>'+res.message,{type: 'error'})
        // })
      }
    } else {
      next();
    }
  }
});

router.afterEach(() => {
  NProgress.done();
});

export default router;
