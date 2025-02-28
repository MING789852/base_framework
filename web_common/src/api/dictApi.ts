import { http } from "@/utils/http";

class Api implements ApiBase{
  selectByPage = (data) => {
    return http.post("/dict/selectByPage",{data:data})
  }

  deleteData = (data) => {
    return http.post("/dict/deleteData",{data:data})
  }

  saveOrUpdateData = (data) => {
    return http.post("/dict/saveOrUpdateData",{data:data})
  }

  getDictMappingByQuery = (data:DictQueryType[]) => {
    return http.post<any,HttpResult>("/dict/getDictMappingByQuery",{data:data})
  }

  getDictByGroupKey = async (data) => {
    let res: any = await http.get("/dict/getDictByGroupKey?groupKey=" + data)
    if (res.code === 200) {
      return res.data
    } else {
      return {}
    }
  }
}


const api = new Api()
export  default api
