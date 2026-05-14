package com.jiaoshoujia.controller.system;

import com.jiaoshoujia.common.annotation.Anonymous;
import com.jiaoshoujia.common.constant.Constants;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.framework.cache.CacheService;
import com.jiaoshoujia.framework.security.JwtTokenProvider;
import com.jiaoshoujia.framework.security.LoginUser;
import com.jiaoshoujia.system.service.ISysMenuService;
import com.jiaoshoujia.system.service.ISysRoleService;
import com.jiaoshoujia.system.service.ISysUserService;
import com.jiaoshoujia.system.domain.SysMenu;
import com.jiaoshoujia.system.domain.SysRole;
import com.jiaoshoujia.system.domain.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CacheService cacheService;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysMenuService menuService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          CacheService cacheService,
                          ISysUserService userService,
                          ISysRoleService roleService,
                          ISysMenuService menuService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cacheService = cacheService;
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
    }

    @Anonymous
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginBody loginBody) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginBody.username(), loginBody.password()));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(loginUser);
        String refreshToken = jwtTokenProvider.createRefreshToken(loginUser);

        String tokenId = jwtTokenProvider.getTokenId(accessToken);
        cacheService.set(Constants.LOGIN_USER_KEY + tokenId, loginUser, 30, TimeUnit.MINUTES);

        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("access_token", accessToken);
        tokenMap.put("refresh_token", refreshToken);
        return R.ok(tokenMap);
    }

    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        if (StringUtils.isNotEmpty(token)) {
            String tokenId = jwtTokenProvider.getTokenId(token);
            cacheService.delete(Constants.LOGIN_USER_KEY + tokenId);
        }
        return R.ok();
    }

    @Anonymous
    @PostMapping("/refresh")
    public R<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        if (StringUtils.isEmpty(refreshToken)) {
            return R.fail("refresh_token不能为空");
        }
        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            return R.fail("refresh_token已过期，请重新登录");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }

        Set<String> permissions = menuService.selectMenuPermsByUserId(userId);
        LoginUser loginUser = new LoginUser(userId, username, user.getPassword(),
                user.getDeptId(), permissions);

        String newAccessToken = jwtTokenProvider.createAccessToken(loginUser);
        String newTokenId = jwtTokenProvider.getTokenId(newAccessToken);
        cacheService.set(Constants.LOGIN_USER_KEY + newTokenId, loginUser, 30, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("access_token", newAccessToken);
        return R.ok(result);
    }

    @GetMapping("/getInfo")
    public R<Map<String, Object>> getInfo() {
        Long userId = SecurityUtils.getUserId();
        SysUser user = userService.selectUserById(userId);
        Set<String> roles = roleService.selectRolePermsByUserId(userId);
        Set<String> permissions = menuService.selectMenuPermsByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roles", roles);
        result.put("permissions", permissions);
        return R.ok(result);
    }

    @GetMapping("/getRouters")
    public R<List<SysMenu>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return R.ok(menus);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.isNotEmpty(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }

    public record LoginBody(String username, String password, String code, String uuid) {
    }
}
