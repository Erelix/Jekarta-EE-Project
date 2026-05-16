# CDI Glass-box Extensibility Demonstration

## Overview

Glass-box extensibility refers to the ability to extend and customize the behavior of a system at specific extension points while being aware of the internal mechanisms. This demonstration showcases four key CDI (Context and Dependency Injection) features that enable glass-box extensibility in Jakarta EE applications.

## Important: Bean Scope Note

All `GreetingService` implementations use `@Dependent` scope (instead of `@ApplicationScoped`) to support CDI Decorators, which have a CDI specification requirement: **decorated beans must be @Dependent scoped**. This is essential for the decorator feature to work correctly. In production, you would create separate beans with appropriate scopes and decorate only the @Dependent ones.

## Four Core CDI Extensibility Features

### 1. CDI Alternatives (@Alternative) - 0.1

**What it does:**
- Provides alternative implementations of a bean interface
- Only the selected alternative is used when explicitly enabled
- The non-selected implementation is effectively disabled

**How it works:**
```java
// Default implementation
@Dependent
public class BasicGreetingService implements GreetingService { }

// Alternative implementation
@Dependent
@Alternative
public class EnhancedGreetingService implements GreetingService { }
```

**Configuration in beans.xml:**
```xml
<alternatives>
    <class>com.myfirstproject.cdi.service.EnhancedGreetingService</class>
</alternatives>
```

**Key characteristics:**
- Only one alternative can be selected per interface
- Alternative MUST be explicitly enabled in beans.xml
- If no alternative is selected, the default bean is used
- Both implementations must implement the same interface

**Use cases:**
- Different implementations for different environments (test, production)
- Feature toggles (enable/disable features dynamically)
- Plugin architecture with swappable implementations
- Region-specific or locale-specific implementations

**Demo behaviors:**
- Without selection: `BasicGreetingService` returns "Hello, John Doe!"
- With selection: `EnhancedGreetingService` returns "Greetings, esteemed John Doe! Welcome to our service."

---

### 2. CDI Specialization (@Specializes) - 0.1

**What it does:**
- A specialized bean automatically replaces its parent bean
- The replacement is transparent - all injections get the specialized version
- The specialized bean extends or implements the parent bean

**How it works:**
```java
// Parent bean
@ApplicationScoped
public class BasicGreetingService implements GreetingService {
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}

// Specialized bean that extends the parent
@ApplicationScoped
@Specializes
public class PremiumGreetingService extends BasicGreetingService {
    @Override
    public String greet(String name) {
        String baseGreeting = super.greet(name);
        return baseGreeting + " [Premium Member - VIP Treatment Applied]";
    }
}
```

**Configuration in beans.xml:**
```xml
<alternatives>
    <class>com.myfirstproject.cdi.service.PremiumGreetingService</class>
</alternatives>
```

**Key characteristics:**
- Must extend or implement the parent bean
- Automatically replaces parent when enabled
- Can call parent methods using super()
- Only one specialized bean per parent is allowed
- Specialization is also a form of alternative

**Difference from @Alternative:**
- @Alternative requires explicit activation
- @Specializes automatically replaces parent when made eligible
- @Specializes represents "is-a" relationship (inheritance)
- @Alternative represents "different implementation" (composition)

**Use cases:**
- Creating premium versions of a service
- Extending default behavior with additional features
- Region-specific or role-specific specializations
- Building on top of existing beans without modification

**Demo behavior:**
- Without specialization: "Hello, John Doe!"
- With specialization: "Hello, John Doe! [Premium Member - VIP Treatment Applied]"

---

### 3. CDI Interceptor (@Interceptor) - 0.1

**What it does:**
- Wraps method calls to add cross-cutting concerns
- Enables logging, timing, security checks, etc. without modifying business logic
- Applied transparently based on interceptor binding annotations

**How it works:**

**Step 1: Define interceptor binding annotation**
```java
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logged {
    // Marker interface
}
```

**Step 2: Define the interceptor**
```java
@Logged
@Interceptor
public class LoggingInterceptor implements Serializable {
    @AroundInvoke
    public Object logMethodInvocation(InvocationContext ctx) throws Exception {
        System.out.println(">>> Entering " + ctx.getMethod().getName());
        Object result = ctx.proceed();  // Call the actual method
        System.out.println("<<< Exiting " + ctx.getMethod().getName());
        return result;
    }
}
```

**Step 3: Use the binding on beans**
```java
@Dependent
@Logged
public class LoggedGreetingService implements GreetingService { }
```

**Step 4: Enable in beans.xml**
```xml
<interceptors>
    <class>com.myfirstproject.cdi.interceptor.LoggingInterceptor</class>
</interceptors>
```

**Key characteristics:**
- Requires interceptor binding annotation
- Must be explicitly enabled in beans.xml
- Wraps method invocations (before and after)
- Access to method name, parameters, and return value
- Can handle or log exceptions
- Order matters when multiple interceptors are enabled

**InvocationContext methods:**
- `ctx.proceed()` - Execute the actual method
- `ctx.getMethod()` - Get method being called
- `ctx.getTarget()` - Get bean instance
- `ctx.getParameters()` - Get method parameters
- `ctx.setParameters(...)` - Modify parameters

**Use cases:**
- Logging method calls and results
- Performance monitoring and timing
- Security checks and authorization
- Transaction management
- Caching strategies
- Input validation

**Demo behavior:**
The LoggingInterceptor logs:
- Method entry with parameters
- Method exit with result and execution time
- Any exceptions that occur

Check the server console for output like:
```
>>> INTERCEPTOR LOG: Entering GreetingService.greet(John Doe)
<<< INTERCEPTOR LOG: Exiting GreetingService.greet - Result: Hello, John Doe! - Time: 105ms
```

---

### 4. CDI Decorator (@Decorator) - 0.1

**What it does:**
- Decorates beans to add functionality using the Decorator design pattern
- Wraps the original bean and delegates to it while adding behavior
- Applied transparently to all injections of the decorated type

**CDI Decorator Requirement (Important):**
- The decorated bean MUST be `@Dependent` scoped (CDI specification requirement)
- The decorator itself must also be `@Dependent` scoped
- This is why all `GreetingService` implementations use `@Dependent` instead of `@ApplicationScoped`

**How it works:**
```java
@Dependent
@Decorator
public class GreetingDecorator implements GreetingService {
    @Inject
    @Delegate
    private GreetingService delegate;  // Must be @Dependent scoped

    @Override
    public String greet(String name) {
        // Add extra behavior
        String originalGreeting = delegate.greet(name);
        String timestamp = java.time.LocalDateTime.now().toString();
        
        // Return enhanced result
        return originalGreeting + " [Decorated | Time: " + timestamp + "]";
    }

    @Override
    public String getServiceName() {
        return "GreetingDecorator wrapping [" + delegate.getServiceName() + "]";
    }
}
```

**Configuration in beans.xml:**
```xml
<decorators>
    <class>com.myfirstproject.cdi.decorator.GreetingDecorator</class>
</decorators>
```

**Key characteristics:**
- Must implement the same interface as the decorated bean
- Uses `@Delegate` to inject the original bean
- Can wrap or intercept any bean implementing that interface
- Applied transparently - all injections get wrapped
- Can add behavior before/after delegation
- Multiple decorators can be chained

**Decorator requirements:**
- MUST have exactly one `@Delegate` injection point
- The delegate type must be the decorated bean interface
- Must implement the same interface it decorates
- **Both decorator and decorated bean MUST be @Dependent scoped**

**Difference from Interceptor:**
- **Interceptor**: Wraps method calls on marked beans
- **Decorator**: Wraps entire bean instances at injection points
- **Interceptor**: Method-level control
- **Decorator**: Bean-level control
- **Interceptor**: More suited for cross-cutting concerns (logging, timing)
- **Decorator**: More suited for behavior augmentation (validation, enrichment)

**Use cases:**
- Adding validation or error handling
- Logging at bean level
- Caching results
- Transforming or enriching data
- Restricting access (access control wrapper)
- Adding metrics collection

**Demo behavior:**
Without decorator:
```
Greeting: Hello, John Doe!
Service: BasicGreetingService
```

With decorator:
```
Greeting: Hello, John Doe! [Decorated by GreetingDecorator | Service: BasicGreetingService | Time: 2024-01-15T10:30:45.123]
Service: GreetingDecorator wrapping [BasicGreetingService]
```

---

## File Structure

```
src/main/java/com/myfirstproject/
├── cdi/
│   ├── service/
│   │   ├── GreetingService.java           (Interface)
│   │   ├── BasicGreetingService.java      (Default implementation)
│   │   ├── EnhancedGreetingService.java   (@Alternative)
│   │   ├── PremiumGreetingService.java    (@Specializes)
│   │   └── LoggedGreetingService.java     (With @Logged binding)
│   ├── interceptor/
│   │   ├── Logged.java                    (InterceptorBinding annotation)
│   │   └── LoggingInterceptor.java        (@Interceptor implementation)
│   └── decorator/
│       └── GreetingDecorator.java         (@Decorator implementation)
└── bean/
    └── CDIExtensibilityDemoBean.java      (Demo controller)

src/main/webapp/
├── WEB-INF/
│   └── beans.xml                           (CDI configuration)
└── cdiExtensibilityDemo.xhtml             (JSF demo page)
```

---

## Configuration Examples

### 1. Basic Configuration (All Disabled)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee" version="4.0" bean-discovery-mode="all">
    <interceptors>
        <class>com.myfirstproject.transaction.TransactionalInterceptor</class>
    </interceptors>
</beans>
```
**Result**: BasicGreetingService is used, no decorator, no logging interceptor.

### 2. Enable Alternative
```xml
<alternatives>
    <class>com.myfirstproject.cdi.service.EnhancedGreetingService</class>
</alternatives>
```
**Result**: EnhancedGreetingService is used instead of BasicGreetingService.

### 3. Enable Specialization
```xml
<alternatives>
    <class>com.myfirstproject.cdi.service.PremiumGreetingService</class>
</alternatives>
```
**Result**: PremiumGreetingService replaces BasicGreetingService with VIP treatment.

### 4. Enable Interceptor
```xml
<interceptors>
    <class>com.myfirstproject.cdi.interceptor.LoggingInterceptor</class>
</interceptors>
```
**Result**: Method calls are logged to console/server logs.

### 5. Enable Decorator
```xml
<decorators>
    <class>com.myfirstproject.cdi.decorator.GreetingDecorator</class>
</decorators>
```
**Result**: All GreetingService calls return decorated results with timestamp.

### 6. Full Configuration (All Features Enabled)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee" version="4.0" bean-discovery-mode="all">
    <!-- Interceptors -->
    <interceptors>
        <class>com.myfirstproject.transaction.TransactionalInterceptor</class>
        <class>com.myfirstproject.cdi.interceptor.LoggingInterceptor</class>
    </interceptors>
    
    <!-- Decorators -->
    <decorators>
        <class>com.myfirstproject.cdi.decorator.GreetingDecorator</class>
    </decorators>
    
    <!-- Alternatives -->
    <alternatives>
        <!-- Try one of these: -->
        <class>com.myfirstproject.cdi.service.EnhancedGreetingService</class>
        <!-- <class>com.myfirstproject.cdi.service.PremiumGreetingService</class> -->
    </alternatives>
</beans>
```

---

## How to Use the Demo

1. **Start the application** - Deploy to your Jakarta EE server (GlassFish, Wildfly, Tomcat with Jakarta EE support)

2. **Navigate to the demo** - Go to `index.xhtml` and click "CDI Extensibility Demo"

3. **Try each demonstration:**
   - Click each demo button to see current behavior
   - Results depend on what's enabled in `beans.xml`

4. **Experiment with configuration:**
   - Edit `WEB-INF/beans.xml`
   - Uncomment/comment out features
   - Redeploy and see how behavior changes

5. **Check console for logs:**
   - If interceptor is enabled, check server console for logging output
   - Look for `>>> INTERCEPTOR LOG` and `<<< INTERCEPTOR LOG` messages

---

## Comparison Table

| Feature | Mechanism | Control Point | Scope | Configuration |
|---------|-----------|---------------|-------|---------------|
| **Alternative** | Swap implementation | Interface level | All injections of type | `<alternatives>` |
| **Specialization** | Extend & replace | Parent bean level | All injections of parent | `<alternatives>` |
| **Interceptor** | Wrap method calls | Method level | @Logged marked methods | `<interceptors>` |
| **Decorator** | Wrap bean instances | Bean level | All injections of interface | `<decorators>` |

---

## Key Takeaways

1. **@Alternative**: Choose different implementations based on needs
2. **@Specializes**: Extend a bean with enhanced behavior
3. **@Interceptor**: Add cross-cutting concerns (logging, timing, security)
4. **@Decorator**: Augment bean behavior transparently

These features work together to provide flexible, extensible architectures following established design patterns while maintaining clean separation of concerns.

---

## Testing the Demo

The demo bean provides methods to test each feature:
- `demonstrateAlternatives()` - Shows which implementation is active
- `demonstrateSpecialization()` - Shows specialized behavior
- `demonstrateInterceptors()` - Triggers interceptor logging
- `demonstrateDecorators()` - Shows decorated results
- `testCurrentConfiguration()` - Shows summary of all enabled features

Modify `beans.xml` and reload the page to see behavior changes.
