package com.jiaoshoujia.system.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysRole;
import com.jiaoshoujia.system.domain.SysRoleDept;
import com.jiaoshoujia.system.domain.SysRoleMenu;
import com.jiaoshoujia.system.domain.SysUserRole;
import com.jiaoshoujia.system.mapper.SysRoleDeptMapper;
import com.jiaoshoujia.system.mapper.SysRoleMapper;
import com.jiaoshoujia.system.mapper.SysRoleMenuMapper;
import com.jiaoshoujia.system.mapper.SysUserRoleMapper;
import com.jiaoshoujia.system.service.ISysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleDeptMapper roleDeptMapper;

    public SysRoleServiceImpl(SysUserRoleMapper userRoleMapper, SysRoleMenuMapper roleMenuMapper,
                              SysRoleDeptMapper roleDeptMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleDeptMapper = roleDeptMapper;
    }

    @Override
    public List<SysRole> selectRolesByUserId(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return baseMapper.selectBatchIds(roleIds);
    }

    @Override
    public Set<String> selectRolePermsByUserId(Long userId) {
        List<SysRole> roles = selectRolesByUserId(userId);
        return roles.stream()
                .map(SysRole::getRoleKey)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toSet());
    }

    @Override
    public SysRole selectRoleById(Long roleId) {
        return getById(roleId);
    }

    @Override
    public Page<SysRole> selectRolePage(Page<SysRole> page, SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(role.getRoleName()), SysRole::getRoleName, role.getRoleName())
                .like(StringUtils.isNotEmpty(role.getRoleKey()), SysRole::getRoleKey, role.getRoleKey())
                .eq(role.getStatus() != null, SysRole::getStatus, role.getStatus())
                .orderByAsc(SysRole::getRoleSort);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysRole> selectRoleAll() {
        return lambdaQuery().orderByAsc(SysRole::getRoleSort).list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertRole(SysRole role) {
        save(role);
        insertRoleMenus(role.getId(), role.getMenuIds());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateRole(SysRole role) {
        updateById(role);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getId()));
        insertRoleMenus(role.getId(), role.getMenuIds());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteRoleByIds(Long[] roleIds) {
        List<Long> ids = Arrays.asList(roleIds);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, ids));
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, ids));
        removeBatchByIds(ids);
        return 1;
    }

    @Override
    public int updateRoleStatus(SysRole role) {
        return lambdaUpdate().eq(SysRole::getId, role.getId())
                .set(SysRole::getStatus, role.getStatus())
                .update() ? 1 : 0;
    }

    private void insertRoleMenus(Long roleId, Long[] menuIds) {
        if (menuIds == null || menuIds.length == 0) {
            return;
        }
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuMapper.insert(rm);
        }
    }
}
