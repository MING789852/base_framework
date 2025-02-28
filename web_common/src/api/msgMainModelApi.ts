import { http } from "@/utils/http";

class Api implements ApiBase{
    deleteData(data:any) {
        return http.post("/msgMainModel/deleteData",{data:data})
    }


    saveOrUpdateData(data:any) {
    }

    selectByPage(data:any) {
        return http.post("/msgMainModel/selectByPage",{data:data})
    }

    saveOrUpdateMsg(data:any) {
        return http.post("/msgMainModel/saveOrUpdateMsg",{data:data})
    }

    getDictMapping(){
        return http.get<any,HttpResult>("/msgMainModel/getDictMapping")
    }
}

const api = new Api()
export  default api