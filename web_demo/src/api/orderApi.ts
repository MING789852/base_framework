import { http } from "@/utils/http";
class Api implements ApiBase{
  deleteData(data) {
    return http.post("/order/deleteData",{data:data})
  }

  exportExcel(data) {
    return http.post("/order/exportExcel",{data:data,responseType:'blob'})
  }

  saveOrUpdateData(data) {
    return http.post("/order/saveOrUpdateData",{data:data})
  }

  selectByPage(data) {
    return http.post("/order/selectByPage",{data:data})
  }
}

const api = new Api()
export  default api
