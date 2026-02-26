import Cookies from "js-cookie";
import {isNullOrUnDef, storageLocal} from "@pureadmin/utils";
import { useUserStoreHook } from "@/store/modules/user";
import {resetRouter} from "@/router";

export interface DataInfo<T> {
  /** token */
  accessToken?: string;
  /** `accessToken`的过期时间（时间戳） */
  expires?: T;
  /** 用于调用刷新accessToken的接口时所需的token */
  refreshToken?: string;
  /** 用户名 */
  username?: string;
  /** 用户昵称 */
  nickName?: string;
  /** 用户id */
  userId?:string;
  /** 用户邮箱 */
  email?:string;
  /** 当前登陆用户的角色 */
  roles?: Array<string>;
  /** 当前登陆用户的类型 */
  userType?: number;
  /** 当前登陆用户的权限 */
  routerActionMapping?:Record<string, Record<string,string>>;
}

export const userKey = "user-info";
export const TokenKey = "authorized-token";
/**
 * 通过`multiple-tabs`是否在`cookie`中，判断用户是否已经登录系统，
 * 从而支持多标签页打开已经登录的系统后无需再登录。
 * 浏览器完全关闭后`multiple-tabs`将自动从`cookie`中销毁，
 * 再次打开浏览器需要重新登录系统
 * */
export const multipleTabsKey = "multiple-tabs";

/** 获取`token` */
export function getToken(): DataInfo<number> {
  // 此处与`TokenKey`相同，此写法解决初始化时`Cookies`中不存在`TokenKey`报错
  return Cookies.get(TokenKey)
    ? JSON.parse(Cookies.get(TokenKey))
    : storageLocal().getItem(userKey);
}

export function getUserInfo():DataInfo<number>{
  return storageLocal().getItem(userKey)
}


export function updateTokenProp(data:DataInfo<number>|Record<string, any>){
    let userInfo = getUserInfo();
    storageLocal().setItem(userKey,{
      ...userInfo, ...data
    })
}


/**
 * @description 设置`token`以及一些必要信息并采用无感刷新`token`方案
 * 无感刷新：后端返回`accessToken`（访问接口使用的`token`）、`refreshToken`（用于调用刷新`accessToken`的接口时所需的`token`，`refreshToken`的过期时间（比如30天）应大于`accessToken`的过期时间（比如2小时））、`expires`（`accessToken`的过期时间）
 * 将`accessToken`、`expires`这两条信息放在key值为authorized-token的cookie里（过期自动销毁）
 * 将`username`、`roles`、`refreshToken`、`expires`这四条信息放在key值为`user-info`的localStorage里（利用`multipleTabsKey`当浏览器完全关闭后自动销毁）
 */
export function setToken(data: DataInfo<Date>) {
    if (isNullOrUnDef(data)) {
        return
    }
    let expires = 0;
    const {accessToken, refreshToken, userId, email} = data;
    const {isRemembered, loginDay} = useUserStoreHook();
    expires = new Date(data.expires).getTime(); // 如果后端直接设置时间戳，将此处代码改为expires = data.expires，然后把上面的DataInfo<Date>改成DataInfo<number>即可
    const cookieString = JSON.stringify({accessToken, refreshToken, expires});

    expires > 0
        ? Cookies.set(TokenKey, cookieString, {
            expires: (expires - Date.now()) / 86400000
        })
        : Cookies.set(TokenKey, cookieString);

    Cookies.set(
        multipleTabsKey,
        "true",
        isRemembered
            ? {
                expires: loginDay
            }
            : {}
    );

    function setUserKey(username: string, nickName: string, roles: Array<string>, routerActionMapping: Record<string, Record<string, string>>, userType: number) {
        //内存存储
        useUserStoreHook().SET_USERNAME(username);
        useUserStoreHook().SET_ROLES(roles);
        useUserStoreHook().SET_NICKNAME(nickName);
        useUserStoreHook().SET_USER_ID(userId);
        useUserStoreHook().SET_ROUTER_ACTION_MAPPING(routerActionMapping)
        //本地存储
        storageLocal().setItem(userKey, {
            userId,
            refreshToken,
            accessToken,
            expires,
            nickName,
            username,
            routerActionMapping,
            roles,
            email,
            userType
        });
    }
    if (data.username) {
        const {username, nickName, roles, routerActionMapping, userType} = data;
        setUserKey(username, nickName, roles, routerActionMapping, userType);
    } else {
        const username =
            storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "";
        const nickName =
            storageLocal().getItem<DataInfo<number>>(userKey)?.nickName ?? "";
        const roles =
            storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [];
        const routerActionMapping =
            storageLocal().getItem<DataInfo<number>>(userKey)?.routerActionMapping ?? {};
        const userType =
            storageLocal().getItem<DataInfo<number>>(userKey)?.userType;
        setUserKey(username, nickName, roles, routerActionMapping, userType);
    }
}

/** 删除`token`以及key值为`user-info`的localStorage信息 */
export function removeToken() {
  Cookies.remove(TokenKey);
  Cookies.remove(multipleTabsKey);
  storageLocal().removeItem(userKey);
}

function clearAllCookies() {
  Object.keys(Cookies.get()).forEach(cookieName =>{
      Cookies.remove(cookieName)
  });
}

export function removeAllAuth() {
  clearAllCookies();
  storageLocal().clear();
  resetRouter();
}

export function hasRoles(roleCodeList:Array<string>=[]) {
  if (useUserStoreHook().username === 'admin') {
    return  true
  }
  let userRoleCodeList = useUserStoreHook().roles
  if (isNullOrUnDef(userRoleCodeList)){
    return  false
  }
  for (let i = 0; i < roleCodeList.length; i++) {
    if (userRoleCodeList.includes(roleCodeList[i])){
      return true
    }
  }
  return false
}

/** 格式化token（jwt格式） */
export const formatToken = (token: string): string => {
  // return "Bearer " + token;
  return token;
};