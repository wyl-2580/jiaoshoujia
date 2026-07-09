package com.jiaoshoujia.controller;

import com.jiaoshoujia.common.annotation.Anonymous;
import com.jiaoshoujia.common.annotation.RateLimiter;
import com.jiaoshoujia.common.constant.Constants;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.exception.BusinessException;
import com.jiaoshoujia.common.utils.IpUtils;
import com.jiaoshoujia.common.utils.MessageUtils;
import com.jiaoshoujia.common.utils.SecurityUtils;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.framework.cache.CacheService;
import com.jiaoshoujia.framework.security.JwtTokenProvider;
import com.jiaoshoujia.framework.security.LoginUser;
import com.jiaoshoujia.service.ISysLoginInforService;
import com.jiaoshoujia.service.ISysMenuService;
import com.jiaoshoujia.service.ISysRoleService;
import com.jiaoshoujia.service.ISysUserService;
import com.jiaoshoujia.domain.SysMenu;
import com.jiaoshoujia.domain.SysUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String REFRESH_TOKEN_KEY = "refresh_token:";

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CacheService cacheService;
    private final ISysUserService userService;
    private final ISysRoleService roleService;
    private final ISysMenuService menuService;
    private final ISysLoginInforService loginInforService;

    @Value("${app.security.captcha-enabled:false}")
    private boolean captchaEnabled;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          CacheService cacheService,
                          ISysUserService userService,
                          ISysRoleService roleService,
                          ISysMenuService menuService,
                          ISysLoginInforService loginInforService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cacheService = cacheService;
        this.userService = userService;
        this.roleService = roleService;
        this.menuService = menuService;
        this.loginInforService = loginInforService;
    }

    @Anonymous
    @RateLimiter(qps = 5, message = "登录请求过于频繁，请稍后再试")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginBody loginBody, HttpServletRequest request) {
        String username = loginBody.username();
        String ip = IpUtils.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(loginBody.password())) {
            loginInforService.recordLogin(username, ip, userAgent, 1, "用户名或密码为空");
            return R.fail("用户名和密码不能为空");
        }

        try {
            if (captchaEnabled) {
                validateCaptcha(loginBody.code(), loginBody.uuid());
            }
            checkLoginAttempts(username, ip);
        } catch (BusinessException e) {
            loginInforService.recordLogin(username, ip, userAgent, 1, e.getMessage());
            throw e;
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginBody.password()));
        } catch (BadCredentialsException e) {
            recordLoginFailure(username, ip);
            loginInforService.recordLogin(username, ip, userAgent, 1, MessageUtils.message("auth.login.failed"));
            return R.fail(MessageUtils.message("auth.login.failed"));
        }

        clearLoginAttempts(username, ip);
        loginInforService.recordLogin(username, ip, userAgent, 0, "登录成功");
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        cacheService.delete(Constants.FORCE_LOGOUT_KEY + loginUser.getUserId());

        String accessToken = jwtTokenProvider.createAccessToken(loginUser);
        String refreshToken = jwtTokenProvider.createRefreshToken(loginUser);

        String accessTokenId = jwtTokenProvider.getTokenId(accessToken);
        cacheService.set(Constants.LOGIN_USER_KEY + accessTokenId, loginUser, 30, TimeUnit.MINUTES);

        String refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
        cacheService.set(REFRESH_TOKEN_KEY + refreshTokenId, loginUser.getUserId(), 7, TimeUnit.DAYS);

        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("access_token", accessToken);
        tokenMap.put("refresh_token", refreshToken);
        return R.ok(tokenMap);
    }

    @PostMapping("/logout")
    public R<Void> logout(@RequestBody(required = false) Map<String, String> body,
                          HttpServletRequest request) {
        String token = resolveToken(request);
        if (StringUtils.isNotEmpty(token)) {
            String tokenId = jwtTokenProvider.getTokenId(token);
            cacheService.delete(Constants.LOGIN_USER_KEY + tokenId);
            try {
                String username = jwtTokenProvider.getUsername(token);
                loginInforService.recordLogin(username, IpUtils.getIpAddr(request),
                        request.getHeader("User-Agent"), 0, "退出成功");
            } catch (Exception ignored) {
            }
        }
        if (body != null) {
            String refreshToken = body.get("refresh_token");
            if (StringUtils.isNotEmpty(refreshToken)) {
                try {
                    String refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
                    cacheService.delete(REFRESH_TOKEN_KEY + refreshTokenId);
                } catch (Exception ignored) {
                }
            }
        }
        return R.ok();
    }

    @Anonymous
    @RateLimiter(qps = 5, message = "请求过于频繁，请稍后再试")
    @PostMapping("/refresh")
    public R<Map<String, String>> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");
        if (StringUtils.isEmpty(refreshToken)) {
            return R.fail("refresh_token不能为空");
        }
        if (jwtTokenProvider.isTokenExpired(refreshToken)) {
            return R.fail("refresh_token已过期，请重新登录");
        }

        String refreshTokenId = jwtTokenProvider.getTokenId(refreshToken);
        Long cachedUserId = cacheService.getAndDelete(REFRESH_TOKEN_KEY + refreshTokenId);
        if (cachedUserId == null) {
            return R.fail("refresh_token已失效，请重新登录");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        if (!cachedUserId.equals(userId)) {
            return R.fail("refresh_token无效");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        SysUser user = userService.selectUserById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            return R.fail(MessageUtils.message("auth.account.disabled"));
        }

        Set<String> permissions = menuService.selectMenuPermsByUserId(userId);
        int dataScope = roleService.resolveUserDataScope(userId);
        LoginUser loginUser = new LoginUser(userId, username, user.getPassword(),
                user.getDeptId(), permissions, dataScope);

        String newAccessToken = jwtTokenProvider.createAccessToken(loginUser);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(loginUser);

        String newAccessTokenId = jwtTokenProvider.getTokenId(newAccessToken);
        cacheService.set(Constants.LOGIN_USER_KEY + newAccessTokenId, loginUser, 30, TimeUnit.MINUTES);

        String newRefreshTokenId = jwtTokenProvider.getTokenId(newRefreshToken);
        cacheService.set(REFRESH_TOKEN_KEY + newRefreshTokenId, userId, 7, TimeUnit.DAYS);

        Map<String, String> result = new HashMap<>();
        result.put("access_token", newAccessToken);
        result.put("refresh_token", newRefreshToken);
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

    @GetMapping("/profile")
    public R<SysUser> profile() {
        Long userId = SecurityUtils.getUserId();
        SysUser user = userService.selectUserById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return R.ok(user);
    }

    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody SysUser user) {
        Long userId = SecurityUtils.getUserId();
        SysUser update = new SysUser();
        update.setId(userId);
        update.setNickname(user.getNickname());
        update.setEmail(user.getEmail());
        update.setPhone(user.getPhone());
        update.setSex(user.getSex());
        return userService.updateById(update) ? R.ok() : R.fail();
    }

    @PutMapping("/profile/password")
    public R<Void> updatePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(newPassword)) {
            return R.fail("旧密码和新密码不能为空");
        }
        Long userId = SecurityUtils.getUserId();
        SysUser user = userService.selectUserById(userId);
        if (!SecurityUtils.matchesPassword(oldPassword, user.getPassword())) {
            return R.fail("原密码错误");
        }
        userService.checkPasswordStrength(newPassword);
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(SecurityUtils.encryptPassword(newPassword));
        return userService.updateById(update) ? R.ok() : R.fail();
    }

    private void validateCaptcha(String code, String uuid) {
        if (StringUtils.isEmpty(uuid) || StringUtils.isEmpty(code)) {
            throw new BusinessException("验证码不能为空");
        }
        String cacheKey = Constants.CAPTCHA_CODE_KEY + uuid;
        String cachedCode = cacheService.get(cacheKey);
        cacheService.delete(cacheKey);
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期");
        }
        if (!cachedCode.equalsIgnoreCase(code)) {
            throw new BusinessException("验证码错误");
        }
    }

    private void checkLoginAttempts(String username, String ip) {
        String key = Constants.LOGIN_ATTEMPT_KEY + ip + ":" + username;
        Long attempts = cacheService.get(key);
        if (attempts != null && attempts >= Constants.MAX_LOGIN_ATTEMPTS) {
            throw new BusinessException(
                    MessageUtils.message("auth.account.locked", Constants.LOCK_DURATION_MINUTES));
        }
    }

    private void recordLoginFailure(String username, String ip) {
        String key = Constants.LOGIN_ATTEMPT_KEY + ip + ":" + username;
        long count = cacheService.increment(key);
        if (count == 1) {
            cacheService.expire(key, Constants.LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }

    private void clearLoginAttempts(String username, String ip) {
        cacheService.delete(Constants.LOGIN_ATTEMPT_KEY + ip + ":" + username);
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
