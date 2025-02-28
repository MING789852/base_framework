import {http} from "@/utils/http";

class Api {
    setParamsCache(data: any) {
        return http.post<any, HttpResult>("/paramsCache/setParamsCache", {data: data})
    }
}

const api = new Api()
export default api