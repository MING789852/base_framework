package com.xm.core.params;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ColumnProps implements Serializable {

    public ColumnProps() {
    }

    public ColumnProps(String label, String prop) {
        this.label = label;
        this.prop = prop;
        this.show = true;
    }

    public ColumnProps(String label, String prop, String type) {
        this.label = label;
        this.prop = prop;
        this.type = type;
        this.show = true;
    }

    public ColumnProps(String label, String prop, String type,Boolean show) {
        this.label = label;
        this.prop = prop;
        this.type = type;
        this.show = show;
    }

    public ColumnProps(String label, String prop, String type,String width) {
        this.label = label;
        this.prop = prop;
        this.type = type;
        this.show = true;
        this.width = width;
    }

    public ColumnProps(String label, String prop, String type,String width,String queryType,String queryCondition) {
        this.label = label;
        this.prop = prop;
        this.type = type;
        this.show = true;
        this.query=true;
        this.width=width;
        this.queryType = queryType;
        this.queryCondition = queryCondition;
    }

    /*
        动态列：必填
        excel生成：必填
         */
    private String label;
    /*
    动态列：必填
    excel生成：必填
     */
    private String prop;
    /*
    动态列：选填
    excel生成：选填
     */
    private Boolean show;
    /*
    动态列：选填
    excel生成：选填
     */
    private boolean fixed;
    /*
    动态列：选填
    excel生成：选填
     */
    private String width;
    /*
    动态列：选填
    excel生成：选填
     */
    private String minWidth;
    /*
    动态列：选填
    excel生成：选填
     */
    private String headerAlign;
    /*
    动态列：选填
    excel生成：选填
     */
    private String align;
    /*
    动态列：必填
    excel生成：选填
     */
    private String type;
    /*
    动态列：选填
    excel生成：选填
     */
    private boolean query;
    /*
    动态列：选填
    excel生成：选填
     */
    private boolean sortable;
    /*
    动态列：选填
    excel生成：选填
     */
    private String queryCondition;
    /*
    动态列：选填
    excel生成：选填
     */
    private String queryType;
    /*
    动态列：选填
    excel生成：选填
     */
    private String tooltip;

    /*
    动态列：选填
    excel生成：选填
     */
    private List<ColumnProps> children;
}
