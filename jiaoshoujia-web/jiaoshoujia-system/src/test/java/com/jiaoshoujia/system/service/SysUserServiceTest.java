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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        testUser = new SysUser();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        testUser.setNickname("Test User");
        testUser.setPassword("encoded_password");
        testUser.setDeptId(1L);
        testUser.setStatus(0);
    }

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
    @DisplayName("删除用户 - 同时清理角色关联")
    void deleteUserByIds() {
        Long[] userIds = {100L, 200L};
        when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);

        int result = userService.deleteUserByIds(userIds);

        assertEquals(1, result);
        verify(userRoleMapper).delete(any(LambdaQueryWrapper.class));
    }
}
