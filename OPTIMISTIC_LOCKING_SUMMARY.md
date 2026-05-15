# Optimistic Locking Implementation Summary

## What Was Implemented

### 1. **@Version Annotation Added to All Entities**
   - **Student.java**: Added `@Version private Long version;`
   - **Subject.java**: Added `@Version private Long version;`
   - **Group.java**: Added `@Version private Long version;`

   The version field is automatically:
   - Managed by Hibernate (developers don't modify it)
   - Incremented on every UPDATE
   - Stored in a database column (e.g., `version BIGINT`)
   - Checked in UPDATE WHERE clause for concurrency control

### 2. **Service Class with Retry Logic**
   **File**: `StudentOptimisticLockService.java`
   - Implements automatic retry mechanism (up to 3 attempts)
   - Handles OptimisticLockException properly:
     1. Catches exception
     2. Rolls back transaction (done automatically by JPA)
     3. Clears persistence context (`em.clear()`)
     4. Reloads fresh data from database
     5. Retries operation
   - Throws custom exception after max retries exhausted

### 3. **Demo/Test Classes**
   **File**: `OptimisticLockingDemo.java`
   - Shows how OptimisticLockException occurs
   - Demonstrates transaction rollback behavior
   - Shows EntityManager remains usable after exception
   - Includes comprehensive explanation methods

### 4. **Interactive Web Demo**
   **Files**:
   - `optimisticLockingDemo.xhtml` - JSF page for interactive demonstration
   - `OptimisticLockingDemoBean.java` - Session-scoped backing bean

   **Demo Steps**:
   1. Load a student (see version number)
   2. Simulate another user updating the same student
   3. Attempt update with stale version → OptimisticLockException
   4. Show proper recovery: clear cache, reload fresh data, retry

### 5. **Documentation**
   **File**: `OPTIMISTIC_LOCKING.md`
   - Complete explanation of how optimistic locking works
   - Database schema example
   - Visual flow diagrams
   - Best practices and common mistakes
   - Testing examples

## Key Concepts Demonstrated

### What Happens with OptimisticLockException

#### Transaction State
```
Before exception: em.getTransaction().isActive() = true
After exception:  em.getTransaction().isActive() = true (but marked for rollback)
After em.getTransaction().rollback(): = false
```
- Transaction is **automatically marked for rollback** by Hibernate
- Cannot commit further in the same transaction
- Must call rollback to clean up state

#### EntityManager State
```
em.close() → NOT called automatically
em remaining open → YES, can be reused
em.find() still works → YES
em.merge() throws exception → YES (until clear)
```
- EntityManager **remains open** (not closed)
- Persistence context contains stale entities
- Must call `em.clear()` before using it again

#### Entity State
```
Database: version=2 (after User 2's update)
Entity in cache: version=1 (stale, User 1's loaded version)
→ Must reload with em.find() after em.clear()
```

#### Database State
```
Before exception: No partial updates
After exception: No changes committed
Database consistency: Maintained
```

## Proper Error Handling Pattern

```java
while (retryCount < MAX_RETRIES) {
    try {
        em.getTransaction().begin();
        
        // Always load fresh data
        Entity entity = em.find(Entity.class, id);
        
        // Modify
        entity.setSomeField(newValue);
        
        // Flush triggers version check
        em.merge(entity);
        em.flush();
        em.getTransaction().commit();
        
        return entity;  // Success
        
    } catch (OptimisticLockException e) {
        retryCount++;
        
        // 1. Rollback
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        
        // 2. Clear stale entities
        em.clear();
        
        // 3. Loop continues → goto try block → loads fresh data
        if (retryCount >= MAX_RETRIES) {
            throw new MaxRetriesExceededException("...", e);
        }
    }
}
```

## SQL-Level Behavior

### Normal Scenario (No Conflict)
```sql
-- User 1 loads
SELECT id, first_name, email, version FROM student WHERE id=1;
-- Result: version=5

-- User 2 loads
SELECT id, first_name, email, version FROM student WHERE id=1;
-- Result: version=5

-- User 2 updates and commits
UPDATE student SET email='user2@test.com', version=6 WHERE id=1 AND version=5;
-- Result: 1 row updated ✓

-- User 1 tries to update
UPDATE student SET email='user1@test.com', version=6 WHERE id=1 AND version=5;
-- Result: 0 rows updated (version mismatch) → OptimisticLockException
```

## How to Use in Your Application

### For Normal Updates
```java
@Inject
private StudentOptimisticLockService service;

// Simple use case
Student updated = service.updateStudentWithRetry(
    studentId, 
    "John", 
    "john@example.com"
);
```

### For Custom Logic
```java
@Transactional
public void complexUpdate(Long studentId) {
    int retries = 0;
    while (retries < 3) {
        try {
            em.getTransaction().begin();
            
            Student student = em.find(Student.class, studentId);
            // ... custom logic ...
            em.merge(student);
            em.flush();
            em.getTransaction().commit();
            
            return;
            
        } catch (OptimisticLockException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();
            retries++;
        }
    }
}
```

## Testing the Implementation

### Using JSF Demo Page
1. Navigate to: `http://localhost:8080/uzd_1/optimisticLockingDemo.xhtml`
2. Follow the steps to see OptimisticLockException in action
3. Observe how transaction is rolled back but EntityManager stays open
4. See proper recovery with fresh data reload

### Running Test Code
```java
OptimisticLockingDemo.demonstrateOptimisticLockException();
OptimisticLockingDemo.demonstrateProperErrorHandling();
OptimisticLockingDemo.explainOptimisticLocking();
```

## Files Modified/Created

### Modified Entities
- `src/main/java/com/myfirstproject/entity/Student.java`
- `src/main/java/com/myfirstproject/entity/Subject.java`
- `src/main/java/com/myfirstproject/entity/Group.java`

### New Java Classes
- `src/main/java/com/myfirstproject/service/StudentOptimisticLockService.java`
- `src/main/java/com/myfirstproject/demo/OptimisticLockingDemo.java`
- `src/main/java/com/myfirstproject/bean/OptimisticLockingDemoBean.java`

### New Web Resources
- `src/main/webapp/optimisticLockingDemo.xhtml`

### Documentation
- `OPTIMISTIC_LOCKING.md` (comprehensive guide)

## Key Takeaways

| Question | Answer |
|----------|--------|
| **What is @Version?** | Auto-managed field incremented on updates for concurrency control |
| **When is version checked?** | During flush/commit when UPDATE SQL executes |
| **What causes OptimisticLockException?** | UPDATE affects 0 rows due to version mismatch in WHERE clause |
| **What happens to transaction?** | Automatically marked for rollback, cannot commit further |
| **What happens to EntityManager?** | Remains open, can be cleared and reused |
| **How to save after exception?** | Call em.clear(), reload fresh data, retry operation |
| **Is version ever decremented?** | No, always increments on UPDATE |
| **Can I modify @Version field?** | No, Hibernate manages it automatically |
| **When should I use it?** | Read-heavy apps with infrequent concurrent updates |
| **When should I use pessimistic locking instead?** | High contention scenarios where conflicts are likely |
