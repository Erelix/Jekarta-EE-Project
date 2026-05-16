package com.myfirstproject.cdi.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

/**
 * Qualifier annotation to mark the primary/default GreetingService implementation.
 * Used to disambiguate between multiple GreetingService implementations when injecting.
 */
@Qualifier
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultGreeting {
}
