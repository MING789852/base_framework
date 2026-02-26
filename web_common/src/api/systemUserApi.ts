import { http } from "@/utils/http";
class Api implements ApiBase{
  selectByPage = (data) => {
    return http.post("/user/selectByPage",{data:data})
  }


  saveOrUpdateData = (data) => {
    return http.post("/user/saveOrUpdateData",{data:data})
  }

  deleteData(data) {
    return http.post("/user/deleteData",{data:data})
  }

  updateUserInfo = (data)=>{
    return http.post("/user/updateUserInfo",{data:data})
  }

  updatePassword = (data)=>{
    return http.post("/user/updatePassword",{data:data})
  }

  changeEnable = (data) => {
    return http.post("/user/changeEnable",{data:data})
  }


  getUserIdAndNickNameMapping = () => {
    return http.get<any,HttpResult>("/user/getUserIdAndNickNameMapping")
  }

  removeLoginUser = (data) => {
    return http.post("/user/removeLoginUser",{data:data})
  }

  getRequestAction = (data) => {
    return http.post("/user/getRequestAction",{data:data})
  }

  getAllLoginUser = ()=>{
    return http.get("/user/getAllLoginUser")
  }

  getUserTypeMapping = () => {
    return http.get("/user/getUserTypeMapping")
  }

  resetLoginTry = (data) => {
      return http.post("/user/resetLoginTry",{data:data})
  }
}

const api = new Api()
export  default api
