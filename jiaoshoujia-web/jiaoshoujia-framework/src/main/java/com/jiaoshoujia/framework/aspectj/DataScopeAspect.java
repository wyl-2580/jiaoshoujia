package com.jiaoshoujia.framework.aspectj;

import com.jiaoshoujia.common.annotation.DataScope;
import com.jiaoshoujia.common.utils.StringUtils;
import com.jiaoshoujia.framework.security.LoginUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataScopeAspect {

    private static final int DATA_SCOPE_ALL = 1;
    private static final int DATA_SCOPE_CUSTOM = 2;
    private static final int DATA_SCOPE_DEPT = 3;
    private static final int DATA_SCOPE_DEPT_AND_BELOW = 4;
    private static final int DATA_SCOPE_SELF = 5;

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        try {
            LoginUser loginUser = getLoginUser();
            if (loginUser != null) {
                String deptAlias = dataScope.deptAlias();
                String userAlias = dataScope.userAlias();
                int scopeType = resolveDataScopeType(loginUser);
                String sqlCondition = buildSqlCondition(scopeType, loginUser, deptAlias, userAlias);
                if (StringUtils.isNotEmpty(sqlCondition)) {
                    DataScopeContextHolder.set(sqlCondition);
                }
            }
            return joinPoint.proceed();
        } finally {
            DataScopeContextHolder.clear();
        }
    }

    private String buildSqlCondition(int scopeType, LoginUser loginUser,
                                     String deptAlias, String userAlias) {
        String da = StringUtils.isNotEmpty(deptAlias) ? deptAlias + "." : "";
        String ua = StringUtils.isNotEmpty(userAlias) ? userAlias + "." : "";

        return switch (scopeType) {
            case DATA_SCOPE_ALL -> "";
            case DATA_SCOPE_CUSTOM -> da + "dept_id IN (" +
                    "SELECT dept_id FROM sys_role_dept WHERE role_id IN (" +
                    "SELECT role_id FROM sys_user_role WHERE user_id = " + loginUser.getUserId() +
                    "))";
            case DATA_SCOPE_DEPT -> da + "dept_id = " + loginUser.getDeptId();
            case DATA_SCOPE_DEPT_AND_BELOW -> da + "dept_id IN (" +
                    "SELECT dept_id FROM sys_dept WHERE dept_id = " + loginUser.getDeptId() +
                    " OR find_in_set(" + loginUser.getDeptId() + ", ancestors))";
            case DATA_SCOPE_SELF -> ua + "user_id = " + loginUser.getUserId();
            default -> "";
        };
    }

    private int resolveDataScopeType(LoginUser loginUser) {
        if (loginUser.getPermissions() != null && loginUser.getPermissions().contains("*:*:*")) {
            return DATA_SCOPE_ALL;
        }
        return DATA_SCOPE_SELF;
    }

    private LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }
}
