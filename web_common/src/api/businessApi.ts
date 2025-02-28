import { http } from "@/utils/http";

class Api implements ApiBase{
  deleteData(data:any) {
    return http.post("/business/deleteData",{data:data})
  }

  saveOrUpdateData(data:any) {
    return http.post("/business/saveOrUpdateData", {
      data: data,
      headers: {
        "Content-Type": "multipart/form-data"
      }
    })
  }

  selectByPage(data:any) {
    return http.post("/business/selectDynamicByPage",{data:data})
  }

  selectApprovingList(data:any) {
    return http.post("/business/selectApprovingList",{data:data})
  }

  selectOne(data:any) {
    return http.post("/business/selectOne",{data:data})
  }

  startBusiness(data:any) {
    return http.post("/business/startBusiness",{data:data})
  }

  taskBusiness(data:any) {
    return http.post("/business/taskBusiness", {
      data: data,
      headers: {
        "Content-Type": "multipart/form-data"
      }
    })
  }

  taskBusinessAll(data:any) {
    return http.post("/business/taskBusinessAll",{data:data})
  }

  rollBackAndDeleteDeployment(data:any) {
    return http.post("/business/rollBackAndDeleteDeployment",{data:data})
  }

  rollBackBusiness(data:any) {
    return http.post("/business/rollBackBusiness",{data:data})
  }

  getProjectWithItemIdentity(data:any) {
    return http.post("/business/getProjectWithItemIdentity",{data:data})
  }

  exportExcel = (data:any) => {
    return http.post("/business/exportExcel",{data:data,responseType:'blob'})
  }
}

const api = new Api()
export  default api
