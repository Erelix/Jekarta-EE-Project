package com.myfirstproject.cdi.decorator;

import java.io.Serializable;

import com.myfirstproject.cdi.service.GreetingService;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Greeting service decorator that adds extra functionality.
 * 
 * Demonstrates: CDI Decorator
 * 
 * A decorator:
 * - Implements the same interface as the decorated bean
 * - Wraps the original bean using @Delegate
 * - Adds additional behavior before/after delegate calls
 * - Must be enabled in beans.xml with <decorators>
 * - Is applied transparently to all injections of that type
 * 
 * This particular decorator adds timestamp information to greetings.
 * 
 * IMPORTANT: Decorators MUST be @Dependent scoped (CDI requirement).
 */
@Dependent
@Decorator
public class GreetingDecorator implements GreetingService, Serializable {

    @Inject
    @Delegate
    private GreetingService delegate;

    @Override
    public String greet(String name) {
        String originalGreeting = delegate.greet(name);
        String timestamp = java.time.LocalDateTime.now().toString();
        String serviceName = delegate.getServiceName();
        
        return originalGreeting + " [Decorated by GreetingDecorator | Service: " 
            + serviceName + " | Time: " + timestamp + "]";
    }

    @Override
    public String getServiceName() {
        return "GreetingDecorator wrapping [" + delegate.getServiceName() + "]";
    }
}
