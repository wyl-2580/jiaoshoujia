package com.jiaoshoujia.security;

import java.util.List;
import java.util.Set;

import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.MessageUtils;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.framework.security.LoginUser;
import com.jiaoshoujia.domain.SysRole;
import com.jiaoshoujia.domain.SysUser;
import com.jiaoshoujia.service.ISysMenuService;
import com.jiaoshoujia.service.ISysRoleService;
import com.jiaoshoujia.service.ISysUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 认证用户加载实现。
 *
 * <p>该实现依赖 system 模块的用户/菜单服务，因此放置在 system 模块（system -&gt; framework -&gt; common），
 * 避免 framework 反向依赖 system 造成模块循环依赖。</p>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ISysUserService userService;
    private final ISysMenuService menuService;
    private final ISysRoleService roleService;

    public UserDetailsServiceImpl(ISysUserService userService, ISysMenuService menuService,
                                  ISysRoleService roleService) {
        this.userService = userService;
        this.menuService = menuService;
        this.roleService = roleService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(MessageUtils.message("auth.login.failed"));
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(MessageUtils.message("auth.login.failed"));
        }
        Set<String> permissions = menuService.selectMenuPermsByUserId(user.getId());
        int dataScope = roleService.resolveUserDataScope(user.getId());
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(),
                user.getDeptId(), permissions, dataScope);
    }
}
