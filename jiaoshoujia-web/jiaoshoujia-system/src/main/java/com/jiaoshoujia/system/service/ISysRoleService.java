package com.jiaoshoujia.system.service;

import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiaoshoujia.system.domain.SysRole;

public interface ISysRoleService extends IService<SysRole> {

    List<SysRole> selectRolesByUserId(Long userId);

    Set<String> selectRolePermsByUserId(Long userId);

    SysRole selectRoleById(Long roleId);

    Page<SysRole> selectRolePage(Page<SysRole> page, SysRole role);

    List<SysRole> selectRoleAll();

    int insertRole(SysRole role);

    int updateRole(SysRole role);

    int deleteRoleByIds(Long[] roleIds);

    int updateRoleStatus(SysRole role);
}
