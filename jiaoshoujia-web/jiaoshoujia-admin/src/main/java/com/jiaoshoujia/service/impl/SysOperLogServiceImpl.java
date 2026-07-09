package com.jiaoshoujia.service.impl;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.domain.SysOperLog;
import com.jiaoshoujia.mapper.SysOperLogMapper;
import com.jiaoshoujia.service.ISysOperLogService;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

    @Override
    public void insertOperLog(SysOperLog operLog) {
        save(operLog);
    }

    @Override
    public Page<SysOperLog> selectOperLogPage(Page<SysOperLog> page, SysOperLog operLog) {
        return baseMapper.selectPage(page, buildQueryWrapper(operLog));
    }

    @Override
    public List<SysOperLog> selectOperLogList(SysOperLog operLog) {
        return baseMapper.selectList(buildQueryWrapper(operLog));
    }

    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        removeBatchByIds(Arrays.asList(operIds));
        return 1;
    }

    @Override
    public void cleanOperLog() {
        baseMapper.delete(null);
    }

    private LambdaQueryWrapper<SysOperLog> buildQueryWrapper(SysOperLog operLog) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
                .eq(operLog.getBusinessType() != null, SysOperLog::getBusinessType, operLog.getBusinessType())
                .like(StringUtils.isNotEmpty(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
                .eq(operLog.getStatus() != null, SysOperLog::getStatus, operLog.getStatus())
                .ge(StringUtils.isNotEmpty(operLog.getBeginTime()), SysOperLog::getOperTime, operLog.getBeginTime())
                .le(StringUtils.isNotEmpty(operLog.getEndTime()), SysOperLog::getOperTime, endOfDay(operLog.getEndTime()))
                .orderByDesc(SysOperLog::getOperTime);
        return wrapper;
    }

    private String endOfDay(String endTime) {
        if (StringUtils.isEmpty(endTime) || endTime.length() > 10) {
            return endTime;
        }
        return endTime + " 23:59:59";
    }
}
