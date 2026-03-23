package com.myfirstproject.dao.jpa;

import java.util.List;

import com.myfirstproject.entity.Student;
import com.myfirstproject.transaction.Transactional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/**
 * JPA Data Access Object for Student entity.
 * Uses EntityManager for database operations.
 * Transactions managed declaratively with @Transactional annotation.
 * 
 * This meets the requirement: "Būtinos automatinės/deklaratyvios DB transakcijos"
 * (Automatic/declarative transactions required - manual begin()/commit() not allowed).
 */
@ApplicationScoped
@Transactional
public class StudentJpaDAO {
    
    @Inject
    private EntityManager em;
    
    /**
     * Find student by ID with eager loading of relationships.
     */
    public Student findById(Long id) {
        Student student = em.find(Student.class, id);
        if (student != null) {
            // Eager load relationships to avoid lazy loading issues
            student.getGroup().getName();
            student.getSubjects().size();
        }
        return student;
    }
    
    /**
     * Find all students with their groups (demonstrates one-to-many).
     */
    public List<Student> findAll() {
        return em.createQuery(
            "SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.group ORDER BY s.lastName, s.firstName", 
            Student.class
        ).getResultList();
    }
    
    /**
     * Find students by group ID (demonstrates navigating one-to-many relationship).
     */
    public List<Student> findByGroupId(Long groupId) {
        return em.createQuery(
            "SELECT s FROM Student s WHERE s.group.id = :groupId ORDER BY s.lastName", 
            Student.class
        )
        .setParameter("groupId", groupId)
        .getResultList();
    }
    
    /**
     * Save new student with declarative transaction.
     * Transaction automatically managed by @Transactional interceptor.
     */
    public void save(Student student) {
        em.persist(student);
    }
    
    /**
     * Update existing student.
     * Transaction automatically managed by @Transactional interceptor.
     */
    public Student update(Student student) {
        return em.merge(student);
    }
    
    /**
     * Delete student.
     * Transaction automatically managed by @Transactional interceptor.
     */
    public void delete(Long id) {
        Student student = em.find(Student.class, id);
        if (student != null) {
            em.remove(student);
        }
    }
    
    /**
     * Enroll student in subject (demonstrates many-to-many).
     * Transaction automatically managed by @Transactional interceptor.
     */
    public void enrollInSubject(Long studentId, Long subjectId) {
        Student student = em.find(Student.class, studentId);
        com.myfirstproject.entity.Subject subject = em.find(com.myfirstproject.entity.Subject.class, subjectId);
        if (student != null && subject != null) {
            student.addSubject(subject);
        }
    }
}
