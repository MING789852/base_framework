import { http } from "@/utils/http";

class Api implements ApiBase{
  selectByPage = (data) => {
    return http.post("/report/selectByPage",{data:data})
  }

  deleteData = (data) => {
    return http.post("/report/deleteData",{data:data})
  }

  saveOrUpdateData = (data) => {
    return http.post("/report/saveOrUpdateData",{data:data})
  }

  exportExcel(data) {
    return http.post("/report/exportExcel",{data:data,responseType:'blob'})
  }

  generateColumnsBySql = (data) => {
    return http.post("/report/generateColumnsBySql",{data:data})
  }

  generateQueryColumnsByShowColumns = (data) => {
    return http.post("/report/generateQueryColumnsByShowColumns",{data:data})
  }

  getConfig = (data) => {
    return http.get("/report/getConfig?reportType="+data)
  }
  saveConfig = (data) => {
    return http.post("/report/saveConfig",{data:data})
  }
  clearDataCache = () => {
    return http.get("/report/clearDataCache")
  }
}


const api = new Api()
export  default api
