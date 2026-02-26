import {http} from "@/utils/http";

class Api implements ApiBase {
    viewProcess(data: any) {
        return http.get("/flowable/viewProcess?processId=" + data)
    }

    getDictList() {
        return http.get<any, HttpResult>("/flowable/getDictList")
    }

    initUnRecordModel() {
        return http.get<any, HttpResult>("/flowable/initUnRecordModel")
    }

    deleteData(data: any) {
        return http.post("/flowable/deleteData", {data: data})
    }

    forceDeleteData(data: any) {
        return http.post("/flowable/forceDeleteData", {data: data})
    }

    saveOrUpdateData(data: any) {
        console.log(data)
    }

    selectByPage(data: any) {
        return http.post("/flowable/selectByPage", {data: data})
    }

    fillModel(data: any) {
        return http.post("/flowable/fillModel", {data: data})
    }

    saveModel(data: any) {
        return http.post("/flowable/saveModel", {data: data})
    }

    changeModelNode(data: any) {
        return http.post("/flowable/changeModelNode", {data: data})
    }

    changeModelEdge(data: any) {
        return http.post("/flowable/changeModelEdge", {data: data})
    }

    getProcInsPageByProcDefKey(data: any) {
        return http.post("/flowable/getProcInsPageByProcDefKey", {data: data})
    }

    deleteProcessInstance(data: any) {
        return http.post("/flowable/deleteProcessInstance", {data: data})
    }


    getExecuteInfoByProcessId(data: any) {
        return http.get<any, HttpResult>('/flowable/getExecuteInfoByProcessId?processInstanceId=' + data)
    }

    createTestProcess(data: any) {
        return http.post<any, HttpResult>("/flowable/createTestProcess", {data: data})
    }

    executeProcess(data: any) {
        return http.post<any, HttpResult>("/flowable/executeProcess", {data: data})
    }

    reSendApproveMsg(data: any) {
        return http.post<any, HttpResult>("/flowable/reSendApproveMsg", {data: data})
    }

    getProcessVariableList(data: any) {
        return http.get<any, HttpResult>('/flowable/getProcessVariableList?processInstanceId=' + data)
    }

    getTaskVariableList(data: any) {
        return http.get<any, HttpResult>('/flowable/getTaskVariableList?processInstanceId=' + data)
    }

    saveOrUpdateProcessVariable(processInstanceId, data:any){
        return http.post<any, HttpResult>('/flowable/saveOrUpdateProcessVariable?processInstanceId=' + processInstanceId, {data: data})
    }

    saveOrUpdateTaskVariable(processInstanceId,taskId, data:any){
        return http.post<any, HttpResult>('/flowable/saveOrUpdateTaskVariable?taskId=' + taskId + '&processInstanceId=' + processInstanceId, {data: data})
    }

    deleteProcessVariable(processInstanceId, data){
        return http.post<any, HttpResult>('/flowable/deleteProcessVariable?processInstanceId=' + processInstanceId, {data: data})
    }

    deleteTaskVariable(processInstanceId,taskId, data){
        return http.post<any, HttpResult>('/flowable/deleteTaskVariable?taskId=' + taskId + '&processInstanceId=' + processInstanceId, {data: data})
    }
}

const api = new Api()
export default api
