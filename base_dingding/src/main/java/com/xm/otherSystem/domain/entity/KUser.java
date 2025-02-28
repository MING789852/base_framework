package com.xm.otherSystem.domain.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

public class KUser {
    @Size(max = 200,message = "最大长度要小于 200")
    @NotBlank(message = "不能为空")
    private String id;

    /**
    * 姓名
    */
    @Size(max = 20,message = "姓名最大长度要小于 20")
    private String name;

    /**
    * 拼音
    */
    @Size(max = 50,message = "拼音最大长度要小于 50")
    private String pinyin;

    /**
    * 工号
    */
    @Size(max = 50,message = "工号最大长度要小于 50")
    private String jobnumber;

    /**
    * 岗位职级
    */
    @Size(max = 80,message = "岗位职级最大长度要小于 80")
    private String positionLevel;

    /**
    * 职位
    */
    @Size(max = 50,message = "职位最大长度要小于 50")
    private String position;

    /**
    * 职等
    */
    @Size(max = 50,message = "职等最大长度要小于 50")
    private String jobGrade;

    /**
    * 职级
    */
    @Size(max = 50,message = "职级最大长度要小于 50")
    private String jobLevel;

    /**
    * 职称
    */
    @Size(max = 50,message = "职称最大长度要小于 50")
    private String jobTitle;

    /**
    * 归属序列
    */
    @Size(max = 50,message = "归属序列最大长度要小于 50")
    private String jobDuty;

    /**
    * 序列代码
    */
    @Size(max = 50,message = "序列代码最大长度要小于 50")
    private String jobDutyCode;

    /**
    * 部门id
    */
    @Size(max = 100,message = "部门id最大长度要小于 100")
    private String deptId;

    /**
    * 部门id拼接
    */
    @Size(max = 100,message = "部门id拼接最大长度要小于 100")
    private String deptIds;

    /**
    * 部门
    */
    @Size(max = 100,message = "部门最大长度要小于 100")
    private String deptName;

    /**
    * 部门编码
    */
    @Size(max = 100,message = "部门编码最大长度要小于 100")
    private String deptCode;

    /**
    * 是否离职 0 在职  1离职
    */
    private Integer leaveType;

    /**
    * 员工类型
    */
    @Size(max = 50,message = "员工类型最大长度要小于 50")
    private String userType;

    /**
    * 员工状态 正式，试用
    */
    @Size(max = 50,message = "员工状态 正式，试用最大长度要小于 50")
    private String userStatus;

    /**
    * 办公地点
    */
    @Size(max = 100,message = "办公地点最大长度要小于 100")
    private String workPlace;

    /**
    * 所属公司
    */
    @Size(max = 100,message = "所属公司最大长度要小于 100")
    private String factory;

    /**
    * 工资核算地
    */
    @Size(max = 50,message = "工资核算地最大长度要小于 50")
    private String payPlace;

    /**
    * 劳务派遣公司
    */
    @Size(max = 50,message = "劳务派遣公司最大长度要小于 50")
    private String dispatchFactory;

    /**
    * 工资计算方式
    */
    @Size(max = 50,message = "工资计算方式最大长度要小于 50")
    private String payComputeType;

    /**
    * 性别
    */
    @Size(max = 10,message = "性别最大长度要小于 10")
    private String sexType;

    /**
    * 身份证
    */
    @Size(max = 100,message = "身份证最大长度要小于 100")
    private String certNo;

    /**
    * 电话号码
    */
    @Size(max = 50,message = "电话号码最大长度要小于 50")
    private String phone;

    /**
    * 银行卡号
    */
    @Size(max = 100,message = "银行卡号最大长度要小于 100")
    private String bankAccountNo;

    /**
    * 开户行
    */
    @Size(max = 100,message = "开户行最大长度要小于 100")
    private String accountBank;

    /**
    * 出生日期
    */
    private Date birthTime;

    /**
    * 离职日期
    */
    private Date lastWorkDay;

    /**
    * 离职原因备注
    */
    @Size(max = 50,message = "离职原因备注最大长度要小于 50")
    private String leaveReasonType;

    /**
    * 入职时间
    */
    private Date hiredDate;

    /**
    * 身份证地址
    */
    @Size(max = 150,message = "身份证地址最大长度要小于 150")
    private String certAddress;

    /**
    * 籍贯
    */
    @Size(max = 100,message = "籍贯最大长度要小于 100")
    private String nativePlace;

    /**
    * 户籍类型
    */
    @Size(max = 50,message = "户籍类型最大长度要小于 50")
    private String residenceType;

    /**
    * 状态  0 正常 1 工号相同,但这条注销 
    */
    private Byte state;

    /**
    * 所处的位置  0 钉钉架构内员工 1 外部员工
    */
    private Byte site;

    private Date createTime;

    private Date updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin == null ? null : pinyin.trim();
    }

    public String getJobnumber() {
        return jobnumber;
    }

    public void setJobnumber(String jobnumber) {
        this.jobnumber = jobnumber == null ? null : jobnumber.trim();
    }

    public String getPositionLevel() {
        return positionLevel;
    }

    public void setPositionLevel(String positionLevel) {
        this.positionLevel = positionLevel == null ? null : positionLevel.trim();
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position == null ? null : position.trim();
    }

    public String getJobGrade() {
        return jobGrade;
    }

    public void setJobGrade(String jobGrade) {
        this.jobGrade = jobGrade == null ? null : jobGrade.trim();
    }

    public String getJobLevel() {
        return jobLevel;
    }

    public void setJobLevel(String jobLevel) {
        this.jobLevel = jobLevel == null ? null : jobLevel.trim();
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle == null ? null : jobTitle.trim();
    }

    public String getJobDuty() {
        return jobDuty;
    }

    public void setJobDuty(String jobDuty) {
        this.jobDuty = jobDuty == null ? null : jobDuty.trim();
    }

    public String getJobDutyCode() {
        return jobDutyCode;
    }

    public void setJobDutyCode(String jobDutyCode) {
        this.jobDutyCode = jobDutyCode == null ? null : jobDutyCode.trim();
    }

    public String getDeptId() {
        return deptId;
    }

    public void setDeptId(String deptId) {
        this.deptId = deptId == null ? null : deptId.trim();
    }

    public String getDeptIds() {
        return deptIds;
    }

    public void setDeptIds(String deptIds) {
        this.deptIds = deptIds == null ? null : deptIds.trim();
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName == null ? null : deptName.trim();
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode == null ? null : deptCode.trim();
    }

    public Integer getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(Integer leaveType) {
        this.leaveType = leaveType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType == null ? null : userType.trim();
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus == null ? null : userStatus.trim();
    }

    public String getWorkPlace() {
        return workPlace;
    }

    public void setWorkPlace(String workPlace) {
        this.workPlace = workPlace == null ? null : workPlace.trim();
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory == null ? null : factory.trim();
    }

    public String getPayPlace() {
        return payPlace;
    }

    public void setPayPlace(String payPlace) {
        this.payPlace = payPlace == null ? null : payPlace.trim();
    }

    public String getDispatchFactory() {
        return dispatchFactory;
    }

    public void setDispatchFactory(String dispatchFactory) {
        this.dispatchFactory = dispatchFactory == null ? null : dispatchFactory.trim();
    }

    public String getPayComputeType() {
        return payComputeType;
    }

    public void setPayComputeType(String payComputeType) {
        this.payComputeType = payComputeType == null ? null : payComputeType.trim();
    }

    public String getSexType() {
        return sexType;
    }

    public void setSexType(String sexType) {
        this.sexType = sexType == null ? null : sexType.trim();
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo == null ? null : certNo.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo == null ? null : bankAccountNo.trim();
    }

    public String getAccountBank() {
        return accountBank;
    }

    public void setAccountBank(String accountBank) {
        this.accountBank = accountBank == null ? null : accountBank.trim();
    }

    public Date getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(Date birthTime) {
        this.birthTime = birthTime;
    }

    public Date getLastWorkDay() {
        return lastWorkDay;
    }

    public void setLastWorkDay(Date lastWorkDay) {
        this.lastWorkDay = lastWorkDay;
    }

    public String getLeaveReasonType() {
        return leaveReasonType;
    }

    public void setLeaveReasonType(String leaveReasonType) {
        this.leaveReasonType = leaveReasonType == null ? null : leaveReasonType.trim();
    }

    public Date getHiredDate() {
        return hiredDate;
    }

    public void setHiredDate(Date hiredDate) {
        this.hiredDate = hiredDate;
    }

    public String getCertAddress() {
        return certAddress;
    }

    public void setCertAddress(String certAddress) {
        this.certAddress = certAddress == null ? null : certAddress.trim();
    }

    public String getNativePlace() {
        return nativePlace;
    }

    public void setNativePlace(String nativePlace) {
        this.nativePlace = nativePlace == null ? null : nativePlace.trim();
    }

    public String getResidenceType() {
        return residenceType;
    }

    public void setResidenceType(String residenceType) {
        this.residenceType = residenceType == null ? null : residenceType.trim();
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Byte getSite() {
        return site;
    }

    public void setSite(Byte site) {
        this.site = site;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}