import { http } from "@/utils/http";

class Api{
    getColumnPropsConfig = (data) => {
        return http.post("/column/getColumnPropsConfig",{data:data})
    }

    saveColumnPropsConfig = (data) => {
        return http.post("/column/saveColumnPropsConfig",{data:data})
    }

    resetColumnPropsConfig = (data)=>{
        return http.post("/column/resetColumnPropsConfig",{data:data})
    }
}

const api = new Api()
export  default api