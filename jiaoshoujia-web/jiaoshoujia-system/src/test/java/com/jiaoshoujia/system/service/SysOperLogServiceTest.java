package com.jiaoshoujia.system.service;

import com.jiaoshoujia.system.domain.SysOperLog;
import com.jiaoshoujia.system.mapper.SysOperLogMapper;
import com.jiaoshoujia.system.service.impl.SysOperLogServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("操作日志 Service 测试")
class SysOperLogServiceTest {

    @Mock
    private SysOperLogMapper operLogMapper;

    @InjectMocks
    private SysOperLogServiceImpl operLogService;

    @Test
    @DisplayName("插入操作日志 - 包含耗时字段")
    void insertOperLog_withCostTime() {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle("用户管理");
        operLog.setBusinessType(1);
        operLog.setMethod("SysUserController.add()");
        operLog.setRequestMethod("POST");
        operLog.setOperName("admin");
        operLog.setOperUrl("/api/system/user");
        operLog.setOperIp("127.0.0.1");
        operLog.setStatus(0);
        operLog.setCostTime(56L);
        operLog.setOperTime(LocalDateTime.now());

        when(operLogMapper.insert(any(SysOperLog.class))).thenReturn(1);

        operLogService.insertOperLog(operLog);

        verify(operLogMapper).insert(operLog);
        assertEquals(56L, operLog.getCostTime());
    }

    @Test
    @DisplayName("清空操作日志")
    void cleanOperLog() {
        when(operLogMapper.delete(null)).thenReturn(100);

        operLogService.cleanOperLog();

        verify(operLogMapper).delete(null);
    }
}
