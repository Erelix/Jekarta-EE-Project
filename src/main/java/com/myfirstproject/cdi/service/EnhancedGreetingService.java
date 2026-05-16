package com.myfirstproject.cdi.service;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;

/**
 * Enhanced implementation of GreetingService marked as @Alternative.
 * This implementation is only used when explicitly selected in beans.xml.
 * 
 * Demonstrates: CDI @Alternative - alternative implementation selection
 * 
 * Uses @Dependent scope to support CDI Decorator usage (decorators require @Dependent beans).
 */
@Dependent
@Alternative
@DefaultGreeting
public class EnhancedGreetingService implements GreetingService, Serializable {

    @Override
    public String greet(String name) {
        return "Greetings, dear " + name + "! Welcome!.";
    }

    @Override
    public String getServiceName() {
        return "EnhancedGreetingService";
    }
}
