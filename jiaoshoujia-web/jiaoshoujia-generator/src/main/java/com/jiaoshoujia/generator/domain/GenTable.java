package com.jiaoshoujia.generator.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jiaoshoujia.common.core.BaseEntity;

import java.util.List;

@TableName("gen_table")
public class GenTable extends BaseEntity {

    private String tableName;

    private String tableComment;

    private String className;

    private String packageName;

    private String moduleName;

    private String businessName;

    private String functionName;

    @TableField("function_author")
    private String author;

    /** 生成类型（0=zip压缩包，1=自定义路径） */
    private String genType;

    private String genPath;

    @TableField(exist = false)
    private List<GenTableColumn> columns;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableComment() {
        return tableComment;
    }

    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenType() {
        return genType;
    }

    public void setGenType(String genType) {
        this.genType = genType;
    }

    public String getGenPath() {
        return genPath;
    }

    public void setGenPath(String genPath) {
        this.genPath = genPath;
    }

    public List<GenTableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<GenTableColumn> columns) {
        this.columns = columns;
    }
}
