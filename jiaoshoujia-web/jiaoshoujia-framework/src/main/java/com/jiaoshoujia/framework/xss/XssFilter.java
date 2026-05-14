package com.jiaoshoujia.framework.xss;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();
        if (contentType != null && contentType.contains("multipart/form-data")) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
    }

    @Configuration
    static class XssFilterRegistration {

        @Bean
        public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
            FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new XssFilter());
            registration.addUrlPatterns("/*");
            registration.setName("xssFilter");
            registration.setOrder(1);
            return registration;
        }
    }
}
