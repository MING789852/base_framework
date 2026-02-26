import {http} from "@/utils/http";

class Api {
    getData(data: any) {
        return http.post("/flowableShow/getData", {data: data})
    }

    getDictMapping() {
        return http.post("/flowableShow/getDictMapping")
    }

    getTodoList(){
        return http.post("/flowableShow/getTodoList")
    }
}

const api = new Api()
export default api
