package com.tourist.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {
    String module();
    String action();
    String detail() default "";
    String targetIdParam() default "";
}
