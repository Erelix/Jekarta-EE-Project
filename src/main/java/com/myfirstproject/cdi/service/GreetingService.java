package com.myfirstproject.cdi.service;

/**
 * Core service interface for CDI extensibility demonstrations.
 * This interface is used to demonstrate Alternatives, Specialization, and Decorators.
 */
public interface GreetingService {
    
    String greet(String name);
    
    String getServiceName();
}
