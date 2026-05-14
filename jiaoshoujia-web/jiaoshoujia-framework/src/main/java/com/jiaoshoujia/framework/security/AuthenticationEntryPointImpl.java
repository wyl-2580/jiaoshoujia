package com.jiaoshoujia.framework.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiaoshoujia.common.constant.HttpStatus;
import com.jiaoshoujia.common.core.R;
import com.jiaoshoujia.common.utils.MessageUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = 1L;

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPointImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        R<?> result = R.fail(HttpStatus.UNAUTHORIZED, MessageUtils.message("auth.failed"));
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
