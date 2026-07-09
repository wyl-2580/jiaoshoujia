package com.jiaoshoujia.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.domain.SysUser;
import com.jiaoshoujia.domain.dto.SysUserQuery;

public interface ISysUserService extends IService<SysUser> {

    SysUser selectUserByUsername(String username);

    SysUser selectUserById(Long userId);

    Page<SysUser> selectUserPage(Page<SysUser> page, SysUserQuery query);

    int insertUser(SysUser user);

    int updateUser(SysUser user);

    int deleteUserByIds(Long[] userIds);

    int resetPwd(SysUser user);

    int updateUserStatus(SysUser user);

    boolean checkUsernameUnique(SysUser user);

    void checkPasswordStrength(String rawPassword);
}
