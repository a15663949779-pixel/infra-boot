package com.example.common.annotation;

import com.example.common.enums.BusinessType;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String title() default "";

    BusinessType businessType() default BusinessType.OTHER;

    boolean isSaveRequestData() default true;

    boolean isSaveResponseData() default true;
}
