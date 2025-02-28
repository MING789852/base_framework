import { http } from "@/utils/http";

class Api implements ApiBase{
    deleteData(data:any) {
        console.log(data)
    }


    saveOrUpdateData(data:any) {
        console.log(data)
    }

    selectByPage(data:any) {
        console.log(data)
    }

    getAllLabelNameMapping() {
        return http.get<any,HttpResult>("/formLabel/getAllLabelNameMapping")
    }
}

const api = new Api()
export  default api
