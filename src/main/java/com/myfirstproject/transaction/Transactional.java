package com.myfirstproject.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

/**
 * Declarative transaction annotation.
 * Methods annotated with @Transactional will automatically have transactions managed.
 * The interceptor will begin a transaction before the method executes and commit after success,
 * or rollback on exception.
 * 
 * This meets the requirement: "Būtinos automatinės/deklaratyvios DB transakcijos"
 * (Automatic/declarative DB transactions are required).
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Transactional {
}
