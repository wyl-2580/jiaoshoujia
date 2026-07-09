package com.jiaoshoujia.framework.security;

import com.jiaoshoujia.common.constant.Constants;
import com.jiaoshoujia.framework.cache.CacheService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CacheService cacheService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CacheService cacheService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cacheService = cacheService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(Constants.TOKEN_HEADER);
        if (header == null || !header.startsWith(Constants.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(Constants.TOKEN_PREFIX.length()).trim();
        try {
            if (jwtTokenProvider.isTokenExpired(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtTokenProvider.getUserId(token);
            if (userId != null && cacheService.hasKey(Constants.FORCE_LOGOUT_KEY + userId)) {
                String tokenId = jwtTokenProvider.getTokenId(token);
                cacheService.delete(Constants.LOGIN_USER_KEY + tokenId);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"密码已被重置，请重新登录\"}");
                return;
            }

            String tokenId = jwtTokenProvider.getTokenId(token);
            String cacheKey = Constants.LOGIN_USER_KEY + tokenId;
            LoginUser loginUser = cacheService.get(cacheKey);
            if (loginUser != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.debug("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
