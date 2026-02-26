import { http } from "@/utils/http";
import common from "@/utils/common";

export const getAsyncRoutes = () => {
    // console.log('第一次加载路由')
    return http.get<any, HttpResult>("/router/getCurrentRouter").then(res=>{
        if (res.code === 200){
            common.updateCurrentUserInfo({
                routerActionMapping:res.data.routerActionMapping,
                roles: res.data.roles
            })
            return res
        }else {
            return {}
        }
    })
};
