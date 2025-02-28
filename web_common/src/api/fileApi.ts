import {http} from "@/utils/http";

class Api {
    realDeleteFile = (data) => {
        return http.get<any, HttpResult>("/file/realDeleteFile?id="+data)
    }

    getIdAndFileMapping = (data) => {
        return http.post<any, HttpResult>("/file/getIdAndFileMapping", {data: data})
    }

    getFileInfoByIdList = (data) => {
        return http.post<any, HttpResult>("/file/getFileInfoByIdList", {data: data})
    }

    uploadFile = (data) => {
        return http.post<any, HttpResult>("/file/uploadFile", {
            data: data,
            headers: {
                "Content-Type": "multipart/form-data"
            }
        })
    }

    createChunkUpload = (data) => {
        return http.post<any, HttpResult>("/file/createChunkUpload", {data: data})
    }

    uploadChunk = (data) => {
        return http.post<any, HttpResult>("/file/uploadChunk", {
          data: data,
          headers: {
            "Content-Type": "multipart/form-data"
          }
        })
    }

    mergeChunk = (data) => {
        return http.post<any, HttpResult>("/file/mergeChunk", {data: data})
    }
}

const api = new Api()
export default api

