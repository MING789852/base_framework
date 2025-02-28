import { http } from "@/utils/http";

class Api implements ApiBase{
  deleteData(data:any) {
    return http.post("/businessDict/deleteData",{data:data})
  }

  saveOrUpdateData(data:any) {
    return http.post("/businessDict/saveOrUpdateData",{data:data})
  }

  selectByPage(data:any) {
    return http.post("/businessDict/selectByPage",{data:data})
  }

}

const api = new Api()
export  default api
