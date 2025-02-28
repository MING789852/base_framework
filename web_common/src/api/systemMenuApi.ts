import { http } from "@/utils/http";

class Api{
  getRouterWithAction = (data) => {
    return http.post("/router/getRouterWithAction",{data:data})
  }

  getRouterList = () => {
    return http.get("/router/getRouterList")
  }

  getPrivateRouterList = () => {
    return http.get("/router/getPrivateRouterList")
  }

  addRouter = (data) => {
    return http.post('/router/addRouter',{data:data})
  }


  updateRouter = (data) => {
    return http.post('/router/updateRouter',{data:data})
  }

  deleteRouter = (data) => {
    return http.post('/router/deleteRouter',{data:data})
  }
}


const api = new Api()
export  default api
