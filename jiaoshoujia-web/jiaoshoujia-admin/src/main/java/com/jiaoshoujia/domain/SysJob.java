package com.jiaoshoujia.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jiaoshoujia.common.core.BaseEntity;

@TableName("sys_job")
public class SysJob extends BaseEntity {

    private String jobName;
    private String jobGroup = "DEFAULT";
    private String invokeTarget;
    private String cronExpression;
    private Integer misfirePolicy = 0;
    private Integer concurrent = 1;
    private Integer status;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getInvokeTarget() {
        return invokeTarget;
    }

    public void setInvokeTarget(String invokeTarget) {
        this.invokeTarget = invokeTarget;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Integer getMisfirePolicy() {
        return misfirePolicy;
    }

    public void setMisfirePolicy(Integer misfirePolicy) {
        this.misfirePolicy = misfirePolicy;
    }

    public Integer getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(Integer concurrent) {
        this.concurrent = concurrent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
