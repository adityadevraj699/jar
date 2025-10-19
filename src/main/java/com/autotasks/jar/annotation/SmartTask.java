package com.autotasks.jar.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SmartTask {
    String hint() default ""; // optional manual hint: CPU, IO, MIXED
}
