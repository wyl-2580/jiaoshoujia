package com.jiaoshoujia.common.annotation;

import com.jiaoshoujia.common.enums.BusinessType;
import com.jiaoshoujia.common.enums.OperatorType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String title() default "";

    BusinessType businessType() default BusinessType.OTHER;

    OperatorType operatorType() default OperatorType.ADMIN;

    boolean isSaveRequestData() default true;

    boolean isSaveResponseData() default true;
}
