import { http } from "@/utils/http";

class Api implements ApiBase{
  deleteData(p0) {
    console.log(p0)
  }

  exportExcel(p0) {
    console.log(p0)
  }

  saveOrUpdateData(p0) {
    console.log(p0)
  }

  selectByPage(p0) {
    console.log(p0)
  }

  selectByList = (data) => {
    return http.post("/dept/selectByList",{data:data})
  }

  findChildrenList = (data) => {
    return http.post("/dept/findChildrenList",{data:data})
  }

  findUserAndDeptRefByDeptId = (data) => {
    return http.get("/dept/findUserAndDeptRefByDeptId?deptId="+data)
  }

  initDept = () => {
    return http.get("/dept/initDept")
  }

  createTcUserWithDeptId = (data) => {
    return http.post("/dept/createTcUserWithDeptId",{data:data})
  }

  allocateRoleToDept = (deptId,roleCodeList) => {
    return http.post("/dept/allocateRoleToDept?deptId="+deptId,{data:roleCodeList})
  }
}

const api = new Api()
export  default api