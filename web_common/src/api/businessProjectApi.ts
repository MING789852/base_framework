import { http } from "@/utils/http";
import urlUtils from "@/utils/urlUtils"

class Api implements ApiBase{
  deleteData(data:any) {
    return http.post("/businessProject/deleteData",{data:data})
  }

  saveOrUpdateData(data:any) {
    return http.post("/businessProject/saveOrUpdateData",{data:data})
  }

  selectByPage(data:any) {
    return http.post("/businessProject/selectByPage",{data:data})
  }

  getProjectConfig(businessType:string, version:string){
    let data = {
      businessType: businessType,
      version: version
    }
    return http.get<any,HttpResult>(urlUtils.setObjToUrlParams("/businessProject/getProjectConfig",data))
  }
}

const api = new Api()
export  default api
