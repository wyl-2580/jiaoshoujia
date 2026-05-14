package com.jiaoshoujia.framework.security;

import java.util.Set;

import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.MessageUtils;
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
            throw new UsernameNotFoundException(MessageUtils.message("auth.login.failed"));
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(MessageUtils.message("auth.login.failed"));
        }
        Set<String> permissions = menuService.selectMenuPermsByUserId(user.getId());
        return new LoginUser(user.getId(), user.getUsername(), user.getPassword(),
                user.getDeptId(), permissions);
    }
}
