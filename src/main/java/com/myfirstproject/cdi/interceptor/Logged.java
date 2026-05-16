package com.myfirstproject.cdi.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

/**
 * Interceptor binding annotation for logging.
 * 
 * Demonstrates: CDI Interceptor Binding
 * 
 * This annotation marks which methods/classes should be intercepted.
 * The actual interception logic is defined in LoggingInterceptor.
 */
@InterceptorBinding
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logged {
    // Marker interface for interceptor binding
}
