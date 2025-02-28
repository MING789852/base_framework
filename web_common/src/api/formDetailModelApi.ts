import { http } from "@/utils/http";

class Api implements ApiBase{
    deleteData(data:any) {
        return http.post("/formDetailModel/deleteData",{data:data})
    }


    saveOrUpdateData(data:any) {
        return http.post("/formDetailModel/saveOrUpdateData",{data:data})
    }

    selectByPage(data:any) {
        return http.post("/formDetailModel/selectByPage",{data:data})
    }

    getDictMapping(){
        return http.get("/formDetailModel/getDictMapping")
    }
}

const api = new Api()
export  default api