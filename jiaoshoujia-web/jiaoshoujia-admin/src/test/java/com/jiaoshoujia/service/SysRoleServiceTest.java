package com.jiaoshoujia.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaoshoujia.domain.SysRole;
import com.jiaoshoujia.domain.SysRoleMenu;
import com.jiaoshoujia.domain.SysUserRole;
import com.jiaoshoujia.mapper.SysRoleDeptMapper;
import com.jiaoshoujia.mapper.SysRoleMapper;
import com.jiaoshoujia.mapper.SysRoleMenuMapper;
import com.jiaoshoujia.mapper.SysUserRoleMapper;
import com.jiaoshoujia.service.impl.SysRoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("角色管理 Service 测试")
class SysRoleServiceTest {

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @Mock
    private SysRoleDeptMapper roleDeptMapper;

    @InjectMocks
    private SysRoleServiceImpl roleService;

    private SysRole testRole;

    @BeforeEach
    void setUp() {
        // MyBatis-Plus ServiceImpl 的 baseMapper 位于父类，Mockito @InjectMocks 无法注入，手动注入 mock
        ReflectionTestUtils.setField(roleService, "baseMapper", roleMapper);

        testRole = new SysRole();
        testRole.setId(1L);
        testRole.setRoleName("管理员");
        testRole.setRoleKey("admin");
        testRole.setRoleSort(1);
        testRole.setStatus(0);
    }

    @Test
    @DisplayName("根据用户ID查询角色列表 - 有角色")
    void selectRolesByUserId_found() {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(1L);
        ur.setRoleId(1L);
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur));
        when(roleMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(testRole));

        List<SysRole> roles = roleService.selectRolesByUserId(1L);

        assertEquals(1, roles.size());
        assertEquals("admin", roles.get(0).getRoleKey());
    }

    @Test
    @DisplayName("根据用户ID查询角色列表 - 无角色")
    void selectRolesByUserId_empty() {
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<SysRole> roles = roleService.selectRolesByUserId(999L);

        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("查询用户角色权限标识")
    void selectRolePermsByUserId() {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(1L);
        ur.setRoleId(1L);
        when(userRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur));
        when(roleMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(testRole));

        Set<String> perms = roleService.selectRolePermsByUserId(1L);

        assertTrue(perms.contains("admin"));
    }

    @Test
    @DisplayName("新增角色 - 同时关联菜单")
    void insertRole_withMenus() {
        SysRole role = new SysRole();
        role.setRoleName("测试角色");
        role.setRoleKey("test");
        role.setMenuIds(new Long[]{1L, 2L, 3L});

        when(roleMapper.insert(any(SysRole.class))).thenReturn(1);
        when(roleMenuMapper.insert(any(SysRoleMenu.class))).thenReturn(1);

        int result = roleService.insertRole(role);

        assertEquals(1, result);
        verify(roleMenuMapper, times(3)).insert(any(SysRoleMenu.class));
    }

    @Test
    @DisplayName("新增角色 - 无菜单关联")
    void insertRole_withoutMenus() {
        SysRole role = new SysRole();
        role.setRoleName("空角色");
        role.setRoleKey("empty");

        when(roleMapper.insert(any(SysRole.class))).thenReturn(1);

        int result = roleService.insertRole(role);

        assertEquals(1, result);
        verify(roleMenuMapper, never()).insert(any(SysRoleMenu.class));
    }

    @Test
    @DisplayName("更新角色 - 先删后插菜单关联")
    void updateRole() {
        testRole.setMenuIds(new Long[]{10L, 20L});

        when(roleMapper.updateById(testRole)).thenReturn(1);
        when(roleMenuMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);
        when(roleMenuMapper.insert(any(SysRoleMenu.class))).thenReturn(1);

        int result = roleService.updateRole(testRole);

        assertEquals(1, result);
        verify(roleMenuMapper).delete(any(LambdaQueryWrapper.class));
        verify(roleMenuMapper, times(2)).insert(any(SysRoleMenu.class));
    }

    @Test
    @DisplayName("批量删除角色 - 同时清理菜单和部门关联")
    void deleteRoleByIds() {
        Long[] roleIds = {1L, 2L};

        when(roleMenuMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(5);
        when(roleDeptMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);

        int result = roleService.deleteRoleByIds(roleIds);

        assertEquals(1, result);
        verify(roleMenuMapper).delete(any(LambdaQueryWrapper.class));
        verify(roleDeptMapper).delete(any(LambdaQueryWrapper.class));
    }
}
