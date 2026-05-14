package com.jiaoshoujia.system.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiaoshoujia.common.annotation.DataScope;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.MessageUtils;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysDept;
import com.jiaoshoujia.system.domain.SysRole;
import com.jiaoshoujia.system.domain.SysUser;
import com.jiaoshoujia.system.domain.SysUserRole;
import com.jiaoshoujia.system.domain.dto.SysUserQuery;
import com.jiaoshoujia.system.mapper.SysDeptMapper;
import com.jiaoshoujia.system.mapper.SysRoleMapper;
import com.jiaoshoujia.system.mapper.SysUserMapper;
import com.jiaoshoujia.system.mapper.SysUserRoleMapper;
import com.jiaoshoujia.system.service.ISysUserService;
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

    @DataScope(deptAlias = "sys_user", userAlias = "sys_user")
    @Override
    public Page<SysUser> selectUserPage(Page<SysUser> page, SysUserQuery query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(query.getUsername()), SysUser::getUsername, query.getUsername())
                .like(StringUtils.isNotEmpty(query.getPhone()), SysUser::getPhone, query.getPhone())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .eq(query.getDeptId() != null, SysUser::getDeptId, query.getDeptId())
                .ge(StringUtils.isNotEmpty(query.getBeginTime()), SysUser::getCreateTime, query.getBeginTime())
                .le(StringUtils.isNotEmpty(query.getEndTime()), SysUser::getCreateTime, query.getEndTime())
                .orderByDesc(SysUser::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertUser(SysUser user) {
        if (!checkUsernameUnique(user)) {
            throw new BusinessException(MessageUtils.message("user.username.exists", user.getUsername()));
        }
        save(user);
        insertUserRoles(user.getId(), user.getRoleIds());
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateUser(SysUser user) {
        if (!checkUsernameUnique(user)) {
            throw new BusinessException(MessageUtils.message("user.username.exists", user.getUsername()));
        }
        user.setPassword(null);
        user.setStatus(null);
        updateById(user);
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
        removeBatchByIds(ids);
        return 1;
    }

    @Override
    public int resetPwd(SysUser user) {
        if (SecurityUtils.isAdmin(user.getId()) && !SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            throw new BusinessException(MessageUtils.message("user.admin.not.allow.reset"));
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
            List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
            user.setRoles(roles);
            user.setRoleIds(roleIds.toArray(new Long[0]));
        } else {
            user.setRoles(Collections.emptyList());
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
