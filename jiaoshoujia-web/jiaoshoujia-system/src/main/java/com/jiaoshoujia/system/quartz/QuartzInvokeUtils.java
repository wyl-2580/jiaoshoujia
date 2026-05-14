package com.jiaoshoujia.system.quartz;

import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.system.domain.SysJob;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
public class QuartzInvokeUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    public static void invokeMethod(SysJob sysJob) throws Exception {
        String invokeTarget = sysJob.getInvokeTarget();
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);
        List<Object[]> methodParams = getMethodParams(invokeTarget);

        Object bean = applicationContext.getBean(beanName);
        if (methodParams.isEmpty()) {
            Method method = bean.getClass().getMethod(methodName);
            method.invoke(bean);
        } else {
            Class<?>[] paramTypes = new Class<?>[methodParams.size()];
            Object[] paramValues = new Object[methodParams.size()];
            for (int i = 0; i < methodParams.size(); i++) {
                paramTypes[i] = (Class<?>) methodParams.get(i)[1];
                paramValues[i] = methodParams.get(i)[0];
            }
            Method method = bean.getClass().getMethod(methodName, paramTypes);
            method.invoke(bean, paramValues);
        }
    }

    private static String getBeanName(String invokeTarget) {
        return invokeTarget.substring(0, invokeTarget.indexOf('.'));
    }

    private static String getMethodName(String invokeTarget) {
        String methodAndParams = invokeTarget.substring(invokeTarget.indexOf('.') + 1);
        if (methodAndParams.contains("(")) {
            return methodAndParams.substring(0, methodAndParams.indexOf('('));
        }
        return methodAndParams;
    }

    private static List<Object[]> getMethodParams(String invokeTarget) {
        List<Object[]> params = new ArrayList<>();
        if (!invokeTarget.contains("(") || !invokeTarget.contains(")")) {
            return params;
        }
        String paramStr = invokeTarget.substring(invokeTarget.indexOf('(') + 1, invokeTarget.indexOf(')'));
        if (StringUtils.isEmpty(paramStr)) {
            return params;
        }
        String[] parts = paramStr.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.startsWith("'") && trimmed.endsWith("'")) {
                params.add(new Object[]{trimmed.substring(1, trimmed.length() - 1), String.class});
            } else if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                params.add(new Object[]{Boolean.valueOf(trimmed), Boolean.class});
            } else if (trimmed.endsWith("L") || trimmed.endsWith("l")) {
                params.add(new Object[]{Long.valueOf(trimmed.substring(0, trimmed.length() - 1)), Long.class});
            } else if (trimmed.endsWith("D") || trimmed.endsWith("d")) {
                params.add(new Object[]{Double.valueOf(trimmed.substring(0, trimmed.length() - 1)), Double.class});
            } else {
                params.add(new Object[]{Integer.valueOf(trimmed), Integer.class});
            }
        }
        return params;
    }
}
