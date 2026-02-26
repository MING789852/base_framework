import dingdingLoginAction from '@/views/authRegister/dingdingLoginAction'
import {message} from "@/utils/message";

class AuthRegister {
    public static instance: AuthRegister;
    private authFunctionMap: Map<string,AuthFunction> = new Map()

    constructor() {
        this.register('dingdingLogin',dingdingLoginAction)
    }

    public register(name: string, authFunction: AuthFunction) {
        this.authFunctionMap.set(name, authFunction)
    }

    public execute(loginType:string,params: AuthFunctionParams) {
        const authFunction = this.authFunctionMap.get(loginType)
        if (authFunction) {
            authFunction.execute(params)
        }else {
            message('暂不支持该登录方式->'+loginType,{type:'error'})
        }
    }
}

const instance = new AuthRegister()
export default instance