package com.xm.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.vo.RoleRelUserVo;
import com.xm.auth.domain.vo.RoleRouterVo;
import com.xm.auth.domain.vo.UserRoleRelVo;
import com.xm.auth.service.TcRoleRouterRelService;
import com.xm.auth.service.TcRoleService;
import com.xm.auth.service.TcUserRoleRelService;
import com.xm.core.params.ColumnProps;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("role")
@RequiredArgsConstructor
public class TcRoleController {

    private final TcRoleService tcRoleService;

    private final TcUserRoleRelService userRoleRelService;

    private final TcRoleRouterRelService roleRouterRelService;

    @PostMapping("saveUserRoleRel")
    public Result<String> saveUserRoleRel(@RequestBody UserRoleRelVo userRoleRelVo){
        return Result.successForData(userRoleRelService.saveUserRoleRel(userRoleRelVo));
    }

    @PostMapping("saveRoleRouterRel")
    public Result<String> saveRoleRouterRel(@RequestBody RoleRouterVo roleRouterVo){
        return Result.successForData(roleRouterRelService.saveRoleRouterRel(roleRouterVo));
    }

    @GetMapping("getRoleList")
    public Result<List<TcRole>> getRoleList(){
        return Result.successForData(tcRoleService.getRoleList());
    }

    @GetMapping("getRoleIdListByUser")
    public Result<List<String>>  getRoleIdListByUser(String userId){
        return Result.successForData(userRoleRelService.getRoleIdListByUser(userId));
    }

    @PostMapping("getRouterIdListByRole")
    public Result<List<String>>  getRouterIdListByRole(@RequestBody TcRole tcRole){
        return Result.successForData(roleRouterRelService.getRouterIdListByRole(tcRole.getId()));
    }

    @PostMapping("selectByPage")
    public Result<Page<TcRole>> selectByPage(@RequestBody QueryData<TcRole> queryData){
        return Result.successForData(tcRoleService.selectByPage(queryData));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcRole> tcRoleList){
        return Result.successForData(tcRoleService.deleteData(tcRoleList));
    }

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody List<TcRole> tcRoleList){
        return Result.successForData(tcRoleService.saveOrUpdateData(tcRoleList));
    }

    @PostMapping("selectUserPageByRoleId")
    public Result<Page<RoleRelUserVo>> selectUserPageByRoleId(String roleId, @RequestBody QueryData<RoleRelUserVo> queryData){
        return Result.successForData(tcRoleService.selectUserPageByRoleId(roleId, queryData));
    }

    @PostMapping("exportRoleUsersExcel")
    public void exportRoleUsersExcel(String roleId, @RequestBody QueryData<RoleRelUserVo> queryData, HttpServletResponse response){
        tcRoleService.exportRoleUsersExcel(roleId, queryData, response);
    }

    @GetMapping("getRoleUsersColumnProps")
    public Result<List<ColumnProps>> getRoleUsersColumnProps(){
        return Result.successForData(tcRoleService.getRoleUsersColumnProps());
    }

    @PostMapping("unRelUserAndRoleAll")
    public Result<String> unRelUserAndRoleAll(String roleId,@RequestBody QueryData<RoleRelUserVo> queryData){
        return Result.successForData(userRoleRelService.unRelUserAndRoleAll(roleId, queryData));
    }

    @GetMapping("unRelUserAndRole")
    public Result<String> unRelUserAndRole(String roleId,String userId){
        return Result.successForData(userRoleRelService.unRelUserAndRole(roleId,userId));
    }

    @PostMapping("roleAddUser")
    public Result<String> roleAddUser(String roleId, @RequestBody List<String> userIdList){
        return Result.successForData(userRoleRelService.roleAddUser(roleId,userIdList));
    }

}
