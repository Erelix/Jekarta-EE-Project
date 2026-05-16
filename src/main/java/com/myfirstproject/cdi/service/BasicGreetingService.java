package com.myfirstproject.cdi.service;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Named;

/**
 * Basic implementation of GreetingService.
 * This serves as the default implementation when no @Alternative is selected.
 * 
 * Uses @Dependent scope to support CDI Decorator usage (decorators require @Dependent beans).
 * Marked with @DefaultGreeting qualifier to disambiguate from other GreetingService implementations.
 */
@Dependent
@Named
@DefaultGreeting
public class BasicGreetingService implements GreetingService, Serializable {

    @Override
    public String greet(String name) {
        return "Hello, " + name + "!";
    }

    @Override
    public String getServiceName() {
        return "BasicGreetingService";
    }
}
