# Requirements Demonstration Guide

This guide explains how to demonstrate each requirement for grading.

## 1. Working with Tools (0.15 points)

### a) IDE - Minimal Changes & Build (0.05)
**How to demonstrate:**
1. Open project in IDE (IntelliJ IDEA, Eclipse, VS Code)
2. Make a simple change (e.g., add comment to a bean)
3. Build project: `./mvnw clean package`
4. Show successful build output

### b) Application Server (0.05)
**How to demonstrate:**
1. **IDE Integration:**
   - IntelliJ IDEA: Tools → Manage Servers → Add Tomcat
   - Eclipse: Servers view → Add Tomcat
   - VS Code: Extensions → Tomcat for Java
   
2. **Start/Stop Server:**
   - Via IDE toolbar or Servers panel
   - Show console output when starting
   
3. **Deploy Application:**
   - Copy `target/uzd_1-1.0-SNAPSHOT.war` to Tomcat's `webapps/`
   - Or use IDE deployment feature
   
4. **Access Application:**
   - http://localhost:8080/uzd_1-1.0-SNAPSHOT/
   
### c) Version Control (0.05)
**How to demonstrate:**
```bash
# Initialize git if not already done
git init
git add .
git commit -m "Initial commit with JPA and MyBatis implementation"

# Show commit history
git log

# Make a change and commit
git add src/main/java/com/myfirstproject/bean/StudentBean.java
git commit -m "Updated StudentBean to use DAO pattern"
git log --oneline
```

---

## 2. Database, ORM/JPA and DataMapper/MyBatis (0.25 points)

### a) Database with Relationships (0.05)

**Files to show:**
- `src/main/resources/META-INF/persistence.xml` - H2 database configuration
- Database schema automatically created by Hibernate

**Relationships to demonstrate:**

**One-to-Many (Group → Students):**
```
Group entity:
@OneToMany(mappedBy = "group")
private List<Student> students;

Student entity:
@ManyToOne
@JoinColumn(name = "group_id")
private Group group;
```

Database:
```sql
-- Group table (one side)
CREATE TABLE student_group (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255)
);

-- Student table (many side)
CREATE TABLE student (
    id BIGINT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    group_id BIGINT,  -- Foreign key to student_group
    FOREIGN KEY (group_id) REFERENCES student_group(id)
);
```

**Many-to-Many (Students ↔ Subjects):**
```
Student entity:
@ManyToMany
@JoinTable(
    name = "student_subject",
    joinColumns = @JoinColumn(name = "student_id"),
    inverseJoinColumns = @JoinColumn(name = "subject_id")
)
private List<Subject> subjects;

Subject entity:
@ManyToMany(mappedBy = "subjects")
private List<Student> students;
```

Database:
```sql
-- Junction table
CREATE TABLE student_subject (
    student_id BIGINT,
    subject_id BIGINT,
    PRIMARY KEY (student_id, subject_id),
    FOREIGN KEY (student_id) REFERENCES student(id),
    FOREIGN KEY (subject_id) REFERENCES subject(id)
);
```

### b) JPA Entity Field Mapping (0.1)

**Files to show:**
- `src/main/java/com/myfirstproject/entity/Student.java`
- `src/main/java/com/myfirstproject/entity/Group.java`
- `src/main/java/com/myfirstproject/entity/Subject.java`

**Key points to explain:**

1. **How fields map to columns:**
```java
@Entity
@Table(name = "student")  // Table name in database
public class Student {
    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Maps to 'id' column
    
    @Column(nullable = false)
    private String firstName;  // Maps to 'first_name' (camelCase → snake_case)
    
    private String email;  // No @Column = uses default mapping
}
```

2. **Custom column names:**
```java
@Column(name = "custom_column_name")
private String myField;
```

Without `name` attribute, JPA uses default naming strategy:
- `firstName` → `first_name` (camelCase to snake_case)
- `email` → `email` (same name)

3. **One-to-Many relationship:**
```java
// In Group.java
@OneToMany(mappedBy = "group")  // "group" = field name in Student
private List<Student> students;

// In Student.java
@ManyToOne
@JoinColumn(name = "group_id")  // FK column in student table
private Group group;
```

Database structure:
- NO column created in `student_group` table for students
- FK column `group_id` created in `student` table

4. **Many-to-Many relationship:**
```java
@ManyToMany
@JoinTable(
    name = "student_subject",              // Junction table name
    joinColumns = @JoinColumn(name = "student_id"),    // FK to student
    inverseJoinColumns = @JoinColumn(name = "subject_id")  // FK to subject
)
private List<Subject> subjects;
```

Database structure:
- Creates junction table `student_subject`
- Two FKs form composite primary key
- No collection column in student or subject tables

### c) MyBatis Entity Mapping (0.1)

**Files to show:**
- `src/main/java/com/myfirstproject/mybatis/model/StudentModel.java`
- `src/main/resources/mybatis/mappers/StudentMapper.xml`
- `src/main/resources/mybatis/mappers/GroupMapper.xml`
- `src/main/resources/mybatis/mappers/SubjectMapper.xml`

**Key points to explain:**

1. **How fields map to columns:**
```java
// Plain POJO - no annotations
public class StudentModel {
    private Long id;           // Maps to 'id' column
    private String firstName;  // Maps to 'first_name' via XML
    private Long groupId;      // Foreign key stored explicitly
    
    // For relationships
    private GroupModel group;
    private List<SubjectModel> subjects;
}
```

Mapping defined in XML:
```xml
<resultMap id="studentBase" type="StudentModel">
    <id property="id" column="id"/>
    <result property="firstName" column="first_name"/>
    <result property="lastName" column="last_name"/>
    <result property="groupId" column="group_id"/>
</resultMap>
```

2. **Custom column names:**
```xml
<result property="javaFieldName" column="database_column_name"/>
```

Unlike JPA, MyBatis requires explicit mapping for all fields in resultMap.

3. **One-to-Many relationship:**
```xml
<resultMap id="groupWithStudents" type="GroupModel">
    <id property="id" column="id"/>
    <result property="name" column="name"/>
    
    <!-- Collection = one-to-many -->
    <collection property="students" ofType="StudentModel">
        <id property="id" column="student_id"/>
        <result property="firstName" column="student_first_name"/>
    </collection>
</resultMap>

<select id="findByIdWithStudents" resultMap="groupWithStudents">
    SELECT 
        g.id, g.name,
        s.id as student_id,
        s.first_name as student_first_name
    FROM student_group g
    LEFT JOIN student s ON g.id = s.group_id
    WHERE g.id = #{id}
</select>
```

**Difference from JPA:**
- Manual SQL query (no automatic generation)
- Explicit JOIN syntax
- Column aliases for disambiguation

4. **Many-to-Many relationship:**
```xml
<resultMap id="studentWithSubjects" type="StudentModel">
    <id property="id" column="id"/>
    <result property="firstName" column="first_name"/>
    
    <!-- Collection = many-to-many -->
    <collection property="subjects" ofType="SubjectModel">
        <id property="id" column="subject_id"/>
        <result property="name" column="subject_name"/>
    </collection>
</resultMap>

<select id="findById" resultMap="studentWithSubjects">
    SELECT 
        s.id, s.first_name,
        subj.id as subject_id, subj.name as subject_name
    FROM student s
    LEFT JOIN student_subject ss ON s.id = ss.student_id
    LEFT JOIN subject subj ON ss.subject_id = subj.id
    WHERE s.id = #{id}
</select>
```

**Difference from JPA:**
- Manual join through junction table
- Must explicitly query association
- No automatic management

---

## 3. Use Case Implementation (0.6 points)

### a) UI with Related Entity Data (0.1)

**Files to show:**
- `src/main/webapp/students.xhtml`
- `src/main/webapp/groups.xhtml`

**Demonstration:**
1. Open http://localhost:8080/uzd_1-1.0-SNAPSHOT/students.xhtml
2. Show student list with their groups (one-to-many navigation)
3. Show dataTable displaying:
   - Student name (from Student entity)
   - Group name (navigating through relationship)
   - Subjects (many-to-many)

Example JSF code:
```xml
<h:dataTable value="#{studentBean.students}" var="student">
    <h:column>
        <f:facet name="header">Name</f:facet>
        #{student.firstName} #{student.lastName}
    </h:column>
    <h:column>
        <f:facet name="header">Group</f:facet>
        #{student.group.name}  <!-- Navigating one-to-many -->
    </h:column>
</h:dataTable>
```

### b) Form with Data Binding (0.1)

**Files to show:**
- `src/main/webapp/student-form.xhtml`

**Demonstration:**
1. Open http://localhost:8080/uzd_1-1.0-SNAPSHOT/student-form.xhtml
2. Fill in form fields
3. Show backing bean code:

```java
@Named
@RequestScoped
public class StudentBean {
    private Student student = new Student();  // Data binding target
    
    public String saveStudent() {
        studentDAO.save(student);  // Auto-bound data saved to DB
        return "students?faces-redirect=true";
    }
}
```

Form fields automatically bind to `student` object properties:
```xml
<h:form>
    <h:inputText value="#{studentBean.student.firstName}"/>  <!-- Bound to student.firstName -->
    <h:inputText value="#{studentBean.student.lastName}"/>
    <h:commandButton value="Save" action="#{studentBean.saveStudent}"/>
</h:form>
```

### c) Business Logic Component (0.05)

**Files to show:**
- `src/main/java/com/myfirstproject/bean/StudentBean.java`
- `src/main/java/com/myfirstproject/bean/GroupBean.java`

**Key annotations to explain:**

```java
@Named                  // CDI annotation: makes bean accessible from JSF EL
@RequestScoped          // New instance created per HTTP request
public class StudentBean implements Serializable {
    
    @Inject             // CDI dependency injection
    private StudentJpaDAO studentDAO;
    
    public String saveStudent() {
        // Business logic here
        studentDAO.save(student);
        return "students?faces-redirect=true";
    }
}
```

**Scope explanations:**

1. **@RequestScoped** (current implementation)
   - New instance per HTTP request
   - Destroyed after response sent
   - Use for: Forms, search results

2. **@SessionScoped**
   - One instance per user session
   - Persists across multiple requests
   - Use for: User preferences, shopping cart

3. **@ApplicationScoped**
   - Singleton (one instance for entire app)
   - Shared by all users
   - Use for: DAOs, services, caches

4. **@Inject**
   - CDI automatically provides dependency
   - Type-safe injection
   - No manual instantiation needed

### d) DAO using ORM/JPA (0.1)

**Files to show:**
- `src/main/java/com/myfirstproject/dao/jpa/StudentJpaDAO.java`
- `src/main/java/com/myfirstproject/dao/jpa/GroupJpaDAO.java`
- `src/main/java/com/myfirstproject/dao/jpa/SubjectJpaDAO.java`

**Key points:**
```java
@ApplicationScoped  // Singleton DAO
public class StudentJpaDAO {
    
    @PersistenceContext(unitName = "uzd_1PU")
    private EntityManager em;  // JPA EntityManager injected
    
    @Transactional  // Automatic transaction management
    public void save(Student student) {
        em.persist(student);  // JPA operation
    }
    
    public List<Student> findAll() {
        return em.createQuery(
            "SELECT s FROM Student s",  // JPQL
            Student.class
        ).getResultList();
    }
}
```

### e) DAO using DataMapper/MyBatis (0.1)

**Files to show:**
- `src/main/java/com/myfirstproject/dao/mybatis/StudentMyBatisDAO.java`
- `src/main/resources/mybatis/mappers/StudentMapper.xml`

**Key points:**
```java
@ApplicationScoped
public class StudentMyBatisDAO {
    
    public void save(StudentModel student) {
        try (SqlSession session = MyBatisUtil.openSession()) {
            StudentMapper mapper = session.getMapper(StudentMapper.class);
            mapper.insert(student);  // Calls XML-defined query
            session.commit();  // Manual commit
        }  // Auto-close session
    }
}
```

Corresponding XML:
```xml
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    INSERT INTO student (first_name, last_name, email, group_id)
    VALUES (#{firstName}, #{lastName}, #{email}, #{groupId})
</insert>
```

### f) ORM vs DataMapper Explanation (0.1)

**Document to show:**
- `ORM_vs_MyBatis.md`

**Key differences:**

| Aspect | JPA | MyBatis |
|--------|-----|---------|
| SQL | Generated | Manual |
| Transactions | `@Transactional` | `session.commit()` |
| Relationships | Automatic | Manual XML |
| Control | Less | Full |
| Complexity | Higher | Lower |

**When to use:**
- **JPA:** CRUD apps, complex object graphs, domain-driven
- **MyBatis:** Reporting, complex SQL, legacy databases

### g) Automatic Transactions (0.05)

**Files to show:**
- `src/main/java/com/myfirstproject/dao/jpa/StudentJpaDAO.java`

**JPA - Automatic (Declarative):**
```java
@Transactional  // Framework handles begin/commit/rollback
public void save(Student student) {
    em.persist(student);
}  // Transaction commits automatically
```

**MyBatis - Manual (for comparison):**
```java
public void save(StudentModel student) {
    try (SqlSession session = MyBatisUtil.openSession()) {
        mapper.insert(student);
        session.commit();  // Manual commit required
    } catch (Exception e) {
        // Manual rollback if needed
    }
}
```

**Key point:** JPA uses `@Transactional` annotation for automatic transaction management (required by grading rubric).

---

## Quick Checklist

- [ ] Show IDE project structure
- [ ] Build project with Maven
- [ ] Start/stop Tomcat from IDE
- [ ] Deploy and access application
- [ ] Show git commits
- [ ] Open Student.java and explain @Entity, @ManyToOne, @ManyToMany
- [ ] Open Group.java and explain @OneToMany
- [ ] Show custom column name example
- [ ] Open StudentModel.java (MyBatis POJO)
- [ ] Open StudentMapper.xml and explain resultMap
- [ ] Show `<association>` for one-to-many
- [ ] Show `<collection>` for many-to-many
- [ ] Open students.xhtml and show related data display
- [ ] Open student-form.xhtml and explain data binding
- [ ] Open StudentBean.java and explain @Named, @RequestScoped, @Inject
- [ ] Show StudentJpaDAO with @Transactional
- [ ] Show StudentMyBatisDAO with SqlSession
- [ ] Reference ORM_vs_MyBatis.md for comparison

---

## Running the Application

```bash
# Build
./mvnw clean package

# Deploy to Tomcat
cp target/uzd_1-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/

# Start Tomcat
$TOMCAT_HOME/bin/catalina.sh run

# Access
http://localhost:8080/uzd_1-1.0-SNAPSHOT/
```
