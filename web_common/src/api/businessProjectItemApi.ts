import { http } from "@/utils/http";

class Api implements ApiBase{
  deleteData(data:any) {
    return http.post("/businessProjectItem/deleteData",{data:data})
  }

  saveOrUpdateData(data:any) {
    return http.post("/businessProjectItem/saveOrUpdateData",{data:data})
  }

  selectByPage(data:any) {
    return http.post("/businessProjectItem/selectByPage",{data:data})
  }

  selectByAllProjectItem(businessType:string,version:string) {
    return http.get("/businessProjectItem/selectByAllProjectItem?businessType="+businessType+'&version='+version)
  }
}

const api = new Api()
export  default api
