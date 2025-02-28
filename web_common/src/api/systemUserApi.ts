import { http } from "@/utils/http";
class Api implements ApiBase{
  selectByPage = (data) => {
    return http.post("/user/selectByPage",{data:data})
  }


  saveOrUpdateData = (data) => {
    return http.post("/user/saveOrUpdateData",{data:data})
  }

  deleteData(p0) {
    console.log(p0)
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

  createTcUserByDingDingUserId = (data) => {
    return http.get("/ddUser/createTcUserByDingDingUserId?userId="+data)
  }

  createTcUserByDingDingWorkNum = (data) => {
    return http.get("/ddUser/createTcUserByDingDingWorkNum?workNum="+data)
  }

  createTcUserByDingDingName = (data) => {
    return http.get("/ddUser/createTcUserByDingDingName?name="+data)
  }

  createTcUserWithLeaderByDingDingUserId = (data) => {
    return http.get("/ddUser/createTcUserWithLeaderByDingDingUserId?userId="+data)
  }
}

const api = new Api()
export  default api
