package com.jiaoshoujia.system.service;

import com.jiaoshoujia.system.domain.SysOperLog;
import com.jiaoshoujia.system.mapper.SysOperLogMapper;
import com.jiaoshoujia.system.service.impl.SysOperLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl 的 baseMapper 位于父类，Mockito @InjectMocks 无法注入，手动注入 mock
        ReflectionTestUtils.setField(operLogService, "baseMapper", operLogMapper);
    }

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
