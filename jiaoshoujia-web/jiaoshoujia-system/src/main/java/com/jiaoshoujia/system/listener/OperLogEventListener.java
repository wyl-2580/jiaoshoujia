package com.jiaoshoujia.system.listener;

import com.jiaoshoujia.framework.aspectj.LogAspect;
import com.jiaoshoujia.system.domain.SysOperLog;
import com.jiaoshoujia.system.service.ISysOperLogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OperLogEventListener {

    private final ISysOperLogService operLogService;

    public OperLogEventListener(ISysOperLogService operLogService) {
        this.operLogService = operLogService;
    }

    @Async
    @EventListener
    public void handleOperLogEvent(LogAspect.OperLogEvent event) {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle(event.title());
        operLog.setBusinessType(event.businessType().ordinal());
        operLog.setMethod(event.method());
        operLog.setRequestMethod(event.requestMethod());
        operLog.setOperatorType(event.operatorType().ordinal());
        operLog.setOperName(event.operatorName());
        operLog.setOperUrl(event.requestUrl());
        operLog.setOperIp(event.ip());
        operLog.setOperParam(event.requestParams());
        operLog.setJsonResult(event.responseResult());
        operLog.setStatus(event.status());
        operLog.setErrorMsg(event.errorMsg());
        operLog.setOperTime(event.operTime());
        operLog.setCostTime(event.costTime());
        operLogService.insertOperLog(operLog);
    }
}
