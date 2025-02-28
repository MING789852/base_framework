import { http } from "@/utils/http";

class Api implements ApiBase{
  selectByPage = (data) => {
    return http.post("/role/selectByPage",{data:data})
  }

  deleteData = (data) => {
    return http.post("/role/deleteData",{data:data})
  }

  saveOrUpdateData = (data) => {
    return http.post("/role/saveOrUpdateData",{data:data})
  }

  getRoleList = () => {
    return http.get('/role/getRoleList')
  }

  getRoleIdListByUser = (data) => {
    return http.get("/role/getRoleIdListByUser?userId="+data)
  }

  getRouterIdListByRole = (data) => {
    return http.post("/role/getRouterIdListByRole",{data:data})
  }


  saveUserRoleRel = (data) => {
    return http.post("/role/saveUserRoleRel",{data:data})
  }

  saveRoleRouterRel = (data) => {
    return http.post("/role/saveRoleRouterRel",{data:data})
  }

  selectUserAndDeptPageByRoleId = (roleId,query) => {
    return http.post("/role/selectUserAndDeptPageByRoleId?roleId="+roleId,{data:query})
  }

  unRelUserAndRoleAll = (roleId,query) => {
    return http.post("/role/unRelUserAndRoleAll?roleId="+roleId,{data:query})
  }


  unRelUserAndRole = (roleId,userId) => {
    return http.get("/role/unRelUserAndRole?roleId="+roleId + '&userId='+userId)
  }

  roleAddUser = (roleId,userIdList) => {
    return http.post("/role/roleAddUser?roleId="+roleId,{data:userIdList})
  }
}

const api = new Api()
export  default api
