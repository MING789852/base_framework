import { http } from "@/utils/http";

class Api{
  saveOrUpdateData(data){
    return http.post<any,HttpResult>("/fileTemplate/saveOrUpdateData",{data:data})
  }

  getDataByCode(data){
    return http.get<any,HttpResult>("/fileTemplate/getDataByCode?code="+data)
  }
}

const api = new Api()
export  default api
