import { defineStore } from "pinia";
import { store } from "@/store";
import type { userType } from "./types";
import { router } from "@/router";
import { storageLocal } from "@pureadmin/utils";
import {getCurrentUser, getLogin, getLoginByDDAuthCode, getLoginByToken, logout, refreshTokenApi} from "@/api/user";
import cimforceAuthApi from "@/api/cimforceAuthApi";
import type { UserResult, RefreshTokenResult } from "@/api/user";
import {type DataInfo, setToken, userKey, getToken, removeAllAuth} from "@/utils/auth";
import {message} from "@/utils/message";

export const useUserStore = defineStore({
  id: "pure-user",
  state: (): userType => ({
    /**
     * 防止浏览器刷新
     */
    // 用户id
    userId: storageLocal().getItem<DataInfo<number>>(userKey)?.userId ?? "",
    // 用户名
    username: storageLocal().getItem<DataInfo<number>>(userKey)?.username ?? "",
    // 名称
    nickName: storageLocal().getItem<DataInfo<number>>(userKey)?.nickName ?? "",
    // 页面级别权限
    roles: storageLocal().getItem<DataInfo<number>>(userKey)?.roles ?? [],
    // 路由权限
    routerActionMapping: storageLocal().getItem<DataInfo<number>>(userKey)?.routerActionMapping ?? {},
    // 是否勾选了登录页的免登录
    isRemembered: false,
    // 登录页的免登录存储几天，默认7天
    loginDay: 7,
    // 登录类型 0、普通 1、钉钉登录
    loginType: 0
  }),
  actions: {
    SET_ROUTER_ACTION_MAPPING(routerActionMapping:Record<string, Record<string,string>>) {
      this.routerActionMapping = routerActionMapping
    },
    SET_USER_ID(userId: string) {
      this.userId = userId
    },
    SET_NICKNAME(nickName: string) {
      this.nickName = nickName
    },
    SET_LOGINTYPE(loginType: number) {
      this.loginType = loginType
    },
    /** 存储用户名 */
    SET_USERNAME(username: string) {
      this.username = username;
    },
    /** 存储角色 */
    SET_ROLES(roles: Array<string>) {
      this.roles = roles;
    },
    /** 存储是否勾选了登录页的免登录 */
    SET_ISREMEMBERED(bool: boolean) {
      this.isRemembered = bool;
    },
    /** 设置登录页的免登录存储几天 */
    SET_LOGINDAY(value: number) {
      this.loginDay = Number(value);
    },
    getIsLogin () {
      return new Promise((resolve, reject) => {
        getCurrentUser().then((res:any) => {
          if (res.code === 200){
            if (res.data==null){
              resolve(false)
            } else {
              resolve(true)
            }
          }else {
            reject(res)
            message('获取登录状态失败=>'+res.msg,{type: 'error'})
          }
        }).catch(res => {
          reject(res)
          message('获取登录状态失败=>'+res.message,{type: 'error'})
        })
      })
    },
    /** 登入 */
    loginByUsername(data) {
      return new Promise<UserResult>((resolve, reject) => {
        getLogin(data)
          .then(res => {
            if (res.code === 200) {
              if (res.data) {
                setToken(res.data);
                resolve(res);
              }
            }else {
              message('登录失败=>'+res.msg,{type: 'error'})
              reject(res);
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    },
    loginByToken(data) {
      return new Promise<UserResult>((resolve, reject) => {
        getLoginByToken(data)
          .then(data => {
            if (data) {
              setToken(data.data);
              resolve(data);
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    },
      /** 思方登录 */
    loginByCimforceAuthCode(data) {
      return new Promise<UserResult>((resolve, reject) => {
          cimforceAuthApi.loginByCimforceAuthCode(data)
              .then(data => {
                  if (data) {
                      setToken(data.data);
                      resolve(data);
                  }
              })
              .catch(error => {
                  reject(error);
              });
      });
    },
      /** 钉钉登录 */
    loginByDDAuthCode(data, configKey) {
      return new Promise<UserResult>((resolve, reject) => {
        getLoginByDDAuthCode(data,configKey)
          .then(data => {
            if (data) {
              setToken(data.data);
              resolve(data);
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    },
    /** 前端登出（不调用接口） */
    logOut() {
      logout().then((res:any) => {
        if (res.code === 200) {
          this.username = "";
          this.roles = [];
          removeAllAuth()
          router.push("/login");
        }
      })
    },
    /** 刷新`token` */
    handRefreshToken(data) {
      return new Promise<RefreshTokenResult>((resolve, reject) => {
        refreshTokenApi(data)
          .then(res => {
            if (res.code === 200) {
              setToken(res.data);
              resolve(res);
            } else {
              message(res.msg,{type: 'error'})
              reject(res.msg)
            }
          })
          .catch(error => {
            reject(error);
          });
      });
    },
    getAccessToken() {
      return new Promise<string>(async (resolve, reject) => {
        const data = getToken();
        const now = new Date().getTime();
        const expired = parseInt(data.expires) - now <= 0;
        if (expired) {
          let res: any = await this.handRefreshToken({refreshToken: data.refreshToken})
          if (res.code === 200) {
            resolve(res.data.accessToken)
          } else {
            reject('刷新token失败')
          }
        } else {
          resolve(data.accessToken)
        }
      });
    }
  }
});

export function useUserStoreHook() {
  return useUserStore(store);
}
