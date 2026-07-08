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

    /**
     * 根据用户所有角色计算最宽松的数据权限范围。
     * 三员账号默认返回全部数据权限。
     */
    default int resolveUserDataScope(Long userId) {
        if (com.jiaoshoujia.common.utils.SecurityUtils.isAdmin(userId)) {
            return 1;
        }
        java.util.List<SysRole> roles = selectRolesByUserId(userId);
        int minScope = 5;
        for (SysRole role : roles) {
            if (role.getDataScope() != null) {
                try {
                    int scope = Integer.parseInt(role.getDataScope());
                    minScope = Math.min(minScope, scope);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return minScope;
    }
}
