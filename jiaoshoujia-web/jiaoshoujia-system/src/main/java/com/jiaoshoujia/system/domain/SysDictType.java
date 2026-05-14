package com.jiaoshoujia.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jiaoshoujia.common.core.BaseEntity;

@TableName("sys_dict_type")
public class SysDictType extends BaseEntity {

    private String dictName;
    private String dictType;
    private Integer status;

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getDictType() {
        return dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
