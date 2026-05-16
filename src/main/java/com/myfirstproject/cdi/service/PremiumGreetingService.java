package com.myfirstproject.cdi.service;

import java.io.Serializable;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Specializes;

/**
 * Premium implementation that extends another implementation and specializes it.
 * 
 * Demonstrates: CDI @Specializes - specialization of an existing bean
 * 
 * When @Specializes is used:
 * - This bean replaces its parent bean in dependency injection
 * - It must extend or implement the same interface as the parent
 * - When activated in beans.xml, this bean's methods are called instead of the parent
 * - Only one specialized bean per parent is allowed
 * 
 * Uses @Dependent scope to support CDI Decorator usage (decorators require @Dependent beans).
 */
@Dependent
@Specializes
// @Alternative
@DefaultGreeting
public class PremiumGreetingService extends BasicGreetingService implements Serializable {

    @Override
    public String greet(String name) {
        String baseGreeting = super.greet(name);
        return baseGreeting + " [Premium Member - VIP]";
    }

    @Override
    public String getServiceName() {
        return "PremiumGreetingService (specializes BasicGreetingService)";
    }
}
