package com.myfirstproject.bean;

import java.io.Serializable;

import com.myfirstproject.cdi.service.DefaultGreeting;
import com.myfirstproject.cdi.service.GreetingService;
import com.myfirstproject.cdi.service.LoggedGreetingService;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Demo bean for CDI Glass-box Extensibility demonstrations.
 * 
 * This bean demonstrates:
 * 1. CDI Alternatives (@Alternative) - alternative implementations
 * 2. CDI Specialization (@Specializes) - bean specialization
 * 3. CDI Interceptors (@Interceptor) - logging/cross-cutting concerns
 * 4. CDI Decorators (@Decorator) - behavior augmentation
 * 
 * The actual behavior depends on what's enabled in beans.xml.
 */
@Named("cdiExtensibilityDemo")
@SessionScoped
public class CDIExtensibilityDemoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    @DefaultGreeting
    private GreetingService greetingService;

    @Inject
    private LoggedGreetingService loggedGreetingService;

    private String testName = "Ben Frank";
    private String testResult = "";
    private String explanationText = "";
    private String currentConfiguration = "";

    /**
     * 1. DEMONSTRATION 1: Alternatives (@Alternative)
     * 
     * Demonstrates how @Alternative allows providing alternative implementations
     * that are only used when explicitly selected in beans.xml.
     * 
     * Steps:
     * a) Without selection in beans.xml: BasicGreetingService is used
     * b) With selection in beans.xml: EnhancedGreetingService is used
     * 
     * The <alternatives> section in beans.xml determines which implementation is active.
     */
    public String demonstrateAlternatives() {
        reset();
        
        try {
            String serviceName = greetingService.getServiceName();
            String greeting = greetingService.greet(testName);
            
            testResult = greeting;
            
            if (serviceName.contains("Enhanced")) {
                currentConfiguration = "EnhancedGreetingService selected in beans.xml";
                explanationText = "@Alternative is ACTIVE: EnhancedGreetingService is used.";
            } else if (serviceName.contains("Basic")) {
                currentConfiguration = "BasicGreetingService (no alternative selected)";
                explanationText = "@Alternative is INACTIVE: BasicGreetingService is used.";
            } else {
                currentConfiguration = "Unknown service: " + serviceName;
                explanationText = "Service name: " + serviceName;
            }
            
            return null;
        } catch (Exception e) {
            testResult = "ERROR: " + e.getMessage();
            explanationText = "Exception occurred: " + e.getClass().getSimpleName();
            return null;
        }
    }

    /**
     * 2. DEMONSTRATION 2: Specialization (@Specializes)
     * 
     * Demonstrates how @Specializes allows a bean to replace another bean.
     * When @Specializes is enabled, it transparently replaces the parent bean
     * in all dependency injection points.
     * 
     * Steps:
     * a) Without PremiumGreetingService active: BasicGreetingService is used
     * b) When PremiumGreetingService is enabled: it replaces BasicGreetingService
     * 
     * Key difference from @Alternative:
     * - @Specializes automatically replaces the parent without explicit selection
     * - @Alternative requires explicit selection in <alternatives>
     */
    public String demonstrateSpecialization() {
        reset();
        
        try {
            String serviceName = greetingService.getServiceName();
            String greeting = greetingService.greet(testName);
            
            testResult = greeting;
            
            if (serviceName.contains("Premium")) {
                currentConfiguration = "PremiumGreetingService (with @Specializes)";
                explanationText = "@Specializes is ACTIVE: PremiumGreetingService is replacing " +
                    "BasicGreetingService. Replaces its parent bean. ";
            } else if (serviceName.contains("Basic")) {
                currentConfiguration = "BasicGreetingService (no specialization)";
                explanationText = "@Specializes is INACTIVE: BasicGreetingService is being used because " +
                    "PremiumGreetingService is not activated.";
            } else {
                currentConfiguration = "Unexpected service: " + serviceName;
                explanationText = "Service name: " + serviceName;
            }
            
            return null;
        } catch (Exception e) {
            testResult = "ERROR: " + e.getMessage();
            explanationText = "Exception occurred: " + e.getClass().getSimpleName();
            return null;
        }
    }

    /**
     * 3. DEMONSTRATION 3: Interceptors (@Interceptor)
     * 
     * Demonstrates how interceptors enable cross-cutting concerns like logging,
     * timing, security checks, etc. without modifying business logic.
     * 
     * The LoggingInterceptor:
     * - Logs method entry with parameters
     * - Logs method exit with result
     * - Logs execution time
     * - Logs exceptions
     * - Is enabled/disabled in beans.xml <interceptors>
     * 
     * How it works:
     * 1. LoggedGreetingService has @Logged annotation (interceptor binding)
     * 2. LoggingInterceptor has @Logged and @Interceptor annotations
     * 3. When interceptor is enabled in beans.xml, it wraps method calls
     * 4. Check console/logs for interceptor output
     */
    public String demonstrateInterceptors() {
        reset();
        
        try {
            System.out.println("\n========== INTERCEPTOR DEMONSTRATION START ==========");
            System.out.println("If interceptor is ENABLED in beans.xml, you will see logs:");
            System.out.println("  >>> INTERCEPTOR LOG: Entering ...");
            System.out.println("  <<< INTERCEPTOR LOG: Exiting ...");
            System.out.println("If interceptor is DISABLED, you won't see these logs.");
            System.out.println("===================================================\n");
            // Trigger the @Logged service so the LoggingInterceptor can run
            String serviceName = loggedGreetingService.getServiceName();
            String greeting = loggedGreetingService.greet(testName);

            testResult = greeting;
            currentConfiguration = "LoggingInterceptor enabled in beans.xml (using LoggedGreetingService)";
            explanationText = "Invoked " + serviceName + ". Check server logs/console for interceptor output.";
            
            return null;
        } catch (Exception e) {
            testResult = "ERROR: " + e.getMessage();
            explanationText = "Exception occurred: " + e.getClass().getSimpleName();
            return null;
        }
    }

    /**
     * 4. DEMONSTRATION 4: Decorators (@Decorator)
     * 
     * Demonstrates how decorators add behavior to beans without using inheritance.
     * A decorator wraps the original bean and delegates to it while adding extra logic.
     * 
     * Key characteristics:
     * - Must implement the same interface as the decorated bean
     * - Uses @Delegate to inject the original bean
     * - Can add behavior before/after delegation
     * - Is enabled/disabled in beans.xml <decorators>
     * - Applied transparently to all injections of that type
     * 
     * The GreetingDecorator:
     * - Wraps any GreetingService implementation
     * - Adds timestamp information to greetings
     * - Adds service name information
     * - Works together with other features (alternatives, specialization)
     */
    public String demonstrateDecorators() {
        reset();
        
        try {
            String serviceName = greetingService.getServiceName();
            String greeting = greetingService.greet(testName);
            
            testResult = greeting;
            
            if (serviceName.contains("Decorated") || serviceName.contains("Decorator")) {
                currentConfiguration = "GreetingDecorator enabled in beans.xml";
                explanationText = "@Decorator is ACTIVE: GreetingDecorator is wrapping the base service. " +
                    "Adds timestamp and service information to the greeting. ";
            } else {
                currentConfiguration = "No decorator (GreetingDecorator disabled)";
                explanationText = "@Decorator is INACTIVE: The base service is returning directly.";
            }
            
            return null;
        } catch (Exception e) {
            testResult = "ERROR: " + e.getMessage();
            explanationText = "Exception occurred: " + e.getClass().getSimpleName();
            return null;
        }
    }

    /**
     * Test the service with all features combined
     */
    public String testCurrentConfiguration() {
        reset();
        
        try {
            String serviceName = greetingService.getServiceName();
            String greeting = greetingService.greet(testName);
            
            testResult = greeting;
            currentConfiguration = serviceName;
            
            StringBuilder explanation = new StringBuilder();
            explanation.append("Current configuration summary:\n");
            explanation.append("Service Implementation: ").append(serviceName).append("\n\n");
            
            if (serviceName.contains("Decorator")) {
                explanation.append("Decorator is ACTIVE\n");
            } else {
                explanation.append("Decorator is inactive\n");
            }
            
            if (serviceName.contains("Premium")) {
                explanation.append("Specialization is ACTIVE (PremiumGreetingService)\n");
            } else if (serviceName.contains("Enhanced")) {
                explanation.append("Alternative is ACTIVE (EnhancedGreetingService)\n");
            } else if (serviceName.contains("Logged")) {
                explanation.append("Using LoggedGreetingService with interceptor binding\n");
            } else {
                explanation.append("Using BasicGreetingService (default)\n");
            }
            
            explanation.append("\nNote: Check the server console/logs for any interceptor output.");
            explanationText = explanation.toString();
            
            return null;
        } catch (Exception e) {
            testResult = "ERROR: " + e.getMessage();
            explanationText = "Exception occurred: " + e.getClass().getSimpleName();
            return null;
        }
    }

    /**
     * Reset demo state
     */
    public String reset() {
        testResult = "";
        explanationText = "";
        currentConfiguration = "";
        testName = "John Doe";
        return null;
    }

    // Getters and Setters
    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getExplanationText() {
        return explanationText;
    }

    public void setExplanationText(String explanationText) {
        this.explanationText = explanationText;
    }

    public String getCurrentConfiguration() {
        return currentConfiguration;
    }

    public void setCurrentConfiguration(String currentConfiguration) {
        this.currentConfiguration = currentConfiguration;
    }

    public GreetingService getGreetingService() {
        return greetingService;
    }

    public void setGreetingService(GreetingService greetingService) {
        this.greetingService = greetingService;
    }
}
