import { http } from "@/utils/http";

class Api{
    // 绑定api
    saveProcessBindData = (data) => {
        return http.post("/processBind/saveProcessBindData",{data:data})
    }

    // 解绑api
    getProcessBindData = (data) => {
        return http.get<any,HttpResult>("/processBind/getProcessBindData?groupName=" + data)
    }

    // 获取绑定的api
    getDictMapping = () => {
        return http.get<any,HttpResult>("/processBind/getDictMapping")
    }
}

const api = new Api()
export  default api