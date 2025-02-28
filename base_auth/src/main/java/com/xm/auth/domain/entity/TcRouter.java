package com.xm.auth.domain.entity;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 路由
 */
public class TcRouter implements Serializable {
    /**
     * 主键
     */
    @Size(max = 32, message = "主键最大长度要小于 32")
    @NotBlank(message = "主键不能为空")
    private String id;

    /**
     * 路由地址
     */
    @Size(max = 100, message = "路由地址最大长度要小于 100")
    @NotBlank(message = "路由地址不能为空")
    private String path;

    /**
     * 路由名称
     */
    @Size(max = 100, message = "路由名称最大长度要小于 100")
    @NotBlank(message = "路由名称不能为空")
    private String name;

    /**
     * 标题
     */
    @Size(max = 100, message = "标题最大长度要小于 100")
    private String title;

    /**
     * 重定向
     */
    @Size(max = 100, message = "重定向最大长度要小于 100")
    private String redirect;

    /**
     * 菜单图标
     */
    @Size(max = 100, message = "菜单图标最大长度要小于 100")
    private String icon;

    /**
     * 组件路径
     */
    @Size(max = 100, message = "组件路径最大长度要小于 100")
    private String component;

    /**
     * 是否在菜单中显示
     */
    private Boolean showLink;

    /**
     * 菜单升序排序,值越高越后（只对顶级路由有效）
     */
    private Integer rankInt;

    /**
     * 路由组件缓存
     */
    private Boolean keepAlive;

    /**
     * 0代表顶级菜单，1代表页面
     */
    private Integer level;

    /**
     * 是否存在按钮
     */
    private Boolean hasButton;

    /**
     * 父主键
     */
    @Size(max = 32, message = "父主键最大长度要小于 32")
    private String parentId;

    /**
     * 是否可用
     */
    @NotNull(message = "是否可用不能为null")
    private Integer judgeEnable;

    /**
     * 创建人员
     */
    @Size(max = 50, message = "创建人员最大长度要小于 50")
    private String createUser;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 修改人员
     */
    @Size(max = 50, message = "修改人员最大长度要小于 50")
    private String updateUser;

    /**
     * 修改日期
     */
    private Date updateDate;

    /**
     * 不显示标签页
     */
    private Boolean hiddenTag;

    /**
     * 是否公开
     */
    @NotNull(message = "是否公开不能为null")
    private Boolean judgePublic;

    /**
     * 是否全屏展示
     */
    private Boolean fullscreen;

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect == null ? null : redirect.trim();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? null : icon.trim();
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component == null ? null : component.trim();
    }

    public Boolean getShowLink() {
        return showLink;
    }

    public void setShowLink(Boolean showLink) {
        this.showLink = showLink;
    }

    public Integer getRankInt() {
        return rankInt;
    }

    public void setRankInt(Integer rankInt) {
        this.rankInt = rankInt;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Boolean getHasButton() {
        return hasButton;
    }

    public void setHasButton(Boolean hasButton) {
        this.hasButton = hasButton;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    public Integer getJudgeEnable() {
        return judgeEnable;
    }

    public void setJudgeEnable(Integer judgeEnable) {
        this.judgeEnable = judgeEnable;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser == null ? null : updateUser.trim();
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Boolean getHiddenTag() {
        return hiddenTag;
    }

    public void setHiddenTag(Boolean hiddenTag) {
        this.hiddenTag = hiddenTag;
    }

    public Boolean getJudgePublic() {
        return judgePublic;
    }

    public void setJudgePublic(Boolean judgePublic) {
        this.judgePublic = judgePublic;
    }

    public Boolean getFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(Boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", path=").append(path);
        sb.append(", name=").append(name);
        sb.append(", title=").append(title);
        sb.append(", redirect=").append(redirect);
        sb.append(", icon=").append(icon);
        sb.append(", component=").append(component);
        sb.append(", showLink=").append(showLink);
        sb.append(", rankInt=").append(rankInt);
        sb.append(", keepAlive=").append(keepAlive);
        sb.append(", level=").append(level);
        sb.append(", hasButton=").append(hasButton);
        sb.append(", parentId=").append(parentId);
        sb.append(", judgeEnable=").append(judgeEnable);
        sb.append(", createUser=").append(createUser);
        sb.append(", createDate=").append(createDate);
        sb.append(", updateUser=").append(updateUser);
        sb.append(", updateDate=").append(updateDate);
        sb.append(", hiddenTag=").append(hiddenTag);
        sb.append(", judgePublic=").append(judgePublic);
        sb.append(", fullscreen=").append(fullscreen);
        sb.append("]");
        return sb.toString();
    }
}