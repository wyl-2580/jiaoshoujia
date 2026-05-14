package com.jiaoshoujia.framework.security;

import java.util.Set;

import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.system.domain.SysUser;
import com.jiaoshoujia.system.service.ISysMenuService;
import com.jiaoshoujia.system.service.ISysUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ISysUserService userService;
    private final ISysMenuService menuService;

    public UserDetailsServiceImpl(ISysUserService userService, ISysMenuService menuService) {
        this.userService = userService;
        this.menuService = menuService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userService.selectUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException("账号已被停用: " + username);
        }
        Set<String> permissions = menuService.selectMenuPermsByUserId(user.getId());
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(),
                user.getDeptId(), permissions);
    }
}
