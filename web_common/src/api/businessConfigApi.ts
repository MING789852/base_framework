import { http } from "@/utils/http";
import urlUtils from "@/utils/urlUtils"

class Api{
  saveConfig(data:any) {
    return http.post("/businessConfig/saveConfig",{data:data})
  }
  getConfig(code:string, businessType:string){
    let data = {
      businessType: businessType,
      code: code
    }
    return http.get<any,HttpResult>(urlUtils.setObjToUrlParams("/businessConfig/getConfig",data))
  }
}

const api = new Api()
export  default api
