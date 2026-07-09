package com.jiaoshoujia.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.domain.SysOperLog;

public interface ISysOperLogService extends IService<SysOperLog> {

    void insertOperLog(SysOperLog operLog);

    Page<SysOperLog> selectOperLogPage(Page<SysOperLog> page, SysOperLog operLog);

    List<SysOperLog> selectOperLogList(SysOperLog operLog);

    int deleteOperLogByIds(Long[] operIds);

    void cleanOperLog();
}
