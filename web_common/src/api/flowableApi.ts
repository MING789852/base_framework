import { http } from "@/utils/http";

class Api implements ApiBase{
  viewProcess(data:any) {
    return http.get("/flowable/viewProcess?processId="+data)
  }

  getDictList() {
    return http.get<any,HttpResult>("/flowable/getDictList")
  }

  initUnRecordModel() {
    return http.get<any,HttpResult>("/flowable/initUnRecordModel")
  }
  deleteData(data:any) {
    return http.post("/flowable/deleteData",{data:data})
  }

  forceDeleteData(data:any) {
    return http.post("/flowable/forceDeleteData",{data:data})
  }

  saveOrUpdateData(data:any) {
    console.log(data)
  }

  selectByPage(data:any) {
    return http.post("/flowable/selectByPage",{data:data})
  }

  fillModel(data:any) {
    return http.post("/flowable/fillModel",{data:data})
  }

  saveModel(data:any) {
    return http.post("/flowable/saveModel",{data:data})
  }

  getProcInsPageByProcDefKey(data:any) {
    return http.post("/flowable/getProcInsPageByProcDefKey",{data:data})
  }

  deleteProcessInstance(data:any) {
    return http.post("/flowable/deleteProcessInstance",{data:data})
  }
}

const api = new Api()
export  default api
