package com.jiaoshoujia.framework.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class LoginUser implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    private String username;

    private String password;

    private Long deptId;

    private int dataScope = 5;

    private Set<String> permissions;

    private boolean accountNonLocked = true;

    private transient Collection<GrantedAuthority> authorities;

    public LoginUser() {
    }

    public LoginUser(Long userId, String username, String password, Long deptId,
                     Set<String> permissions, int dataScope) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.deptId = deptId;
        this.permissions = permissions;
        this.dataScope = dataScope;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.authorities == null) {
            if (permissions == null || permissions.isEmpty()) {
                this.authorities = Collections.emptyList();
            } else {
                this.authorities = permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableSet());
            }
        }
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
        this.authorities = null;
    }

    public int getDataScope() {
        return dataScope;
    }

    public void setDataScope(int dataScope) {
        this.dataScope = dataScope;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
}
