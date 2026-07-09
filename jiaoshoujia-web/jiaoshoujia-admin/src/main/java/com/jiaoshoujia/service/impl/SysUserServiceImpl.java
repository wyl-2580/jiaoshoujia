package com.jiaoshoujia.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.annotation.DataScope;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.MessageUtils;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.domain.SysDept;
import com.jiaoshoujia.domain.SysRole;
import com.jiaoshoujia.domain.SysUser;
import com.jiaoshoujia.domain.SysUserRole;
import com.jiaoshoujia.domain.dto.SysUserQuery;
import com.jiaoshoujia.mapper.SysDeptMapper;
import com.jiaoshoujia.mapper.SysRoleMapper;
import com.jiaoshoujia.mapper.SysUserMapper;
import com.jiaoshoujia.mapper.SysUserRoleMapper;
import com.jiaoshoujia.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    public SysUserServiceImpl(SysDeptMapper deptMapper, SysRoleMapper roleMapper, SysUserRoleMapper userRoleMapper) {
        this.deptMapper = deptMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public SysUser selectUserByUsername(String username) {
        SysUser user = lambdaQuery().eq(SysUser::getUsername, username).one();
        if (user != null) {
            populateUserExtras(user);
        }
        return user;
    }

    @Override
    public SysUser selectUserById(Long userId) {
        SysUser user = getById(userId);
        if (user != null) {
            populateUserExtras(user);
        }
        return user;
    }

    @DataScope(deptAlias = "sys_user", userAlias = "sys_user", userColumn = "id")
    @Override
    public Page<SysUser> selectUserPage(Page<SysUser> page, SysUserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.isNotEmpty(query.getPhone()), SysUser::getPhone, query.getPhone())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
                .ge(StringUtils.isNotEmpty(query.getBeginTime()), SysUser::getCreateTime, query.getBeginTime())
                .le(StringUtils.isNotEmpty(query.getEndTime()), SysUser::getCreateTime, endOfDay(query.getEndTime()))
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = baseMapper.selectPage(page, wrapper);
        populateUserListExtras(result.getRecords());
        return result;
    }

    private void populateUserListExtras(List<SysUser> users) {
        if (users.isEmpty()) return;

        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());

        // 批量查部门
        List<Long> deptIds = users.stream().map(SysUser::getDeptId)
                .filter(id -> id != null).distinct().collect(Collectors.toList());
        java.util.Map<Long, String> deptNameMap = new java.util.HashMap<>();
        if (!deptIds.isEmpty()) {
            List<SysDept> depts = deptMapper.selectList(
                    new LambdaQueryWrapper<SysDept>().in(SysDept::getId, deptIds));
            for (SysDept dept : depts) {
                deptNameMap.put(dept.getId(), dept.getDeptName());
            }
        }

        // 批量查角色
        List<SysUserRole> allUserRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        List<Long> roleIds = allUserRoles.stream().map(SysUserRole::getRoleId)
                .distinct().collect(Collectors.toList());
        java.util.Map<Long, SysRole> roleMap = new java.util.HashMap<>();
        if (!roleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds));
            for (SysRole role : roles) {
                roleMap.put(role.getId(), role);
            }
        }

        for (SysUser user : users) {
            user.setDeptName(deptNameMap.get(user.getDeptId()));

            List<SysRole> userRoles = allUserRoles.stream()
                    .filter(ur -> ur.getUserId().equals(user.getId()))
                    .map(ur -> roleMap.get(ur.getRoleId()))
                    .filter(r -> r != null)
                    .collect(Collectors.toList());
            user.setRoles(userRoles);
        }
    }

    private String endOfDay(String endTime) {
        if (StringUtils.isEmpty(endTime) || endTime.length() > 10) {
            return endTime;
        }
        return endTime + " 23:59:59";
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertUser(SysUser user) {
        if (!checkUsernameUnique(user)) {
            throw new BusinessException(MessageUtils.message("user.username.exists", user.getUsername()));
        }
        checkRoleIdsValid(user.getRoleIds());
        if (!save(user)) {
            return 0;
        }
        insertUserRoles(user.getId(), user.getRoleIds());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUser(SysUser user) {
        if (!checkUsernameUnique(user)) {
            throw new BusinessException(MessageUtils.message("user.username.exists", user.getUsername()));
        }
        checkRoleIdsValid(user.getRoleIds());
        checkSelfRoleChange(user);
        user.setPassword(null);
        user.setStatus(null);
        if (!updateById(user)) {
            return 0;
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        insertUserRoles(user.getId(), user.getRoleIds());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            if (SecurityUtils.isAdmin(userId)) {
                throw new BusinessException(MessageUtils.message("user.admin.not.allow.delete"));
            }
            if (userId.equals(SecurityUtils.getUserId())) {
                throw new BusinessException(MessageUtils.message("user.current.not.allow.delete"));
            }
        }
        List<Long> ids = Arrays.asList(userIds);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, ids));
        return removeBatchByIds(ids) ? 1 : 0;
    }

    @Override
    public int resetPwd(SysUser user) {
        if (SecurityUtils.isAdmin(user.getId()) && !SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            throw new BusinessException(MessageUtils.message("user.admin.not.allow.reset"));
        }
        if (user.getId().equals(SecurityUtils.getUserId())) {
            throw new BusinessException(MessageUtils.message("user.current.not.allow.delete"));
        }
        return lambdaUpdate().eq(SysUser::getId, user.getId())
                .set(SysUser::getPassword, user.getPassword())
                .update() ? 1 : 0;
    }

    @Override
    public int updateUserStatus(SysUser user) {
        if (SecurityUtils.isAdmin(user.getId())) {
            throw new BusinessException(MessageUtils.message("user.admin.not.allow.disable"));
        }
        if (user.getId().equals(SecurityUtils.getUserId())) {
            throw new BusinessException(MessageUtils.message("user.self.not.allow.disable"));
        }
        return lambdaUpdate().eq(SysUser::getId, user.getId())
                .set(SysUser::getStatus, user.getStatus())
                .update() ? 1 : 0;
    }

    @Override
    public boolean checkUsernameUnique(SysUser user) {
        SysUser existing = lambdaQuery().eq(SysUser::getUsername, user.getUsername()).one();
        if (existing != null && (user.getId() == null || !existing.getId().equals(user.getId()))) {
            return false;
        }
        return true;
    }

    @Override
    public void checkPasswordStrength(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 8
                || !rawPassword.matches(".*[a-zA-Z].*")
                || !rawPassword.matches(".*\\d.*")) {
            throw new BusinessException(MessageUtils.message("user.password.too.weak"));
        }
    }

    private void populateUserExtras(SysUser user) {
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                user.setDeptName(dept.getDeptName());
            }
        }
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, user.getId()));
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            List<SysRole> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().in(SysRole::getId, roleIds));
            user.setRoles(roles);
            user.setRoleIds(roleIds.toArray(new Long[0]));
        } else {
            user.setRoles(Collections.emptyList());
        }
    }

    /**
     * 三权分立：禁止通过管理接口修改自己的角色关联。
     * 如果被编辑的用户是当前登录用户，且角色列表发生了变更，则拒绝操作。
     */
    private void checkSelfRoleChange(SysUser user) {
        Long currentUserId = SecurityUtils.getUserId();
        if (!currentUserId.equals(user.getId())) {
            return;
        }
        List<SysUserRole> existing = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, currentUserId));
        Set<Long> currentRoleIds = existing.stream()
                .map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Set<Long> newRoleIds = user.getRoleIds() != null
                ? new HashSet<>(Arrays.asList(user.getRoleIds()))
                : Collections.emptySet();
        if (!currentRoleIds.equals(newRoleIds)) {
            throw new BusinessException(MessageUtils.message("user.self.role.not.allow.edit"));
        }
    }

    /**
     * 校验分配的 roleIds 是否合法（角色存在且启用）。
     */
    private void checkRoleIdsValid(Long[] roleIds) {
        if (roleIds == null || roleIds.length == 0) {
            return;
        }
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().in(SysRole::getId, Arrays.asList(roleIds)));
        if (roles.size() != roleIds.length) {
            throw new BusinessException(MessageUtils.message("role.not.found"));
        }
        for (SysRole role : roles) {
            if (role.getStatus() != null && role.getStatus() == 1) {
                throw new BusinessException(MessageUtils.message("role.not.found"));
            }
        }
    }

    private void insertUserRoles(Long userId, Long[] roleIds) {
        if (roleIds == null || roleIds.length == 0) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }
}
