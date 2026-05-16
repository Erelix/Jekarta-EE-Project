# Asynchronous Communication in Java EE

## Overview

Asynchronous communication allows long-running operations to execute independently without blocking the caller. This document explains how to properly implement and communicate with asynchronous components in Java EE/Jakarta EE.

## Key Questions Answered

### 1. Can an asynchronous component join the caller's started transaction?

**SHORT ANSWER: NO**

#### Why NOT?

Transactions in Java EE are **thread-bound**. Each thread has its own transaction context, and transactions cannot be shared across threads.

When you call an `@Asynchronous` method:
- **Caller's thread:** Executes in Thread A with Transaction X
- **Async method's thread:** Executes in Thread B with its own Transaction Y

These are completely independent transactions.

#### What Happens If You Try?

```java
// Caller thread - Transaction X is active
@Transactional
public void caller() {
    // Transaction X is in progress
    asyncService.asyncMethod();  // Returns immediately
    // Transaction X continues...
    // Eventually commits or rolls back
}

// Meanwhile, async method runs in different thread
@Asynchronous
@Transactional
public Future<Result> asyncMethod() {
    // This is Transaction Y - DIFFERENT transaction
    // Changes here are independent from Transaction X
    // If asyncMethod() fails, Transaction X still commits
    // If asyncMethod() commits, Transaction X might still roll back
}
```

#### Implications

1. **No Transactional Coupling:** If the caller's transaction rolls back, the async method's changes won't roll back with it
2. **No Isolation:** The async method cannot see uncommitted changes from the caller
3. **Separate Failure Domains:** One transaction's failure doesn't affect the other

#### Code Demonstration

```java
@Stateless
public class OrderService {
    @Inject private EntityManager em;
    @Inject private AsyncComputationService asyncService;
    
    @Transactional
    public void processOrder(Order order) {
        // Transaction A: Save order
        em.persist(order);
        em.flush();
        
        // Call async operation
        Future<ReportResult> reportFuture = asyncService.generateReport(order.getId());
        
        // Transaction A commits here
        // But async method hasn't started yet!
    }
}

@Stateless
public class AsyncComputationService {
    @Inject private EntityManager em;
    
    @Asynchronous
    @Transactional
    public Future<ReportResult> generateReport(Long orderId) {
        // Transaction B: Load order (freshly persisted by Transaction A)
        Order order = em.find(Order.class, orderId);
        // ... do work ...
        return new AsyncResult<>(result);
        // Transaction B commits independently
    }
}
```

**Timeline:**
1. Caller's Transaction A starts
2. Order persisted in Transaction A
3. Async method called → returns immediately
4. Caller's Transaction A commits
5. Async method's Transaction B eventually starts (in different thread)
6. Async method's Transaction B commits separately

---

### 2. Can an asynchronous component use a @RequestScoped EntityManager?

**SHORT ANSWER: NO**

#### Why NOT?

`@RequestScoped` beans are tied to the HTTP request thread. When an `@Asynchronous` method runs in a different thread:

1. There is **no request context** in the async thread
2. The `@RequestScoped` EntityManager is **not available**
3. Attempting to use it will cause **NullPointerException or IllegalStateException**

#### How Scopes Work

```
HTTP Request Thread
├── @RequestScoped → Available (tied to this request)
├── @SessionScoped → Available (session is shared)
├── @ApplicationScoped → Available (always available)
└── @Dependent → Available (created for this thread)

Async Thread (from @Asynchronous)
├── @RequestScoped → NOT available (no request context)
├── @SessionScoped → Might be available (depends on configuration)
├── @ApplicationScoped → Available (always available)
└── @Dependent → Available (created for this thread)
```

#### What Happens If You Try?

```java
@RequestScoped
public class RequestScopedService {
    @Inject private EntityManager em;  // Scoped to HTTP request thread
}

@Stateless
public class AsyncService {
    @Inject private RequestScopedService service;  // WRONG!
    
    @Asynchronous
    public Future<Result> asyncMethod() {
        // This runs in a different thread!
        service.doSomething();  // NullPointerException - 'em' is null
        return new AsyncResult<>(null);
    }
}
```

#### Solutions

**Option 1: Use @Dependent EntityManager**
```java
@Stateless
public class AsyncService {
    @Inject
    private EntityManager em;  // @Dependent - fresh instance per thread
    
    @Asynchronous
    public Future<Result> asyncMethod() {
        // This EntityManager is valid in async thread
        return new AsyncResult<>(processData());
    }
}
```

**Option 2: Create New EntityManager in Async Method**
```java
@Stateless
public class AsyncService {
    @PersistenceUnit(name = "myPU")
    private EntityManagerFactory emf;
    
    @Asynchronous
    public Future<Result> asyncMethod() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // Do work...
            em.getTransaction().commit();
            return new AsyncResult<>(result);
        } finally {
            em.close();
        }
    }
}
```

**Option 3: Use @ApplicationScoped Service**
```java
@ApplicationScoped  // Single instance, thread-safe
public class SharedDataService {
    @Inject private EntityManager em;
    
    public Result getData(Long id) {
        return em.find(Result.class, id);
    }
}

@Stateless
public class AsyncService {
    @Inject private SharedDataService dataService;  // Safe to use in async
    
    @Asynchronous
    public Future<Result> asyncMethod() {
        Result result = dataService.getData(1L);
        return new AsyncResult<>(result);
    }
}
```

---

### 3. Proper Communication Pattern with Async Components

#### The Correct Pattern: Fire and Forget with Future

```java
// 1. START: Call async method, get Future immediately
Future<Result> future = asyncService.performCalculation();

// 2. CONTINUE: Caller can do other work
doOtherStuff();

// 3. POLL/CHECK: Periodically check if done
if (future.isDone()) {
    Result result = future.get();
    processResult(result);
}

// OR WAIT: Block until result is ready
Result result = future.get();  // Blocks if not done
processResult(result);

// OR WITH TIMEOUT: Wait up to N milliseconds
try {
    Result result = future.get(5, TimeUnit.SECONDS);
    processResult(result);
} catch (TimeoutException e) {
    logger.warn("Async operation timed out");
}
```

#### What NOT to Do

```java
// WRONG 1: Synchronous call (defeats the purpose)
Result result = asyncService.performCalculation().get();  // Blocks!

// WRONG 2: Fire and forget without checking result
asyncService.performCalculation();  // Never check if it succeeded

// WRONG 3: Assume it's done immediately
Future<Result> future = asyncService.performCalculation();
Result result = future.get(1, TimeUnit.MILLISECONDS);  // Timeout!

// WRONG 4: Try to use caller's transaction context
@Transactional
public void caller() {
    asyncService.asyncMethod();  // Different transaction!
}
```

#### Implementation in Our Demo

The demo shows the proper pattern:

```java
// 1. Start async task
@RequestScoped
public String startAsyncTask() {
    Future<ComputationResult> future = asyncService.performLongCalculation(
        taskName, 
        durationSeconds
    );
    // Store reference and return immediately
    activeTasks.add(new AsyncTaskTracker(taskId, taskName, future));
    return null;  // No blocking
}

// 2. Poll task status
public String pollTask() {
    if (tracker.getFuture().isDone()) {
        // Check if done without blocking
        ComputationResult result = tracker.getFuture().get();
        // Process result
    } else {
        // Still running - show elapsed time
        message = "Still running after " + tracker.getElapsedSeconds() + "s";
    }
    return null;
}

// 3. Retrieve result when ready
// Result appears in UI once task completes
```

---

## Implementation Details

### AsyncComputationService Structure

```
┌─ AsyncComputationService (EJB Stateless)
│
├─ performLongCalculation()
│  └─ @Asynchronous → runs in separate thread
│     └─ Returns Future<ComputationResult>
│
├─ performCalculationWithDbAccess()
│  └─ @Asynchronous → separate thread with own EntityManager context
│     └─ Returns Future<ComputationResult>
│
└─ ComputationResult (data container)
   ├─ taskName
   ├─ status (COMPLETED, INTERRUPTED, etc)
   ├─ result (output message)
   ├─ durationSeconds
   └─ timestamp
```

### AsyncCommunicationBean Structure

```
┌─ AsyncCommunicationBean (Session Scoped Backing Bean)
│
├─ startAsyncTask() → calls asyncService.performLongCalculation()
├─ startAsyncTaskWithDb() → calls asyncService.performCalculationWithDbAccess()
├─ pollTask() → checks future.isDone() and gets result
├─ cancelTask() → calls future.cancel()
├─ refreshAllTasks() → updates all task statuses
│
└─ AsyncTaskTracker (inner class)
   ├─ id: unique task identifier
   ├─ future: Future<ComputationResult>
   ├─ startTime: when task was created
   ├─ elapsedSeconds: how long it's been running
   └─ status: QUEUED/RUNNING/DONE/CANCELLED
```

---

## Testing Scenarios

### Test 1: Basic Async Execution
```
1. Start task with 5 second duration
2. Verify it returns immediately (UI updates quickly)
3. Check elapsed time increases (background execution)
4. Poll after 6 seconds
5. Verify task is DONE and result is available
```

### Test 2: Multiple Concurrent Tasks
```
1. Start Task A (10 seconds)
2. Start Task B (5 seconds)  
3. Poll Task B at 6 seconds → DONE
4. Poll Task A at 6 seconds → still RUNNING (4s elapsed)
5. Both should complete independently
```

### Test 3: Task Cancellation
```
1. Start task with 30 second duration
2. Immediately poll and verify RUNNING
3. Click Cancel
4. Verify status changes to CANCELLED
5. No result should appear
```

### Test 4: EntityManager in Async Context
```
1. Start "Task with DB Access"
2. Verify in result message that EntityManager was available
3. Demonstrates that new EntityManager context exists in async thread
```

---

## Key Takeaways

| Question | Answer | Why |
|----------|--------|-----|
| Can async join caller's transaction? | **NO** | Transactions are thread-bound; different threads have different transaction contexts |
| Can async use @RequestScoped EM? | **NO** | @RequestScoped is tied to HTTP request thread; async runs in different thread with no request context |
| How to properly communicate? | Use Future + polling | Fire-and-forget pattern: call returns immediately, check status later |

---

## Common Mistakes and Fixes

### Mistake 1: Blocking Call
```java
// WRONG - Blocks caller
Result result = service.asyncMethod().get();

// CORRECT - Non-blocking
Future<Result> future = service.asyncMethod();
if (future.isDone()) {
    Result result = future.get();
}
```

### Mistake 2: Ignoring Results
```java
// WRONG - Fire and forget
service.asyncMethod();

// CORRECT - Store reference
Future<Result> future = service.asyncMethod();
// Track and check later
```

### Mistake 3: Wrong EntityManager Scope
```java
// WRONG - RequestScoped EM
@Inject @RequestScoped EntityManager em;

@Asynchronous
public void asyncMethod() {
    em.find(...);  // NullPointerException!
}

// CORRECT - Dependent EM (or create new)
@Inject EntityManager em;  // @Dependent by default

@Asynchronous  
public void asyncMethod() {
    em.find(...);  // Works - new context in async thread
}
```

---

## Related Concepts

- **@Stateless EJB**: Thread-safe, pooled, good for async services
- **@Transactional**: Container-managed transactions (thread-local)
- **Future<T>**: Standard Java interface for async results
- **AsyncResult<T>**: EJB wrapper for returning results from @Asynchronous methods
- **ThreadPoolExecutor**: Underlying mechanism for @Asynchronous thread pool

## References

- Jakarta EE Specification: Asynchronous Method Invocation (AMI)
- EJB 4.0 Specification
- Java Concurrency in Practice: Chapters on Thread Safety and Transactions
