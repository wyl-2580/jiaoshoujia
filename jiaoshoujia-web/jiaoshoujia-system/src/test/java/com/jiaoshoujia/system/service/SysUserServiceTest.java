package com.jiaoshoujia.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.system.domain.SysRole;
import com.jiaoshoujia.system.domain.SysUser;
import com.jiaoshoujia.system.domain.SysUserRole;
import com.jiaoshoujia.system.mapper.SysDeptMapper;
import com.jiaoshoujia.system.mapper.SysRoleMapper;
import com.jiaoshoujia.system.mapper.SysUserMapper;
import com.jiaoshoujia.system.mapper.SysUserRoleMapper;
import com.jiaoshoujia.system.service.impl.SysUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.jiaoshoujia.common.utils.SecurityUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户管理 Service 测试")
class SysUserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysDeptMapper deptMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @InjectMocks
    private SysUserServiceImpl userService;

    private SysUser testUser;

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl 的 baseMapper/entityClass/mapperClass 位于父类，Mockito @InjectMocks 无法注入。
        // 手动注入：baseMapper 提供 mock；entityClass/mapperClass 避免 lambdaQuery()/lambdaUpdate() 通过真实 MapperProxy 推断类型。
        ReflectionTestUtils.setField(userService, "baseMapper", userMapper);
        ReflectionTestUtils.setField(userService, "entityClass", SysUser.class);
        ReflectionTestUtils.setField(userService, "mapperClass", SysUserMapper.class);

        testUser = new SysUser();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        testUser.setNickname("Test User");
        testUser.setPassword("encoded_password");
        testUser.setDeptId(1L);
        testUser.setStatus(0);
    }

    // ==================== 查询相关 ====================

    @Test
    @DisplayName("根据用户名查询用户 - 存在")
    void selectUserByUsername_found() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        SysUser result = userService.selectUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    @DisplayName("根据用户名查询用户 - 不存在")
    void selectUserByUsername_notFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        SysUser result = userService.selectUserByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    @DisplayName("根据ID查询用户 - 附带角色信息")
    void selectUserById_withRoles() {
        when(userMapper.selectById(100L)).thenReturn(testUser);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(100L);
        ur.setRoleId(1L);
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur));

        SysRole role = new SysRole();
        role.setId(1L);
        role.setRoleName("admin");
        when(roleMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(role));

        SysUser result = userService.selectUserById(100L);

        assertNotNull(result);
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertEquals("admin", result.getRoles().get(0).getRoleName());
    }

    // ==================== 新增用户 ====================

    @Test
    @DisplayName("新增用户 - 用户名唯一校验通过")
    void insertUser_success() {
        SysUser newUser = new SysUser();
        newUser.setUsername("newuser");
        newUser.setRoleIds(new Long[]{1L, 2L});

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.insert(any(SysUser.class))).thenReturn(1);
        when(userRoleMapper.insert(any(SysUserRole.class))).thenReturn(1);

        int result = userService.insertUser(newUser);

        assertEquals(1, result);
        verify(userRoleMapper, times(2)).insert(any(SysUserRole.class));
    }

    @Test
    @DisplayName("新增用户 - 用户名已存在抛异常")
    void insertUser_duplicateUsername() {
        SysUser newUser = new SysUser();
        newUser.setUsername("testuser");

        SysUser existing = new SysUser();
        existing.setId(200L);
        existing.setUsername("testuser");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        assertThrows(BusinessException.class, () -> userService.insertUser(newUser));
    }

    // ==================== 更新用户 ====================

    @Test
    @DisplayName("更新用户 - 用户名重复抛异常")
    void updateUser_duplicateUsername() {
        SysUser user = new SysUser();
        user.setId(100L);
        user.setUsername("taken");

        SysUser existing = new SysUser();
        existing.setId(200L);
        existing.setUsername("taken");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        assertThrows(BusinessException.class, () -> userService.updateUser(user));
    }

    @Test
    @DisplayName("更新用户 - password和status字段被置空防止越权")
    void updateUser_clearsSensitiveFields() {
        SysUser user = new SysUser();
        user.setId(100L);
        user.setUsername("testuser");
        user.setPassword("hackedPassword");
        user.setStatus(1);
        user.setRoleIds(new Long[]{});

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(userMapper.updateById(any(SysUser.class))).thenReturn(1);
        when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);

        userService.updateUser(user);

        assertNull(user.getPassword());
        assertNull(user.getStatus());
    }

    // ==================== 删除用户 ====================

    @Test
    @DisplayName("删除用户 - 同时清理角色关联")
    void deleteUserByIds_success() {
        Long[] userIds = {100L, 200L};

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(() -> SecurityUtils.isAdmin(any())).thenReturn(false);
            securityMock.when(SecurityUtils::getUserId).thenReturn(999L);
            when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);

            int result = userService.deleteUserByIds(userIds);

            assertEquals(1, result);
            verify(userRoleMapper).delete(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    @DisplayName("删除用户 - 不允许删除超级管理员")
    void deleteUserByIds_adminForbidden() {
        Long[] userIds = {1L};

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);

            assertThrows(BusinessException.class, () -> userService.deleteUserByIds(userIds));
        }
    }

    @Test
    @DisplayName("删除用户 - 不允许删除当前登录用户")
    void deleteUserByIds_selfForbidden() {
        Long[] userIds = {100L};

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(() -> SecurityUtils.isAdmin(100L)).thenReturn(false);
            securityMock.when(SecurityUtils::getUserId).thenReturn(100L);

            assertThrows(BusinessException.class, () -> userService.deleteUserByIds(userIds));
        }
    }

    // ==================== 重置密码 ====================

    @Test
    @DisplayName("重置密码 - 非管理员不能重置超管密码")
    void resetPwd_adminForbiddenForNonAdmin() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setPassword("newEncryptedPwd");

        try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
            securityMock.when(() -> SecurityUtils.isAdmin(1L)).thenReturn(true);
            securityMock.when(SecurityUtils::getUserId).thenReturn(100L);
            securityMock.when(() -> SecurityUtils.isAdmin(100L)).thenReturn(false);

            assertThrows(BusinessException.class, () -> userService.resetPwd(user));
        }
    }

    // ==================== 更新用户状态 ====================

    @Test
    @DisplayName("更新状态 - 不允许禁用超级管理员")
    void updateUserStatus_adminForbidden() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setStatus(1);

        assertThrows(BusinessException.class, () -> userService.updateUserStatus(user));
    }

    // ==================== 用户名唯一校验 ====================

    @Test
    @DisplayName("用户名唯一校验 - 新用户名唯一")
    void checkUsernameUnique_unique() {
        SysUser user = new SysUser();
        user.setUsername("uniqueuser");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertTrue(userService.checkUsernameUnique(user));
    }

    @Test
    @DisplayName("用户名唯一校验 - 同一用户编辑时跳过自身")
    void checkUsernameUnique_sameUser() {
        SysUser user = new SysUser();
        user.setId(100L);
        user.setUsername("testuser");

        SysUser existing = new SysUser();
        existing.setId(100L);
        existing.setUsername("testuser");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        assertTrue(userService.checkUsernameUnique(user));
    }

    @Test
    @DisplayName("用户名唯一校验 - 被他人占用")
    void checkUsernameUnique_takenByOther() {
        SysUser user = new SysUser();
        user.setId(100L);
        user.setUsername("testuser");

        SysUser existing = new SysUser();
        existing.setId(200L);
        existing.setUsername("testuser");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        assertFalse(userService.checkUsernameUnique(user));
    }

    @Test
    @DisplayName("用户名唯一校验 - 新用户id为null时被占用")
    void checkUsernameUnique_newUserIdNull_taken() {
        SysUser user = new SysUser();
        user.setUsername("testuser");

        SysUser existing = new SysUser();
        existing.setId(200L);
        existing.setUsername("testuser");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        assertFalse(userService.checkUsernameUnique(user));
    }

    // ==================== 密码强度校验 ====================

    @Test
    @DisplayName("密码强度 - null密码抛异常")
    void checkPasswordStrength_null() {
        assertThrows(BusinessException.class, () -> userService.checkPasswordStrength(null));
    }

    @Test
    @DisplayName("密码强度 - 过短抛异常")
    void checkPasswordStrength_tooShort() {
        assertThrows(BusinessException.class, () -> userService.checkPasswordStrength("Ab1"));
    }

    @Test
    @DisplayName("密码强度 - 纯数字抛异常")
    void checkPasswordStrength_digitsOnly() {
        assertThrows(BusinessException.class, () -> userService.checkPasswordStrength("12345678"));
    }

    @Test
    @DisplayName("密码强度 - 纯字母抛异常")
    void checkPasswordStrength_lettersOnly() {
        assertThrows(BusinessException.class, () -> userService.checkPasswordStrength("abcdefgh"));
    }

    @Test
    @DisplayName("密码强度 - 合格密码通过")
    void checkPasswordStrength_valid() {
        assertDoesNotThrow(() -> userService.checkPasswordStrength("Admin123"));
    }
}
