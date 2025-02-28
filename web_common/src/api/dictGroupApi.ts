import { http } from "@/utils/http";

class Api implements ApiBase{
  selectByPage = (data) => {
    return http.post("/dictGroup/selectByPage",{data:data})
  }

  deleteData = (data) => {
    return http.post("/dictGroup/deleteData",{data:data})
  }

  saveOrUpdateData = (data) => {
    return http.post("/dictGroup/saveOrUpdateData",{data:data})
  }

  importExcel = (data) => {
    return http.request(
      "post",
      "/dictGroup/importExcel",
      { data },
      {
        headers: {
          "Content-Type": "multipart/form-data"
        }
      }
    );
  }
}


const api = new Api()
export  default api
