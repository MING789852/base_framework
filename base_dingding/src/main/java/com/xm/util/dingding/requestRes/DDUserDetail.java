package com.xm.util.dingding.requestRes;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class DDUserDetail {

    private String extension;
    private String unionid;
    private String boss;
    private RoleListDTO role_list;
    private boolean exclusive_account;
    private String manager_userid;
    private String admin;
    private String remark;
    private String title;
    private String hired_date;
    private String userid;
    private String work_place;
    private DeptOrderListDTO dept_order_list;
    private String real_authed;
    private List<Integer> dept_id_list;
    private String job_number;
    private String email;
    private LeaderInDeptDTO leader_in_dept;
    private String mobile;
    private String active;
    private String org_email;
    private String telephone;
    private String avatar;
    private String hide_mobile;
    private String senior;
    private String name;
    private UnionEmpExtDTO union_emp_ext;
    private String state_code;

    @NoArgsConstructor
    @Data
    public static class RoleListDTO {
        private String group_name;
        private String name;
        private String id;
    }

    @NoArgsConstructor
    @Data
    public static class DeptOrderListDTO {
        private String dept_id;
        private String order;
    }

    @NoArgsConstructor
    @Data
    public static class LeaderInDeptDTO {
        private String leader;
        private String dept_id;
    }

    @NoArgsConstructor
    @Data
    public static class UnionEmpExtDTO {
        private UnionEmpMapListDTO union_emp_map_list;
        private String userid;
        private String corp_id;

        @NoArgsConstructor
        @Data
        public static class UnionEmpMapListDTO {
            private String userid;
            private String corp_id;
        }
    }
}