import { http } from "@/utils/http";

class Api{
  getConfig = (configKey,nameList) => {
    return http.post<any,HttpResult>("/mappingStringConfig/getConfig?configKey="+configKey,{data:nameList})
  }

  saveConfig = (configKey,data) => {
    return http.post<any,HttpResult>("/mappingStringConfig/saveConfig?configKey="+configKey,{data:data})
  }
}


const api = new Api()
export  default api
