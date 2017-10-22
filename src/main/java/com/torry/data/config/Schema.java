package com.torry.data.config;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 介绍
 *
 * @author zhangtongrui
 * @date 2017/10/15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public  @interface Schema {
    String value() default "";
}
