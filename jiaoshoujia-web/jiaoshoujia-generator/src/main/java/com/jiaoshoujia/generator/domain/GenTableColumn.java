package com.jiaoshoujia.generator.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jiaoshoujia.common.core.BaseEntity;

@TableName("gen_table_column")
public class GenTableColumn extends BaseEntity {

    private Long tableId;

    private String columnName;

    private String columnComment;

    private String columnType;

    private String javaType;

    private String javaField;

    /** 是否主键（0=是，1=否） */
    private Integer isPk;

    /** 是否为插入字段 */
    private Integer isInsert;

    /** 是否编辑字段 */
    private Integer isEdit;

    /** 是否列表字段 */
    private Integer isList;

    /** 是否查询字段 */
    private Integer isQuery;

    /** 查询方式（EQ,NE,GT,GTE,LT,LTE,LIKE,BETWEEN） */
    private String queryType;

    /** 显示类型（input,textarea,select,radio,checkbox,datetime） */
    private String htmlType;

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getJavaField() {
        return javaField;
    }

    public void setJavaField(String javaField) {
        this.javaField = javaField;
    }

    public Integer getIsPk() {
        return isPk;
    }

    public void setIsPk(Integer isPk) {
        this.isPk = isPk;
    }

    public Integer getIsInsert() {
        return isInsert;
    }

    public void setIsInsert(Integer isInsert) {
        this.isInsert = isInsert;
    }

    public Integer getIsEdit() {
        return isEdit;
    }

    public void setIsEdit(Integer isEdit) {
        this.isEdit = isEdit;
    }

    public Integer getIsList() {
        return isList;
    }

    public void setIsList(Integer isList) {
        this.isList = isList;
    }

    public Integer getIsQuery() {
        return isQuery;
    }

    public void setIsQuery(Integer isQuery) {
        this.isQuery = isQuery;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getHtmlType() {
        return htmlType;
    }

    public void setHtmlType(String htmlType) {
        this.htmlType = htmlType;
    }
}
