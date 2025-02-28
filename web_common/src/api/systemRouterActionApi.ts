import { http } from "@/utils/http";
import * as console from "console";

class Api{
    getRouterActionDataByRouter = (data) => {
        return http.post("/routerAction/getRouterActionDataByRouter",{data:data})
    }

    saveRouterActionData = (data) => {
        return http.post("/routerAction/saveRouterActionData",{data:data})
    }

    saveRouterActionRef = (data) => {
        return http.post("/routerAction/saveRouterActionRef",{data:data})
    }

    getRouterActionRefByRole = (data) => {
        return http.post("/routerAction/getRouterActionRefByRole",{data:data})
    }

    deleteRouterActionData = (data) => {
        return http.post("/routerAction/deleteRouterActionData",{data:data})
    }
}

const api = new Api()
export  default api
