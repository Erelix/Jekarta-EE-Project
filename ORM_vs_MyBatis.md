# ORM/JPA vs DataMapper/MyBatis Comparison

## Overview
This project demonstrates **two different approaches** to database access:
1. **ORM (Object-Relational Mapping)** using JPA/Hibernate
2. **DataMapper** using MyBatis

Both access the **same H2 database** with the same schema.

---

## Architecture Comparison

### JPA/Hibernate (ORM)
```
Bean → JPA DAO → EntityManager → Hibernate → Database
       (Business Logic uses managed entities)
```

### MyBatis (DataMapper)
```
Bean → MyBatis DAO → SqlSession → Mapper (XML/SQL) → Database
       (Business Logic uses POJOs + explicit SQL)
```

---

## Key Differences

| Aspect | JPA/Hibernate (ORM) | MyBatis (DataMapper) |
|--------|---------------------|----------------------|
| **SQL Generation** | Automatic | Manual (you write SQL) |
| **Entities** | Managed objects with annotations | Plain POJOs |
| **Configuration** | Java annotations + persistence.xml | XML mappers + mybatis-config.xml |
| **Relationships** | Automatic (lazy/eager loading) | Manual via `<association>` and `<collection>` |
| **Transactions** | `@Transactional` (declarative) | Manual commit/rollback |
| **Learning Curve** | Steeper (ORM concepts) | Gentler (SQL-centric) |
| **Control** | Less (Hibernate decides SQL) | Full (you write exact SQL) |
| **Performance Tuning** | Can be tricky (N+1 queries, lazy loading) | Direct control over queries |
| **Complex Queries** | JPQL/Criteria API can be verbose | SQL is natural |
| **Type Safety** | Compile-time checking with entities | Runtime via XML |

---

## When to Use Each

### Use JPA/Hibernate when:
✅ **Domain-Driven Design** - modeling complex business domains  
✅ **Standard CRUD operations** - simple database interactions  
✅ **Object-oriented approach** - Java objects represent business entities  
✅ **Portability** - switching databases (PostgreSQL → MySQL)  
✅ **Rapid development** - less boilerplate for basic operations  
✅ **Complex object graphs** - automatic relationship management  

**Example:** CRM system with Customer, Order, Product entities

### Use MyBatis when:
✅ **Complex SQL queries** - advanced joins, window functions, CTEs  
✅ **Performance-critical** - need fine-grained control over SQL  
✅ **Legacy databases** - schema doesn't match OO model  
✅ **Reporting/analytics** - ad-hoc queries, stored procedures  
✅ **Database-first approach** - database design drives application  
✅ **Team expertise** - team knows SQL better than ORM  

**Example:** Financial reporting system with complex aggregations

---

## Code Comparison

### 1. Entity/Model Definition

**JPA Entity:**
```java
@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String firstName;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;
    
    @ManyToMany
    @JoinTable(name = "student_subject", ...)
    private List<Subject> subjects;
}
```

**MyBatis POJO:**
```java
public class StudentModel {
    private Long id;
    private String firstName;
    private Long groupId;  // FK stored explicitly
    
    private GroupModel group;  // Loaded via mapper
    private List<SubjectModel> subjects;  // Loaded via mapper
}
```

### 2. Relationship Mapping

**JPA (Automatic):**
```java
// In Student entity:
@ManyToOne
@JoinColumn(name = "group_id")
private Group group;

// Hibernate generates:
// SELECT s.*, g.* FROM student s LEFT JOIN student_group g ON s.group_id = g.id
```

**MyBatis (Explicit SQL):**
```xml
<!-- StudentMapper.xml -->
<resultMap id="studentWithGroup" type="StudentModel">
    <id property="id" column="id"/>
    <result property="firstName" column="first_name"/>
    <association property="group" javaType="GroupModel">
        <id property="id" column="group_id"/>
        <result property="name" column="group_name"/>
    </association>
</resultMap>

<select id="findById" resultMap="studentWithGroup">
    SELECT s.id, s.first_name, s.group_id,
           g.name as group_name
    FROM student s
    LEFT JOIN student_group g ON s.group_id = g.id
    WHERE s.id = #{id}
</select>
```

### 3. DAO Operations

**JPA DAO:**
```java
@ApplicationScoped
public class StudentJpaDAO {
    @PersistenceContext
    private EntityManager em;
    
    @Transactional  // Automatic transaction
    public void save(Student student) {
        em.persist(student);  // Hibernate generates INSERT
    }
    
    public List<Student> findAll() {
        return em.createQuery(
            "SELECT s FROM Student s",  // JPQL
            Student.class
        ).getResultList();
    }
}
```

**MyBatis DAO:**
```java
@ApplicationScoped
public class StudentMyBatisDAO {
    
    public void save(StudentModel student) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.insert(student);
            session.commit();  // Manual commit
        }
    }
    
    public List<StudentModel> findAll() {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            return mapper.findAll();  // SQL in XML
        }
    }
}
```

---

## Relationship Handling

### One-to-Many (Group → Students)

**JPA:**
```java
// In Group entity:
@OneToMany(mappedBy = "group")
private List<Student> students;

// Usage:
Group group = em.find(Group.class, 1L);
group.getStudents();  // Lazy load or eager fetch
```

**MyBatis:**
```xml
<!-- GroupMapper.xml -->
<resultMap id="groupWithStudents" type="GroupModel">
    <id property="id" column="id"/>
    <collection property="students" ofType="StudentModel">
        <id property="id" column="student_id"/>
        <result property="firstName" column="student_first_name"/>
    </collection>
</resultMap>

<select id="findByIdWithStudents" resultMap="groupWithStudents">
    SELECT g.id, g.name,
           s.id as student_id, s.first_name as student_first_name
    FROM student_group g
    LEFT JOIN student s ON g.id = s.group_id
    WHERE g.id = #{id}
</select>
```

### Many-to-Many (Students ↔ Subjects)

**JPA:**
```java
@ManyToMany
@JoinTable(
    name = "student_subject",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "subject_id")
)
private List<Subject> subjects;

// Hibernate manages junction table automatically
```

**MyBatis:**
```xml
<!-- Manual junction table queries -->
<select id="findById" resultMap="studentWithSubjects">
    SELECT s.*, subj.id as subject_id, subj.name as subject_name
    FROM student s
    LEFT JOIN student_subject ss ON s.id = ss.student_id
    LEFT JOIN subject subj ON ss.subject_id = subj.id
    WHERE s.id = #{id}
</select>

<insert id="enrollInSubject">
    INSERT INTO student_subject (student_id, subject_id)
    VALUES (#{param1}, #{param2})
</insert>
```

---

## Transaction Management

### JPA (Declarative)
```java
@Transactional  // Automatic begin/commit/rollback
public void save(Student student) {
    em.persist(student);
}  // Transaction commits automatically
```

### MyBatis (Manual)
```java
public void save(StudentModel student) {
    try (SqlSession session = MyBatisUtil.openSession()) {
        StudentMapper mapper = session.getMapper(StudentMapper.class);
        mapper.insert(student);
        session.commit();  // Manual commit required!
    }  // Auto-closes session
}
```

---

## Advantages & Disadvantages

### JPA/Hibernate

**Advantages:**
- ✅ Less boilerplate code for CRUD
- ✅ Database-agnostic (switch DB easily)
- ✅ Automatic dirty checking (change tracking)
- ✅ Built-in caching (1st & 2nd level)
- ✅ Declarative transactions
- ✅ Type-safe queries (Criteria API)

**Disadvantages:**
- ❌ Performance issues (N+1 queries, lazy loading exceptions)
- ❌ Less control over generated SQL
- ❌ Steep learning curve (sessions, lazy/eager, detached entities)
- ❌ Complex queries become verbose in JPQL
- ❌ "Magic" behavior can be confusing

### MyBatis

**Advantages:**
- ✅ Full SQL control (optimize for specific database)
- ✅ Easy to use complex SQL (window functions, CTEs)
- ✅ Simple to debug (see exact SQL)
- ✅ Works well with legacy schemas
- ✅ Predictable performance
- ✅ Lower learning curve for SQL developers

**Disadvantages:**
- ❌ More boilerplate (write all SQL)
- ❌ Less portable (SQL is DB-specific)
- ❌ Manual relationship management
- ❌ No automatic dirty checking
- ❌ XML configuration can be verbose
- ❌ Manual transaction management

---

## Project Structure

```
src/main/java/com/myfirstproject/
├── entity/              # JPA entities (Student, Group, Subject)
├── mybatis/
│   ├── model/           # MyBatis POJOs (StudentModel, GroupModel, SubjectModel)
│   ├── mapper/          # MyBatis mapper interfaces
│   └── util/            # MyBatisUtil (SqlSession factory)
├── dao/
│   ├── jpa/             # JPA DAOs (StudentJpaDAO, etc.)
│   └── mybatis/         # MyBatis DAOs (StudentMyBatisDAO, etc.)
└── bean/                # CDI beans (currently use JPA DAOs)

src/main/resources/
├── mybatis-config.xml          # MyBatis configuration
├── mybatis/mappers/            # MyBatis SQL mappings
│   ├── StudentMapper.xml
│   ├── GroupMapper.xml
│   └── SubjectMapper.xml
└── META-INF/persistence.xml    # JPA configuration
```

---

## Demonstration Points for Grading

### DB Setup (0.05)
✅ H2 database configured  
✅ One-to-many: Group → Students  
✅ Many-to-many: Students ↔ Subjects  

### JPA Explanation (0.1)
✅ [Entity files](src/main/java/com/myfirstproject/entity/)  
✅ `@Column` maps Java field to DB column  
✅ `@Column(name = "custom_name")` for custom names  
✅ `@OneToMany(mappedBy = "group")` for Group → Students  
✅ `@ManyToMany` + `@JoinTable` for Students ↔ Subjects  

### MyBatis Explanation (0.1)
✅ [Model classes](src/main/java/com/myfirstproject/mybatis/model/)  
✅ [Mapper XML files](src/main/resources/mybatis/mappers/)  
✅ `<resultMap>` maps columns to fields  
✅ `<association>` for one-to-one/many-to-one  
✅ `<collection>` for one-to-many/many-to-many  

### DAO Implementations
✅ [JPA DAOs](src/main/java/com/myfirstproject/dao/jpa/) (0.1)  
✅ [MyBatis DAOs](src/main/java/com/myfirstproject/dao/mybatis/) (0.1)  

### Transactions (0.05)
✅ JPA: `@Transactional` annotation  
✅ MyBatis: Manual `session.commit()`  

---

## Conclusion

Both approaches are valid:
- **JPA/Hibernate** excels at **object-oriented domain modeling**
- **MyBatis** excels at **SQL-centric data processing**

Many projects use **both**:
- JPA for standard CRUD and business logic
- MyBatis for complex reporting queries

Choose based on:
- Team expertise
- Project requirements
- Performance needs
- Schema complexity
