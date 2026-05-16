package com.myfirstproject.cdi.service;

import java.io.Serializable;

import com.myfirstproject.cdi.interceptor.Logged;

import jakarta.enterprise.context.Dependent;

/**
 * Logged version of BasicGreetingService with interceptor binding.
 * 
 * Demonstrates: Using @Logged interceptor binding on a service
 * 
 * This service has the @Logged annotation which enables the LoggingInterceptor
 * for all its methods. The interceptor will log:
 * - Method entry with parameters
 * - Method exit with return value and execution time
 * - Any exceptions that occur
 * 
 * The interceptor is only active when enabled in beans.xml.
 * 
 * Uses @Dependent scope to support CDI Decorator usage (decorators require @Dependent beans).
 */
@Dependent
@Logged
public class LoggedGreetingService implements GreetingService, Serializable {

    @Override
    public String greet(String name) {
        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Hello, " + name + "! (This greeting is logged)";
    }

    @Override
    public String getServiceName() {
        return "LoggedGreetingService (with @Logged interceptor)";
    }
}
