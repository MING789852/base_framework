import { http } from "@/utils/http";

class Api implements ApiBase{
    deleteData(data:any) {
        return http.post("/statusDetailModel/deleteData",{data:data})
    }


    saveOrUpdateData(data:any) {
        return http.post("/statusDetailModel/saveOrUpdateData",{data:data})
    }

    selectByPage(data:any) {
        return http.post("/statusDetailModel/selectByPage",{data:data})
    }

    getDictMapping(){
        return http.get("/statusDetailModel/getDictMapping")
    }

    getDetailModelMappingByMainModelId(mainModelId:string){
        return http.get("/statusDetailModel/getDetailModelMappingByMainModelId?mainModelId="+mainModelId)
    }

}

const api = new Api()
export  default api