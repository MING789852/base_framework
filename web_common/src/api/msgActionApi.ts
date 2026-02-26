import { http } from "@/utils/http";

class Api{
    jumpAfterFinishMsg(businessType:string,businessKey:string,all:boolean) {
        return http.get<any,HttpResult>("/msgAction/jumpAfterFinishMsg?businessType="+businessType+"&businessKey="+businessKey+"&all="+all)
    }
}

const api = new Api()
export  default api