# Student Management System

A Jakarta EE web application demonstrating **both ORM/JPA and DataMapper/MyBatis** approaches for database access.

## Project Overview

This project fulfills software engineering course requirements by implementing:
- ✅ **Database with relationships** (one-to-many, many-to-many)
- ✅ **JPA/Hibernate** entities with annotations
- ✅ **MyBatis** mappers with XML configuration
- ✅ **DAO layer** for both JPA and MyBatis
- ✅ **CDI beans** with dependency injection
- ✅ **JSF UI** with data binding
- ✅ **Automatic transaction management**

## Technology Stack

| Component | Technology |
|-----------|------------|
| **Backend** | Jakarta EE 10, CDI, JPA |
| **Frontend** | JSF (Jakarta Faces) |
| **Database** | H2 (embedded) |
| **ORM** | Hibernate 6.2 |
| **DataMapper** | MyBatis 3.5 |
| **Build Tool** | Maven |
| **Server** | Tomcat 10.x or compatible |

## Database Schema

```
┌─────────────────┐       ┌──────────────┐       ┌──────────────────┐       ┌─────────────┐
│ student_group   │       │   student    │       │ student_subject  │       │   subject   │
├─────────────────┤       ├──────────────┤       ├──────────────────┤       ├─────────────┤
│ id (PK)         │←─────┐│ id (PK)      │       │ student_id (FK)  │──────→│ id (PK)     │
│ name            │  1:N ││ first_name   │───┐   │ subject_id (FK)  │   ┌──→│ name        │
│ description     │      └│ last_name    │   │   └──────────────────┘   │   │ description │
└─────────────────┘       │ email        │   └──────────────────────────┘   │ credits     │
                          │ group_id(FK) │        (Many-to-Many)            └─────────────┘
                          └──────────────┘
                          
Relationships:
• Group → Students: ONE-TO-MANY (one group has many students)
• Students ↔ Subjects: MANY-TO-MANY (students enroll in multiple subjects)
```

## Project Structure

```
src/main/java/com/myfirstproject/
├── entity/              # JPA entities (@Entity, @ManyToOne, @ManyToMany)
│   ├── Student.java
│   ├── Group.java
│   └── Subject.java
├── mybatis/
│   ├── model/           # MyBatis POJOs (plain Java objects)
│   │   ├── StudentModel.java
│   │   ├── GroupModel.java
│   │   └── SubjectModel.java
│   ├── mapper/          # MyBatis mapper interfaces
│   │   ├── StudentMapper.java
│   │   ├── GroupMapper.java
│   │   └── SubjectMapper.java
│   └── util/
│       └── MyBatisUtil.java  # SqlSessionFactory manager
├── dao/
│   ├── jpa/             # JPA Data Access Objects
│   │   ├── StudentJpaDAO.java
│   │   ├── GroupJpaDAO.java
│   │   └── SubjectJpaDAO.java
│   └── mybatis/         # MyBatis Data Access Objects
│       ├── StudentMyBatisDAO.java
│       ├── GroupMyBatisDAO.java
│       └── SubjectMyBatisDAO.java
├── bean/                # CDI beans (@Named, @RequestScoped)
│   ├── StudentBean.java
│   ├── GroupBean.java
│   └── SubjectBean.java
└── util/
    └── EntityManagerProducer.java

src/main/resources/
├── mybatis-config.xml          # MyBatis configuration
├── mybatis/mappers/            # MyBatis SQL mappings
│   ├── StudentMapper.xml
│   ├── GroupMapper.xml
│   └── SubjectMapper.xml
└── META-INF/
    └── persistence.xml         # JPA configuration

src/main/webapp/
├── *.xhtml                     # JSF pages
└── WEB-INF/
    ├── beans.xml               # CDI configuration
    └── web.xml                 # Servlet configuration
```

## Prerequisites
- Java 17 or higher
- Apache Tomcat 10.x or any Jakarta EE 10 compatible server
- Maven 3.6+

## Build & Run

```bash
# Clean and build the project
./mvnw clean package

# Deploy the WAR file to Tomcat
# Copy target/uzd_1-1.0-SNAPSHOT.war to Tomcat's webapps/ directory

# Access the application
# http://localhost:8080/uzd_1-1.0-SNAPSHOT/
```

## Features

### 1. Student Management
- View all students with their groups
- Create/Edit/Delete students
- Assign students to groups
- Enroll students in subjects (many-to-many)

### 2. Group Management
- View all groups with student counts
- Create/Edit/Delete groups
- View students in each group (one-to-many)

### 3. Subject Management
- View all subjects
- Create/Edit/Delete subjects
- See enrolled students (many-to-many)

## Key Concepts Demonstrated

### 1. JPA/Hibernate (ORM)

**Entity Annotations:**
```java
@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)  // Maps to DB column
    private String firstName;
    
    @ManyToOne(optional = false)  // ONE-TO-MANY from Group side
    @JoinColumn(name = "group_id")  // FK column name in DB
    private Group group;
    
    @ManyToMany  // MANY-TO-MANY relationship
    @JoinTable(
        name = "student_subject",  // Junction table
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects;
}
```

**Custom Column Names:**
```java
@Column(name = "first_name")  // Java field: firstName, DB column: first_name
private String firstName;
```

**Advantages:**
- ✅ Automatic SQL generation
- ✅ Managed entities with change tracking
- ✅ Declarative transactions (`@Transactional`)
- ✅ Database-agnostic

### 2. MyBatis (DataMapper)

**POJO (Plain Object):**
```java
public class StudentModel {
    private Long id;
    private String firstName;  // No annotations needed
    private Long groupId;      // FK stored explicitly
    
    private GroupModel group;  // Populated via mapper
    private List<SubjectModel> subjects;
}
```

**XML Mapper:**
```xml
<mapper namespace="com.myfirstproject.mybatis.mapper.StudentMapper">
    <!-- Result map for one-to-many -->
    <resultMap id="studentWithGroup" type="StudentModel">
        <id property="id" column="id"/>
        <result property="firstName" column="first_name"/>
        <association property="group" javaType="GroupModel">
            <id property="id" column="group_id"/>
            <result property="name" column="group_name"/>
        </association>
    </resultMap>
    
    <!-- Manual SQL query -->
    <select id="findById" resultMap="studentWithGroup">
        SELECT s.id, s.first_name, s.group_id,
               g.name as group_name
        FROM student s
        LEFT JOIN student_group g ON s.group_id = g.id
        WHERE s.id = #{id}
    </select>
</mapper>
```

**Advantages:**
- ✅ Full SQL control
- ✅ Better performance tuning
- ✅ Works with complex/legacy schemas
- ✅ Explicit mapping (no magic)

### 3. CDI Beans & Dependency Injection

```java
@Named                  // Makes bean accessible from JSF pages
@RequestScoped          // New instance per HTTP request
public class StudentBean implements Serializable {
    
    @Inject             // CDI injects DAO automatically
    private StudentJpaDAO studentDAO;
    
    public String saveStudent() {
        studentDAO.save(student);  // @Transactional handles transaction
        return "students?faces-redirect=true";
    }
}
```

**CDI Scopes:**
- `@RequestScoped` - New instance per HTTP request (current implementation)
- `@SessionScoped` - One instance per user session (for user state)
- `@ApplicationScoped` - Singleton (for shared services like DAOs)

### 4. DAO Pattern

**JPA DAO (with automatic transactions):**
```java
@ApplicationScoped
public class StudentJpaDAO {
    @PersistenceContext
    private EntityManager em;
    
    @Transactional  // Automatic begin/commit/rollback
    public void save(Student student) {
        em.persist(student);
    }
}
```

**MyBatis DAO (manual transactions):**
```java
@ApplicationScoped
public class StudentMyBatisDAO {
    
    public void save(StudentModel student) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.insert(student);
            session.commit();  // Manual commit required
        }
    }
}
```

## Database Initialization

The database is created automatically by Hibernate on first run:
```xml
<!-- persistence.xml -->
<property name="hibernate.hbm2ddl.auto" value="update"/>
```

This creates tables based on JPA entity annotations.

## ORM vs DataMapper Comparison

See [ORM_vs_MyBatis.md](ORM_vs_MyBatis.md) for detailed comparison including:
- When to use each approach
- Advantages and disadvantages
- Code examples
- Performance considerations
- Relationship handling

## Course Requirements Checklist

### DB, ORM/JPA and DataMapper/MyBatis (0.25 points)
- ✅ Database with one-to-many and many-to-many relationships (0.05)
- ✅ JPA entities explained with column mappings and relationships (0.1)
- ✅ MyBatis entities explained (0.1)

### Use Case Implementation (0.6 points)
- ✅ UI displaying related entity data (students with groups) (0.1)
- ✅ Form with data binding (JSF forms with backing beans) (0.1)
- ✅ Business logic components (CDI beans) (0.05)
- ✅ DAO using ORM/JPA (0.1)
- ✅ DAO using DataMapper/MyBatis (0.1)
- ✅ ORM vs DataMapper explanation (0.1)
- ✅ Automatic transactions (`@Transactional`) (0.05)

## Additional Resources

- [Jakarta EE Documentation](https://jakarta.ee/)
- [Hibernate ORM](https://hibernate.org/orm/)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [JSF Tutorial](https://www.oracle.com/java/technologies/javaserverfaces.html)

## License

Educational project for VU Software Engineering course.
```