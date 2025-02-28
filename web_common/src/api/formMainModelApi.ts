import { http } from "@/utils/http";

class Api implements ApiBase{
    deleteData(data:any) {
        return http.post("/formMainModel/deleteData",{data:data})
    }


    saveOrUpdateData(data:any) {
        return http.post("/formMainModel/saveOrUpdateData",{data:data})
    }

    selectByPage(data:any) {
        return http.post("/formMainModel/selectByPage",{data:data})
    }

    getFormModelResultById(data:any) {
        return http.post("/formMainModel/getFormModelResultById",{data:data})
    }
}

const api = new Api()
export  default api
