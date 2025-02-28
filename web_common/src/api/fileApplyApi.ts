import {http} from "@/utils/http";

class Api implements ApiBase{
  selectByPage = (data) => {
    return http.post("/fileApply/selectByPage",{data:data})
  }

  deleteData = (data) => {
    return http.post("/fileApply/deleteData",{data:data})
  }

  fileApply = (data) => {
    return http.post("/fileApply/fileApply",{data:data})
  }

  fileAuth = (data) => {
    return http.post("/fileApply/fileAuth",{data:data})
  }

  checkFileAuthStatus = (data) => {
    return http.post("/fileApply/checkFileAuthStatus",{data:data})
  }

  fillData = (data) => {
    return http.post("/fileApply/fillData",{data:data})
  }

  saveOrUpdateData = (data) => {
    console.log(data)
  }
}


const api = new Api()
export  default api
