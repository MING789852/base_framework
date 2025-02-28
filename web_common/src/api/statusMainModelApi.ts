import { http } from "@/utils/http";

class Api implements ApiBase{
    deleteData(data:any) {
        return http.post("/statusMainModel/deleteData",{data:data})
    }


    saveOrUpdateData(data:any) {
        return http.post("/statusMainModel/saveOrUpdateData",{data:data})
    }

    selectByPage(data:any) {
        return http.post("/statusMainModel/selectByPage",{data:data})
    }

    statusModelChangeAdd(data:any) {
        return http.post("/statusMainModel/statusModelChangeAdd",{data:data})
    }

    getStatusModelResult(data:any){
        return http.post("/statusMainModel/getStatusModelResult",{data:data})
    }
}

const api = new Api()
export  default api
