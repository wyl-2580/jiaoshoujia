package com.jiaoshoujia.framework.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

public class SecurityHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Content-Security-Policy", "default-src 'self'");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        chain.doFilter(request, response);
    }

    @Configuration
    static class SecurityHeaderFilterRegistration {

        @Bean
        public FilterRegistrationBean<SecurityHeaderFilter> securityHeaderFilterRegistration() {
            FilterRegistrationBean<SecurityHeaderFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new SecurityHeaderFilter());
            registration.addUrlPatterns("/*");
            registration.setName("securityHeaderFilter");
            registration.setOrder(2);
            return registration;
        }
    }
}
