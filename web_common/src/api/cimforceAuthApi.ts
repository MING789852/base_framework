import { http } from "@/utils/http";
class Api{
    loginByCimforceAuthCode = (data) => {
        return http.post<any,HttpResult>("/cimforceAuth/loginByCimforceAuthCode",{data:data})
    }
}

const api = new Api()
export  default api
