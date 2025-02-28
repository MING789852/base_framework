package com.xm.auth.domain.entity;

import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 部门表
 */
public class TcDept {
    /**
     * 部门id
     */
    @Size(max = 100, message = "部门id最大长度要小于 100")
    @NotBlank(message = "部门id不能为空")
    private String id;

    /**
     * 所属工厂
     */
    @Size(max = 100, message = "所属工厂最大长度要小于 100")
    private String factory;

    /**
     * 部门名称
     */
    @Size(max = 50, message = "部门名称最大长度要小于 50")
    private String name;

    /**
     * 当前部门包含子部门人数
     */
    private Integer nowNum;

    /**
     * 当前部门人数
     */
    private Integer nowNumBase;

    /**
     * 预计人数
     */
    private Integer expectNum;

    /**
     * 待离职人数
     */
    private Integer dimissionNum;

    /**
     * 上级部门id
     */
    @Size(max = 100, message = "上级部门id最大长度要小于 100")
    private String parentId;

    /**
     * 层级
     */
    private Integer level;

    private Long orders;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否可用
     */
    @NotNull(message = "是否可用不能为null")
    private Integer judgeEnable;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory == null ? null : factory.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getNowNum() {
        return nowNum;
    }

    public void setNowNum(Integer nowNum) {
        this.nowNum = nowNum;
    }

    public Integer getNowNumBase() {
        return nowNumBase;
    }

    public void setNowNumBase(Integer nowNumBase) {
        this.nowNumBase = nowNumBase;
    }

    public Integer getExpectNum() {
        return expectNum;
    }

    public void setExpectNum(Integer expectNum) {
        this.expectNum = expectNum;
    }

    public Integer getDimissionNum() {
        return dimissionNum;
    }

    public void setDimissionNum(Integer dimissionNum) {
        this.dimissionNum = dimissionNum;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getOrders() {
        return orders;
    }

    public void setOrders(Long orders) {
        this.orders = orders;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getJudgeEnable() {
        return judgeEnable;
    }

    public void setJudgeEnable(Integer judgeEnable) {
        this.judgeEnable = judgeEnable;
    }
}