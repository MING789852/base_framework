import { http } from "@/utils/http";

export type UserResult = {
  code: number,
  msg: string,
  data: {
    /** token */
    accessToken: string;
    /** `accessToken`的过期时间（时间戳） */
    expires: Date;
    /** 用于调用刷新accessToken的接口时所需的token */
    refreshToken: string;
    /** 用户名 */
    username?: string;
    /** 用户昵称 */
    nickName?: string;
    /** 用户id */
    userId?:string;
    /** 当前登陆用户的角色 */
    roles?: Array<string>;
  };
};

export type RefreshTokenResult = {
  code: number;
  msg: string;
  data: {
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

/** 登录 */
export const getLogin = (data?: object) => {
  return http.post<any,UserResult>("/auth/login", { data });
};

export type CaptchaVo= {
  code: number,
  msg: string,
  data: {
    base64:string
  }
}

export const createCaptcha = (data?: object) => {
  return http.get<any,CaptchaVo>("/auth/createCaptcha");
};

export const getLoginByToken = (token?: object) => {
  return http.get<any,UserResult>("/auth/loginByToken?token=" + token);
};

export const getLoginByAuthCode = (authCode: string,configKey: string) => {
  if (configKey===undefined||configKey===null){
    return http.get<any,UserResult>("/ddUser/loginByAuthCode?authCode=" + authCode);
  }else {
    return http.get<any,UserResult>("/ddUser/loginByAuthCode?authCode=" + authCode + '&configKey=' + configKey);
  }
};

/** 刷新token */
export const refreshTokenApi = (data?: object) => {
  return http.post<any,RefreshTokenResult>("/auth/doRefreshToken", { data });
};

export const logout = () => {
  return http.get("/auth/logout")
}

export const getCurrentUser = () => {
  return http.get("/auth/getCurrentUser")
}
