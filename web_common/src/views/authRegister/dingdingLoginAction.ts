import {removeToken} from "@/utils/auth";
import common from "@/utils/common";
import {message} from "@/utils/message";
import * as dd from "dingtalk-jsapi";
import {useUserStoreHook} from "@/store/modules/user";
import msgActionApi from "@/api/msgActionApi";

interface LoginHook {
    preLogin?: () => void
    afterLogin?: () => void
}

class DingDingLoginAction implements AuthFunction {

    public executeCallBack(callBack: () => void, loginHook?: LoginHook) {
        loginHook?.preLogin?.()
        callBack()
        loginHook?.afterLogin?.()
    }

    public handleDingDingLogin(configKey: string, callBack: () => void, loginHook: LoginHook) {
        try {
            common.showGlobalLoading()
            loginHook?.preLogin?.()
            dd.runtime.permission.requestAuthCode({
                corpId: "ding604ca88369c9094535c2f4657eb6378f"
            }).then(result => {
                let authCode = result.code
                // 设置登录类型为钉钉
                useUserStoreHook().SET_LOGINTYPE(1)
                useUserStoreHook()
                    .loginByDDAuthCode(authCode, configKey)
                    .then(res => {
                        if (res.code === 200) {
                            this.executeCallBack(callBack)
                            loginHook?.afterLogin?.()
                        } else {
                            message('登录失败=>' + res.msg, {type: 'error'})
                        }
                    }).catch(res => {
                    message('登录失败=>' + res.message, {type: 'error'})
                }).catch((reason) => {
                    common.showLog(reason)
                })
            }).catch((reason) => {
                common.showLog(reason)
            })
        } catch (reason) {
            common.showLog(reason)
        } finally {
            common.closeGlobalLoading()
        }
    }

    public execute(params: AuthFunctionParams) {
        const configKey = params.urlParams.configKey
        let loginHook:LoginHook = {
            afterLogin: () => {
                if ((params.urlParams?.jumpAfterFinish??'false')==='true'){
                    let businessKey = params.urlParams.businessKey
                    let businessType = params.urlParams.businessType
                    msgActionApi.jumpAfterFinishMsg(businessType,businessKey,false).then(res=>{
                        console.log(res.data)
                    })
                }
            }
        }
        // 如果是测试则直接通过
        if (params.isTest) {
            this.executeCallBack(params.callBack,loginHook)
        } else {
            //清空缓存
            removeToken();
            //登录
            this.handleDingDingLogin(configKey, params.callBack,loginHook)
        }
    }
}

const instance = new DingDingLoginAction()
export default instance