package com.jiaoshoujia.system.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.MessageUtils;
import com.jiaoshoujia.common.utils.SecurityUtils;
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
        return listByIds(roleIds);
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
        SysRole role = getById(roleId);
        if (role == null) {
            return null;
        }
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        Long[] menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).toArray(Long[]::new);
        role.setMenuIds(menuIds);

        List<SysRoleDept> roleDepts = roleDeptMapper.selectList(
                new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId));
        Long[] deptIds = roleDepts.stream().map(SysRoleDept::getDeptId).toArray(Long[]::new);
        role.setDeptIds(deptIds);
        return role;
    }

    @Override
    public Page<SysRole> selectRolePage(Page<SysRole> page, SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(role.getRoleName()), SysRole::getRoleName, role.getRoleName())
                .like(StringUtils.isNotEmpty(role.getRoleKey()), SysRole::getRoleKey, role.getRoleKey())
                .eq(role.getStatus() != null, SysRole::getStatus, role.getStatus())
                .ge(StringUtils.isNotEmpty(role.getBeginTime()), SysRole::getCreateTime, role.getBeginTime())
                .le(StringUtils.isNotEmpty(role.getEndTime()), SysRole::getCreateTime, endOfDay(role.getEndTime()))
                .orderByAsc(SysRole::getRoleSort);
        return baseMapper.selectPage(page, wrapper);
    }

    private String endOfDay(String endTime) {
        if (StringUtils.isEmpty(endTime) || endTime.length() > 10) {
            return endTime;
        }
        return endTime + " 23:59:59";
    }

    @Override
    public List<SysRole> selectRoleAll() {
        return lambdaQuery().orderByAsc(SysRole::getRoleSort).list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertRole(SysRole role) {
        checkDataScope(role);
        checkMenuIdsInScope(role.getMenuIds());
        if (!save(role)) {
            return 0;
        }
        insertRoleMenus(role.getId(), role.getMenuIds());
        insertRoleDepts(role.getId(), role.getDeptIds());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateRole(SysRole role) {
        checkDataScope(role);
        checkNotOwnRole(role.getId(), "role.self.not.allow.edit");
        checkMenuIdsInScope(role.getMenuIds());
        if (!updateById(role)) {
            return 0;
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, role.getId()));
        insertRoleMenus(role.getId(), role.getMenuIds());
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, role.getId()));
        insertRoleDepts(role.getId(), role.getDeptIds());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteRoleByIds(Long[] roleIds) {
        for (Long roleId : roleIds) {
            checkNotOwnRole(roleId, "role.self.not.allow.delete");
        }
        List<Long> ids = Arrays.asList(roleIds);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, ids));
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, ids));
        roleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().in(SysRoleDept::getRoleId, ids));
        return removeBatchByIds(ids) ? 1 : 0;
    }

    @Override
    public int updateRoleStatus(SysRole role) {
        checkNotOwnRole(role.getId(), "role.self.not.allow.disable");
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

    private void insertRoleDepts(Long roleId, Long[] deptIds) {
        if (deptIds == null || deptIds.length == 0) {
            return;
        }
        for (Long deptId : deptIds) {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(roleId);
            rd.setDeptId(deptId);
            roleDeptMapper.insert(rd);
        }
    }

    /**
     * 校验 dataScope 取值合法性。非管理员不允许设置"全部数据权限"。
     */
    private void checkDataScope(SysRole role) {
        if (role.getDataScope() != null) {
            try {
                int scope = Integer.parseInt(role.getDataScope());
                if (scope < 1 || scope > 5) {
                    throw new BusinessException("数据权限范围值不合法");
                }
                Long userId = SecurityUtils.getUserId();
                if (scope == 1 && !SecurityUtils.isAdmin(userId)) {
                    throw new BusinessException("非管理员不允许设置全部数据权限");
                }
            } catch (NumberFormatException e) {
                throw new BusinessException("数据权限范围值不合法");
            }
        }
    }

    // ======================== 三权分立安全校验 ========================

    /**
     * 校验当前用户是否属于指定角色，如果是则禁止操作（不允许修改/删除/停用自己所属的角色）。
     */
    private void checkNotOwnRole(Long roleId, String messageKey) {
        Long userId = SecurityUtils.getUserId();
        long count = userRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getRoleId, roleId));
        if (count > 0) {
            throw new BusinessException(MessageUtils.message(messageKey));
        }
    }

    /**
     * 校验分配的 menuIds 不超出当前操作者自身拥有的菜单权限范围。
     * 内置三员账号（系统管理员、安全管理员、审计管理员）豁免此校验，因为在三权分立体系中
     * 安全管理员需要能为其他角色分配任意菜单权限（但仍受 checkNotOwnRole 约束无法提升自身权限）。
     */
    private void checkMenuIdsInScope(Long[] menuIds) {
        if (menuIds == null || menuIds.length == 0) {
            return;
        }
        Long userId = SecurityUtils.getUserId();
        if (SecurityUtils.isAdmin(userId)) {
            return;
        }
        Set<Long> ownedMenuIds = getOwnedMenuIds(userId);
        Set<Long> requested = new HashSet<>(Arrays.asList(menuIds));
        requested.removeAll(ownedMenuIds);
        if (!requested.isEmpty()) {
            throw new BusinessException(MessageUtils.message("role.menu.exceed"));
        }
    }

    /**
     * 获取指定用户通过角色拥有的所有菜单ID集合。
     */
    private Set<Long> getOwnedMenuIds(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));
        return roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());
    }
}
