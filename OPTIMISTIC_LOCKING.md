# Optimistic Locking in JPA/Hibernate

## Overview
Optimistic locking is a concurrency control strategy that assumes conflicts are rare and allows multiple transactions to proceed without acquiring database locks. Version checks occur only at commit time.

## Implementation

### 1. Adding @Version to Entity
```java
@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;  // Hibernate auto-manages this field
    
    // ... rest of entity
}
```

The database schema automatically includes a version column:
```sql
CREATE TABLE student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version BIGINT NOT NULL DEFAULT 0,
    first_name VARCHAR(255) NOT NULL,
    -- ... other columns
);
```

## How Optimistic Locking Works

### Normal Flow (No Conflict)
```
User 1 SELECT id=1 (version=5)
User 2 SELECT id=1 (version=5)
User 1 UPDATE WHERE id=1 AND version=5 -> SET version=6 ✓
User 2 UPDATE WHERE id=1 AND version=5 -> 0 rows updated ✗ OptimisticLockException
```

### What Happens When OptimisticLockException is Thrown

#### Transaction State
- Transaction is **automatically rolled back** by Hibernate/JPA
- No changes committed to database
- Cannot commit or flush further in the same transaction

#### EntityManager State
- EntityManager **remains open and usable** (not closed)
- Must call `em.clear()` to clear the persistence context
- Can begin a new transaction immediately

#### Entity State
- The entity in persistence context is left unchanged
- Stale entity should NOT be used for next attempt
- Must fetch fresh data from database

## Example: Demonstrating OptimisticLockException

### Scenario: Two Users Update Same Student
```java
// User 1 loads student (version=1)
Student student1 = em.find(Student.class, 1L);
System.out.println("Version: " + student1.getVersion()); // 1

// User 2 loads same student
Student student2 = em.find(Student.class, 1L);

// User 2 commits update
em.getTransaction().begin();
student2.setEmail("user2@example.com");
em.merge(student2);
em.getTransaction().commit();
// Database: version=2

// User 1 tries to update (still has version=1)
em.getTransaction().begin();
student1.setEmail("user1@example.com");
em.merge(student1);
em.getTransaction().commit();  // OptimisticLockException thrown!
```

## Proper Error Handling & Recovery

### Key Steps
1. **Catch** OptimisticLockException
2. **Rollback** active transaction
3. **Clear** EntityManager persistence context
4. **Reload** fresh data from database
5. **Retry** operation with fresh data

### Implementation Pattern
```java
public Student updateWithRetry(Long studentId, String newEmail) {
    int retries = 0;
    final int MAX_RETRIES = 3;

    while (retries < MAX_RETRIES) {
        try {
            em.getTransaction().begin();
            
            // Always load fresh data
            Student student = em.find(Student.class, studentId);
            System.out.println("Current version: " + student.getVersion());
            
            student.setEmail(newEmail);
            em.merge(student);
            em.flush();  // This triggers version check
            em.getTransaction().commit();
            
            return student;
            
        } catch (OptimisticLockException e) {
            retries++;
            System.out.println("Conflict detected, retry " + retries);
            
            // Rollback failed transaction
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
            // Clear stale entities
            em.clear();
            
            if (retries >= MAX_RETRIES) {
                throw new RuntimeException("Failed after " + MAX_RETRIES + " retries");
            }
        }
    }
    return null;
}
```

## Important Concepts

### Difference from Pessimistic Locking
| Aspect | Optimistic | Pessimistic |
|--------|-----------|-------------|
| Lock Type | No database lock | Database lock acquired |
| Performance | Better (no locks) | Slower (waiting for locks) |
| Conflicts | Detected at commit | Prevented by lock |
| Use Case | Few updates expected | Frequent updates expected |

### When to Use Optimistic Locking
- ✓ Read-heavy applications
- ✓ Infrequent concurrent updates to same entity
- ✓ Long-running transactions acceptable
- ✗ High contention scenarios (use pessimistic locking instead)

### Common Mistakes
1. **Not clearing persistence context** - stale data persists
2. **Not retrying** - silently accepting first failure
3. **Not handling transaction state** - trying to use rolled-back transaction
4. **Unlimited retries** - can cause infinite loops

## Testing OptimisticLockException

```java
// Simulate concurrent updates with multiple EntityManagers
EntityManagerFactory emf = Persistence.createEntityManagerFactory("pu");

EntityManager em1 = emf.createEntityManager();
EntityManager em2 = emf.createEntityManager();

// Both load same student
em1.getTransaction().begin();
Student s1 = em1.find(Student.class, 1L);

em2.getTransaction().begin();
Student s2 = em2.find(Student.class, 1L);

// em2 updates and commits first
s2.setEmail("em2@test.com");
em2.merge(s2);
em2.getTransaction().commit();

// em1 tries to update with stale version -> OptimisticLockException
try {
    s1.setEmail("em1@test.com");
    em1.merge(s1);
    em1.getTransaction().commit();
} catch (OptimisticLockException e) {
    System.out.println("Expected: OptimisticLockException caught");
}
```

## Summary

| Question | Answer |
|----------|--------|
| **What happens to transaction?** | Automatically rolled back, cannot commit further |
| **What happens to EntityManager?** | Remains open, can start new transaction |
| **How to save after exception?** | Clear em.clear(), reload fresh data, retry operation |
| **Version field changes?** | Auto-incremented on UPDATE by Hibernate |
| **When is version checked?** | During flush/commit when UPDATE SQL executes |
