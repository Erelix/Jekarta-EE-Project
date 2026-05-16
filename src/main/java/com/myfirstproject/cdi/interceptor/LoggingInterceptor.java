package com.myfirstproject.cdi.interceptor;

import java.io.Serializable;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Logging interceptor that logs method invocations.
 * 
 * Demonstrates: CDI Interceptor implementation
 * 
 * This interceptor:
 * - Logs before method execution
 * - Logs after method execution with result
 * - Logs execution time
 * - Logs any exceptions
 * - Must be enabled in beans.xml with <interceptors>
 */
@Logged
@Interceptor
public class LoggingInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;

    @AroundInvoke
    public Object logMethodInvocation(InvocationContext ctx) throws Exception {
        long startTime = System.currentTimeMillis();
        String methodName = ctx.getMethod().getName();
        Object target = ctx.getTarget();
        
        String className = target != null ? target.getClass().getSimpleName() : "Unknown";
        
        // Build argument string
        StringBuilder argsStr = new StringBuilder();
        if (ctx.getParameters() != null && ctx.getParameters().length > 0) {
            for (int i = 0; i < ctx.getParameters().length; i++) {
                if (i > 0) argsStr.append(", ");
                Object arg = ctx.getParameters()[i];
                argsStr.append(arg != null ? arg.toString() : "null");
            }
        }
        
        System.out.println(">>> INTERCEPTOR LOG: Entering " + className + "." + methodName 
            + "(" + argsStr.toString() + ")");
        
        try {
            Object result = ctx.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("<<< INTERCEPTOR LOG: Exiting " + className + "." + methodName 
                + " - Result: " + (result != null ? result.toString() : "null") 
                + " - Time: " + executionTime + "ms");
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("!!! INTERCEPTOR LOG: Exception in " + className + "." + methodName 
                + " - Exception: " + e.getClass().getSimpleName() 
                + " - Time: " + executionTime + "ms");
            throw e;
        }
    }
}
