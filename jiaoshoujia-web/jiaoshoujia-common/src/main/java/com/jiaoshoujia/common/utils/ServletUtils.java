package com.jiaoshoujia.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;

public final class ServletUtils {

    private ServletUtils() {
    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("当前线程中不存在 RequestAttributes");
        }
        return attributes.getRequest();
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("当前线程中不存在 RequestAttributes");
        }
        return attributes.getResponse();
    }

    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    public static void renderString(HttpServletResponse response, String string) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print(string);
        } catch (IOException e) {
            throw new RuntimeException("响应写入失败", e);
        }
    }

    private static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes;
        }
        return null;
    }
}
